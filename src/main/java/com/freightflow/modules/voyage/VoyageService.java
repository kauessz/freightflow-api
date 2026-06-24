package com.freightflow.modules.voyage;

import com.freightflow.modules.ais.VesselPositionResolver;
import com.freightflow.modules.ais.dto.AisPositionResponse;
import com.freightflow.modules.ais.dto.VoyageTrackingResponse;
import com.freightflow.modules.port.Port;
import com.freightflow.modules.port.PortRepository;
import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.shipment.dto.ShipmentSummaryResponse;
import com.freightflow.modules.shipment.repository.ShipmentRepository;
import com.freightflow.modules.vessel.Vessel;
import com.freightflow.modules.vessel.VesselRepository;
import com.freightflow.modules.voyage.dto.CreateVoyageRequest;
import com.freightflow.modules.voyage.dto.VoyageFleetMapReadinessResponse;
import com.freightflow.modules.voyage.dto.UpdateVoyageRequest;
import com.freightflow.modules.voyage.dto.VoyageResponse;
import com.freightflow.modules.voyage.enums.VoyageStatus;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import com.freightflow.shared.pagination.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class VoyageService {

    private static final Logger log = LoggerFactory.getLogger(VoyageService.class);

    /**
     * Valid forward-only status transitions for a voyage.
     * CANCELLED is reachable from any non-completed state.
     */
    private static final Map<VoyageStatus, Set<VoyageStatus>> ALLOWED_TRANSITIONS = Map.of(
        VoyageStatus.SCHEDULED,  EnumSet.of(VoyageStatus.DEPARTED, VoyageStatus.CANCELLED),
        VoyageStatus.DEPARTED,   EnumSet.of(VoyageStatus.IN_TRANSIT, VoyageStatus.CANCELLED),
        VoyageStatus.IN_TRANSIT, EnumSet.of(VoyageStatus.ARRIVED, VoyageStatus.CANCELLED),
        VoyageStatus.ARRIVED,    EnumSet.of(VoyageStatus.COMPLETED, VoyageStatus.CANCELLED),
        VoyageStatus.COMPLETED,  EnumSet.noneOf(VoyageStatus.class),
        VoyageStatus.CANCELLED,  EnumSet.noneOf(VoyageStatus.class)
    );

    private final VoyageRepository    voyageRepository;
    private final VesselRepository    vesselRepository;
    private final PortRepository      portRepository;
    private final ShipmentRepository  shipmentRepository;
    private final VesselPositionResolver vesselPositionResolver;
    private final VoyageFleetMapEligibilityService voyageFleetMapEligibilityService;

    public VoyageService(VoyageRepository voyageRepository,
                         VesselRepository vesselRepository,
                         PortRepository portRepository,
                         ShipmentRepository shipmentRepository,
                         VesselPositionResolver vesselPositionResolver,
                         VoyageFleetMapEligibilityService voyageFleetMapEligibilityService) {
        this.voyageRepository   = voyageRepository;
        this.vesselRepository   = vesselRepository;
        this.portRepository     = portRepository;
        this.shipmentRepository = shipmentRepository;
        this.vesselPositionResolver = vesselPositionResolver;
        this.voyageFleetMapEligibilityService = voyageFleetMapEligibilityService;
    }

    // ==================== Queries ====================

    public PageResponse<VoyageResponse> list(Pageable pageable) {
        log.debug("Listing voyages");
        var page = voyageRepository.findAllWithDetails(pageable);
        return PageResponse.from(page.map(VoyageResponse::from));
    }

    public VoyageResponse getById(UUID id) {
        log.debug("Fetching voyage id={}", id);
        Voyage voyage = voyageRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voyage", id));
        return VoyageResponse.from(voyage);
    }

    public VoyageResponse getByVoyageNumber(String voyageNumber) {
        log.debug("Fetching voyage number={}", voyageNumber);
        Voyage voyage = voyageRepository.findByVoyageNumberWithDetails(voyageNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Voyage", voyageNumber));
        return VoyageResponse.from(voyage);
    }

    public VoyageTrackingResponse getTracking(UUID id) {
        log.debug("Fetching tracking for voyage id={}", id);
        Voyage voyage = voyageRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voyage", id));
        AisPositionResponse position = vesselPositionResolver.resolveForVoyage(voyage, true);
        return VoyageTrackingResponse.from(voyage, position);
    }

    // ==================== Commands ====================

    @Transactional
    public VoyageResponse create(CreateVoyageRequest request) {
        log.info("Creating voyage number={}", request.voyageNumber());

        if (voyageRepository.existsByVoyageNumber(request.voyageNumber())) {
            throw new BusinessException("Voyage number " + request.voyageNumber() + " already exists");
        }

        if (request.etd().isBefore(Instant.now())) {
            throw new BusinessException("ETD cannot be in the past");
        }

        if (request.eta().isBefore(request.etd())) {
            throw new BusinessException("ETA must be after ETD");
        }

        Vessel vessel = vesselRepository.findById(request.vesselId())
                .orElseThrow(() -> new ResourceNotFoundException("Vessel", request.vesselId()));

        Port originPort = portRepository.findById(request.originPortId())
                .orElseThrow(() -> new ResourceNotFoundException("Port", request.originPortId()));

        Port destinationPort = portRepository.findById(request.destinationPortId())
                .orElseThrow(() -> new ResourceNotFoundException("Port", request.destinationPortId()));

        if (originPort.getId().equals(destinationPort.getId())) {
            throw new BusinessException("Origin and destination ports must be different");
        }

        Voyage voyage = new Voyage(
                request.voyageNumber(), vessel, originPort, destinationPort,
                request.etd(), request.eta()
        );
        if (request.active() != null) {
            voyage.setActive(request.active());
        }

        Voyage saved = voyageRepository.save(voyage);
        log.info("Voyage created: id={}, number={}", saved.getId(), saved.getVoyageNumber());
        return VoyageResponse.from(saved);
    }

    @Transactional
    public VoyageResponse update(UUID id, UpdateVoyageRequest request) {
        log.info("Updating voyage id={}", id);
        Voyage voyage = voyageRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voyage", id));

        if (request.voyageNumber() != null) {
            if (voyageRepository.existsByVoyageNumberAndIdNot(request.voyageNumber(), id)) {
                throw new BusinessException("Voyage number " + request.voyageNumber() + " already exists");
            }
            voyage.setVoyageNumber(request.voyageNumber());
        }
        if (request.vesselId() != null) {
            Vessel vessel = vesselRepository.findById(request.vesselId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vessel", request.vesselId()));
            voyage.setVessel(vessel);
        }
        if (request.originPortId() != null) {
            Port originPort = portRepository.findById(request.originPortId())
                    .orElseThrow(() -> new ResourceNotFoundException("Port", request.originPortId()));
            voyage.setOriginPort(originPort);
        }
        if (request.destinationPortId() != null) {
            Port destinationPort = portRepository.findById(request.destinationPortId())
                    .orElseThrow(() -> new ResourceNotFoundException("Port", request.destinationPortId()));
            voyage.setDestinationPort(destinationPort);
        }
        if (request.status() != null) {
            VoyageStatus current = voyage.getStatus();
            VoyageStatus next = request.status();
            Set<VoyageStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(VoyageStatus.class));
            if (!allowed.contains(next)) {
                throw new BusinessException(
                        "Invalid status transition: " + current + " → " + next +
                        ". Allowed from " + current + ": " + allowed);
            }
            voyage.setStatus(next);
        }
        if (request.etd() != null) {
            voyage.setEtd(request.etd());
        }
        if (request.eta() != null) {
            voyage.setEta(request.eta());
        }
        if (request.atd() != null) {
            voyage.setAtd(request.atd());
        }
        if (request.ata() != null) {
            voyage.setAta(request.ata());
        }
        if (request.active() != null) {
            voyage.setActive(request.active());
        }

        Voyage saved = voyageRepository.save(voyage);
        return VoyageResponse.from(saved);
    }

    public List<VoyageFleetMapReadinessResponse> listFleetMapReadiness(UUID tenantId, UUID customerId, Boolean eligible) {
        var voyages = voyageRepository.findAllWithDetails(org.springframework.data.domain.Pageable.unpaged()).getContent();
        if (voyages.isEmpty()) {
            return List.of();
        }

        List<UUID> voyageIds = voyages.stream().map(Voyage::getId).toList();
        Map<UUID, Long> shipmentCounts = (customerId != null
                ? shipmentRepository.countByVoyageIdsAndTenantIdAndCustomerId(voyageIds, tenantId, customerId)
                : shipmentRepository.countByVoyageIdsAndTenantId(voyageIds, tenantId))
                .stream()
                .collect(Collectors.toMap(
                        ShipmentRepository.VoyageShipmentCountView::getVoyageId,
                        ShipmentRepository.VoyageShipmentCountView::getShipmentCount
                ));

        return voyages.stream()
                .map(voyage -> {
                    long linkedShipmentCount = shipmentCounts.getOrDefault(voyage.getId(), 0L);
                    var readiness = voyageFleetMapEligibilityService.evaluate(voyage, linkedShipmentCount);
                    return VoyageFleetMapReadinessResponse.from(
                            voyage,
                            linkedShipmentCount,
                            readiness.eligibleForFleetMap(),
                            readiness.ineligibilityReasons()
                    );
                })
                .filter(response -> eligible == null || response.eligibleForFleetMap() == eligible)
                .toList();
    }

    public List<ShipmentSummaryResponse> getShipmentsByVoyage(UUID voyageId, UUID tenantId, UUID customerId) {
        log.debug("Listing shipments for voyageId={} tenantId={}", voyageId, tenantId);

        // Verify voyage exists
        voyageRepository.findById(voyageId)
                .orElseThrow(() -> new ResourceNotFoundException("Voyage", voyageId));

        List<Shipment> shipments = (customerId != null)
                ? shipmentRepository.findByVoyageIdAndTenantIdAndCustomerId(voyageId, tenantId, customerId)
                : shipmentRepository.findByVoyageIdAndTenantId(voyageId, tenantId);

        return shipments.stream()
                .map(ShipmentSummaryResponse::from)
                .toList();
    }

    @Transactional
    public void delete(UUID id) {
        log.info("Deleting voyage id={}", id);
        Voyage voyage = voyageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voyage", id));

        if (!voyage.getShipments().isEmpty()) {
            throw new BusinessException("Cannot delete voyage with " + voyage.getShipments().size() + " active shipments");
        }

        voyageRepository.delete(voyage);
    }
}
