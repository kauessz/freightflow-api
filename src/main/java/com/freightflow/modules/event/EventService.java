package com.freightflow.modules.event;

import com.freightflow.modules.event.dto.CreateEventRequest;
import com.freightflow.modules.event.dto.EventResponse;
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

    public List<EventResponse> listByShipment(UUID shipmentId) {
        log.debug("Listing events for shipment={}", shipmentId);
        if (!shipmentRepository.existsById(shipmentId)) {
            throw new ResourceNotFoundException("Shipment", shipmentId);
        }
        return eventRepository.findByShipmentIdOrderByOccurredAtDesc(shipmentId)
                .stream()
                .map(EventResponse::from)
                .toList();
    }

    public EventResponse getById(UUID id) {
        log.debug("Fetching event id={}", id);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
        return EventResponse.from(event);
    }

    // ==================== Commands ====================

    @Transactional
    public EventResponse create(UUID shipmentId, CreateEventRequest request) {
        log.info("Creating event type={} for shipment={}", request.type(), shipmentId);

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", shipmentId));

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
    public void delete(UUID id) {
        log.info("Deleting event id={}", id);
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event", id);
        }
        eventRepository.deleteById(id);
    }
}
