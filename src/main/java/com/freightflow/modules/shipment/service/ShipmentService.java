package com.freightflow.modules.shipment.service;

import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.notification.EmailService;
import com.freightflow.modules.port.Port;
import com.freightflow.modules.port.PortRepository;
import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.shipment.dto.CreateShipmentRequest;
import com.freightflow.modules.shipment.dto.PublicTrackingResponse;
import com.freightflow.modules.shipment.dto.ShipmentFilterParams;
import com.freightflow.modules.shipment.dto.ShipmentResponse;
import com.freightflow.modules.shipment.dto.ShipmentStatsResponse;
import com.freightflow.modules.shipment.dto.UpdateShipmentRequest;
import com.freightflow.modules.shipment.enums.ShipmentStatus;
import com.freightflow.modules.shipment.repository.ShipmentRepository;
import com.freightflow.modules.voyage.Voyage;
import com.freightflow.modules.voyage.VoyageRepository;
import com.freightflow.modules.webhook.WebhookEventPublisher;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import com.freightflow.shared.pagination.PageResponse;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ShipmentService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentService.class);

    private static final List<ShipmentStatus> FINISHED_STATUSES = List.of(
            ShipmentStatus.ARRIVED,
            ShipmentStatus.DELIVERED,
            ShipmentStatus.GATE_OUT,
            ShipmentStatus.CANCELLED
    );

    private final ShipmentRepository    shipmentRepository;
    private final VoyageRepository      voyageRepository;
    private final PortRepository        portRepository;
    private final TenantRepository      tenantRepository;
    private final WebhookEventPublisher webhookEventPublisher;

    /**
     * Null when {@code spring.mail.username} is not set (local dev without SMTP).
     * Every call site must null-check before invoking methods.
     */
    @Autowired(required = false)
    private EmailService emailService;

    public ShipmentService(ShipmentRepository shipmentRepository,
                           VoyageRepository voyageRepository,
                           PortRepository portRepository,
                           TenantRepository tenantRepository,
                           WebhookEventPublisher webhookEventPublisher) {
        this.shipmentRepository    = shipmentRepository;
        this.voyageRepository      = voyageRepository;
        this.portRepository        = portRepository;
        this.tenantRepository      = tenantRepository;
        this.webhookEventPublisher = webhookEventPublisher;
    }

    // ==================== Queries ====================

    /**
     * Lista paginada de shipments do tenant com filtros opcionais.
     * Qualquer campo nulo/blank em {@code filters} é ignorado (sem restrição).
     */
    public PageResponse<ShipmentResponse> list(UUID tenantId, ShipmentFilterParams filters, Pageable pageable) {
        log.debug("Listing shipments for tenant={} filters={}", tenantId, filters);
        Specification<Shipment> spec = buildSpec(tenantId, filters, null);
        var page = shipmentRepository.findAll(spec, pageable);
        return PageResponse.from(page.map(ShipmentResponse::from));
    }

    /**
     * Variante usada pelo ShipmentController para role CLIENT:
     * restringe a visibilidade ao customer_id do principal.
     */
    public PageResponse<ShipmentResponse> listForClient(UUID tenantId, UUID customerId,
                                                        ShipmentFilterParams filters, Pageable pageable) {
        log.debug("Listing shipments for tenant={} customer={} filters={}", tenantId, customerId, filters);
        Specification<Shipment> spec = buildSpec(tenantId, filters, customerId);
        var page = shipmentRepository.findAll(spec, pageable);
        return PageResponse.from(page.map(ShipmentResponse::from));
    }

    /**
     * Busca shipment pelo id com isolamento de tenant (previne IDOR).
     * Retorna 404 se o shipment não pertencer ao tenant do caller —
     * impede que um tenant veja dados de outro tenant.
     */
    public ShipmentResponse getById(UUID id, UUID tenantId) {
        return getById(id, tenantId, null);
    }

    public ShipmentResponse getById(UUID id, UUID tenantId, UUID customerId) {
        log.debug("Fetching shipment id={} for tenant={}", id, tenantId);
        Shipment shipment = getScopedEntityById(id, tenantId, customerId);
        return ShipmentResponse.from(shipment);
    }

    /** Variante interna: busca sem restrição de tenant (usada por EventService). */
    public Shipment getEntityById(UUID id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", id));
    }

    public PublicTrackingResponse track(String booking) {
        log.info("Public tracking request for booking={}", booking);
        Shipment shipment = shipmentRepository.findByBookingWithDetails(booking)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", booking));

        var events = shipment.getEvents().stream()
                .sorted(Comparator.comparing(e -> e.getOccurredAt()))
                .map(e -> new PublicTrackingResponse.PublicTrackingMilestone(
                        e.getType(),
                        e.getLocation(),
                        e.getOccurredAt()))
                .collect(Collectors.toList());

        var lastUpdate = shipment.getEvents().stream()
                .map(e -> e.getOccurredAt())
                .max(Comparator.naturalOrder())
                .orElse(null);

        return new PublicTrackingResponse(
                shipment.getBooking(),
                shipment.getContainerNumber(),
                shipment.getStatus(),
                buildPublicStatusMessage(shipment.getStatus()),
                shipment.getVoyage().getVessel().getName(),
                shipment.getVoyage().getVoyageNumber(),
                shipment.getOriginPort().getName(),
                shipment.getOriginPort().getUnlocode(),
                shipment.getDestinationPort().getName(),
                shipment.getDestinationPort().getUnlocode(),
                shipment.getVoyage().getEtd(),
                shipment.getVoyage().getEta(),
                lastUpdate,
                events
        );
    }

    private String buildPublicStatusMessage(ShipmentStatus status) {
        return switch (status) {
            case BOOKED -> "Booking received";
            case CONFIRMED -> "Booking confirmed";
            case GATE_IN -> "Cargo received at terminal";
            case LOADED -> "Cargo loaded on vessel";
            case IN_TRANSIT -> "Shipment in transit";
            case ARRIVED -> "Shipment arrived at destination port";
            case GATE_OUT -> "Cargo released from terminal";
            case DELIVERED -> "Shipment delivered";
            case CANCELLED -> "Shipment cancelled";
        };
    }

    /**
     * KPIs agregados do tenant para o dashboard.
     * - delayed: embarques com delay_days > 0 que ainda estão em trânsito
     * - atRisk: embarques com risk_level HIGH ou CRITICAL
     */
    public ShipmentStatsResponse getStats(UUID tenantId) {
        log.debug("Computing shipment stats for tenant={}", tenantId);

        long total     = shipmentRepository.countByTenantId(tenantId);
        long inTransit = shipmentRepository.countByTenantIdAndStatus(tenantId, ShipmentStatus.IN_TRANSIT);
        long arrived   = shipmentRepository.countByTenantIdAndStatus(tenantId, ShipmentStatus.ARRIVED);
        long delayed   = shipmentRepository.countDelayed(tenantId, ShipmentStatus.IN_TRANSIT);
        long atRisk    = shipmentRepository.countAtRisk(tenantId);

        return new ShipmentStatsResponse(total, inTransit, arrived, delayed, atRisk);
    }

    // ==================== Commands ====================

    @Transactional
    public ShipmentResponse create(CreateShipmentRequest request, UUID tenantId) {
        log.info("Creating shipment booking={} for tenant={}", request.booking(), tenantId);

        if (shipmentRepository.existsByBooking(request.booking())) {
            throw new BusinessException("Booking " + request.booking() + " already exists");
        }

        Voyage voyage = voyageRepository.findById(request.voyageId())
                .orElseThrow(() -> new ResourceNotFoundException("Voyage", request.voyageId()));

        Port originPort = portRepository.findById(request.originPortId())
                .orElseThrow(() -> new ResourceNotFoundException("Port", request.originPortId()));

        Port destinationPort = portRepository.findById(request.destinationPortId())
                .orElseThrow(() -> new ResourceNotFoundException("Port", request.destinationPortId()));

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        Shipment shipment = new Shipment(request.booking(), voyage, originPort, destinationPort, tenant);
        shipment.setContainerNumber(request.containerNumber());
        shipment.setContainerType(request.containerType());
        shipment.setConsignee(request.consignee());
        shipment.setShipper(request.shipper());

        Shipment saved = shipmentRepository.save(shipment);
        log.info("Shipment created: id={}, booking={}", saved.getId(), saved.getBooking());
        return ShipmentResponse.from(saved);
    }

    @Transactional
    public ShipmentResponse update(UUID id, UpdateShipmentRequest request, UUID tenantId, UUID customerId) {
        log.info("Updating shipment id={}", id);
        Shipment shipment = getScopedEntityById(id, tenantId, customerId);

        ShipmentStatus previousStatus = shipment.getStatus();

        if (request.containerNumber() != null) shipment.setContainerNumber(request.containerNumber());
        if (request.containerType() != null)   shipment.setContainerType(request.containerType());
        if (request.consignee() != null)        shipment.setConsignee(request.consignee());
        if (request.shipper() != null)          shipment.setShipper(request.shipper());

        Shipment saved = shipmentRepository.save(shipment);

        // ── Post-save notifications (status-change hooks) ─────────────────
        ShipmentStatus newStatus = saved.getStatus();
        if (previousStatus != newStatus) {

            // 1. Outbound webhooks (DELIVERED / CANCELLED)
            if (newStatus == ShipmentStatus.DELIVERED) {
                webhookEventPublisher.publishShipmentStatusChange(
                        saved, WebhookEventPublisher.EVENT_SHIPMENT_DELIVERED);
            } else if (newStatus == ShipmentStatus.CANCELLED) {
                webhookEventPublisher.publishShipmentStatusChange(
                        saved, WebhookEventPublisher.EVENT_SHIPMENT_CANCELLED);
            }

            // 2. E-mail notification (DELIVERED / ARRIVED)
            // emailService is null when spring.mail.username is not configured
            if (emailService != null
                    && (newStatus == ShipmentStatus.DELIVERED || newStatus == ShipmentStatus.ARRIVED)) {
                try {
                    var customer = saved.getCustomer();
                    String contactEmail = customer != null ? customer.getContactEmail() : null;
                    if (contactEmail != null && !contactEmail.isBlank()) {
                        String eta = saved.getVoyage() != null && saved.getVoyage().getEta() != null
                                ? saved.getVoyage().getEta().toString()
                                : null;
                        emailService.sendStatusChangeNotification(
                                contactEmail,
                                saved.getBooking(),
                                newStatus.name(),
                                eta
                        );
                    }
                } catch (Exception e) {
                    // E-mail is best-effort — never block or roll back the transaction
                    log.warn("Status-change email failed for shipment={}: {}", saved.getId(), e.getMessage());
                }
            }
        }

        return ShipmentResponse.from(saved);
    }

    @Transactional
    public void delete(UUID id, UUID tenantId, UUID customerId) {
        log.info("Deleting shipment id={}", id);
        Shipment shipment = getScopedEntityById(id, tenantId, customerId);
        shipmentRepository.delete(shipment);
    }

    private Shipment getScopedEntityById(UUID id, UUID tenantId, UUID customerId) {
        if (customerId != null) {
            return shipmentRepository.findByIdAndTenantIdAndCustomerId(id, tenantId, customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Shipment", id));
        }
        return shipmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", id));
    }

    // ==================== Specification builder ====================

    /**
     * Monta uma {@link Specification} combinando todos os filtros opcionais.
     *
     * <p>Cada spec retorna {@code null} quando o parâmetro está ausente —
     * {@code Specification.and(null)} simplesmente ignora o filtro.</p>
     *
     * <p>Joins sobre {@code voyage → vessel} e {@code originPort} são do tipo
     * {@code MANY-TO-ONE}, portanto não geram linhas duplicadas por shipment.</p>
     *
     * @param tenantId   sempre obrigatório — isolamento de tenant
     * @param filters    filtros opcionais vindos dos query params
     * @param customerId opcional — quando não-null restringe ao customer (role CLIENT)
     */
    private Specification<Shipment> buildSpec(UUID tenantId, ShipmentFilterParams filters, UUID customerId) {
        return Specification
                .where(hasTenant(tenantId))
                .and(hasCustomer(customerId))
                .and(hasBookingLike(filters.booking()))
                .and(hasStatus(filters.status()))
                .and(hasCarrierLike(filters.carrier()))
                .and(hasVesselNameLike(filters.vesselName()))
                .and(hasOriginPortUnlocode(filters.originPortUnlocode()))
                .and(hasRiskLevel(filters.riskLevel()));
    }

    // ── specs individuais ──────────────────────────────────────────────────

    /** WHERE s.tenant.id = :tenantId */
    private static Specification<Shipment> hasTenant(UUID tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenant").get("id"), tenantId);
    }

    /** WHERE s.customer.id = :customerId — null → sem filtro */
    private static Specification<Shipment> hasCustomer(UUID customerId) {
        if (customerId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("customer").get("id"), customerId);
    }

    /** WHERE LOWER(s.booking) LIKE '%valor%' — null/blank → sem filtro */
    private static Specification<Shipment> hasBookingLike(String booking) {
        if (booking == null || booking.isBlank()) return null;
        String pattern = "%" + booking.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("booking")), pattern);
    }

    /**
     * WHERE s.status = :status — null/blank ou enum inválido → sem filtro.
     * O valor recebido é convertido para {@link ShipmentStatus} via valueOf().
     * Valores inválidos são silenciosamente ignorados (sem 400).
     */
    private static Specification<Shipment> hasStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            ShipmentStatus statusEnum = ShipmentStatus.valueOf(status.toUpperCase());
            return (root, query, cb) -> cb.equal(root.get("status"), statusEnum);
        } catch (IllegalArgumentException e) {
            return null; // enum name inválido → ignora o filtro
        }
    }

    /**
     * WHERE LOWER(vessel.carrier) LIKE '%valor%'
     * JOIN: shipment → voyage (MANY-TO-ONE) → vessel (MANY-TO-ONE)
     */
    private static Specification<Shipment> hasCarrierLike(String carrier) {
        if (carrier == null || carrier.isBlank()) return null;
        String pattern = "%" + carrier.toLowerCase() + "%";
        return (root, query, cb) -> {
            Join<Object, Object> voyageJoin = root.join("voyage", JoinType.LEFT);
            Join<Object, Object> vesselJoin = voyageJoin.join("vessel", JoinType.LEFT);
            return cb.like(cb.lower(vesselJoin.get("carrier")), pattern);
        };
    }

    /**
     * WHERE LOWER(vessel.name) LIKE '%valor%'
     * JOIN: shipment → voyage (MANY-TO-ONE) → vessel (MANY-TO-ONE)
     */
    private static Specification<Shipment> hasVesselNameLike(String vesselName) {
        if (vesselName == null || vesselName.isBlank()) return null;
        String pattern = "%" + vesselName.toLowerCase() + "%";
        return (root, query, cb) -> {
            Join<Object, Object> voyageJoin = root.join("voyage", JoinType.LEFT);
            Join<Object, Object> vesselJoin = voyageJoin.join("vessel", JoinType.LEFT);
            return cb.like(cb.lower(vesselJoin.get("name")), pattern);
        };
    }

    /**
     * WHERE originPort.unlocode = :unlocode (case-insensitive via toUpperCase)
     * JOIN: shipment → originPort (MANY-TO-ONE)
     */
    private static Specification<Shipment> hasOriginPortUnlocode(String unlocode) {
        if (unlocode == null || unlocode.isBlank()) return null;
        String value = unlocode.toUpperCase();
        return (root, query, cb) -> {
            Join<Object, Object> portJoin = root.join("originPort", JoinType.LEFT);
            return cb.equal(portJoin.get("unlocode"), value);
        };
    }

    /**
     * WHERE s.riskLevel = :riskLevel (campo String, não enum)
     * Valores aceitos: LOW, MEDIUM, HIGH, CRITICAL
     */
    private static Specification<Shipment> hasRiskLevel(String riskLevel) {
        if (riskLevel == null || riskLevel.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("riskLevel"), riskLevel.toUpperCase());
    }
}
