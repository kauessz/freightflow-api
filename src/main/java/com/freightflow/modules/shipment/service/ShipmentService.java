package com.freightflow.modules.shipment.service;

import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.port.Port;
import com.freightflow.modules.port.PortRepository;
import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.shipment.dto.CreateShipmentRequest;
import com.freightflow.modules.shipment.dto.ShipmentResponse;
import com.freightflow.modules.shipment.dto.TrackingResponse;
import com.freightflow.modules.shipment.dto.UpdateShipmentRequest;
import com.freightflow.modules.shipment.repository.ShipmentRepository;
import com.freightflow.modules.voyage.Voyage;
import com.freightflow.modules.voyage.VoyageRepository;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import com.freightflow.shared.pagination.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ShipmentService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentService.class);

    private final ShipmentRepository shipmentRepository;
    private final VoyageRepository voyageRepository;
    private final PortRepository portRepository;
    private final TenantRepository tenantRepository;

    public ShipmentService(ShipmentRepository shipmentRepository,
                           VoyageRepository voyageRepository,
                           PortRepository portRepository,
                           TenantRepository tenantRepository) {
        this.shipmentRepository = shipmentRepository;
        this.voyageRepository = voyageRepository;
        this.portRepository = portRepository;
        this.tenantRepository = tenantRepository;
    }

    // ==================== Queries ====================

    public PageResponse<ShipmentResponse> list(UUID tenantId, Pageable pageable) {
        log.debug("Listing shipments for tenant={}", tenantId);
        var page = shipmentRepository.findByTenantId(tenantId, pageable);
        return PageResponse.from(page.map(ShipmentResponse::from));
    }

    public ShipmentResponse getById(UUID id) {
        log.debug("Fetching shipment id={}", id);
        return ShipmentResponse.from(getEntityById(id));
    }

    public Shipment getEntityById(UUID id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", id));
    }

    public TrackingResponse track(String booking) {
        log.info("Public tracking request for booking={}", booking);
        Shipment shipment = shipmentRepository.findByBookingWithDetails(booking)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", booking));

        var events = shipment.getEvents().stream()
                .map(e -> new TrackingResponse.TrackingEvent(
                        e.getType(), e.getLocation(), e.getOccurredAt(), e.getDescription()))
                .collect(Collectors.toList());

        return new TrackingResponse(
                shipment.getBooking(),
                shipment.getContainerNumber(),
                shipment.getStatus(),
                shipment.getVoyage().getVessel().getName(),
                shipment.getOriginPort().getName(),
                shipment.getDestinationPort().getName(),
                shipment.getVoyage().getEtd(),
                shipment.getVoyage().getEta(),
                events
        );
    }

    // ==================== Commands ====================

    @Transactional
    public ShipmentResponse create(CreateShipmentRequest request, UUID tenantId) {
        log.info("Creating shipment booking={} for tenant={}", request.booking(), tenantId);

        // Regra de negocio: booking unico
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
    public ShipmentResponse update(UUID id, UpdateShipmentRequest request) {
        log.info("Updating shipment id={}", id);
        Shipment shipment = getEntityById(id);

        if (request.containerNumber() != null) {
            shipment.setContainerNumber(request.containerNumber());
        }
        if (request.containerType() != null) {
            shipment.setContainerType(request.containerType());
        }
        if (request.consignee() != null) {
            shipment.setConsignee(request.consignee());
        }
        if (request.shipper() != null) {
            shipment.setShipper(request.shipper());
        }

        Shipment saved = shipmentRepository.save(shipment);
        return ShipmentResponse.from(saved);
    }

    @Transactional
    public void delete(UUID id) {
        log.info("Deleting shipment id={}", id);
        if (!shipmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Shipment", id);
        }
        shipmentRepository.deleteById(id);
    }
}
