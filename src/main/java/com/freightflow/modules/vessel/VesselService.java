package com.freightflow.modules.vessel;

import com.freightflow.modules.ais.VesselPositionResolver;
import com.freightflow.modules.ais.dto.AisPositionResponse;
import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.shipment.dto.FleetMapShipmentResponse;
import com.freightflow.modules.shipment.repository.ShipmentRepository;
import com.freightflow.modules.vessel.dto.CreateVesselRequest;
import com.freightflow.modules.vessel.dto.UpdateVesselRequest;
import com.freightflow.modules.vessel.dto.VesselResponse;
import com.freightflow.modules.vessel.dto.VesselWithVoyageResponse;
import com.freightflow.modules.voyage.Voyage;
import com.freightflow.modules.voyage.VoyageFleetMapEligibilityService;
import com.freightflow.modules.voyage.VoyageRepository;
import com.freightflow.modules.voyage.dto.FleetMapIneligibilityReason;
import com.freightflow.modules.voyage.enums.VoyageStatus;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import com.freightflow.shared.pagination.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class VesselService {

    private static final Logger log = LoggerFactory.getLogger(VesselService.class);

    private final VesselRepository    vesselRepository;
    private final VoyageRepository    voyageRepository;
    private final ShipmentRepository  shipmentRepository;
    private final VesselPositionResolver vesselPositionResolver;
    private final VoyageFleetMapEligibilityService voyageFleetMapEligibilityService;

    public VesselService(VesselRepository vesselRepository,
                         VoyageRepository voyageRepository,
                         ShipmentRepository shipmentRepository,
                         VesselPositionResolver vesselPositionResolver,
                         VoyageFleetMapEligibilityService voyageFleetMapEligibilityService) {
        this.vesselRepository   = vesselRepository;
        this.voyageRepository   = voyageRepository;
        this.shipmentRepository = shipmentRepository;
        this.vesselPositionResolver = vesselPositionResolver;
        this.voyageFleetMapEligibilityService = voyageFleetMapEligibilityService;
    }

    public PageResponse<VesselResponse> list(Pageable pageable) {
        log.debug("Listing vessels");
        var page = vesselRepository.findAll(pageable);
        return PageResponse.from(page.map(VesselResponse::from));
    }

    public VesselResponse getById(UUID id) {
        log.debug("Fetching vessel id={}", id);
        Vessel vessel = vesselRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vessel", id));
        return VesselResponse.from(vessel);
    }

    public VesselResponse getByImo(String imo) {
        log.debug("Fetching vessel imo={}", imo);
        Vessel vessel = vesselRepository.findByImo(imo)
                .orElseThrow(() -> new ResourceNotFoundException("Vessel", imo));
        return VesselResponse.from(vessel);
    }

    /**
     * Returns all active voyages (IN_TRANSIT or DEPARTED) that contain at least
     * one shipment from the given tenant, enriched with AIS position and shipment count.
     * Used by the Fleet Map "My shipments" toggle.
     */
    public List<VesselWithVoyageResponse> getActiveWithShipments(UUID tenantId, UUID customerId) {
        log.debug("Fetching active voyages with tenant shipments for tenantId={} customerId={}", tenantId, customerId);

        List<VoyageStatus> activeStatuses = List.of(VoyageStatus.IN_TRANSIT, VoyageStatus.DEPARTED);
        List<Voyage> voyages = customerId != null
                ? voyageRepository.findActiveVoyagesWithCustomerShipments(tenantId, customerId, activeStatuses)
                : voyageRepository.findActiveVoyagesWithTenantShipments(tenantId, activeStatuses);

        if (voyages.isEmpty()) {
            return List.of();
        }

        List<UUID> voyageIds = voyages.stream().map(Voyage::getId).toList();
        Map<UUID, Integer> shipmentCounts = (customerId != null
                ? shipmentRepository.countByVoyageIdsAndTenantIdAndCustomerId(voyageIds, tenantId, customerId)
                : shipmentRepository.countByVoyageIdsAndTenantId(voyageIds, tenantId))
                .stream()
                .collect(Collectors.toMap(
                        ShipmentRepository.VoyageShipmentCountView::getVoyageId,
                        view -> Math.toIntExact(view.getShipmentCount())
                ));

        Map<UUID, List<FleetMapShipmentResponse>> relatedShipments = (customerId != null
                ? shipmentRepository.findByVoyageIdsAndTenantIdAndCustomerId(voyageIds, tenantId, customerId)
                : shipmentRepository.findByVoyageIdsAndTenantId(voyageIds, tenantId))
                .stream()
                .collect(Collectors.groupingBy(
                        shipment -> shipment.getVoyage().getId(),
                        Collectors.mapping(FleetMapShipmentResponse::from, Collectors.toList())
                ));

        return voyages.stream().map(voyage -> {
            AisPositionResponse pos = vesselPositionResolver.resolveForVoyage(voyage, true);
            int shipmentCount = shipmentCounts.getOrDefault(voyage.getId(), 0);
            var eligibility = voyageFleetMapEligibilityService.evaluate(voyage, shipmentCount);
            List<FleetMapShipmentResponse> voyageShipments = relatedShipments.getOrDefault(voyage.getId(), List.of());
            return VesselWithVoyageResponse.from(
                    voyage,
                    pos,
                    shipmentCount,
                    aggregateRiskLevel(voyageShipments),
                    voyageShipments,
                    eligibility.eligibleForFleetMap(),
                    eligibility.ineligibilityReasons()
            );
        }).filter(VesselWithVoyageResponse::eligibleForFleetMap).toList();
    }

    @Transactional
    public VesselResponse create(CreateVesselRequest request) {
        String imo = normalizeImo(request.imo());
        log.info("Creating vessel imo={}", imo);

        if (imo != null && vesselRepository.existsByImo(imo)) {
            throw new BusinessException("Vessel with IMO " + imo + " already exists");
        }

        Vessel vessel = new Vessel(
                imo, request.name(), request.flag(),
                request.type(), request.capacityTeu()
        );
        vessel.setCarrier(request.carrier());
        if (request.active() != null) {
            vessel.setActive(request.active());
        }

        Vessel saved = vesselRepository.save(vessel);
        log.info("Vessel created: id={}, imo={}, name={}", saved.getId(), saved.getImo(), saved.getName());
        return VesselResponse.from(saved);
    }

    @Transactional
    public VesselResponse update(UUID id, UpdateVesselRequest request) {
        log.info("Updating vessel id={}", id);
        Vessel vessel = vesselRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vessel", id));

        if (request.imo() != null) {
            String imo = normalizeImo(request.imo());
            if (imo != null && vesselRepository.existsByImoAndIdNot(imo, id)) {
                throw new BusinessException("Vessel with IMO " + imo + " already exists");
            }
            vessel.setImo(imo);
        }
        if (request.name() != null) vessel.setName(request.name());
        if (request.flag() != null) vessel.setFlag(request.flag());
        if (request.type() != null) vessel.setType(request.type());
        if (request.capacityTeu() != null) vessel.setCapacityTeu(request.capacityTeu());
        if (request.carrier() != null) vessel.setCarrier(request.carrier());
        if (request.active() != null) vessel.setActive(request.active());

        Vessel saved = vesselRepository.save(vessel);
        return VesselResponse.from(saved);
    }

    @Transactional
    public void delete(UUID id) {
        log.info("Deleting vessel id={}", id);
        Vessel vessel = vesselRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vessel", id));

        if (!vessel.getVoyages().isEmpty()) {
            throw new BusinessException("Cannot delete vessel with " + vessel.getVoyages().size() + " voyages");
        }

        vesselRepository.delete(vessel);
    }

    private String normalizeImo(String imo) {
        if (imo == null) return null;
        String normalized = imo.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String aggregateRiskLevel(List<FleetMapShipmentResponse> shipments) {
        if (shipments == null || shipments.isEmpty()) {
            return null;
        }

        return shipments.stream()
                .map(FleetMapShipmentResponse::riskLevel)
                .filter(level -> level != null && !level.isBlank())
                .max(java.util.Comparator.comparingInt(this::riskRank))
                .orElse(null);
    }

    private int riskRank(String riskLevel) {
        return switch (riskLevel) {
            case "CRITICAL" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }
}
