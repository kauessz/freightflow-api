package com.freightflow.modules.event;

import com.freightflow.modules.event.dto.CreateEventRequest;
import com.freightflow.modules.event.dto.EventResponse;
import com.freightflow.modules.event.enums.EventType;
import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.shipment.repository.ShipmentRepository;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final ShipmentRepository shipmentRepository;

    public EventService(EventRepository eventRepository,
                        ShipmentRepository shipmentRepository) {
        this.eventRepository = eventRepository;
        this.shipmentRepository = shipmentRepository;
    }

    // ==================== Queries ====================

    public List<EventResponse> listByShipment(UUID shipmentId, UUID tenantId, UUID customerId) {
        log.debug("Listing events for shipment={}", shipmentId);
        Shipment shipment = getScopedShipment(shipmentId, tenantId, customerId);

        List<Event> events = customerId != null
                ? eventRepository.findByShipmentIdAndShipmentTenantIdAndShipmentCustomerIdOrderByOccurredAtAsc(
                        shipment.getId(), tenantId, customerId)
                : eventRepository.findByShipmentIdAndShipmentTenantIdOrderByOccurredAtAsc(
                        shipment.getId(), tenantId);

        return events
                .stream()
                .map(EventResponse::from)
                .toList();
    }

    public EventResponse getById(UUID shipmentId, UUID eventId, UUID tenantId, UUID customerId) {
        log.debug("Fetching event id={} for shipment={}", eventId, shipmentId);
        Event event = getScopedEvent(shipmentId, eventId, tenantId, customerId);
        return EventResponse.from(event);
    }

    // ==================== Commands ====================

    @Transactional
    public EventResponse create(UUID shipmentId, CreateEventRequest request, UUID tenantId, UUID customerId) {
        log.info("Creating event type={} for shipment={}", request.type(), shipmentId);

        Shipment shipment = getScopedShipment(shipmentId, tenantId, customerId);

        // Regra: GATE_IN só pode ocorrer uma vez por shipment
        if (request.type() == EventType.GATE_IN
                && eventRepository.existsByShipmentIdAndType(shipmentId, EventType.GATE_IN)) {
            throw new BusinessException("GATE_IN event already exists for shipment " + shipmentId);
        }

        // Validacao: evento nao pode ser anterior ao ultimo evento registrado
        Event lastEvent = shipment.lastEvent();
        if (lastEvent != null && request.occurredAt().isBefore(lastEvent.getOccurredAt())) {
            throw new BusinessException(
                "Event timestamp (" + request.occurredAt() + ") cannot be before last event ("
                + lastEvent.getOccurredAt() + ")");
        }

        Event event = new Event(shipment, request.type(), request.location(),
                request.description(), request.occurredAt());

        // Dominio: addEvent atualiza o status do shipment automaticamente
        shipment.addEvent(event);
        shipmentRepository.save(shipment);

        log.info("Event created: type={}, shipment={}, new status={}",
                event.getType(), shipment.getBooking(), shipment.getStatus());

        return EventResponse.from(event);
    }

    @Transactional
    public void delete(UUID shipmentId, UUID eventId, UUID tenantId, UUID customerId) {
        log.info("Deleting event id={} for shipment={}", eventId, shipmentId);
        Event event = getScopedEvent(shipmentId, eventId, tenantId, customerId);
        eventRepository.delete(event);
    }

    private Shipment getScopedShipment(UUID shipmentId, UUID tenantId, UUID customerId) {
        if (customerId != null) {
            return shipmentRepository.findByIdAndTenantIdAndCustomerId(shipmentId, tenantId, customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Shipment", shipmentId));
        }
        return shipmentRepository.findByIdAndTenantId(shipmentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", shipmentId));
    }

    private Event getScopedEvent(UUID shipmentId, UUID eventId, UUID tenantId, UUID customerId) {
        if (customerId != null) {
            return eventRepository.findByIdAndShipmentIdAndShipmentTenantIdAndShipmentCustomerId(
                            eventId, shipmentId, tenantId, customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));
        }
        return eventRepository.findByIdAndShipmentIdAndShipmentTenantId(eventId, shipmentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));
    }
}
