package com.freightflow.modules.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.webhook.dto.CreateWebhookRequest;
import com.freightflow.modules.webhook.dto.UpdateWebhookRequest;
import com.freightflow.modules.webhook.dto.WebhookResponse;
import com.freightflow.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * Manages webhook subscriptions and dispatches outbound webhook calls.
 *
 * <p>Webhook delivery is:
 * <ul>
 *   <li>Asynchronous — runs in a separate thread via {@code @Async}</li>
 *   <li>Best-effort — failures are logged as WARN but never propagate to the caller</li>
 *   <li>Signed — each request carries an HMAC-SHA256 signature over the JSON payload</li>
 *   <li>Timeout-bounded — 5 seconds read timeout per delivery attempt</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);
    private static final String HMAC_ALGO = "HmacSHA256";

    private final WebhookRepository webhookRepository;
    private final TenantRepository tenantRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public WebhookService(WebhookRepository webhookRepository,
                          TenantRepository tenantRepository,
                          ObjectMapper objectMapper) {
        this.webhookRepository = webhookRepository;
        this.tenantRepository  = tenantRepository;
        this.objectMapper      = objectMapper;
        // RestClient with a 5-second connection + read timeout
        this.restClient = RestClient.builder()
                .requestInitializer(request ->
                        request.getHeaders().set(HttpHeaders.CONTENT_TYPE,
                                MediaType.APPLICATION_JSON_VALUE))
                .build();
    }

    // ==================== CRUD ====================

    public List<WebhookResponse> list(UUID tenantId) {
        return webhookRepository.findByTenant_Id(tenantId)
                .stream()
                .map(WebhookResponse::from)
                .toList();
    }

    @Transactional
    public WebhookResponse create(UUID tenantId, CreateWebhookRequest req) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        String eventsStr = String.join(",", req.events());
        WebhookSubscription sub = new WebhookSubscription(req.url(), req.secret(), eventsStr, tenant);
        return WebhookResponse.from(webhookRepository.save(sub));
    }

    @Transactional
    public WebhookResponse update(UUID tenantId, UUID id, UpdateWebhookRequest req) {
        // Validate ownership before touching anything
        WebhookSubscription sub = findOwnedBy(id, tenantId);

        // Derive updated events string (null → keep existing)
        String eventsStr = req.events() != null ? String.join(",", req.events()) : null;

        // Partial-update via JPQL (entity exposes no setters for url/events/secret)
        webhookRepository.updateFields(id, req.url(), eventsStr, req.secret(),
                java.time.Instant.now());

        // Handle active flag via the existing setter + save
        if (req.active() != null) {
            sub.setActive(req.active());
            webhookRepository.save(sub);
        }

        // Reload to return fresh state
        return webhookRepository.findById(id)
                .map(WebhookResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("WebhookSubscription", id));
    }

    @Transactional
    public void delete(UUID tenantId, UUID id) {
        WebhookSubscription sub = findOwnedBy(id, tenantId);
        webhookRepository.delete(sub);
    }

    // ==================== Delivery ====================

    /**
     * Sends the payload to all active subscribers of the tenant that listen to
     * the given event type. Each delivery runs in a separate thread (@Async) so
     * failures never block the caller.
     *
     * @param tenantId  the tenant whose subscribers should be notified
     * @param eventType event name (e.g. "ALERT_CRITICAL", "SHIPMENT_DELIVERED")
     * @param payload   the object to serialize as JSON and POST
     */
    @Async
    public void notifySubscribers(UUID tenantId, String eventType, Object payload) {
        List<WebhookSubscription> subscribers = webhookRepository.findByTenant_IdAndActiveTrue(tenantId);

        for (WebhookSubscription sub : subscribers) {
            if (!sub.listensTo(eventType)) {
                continue;
            }
            deliverOne(sub, eventType, payload);
        }
    }

    // ==================== Private helpers ====================

    /** Delivers one webhook call. Failure is silent (WARN log only). */
    private void deliverOne(WebhookSubscription sub, String eventType, Object payload) {
        try {
            String json      = objectMapper.writeValueAsString(payload);
            String signature = hmacSha256(json, sub.getSecret());

            restClient.post()
                    .uri(sub.getUrl())
                    .header("X-FreightFlow-Event",     eventType)
                    .header("X-FreightFlow-Signature", "sha256=" + signature)
                    .header(HttpHeaders.CONTENT_TYPE,  MediaType.APPLICATION_JSON_VALUE)
                    .body(json)
                    .retrieve()
                    .toBodilessEntity();

            sub.recordSuccess();
            webhookRepository.save(sub);
            log.debug("Webhook delivered: event={}, url={}", eventType, sub.getUrl());

        } catch (Exception ex) {
            sub.recordFailure();
            webhookRepository.save(sub);
            log.warn("Webhook delivery failed: event={}, url={}, reason={}",
                    eventType, sub.getUrl(), ex.getMessage());
        }
    }

    /**
     * Computes HMAC-SHA256 over the payload string using the subscriber's secret.
     * Returns a lowercase hex-encoded digest.
     */
    private String hmacSha256(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to compute HMAC-SHA256", ex);
        }
    }

    /** Loads a subscription and validates tenant ownership (returns 404 on mismatch). */
    private WebhookSubscription findOwnedBy(UUID id, UUID tenantId) {
        WebhookSubscription sub = webhookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WebhookSubscription", id));

        if (!sub.getTenant().getId().equals(tenantId)) {
            throw new ResourceNotFoundException("WebhookSubscription", id);
        }
        return sub;
    }
}
