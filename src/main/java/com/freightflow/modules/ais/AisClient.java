package com.freightflow.modules.ais;

import com.freightflow.modules.ais.dto.AisPositionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Client for Marine Digital AIS API.
 * Fetches real-time vessel positions by IMO number.
 *
 * Results are cached in Redis under the "ais-positions" cache.
 * TTL is configurable via freightflow.cache.ais-ttl-minutes (default 5 minutes).
 * Null results (API failures) are never cached, so the next call retries immediately.
 *
 * Fallback chain (handled by VesselPositionResolver, not here):
 *   Live AIS → midpoint estimate → UNAVAILABLE
 */
@Component
public class AisClient {

    private static final Logger log = LoggerFactory.getLogger(AisClient.class);
    private static final String AIS_BASE_URL = "https://api.marine.digital/v1/ais/vessel/";

    private final RestTemplate restTemplate;

    public AisClient() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * Fetch AIS position for a vessel by IMO number.
     *
     * Cached in Redis with the TTL configured for "ais-positions".
     * Returns null on any failure (graceful degradation); null results are NOT cached
     * so that a transient API failure does not freeze position data.
     *
     * @param imo vessel IMO number (e.g. "9321483")
     * @return live position, or null if the AIS API is unreachable / returns no data
     */
    @Cacheable(value = "ais-positions", key = "#imo", unless = "#result == null")
    public AisPositionResponse getPosition(String imo) {
        log.info("AIS cache miss — fetching from API for IMO {}", imo);
        return fetchFromApi(imo);
    }

    private AisPositionResponse fetchFromApi(String imo) {
        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    AIS_BASE_URL + imo, Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = response.getBody();
                return parsePosition(imo, body);
            }

            log.warn("AIS API returned non-200 for IMO {}: {}", imo, response.getStatusCode());
            return null;
        } catch (Exception e) {
            log.warn("AIS API call failed for IMO {}: {}", imo, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private AisPositionResponse parsePosition(String imo, Map<String, Object> body) {
        try {
            Double lat = toDouble(body.get("lat"));
            Double lon = toDouble(body.get("lon"));

            if (lat == null || lon == null) {
                // Try nested data structure
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                if (data != null) {
                    lat = toDouble(data.get("lat"));
                    lon = toDouble(data.get("lon"));
                }
            }

            if (lat == null || lon == null) {
                log.warn("AIS response for IMO {} has no lat/lon", imo);
                return null;
            }

            Double speed = toDouble(body.getOrDefault("speed", body.get("sog")));
            Double course = toDouble(body.getOrDefault("course", body.get("cog")));
            String status = body.containsKey("status")
                    ? String.valueOf(body.get("status"))
                    : "underway";
            String timestampStr = body.containsKey("timestamp")
                    ? String.valueOf(body.get("timestamp"))
                    : null;

            Instant timestamp;
            try {
                timestamp = timestampStr != null ? Instant.parse(timestampStr) : Instant.now();
            } catch (Exception e) {
                timestamp = Instant.now();
            }

            AisPositionResponse result = AisPositionResponse.live(imo, lat, lon, speed, course, status, timestamp);
            log.info("AIS position fetched for IMO {} — storing in cache", imo);
            return result;
        } catch (Exception e) {
            log.warn("Failed to parse AIS response for IMO {}: {}", imo, e.getMessage());
            return null;
        }
    }

    private Double toDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
