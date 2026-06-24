package com.freightflow.modules.event;

import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.event.dto.CreateEventRequest;
import com.freightflow.modules.event.dto.EventResponse;
import com.freightflow.modules.event.enums.EventType;
import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.shipment.enums.ShipmentStatus;
import com.freightflow.modules.shipment.repository.ShipmentRepository;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventService")
class EventServiceTest {

    @Mock private EventRepository eventRepository;
    @Mock private ShipmentRepository shipmentRepository;

    @InjectMocks
    private EventService eventService;

    private Shipment shipment;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        shipment = TestDataFactory.shipment();
        tenantId = shipment.getTenant().getId();
    }

    // ── create() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should_registerEvent_when_shipmentExists")
        void should_registerEvent_when_shipmentExists() {
            // Arrange — LOADED type skips the GATE_IN duplicate check
            UUID shipmentId = shipment.getId();
            CreateEventRequest request = new CreateEventRequest(
                    EventType.LOADED, "Santos, BR", "Container loaded onto vessel", Instant.now());

            when(shipmentRepository.findByIdAndTenantId(shipmentId, tenantId)).thenReturn(Optional.of(shipment));
            when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);

            // Act
            EventResponse result = eventService.create(shipmentId, request, tenantId, null);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.type()).isEqualTo(EventType.LOADED);
            assertThat(result.shipmentId()).isEqualTo(shipmentId);
            verify(shipmentRepository).save(shipment);
        }

        @Test
        @DisplayName("should_updateShipmentStatus_when_gateInEventRegistered")
        void should_updateShipmentStatus_when_gateInEventRegistered() {
            // Arrange
            UUID shipmentId = shipment.getId();
            CreateEventRequest request = new CreateEventRequest(
                    EventType.GATE_IN, "Santos, BR", null, Instant.now());

            when(shipmentRepository.findByIdAndTenantId(shipmentId, tenantId)).thenReturn(Optional.of(shipment));
            when(eventRepository.existsByShipmentIdAndType(shipmentId, EventType.GATE_IN)).thenReturn(false);
            when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);

            // Act
            eventService.create(shipmentId, request, tenantId, null);

            // Assert — shipment.addEvent() mutates status in-memory via domain logic
            assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.GATE_IN);
        }

        @Test
        @DisplayName("should_throwBusinessException_when_gateInAlreadyExistsForShipment")
        void should_throwBusinessException_when_gateInAlreadyExistsForShipment() {
            // Arrange
            UUID shipmentId = shipment.getId();
            CreateEventRequest request = new CreateEventRequest(
                    EventType.GATE_IN, "Santos, BR", null, Instant.now());

            when(shipmentRepository.findByIdAndTenantId(shipmentId, tenantId)).thenReturn(Optional.of(shipment));
            when(eventRepository.existsByShipmentIdAndType(shipmentId, EventType.GATE_IN)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> eventService.create(shipmentId, request, tenantId, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("GATE_IN event already exists");
        }

        @Test
        @DisplayName("should_throwBusinessException_when_eventOccurredAtIsBeforePreviousEvent")
        void should_throwBusinessException_when_eventOccurredAtIsBeforePreviousEvent() {
            // Arrange — seed the shipment with a previous event
            Instant firstEventTime = Instant.now().minus(5, ChronoUnit.HOURS);
            // LOADED doesn't trigger any guard, so we add it directly to populate lastEvent()
            Event existing = new Event(shipment, EventType.LOADED, "Santos, BR", firstEventTime);
            shipment.addEvent(existing); // lastEvent() now returns this event

            Instant tooEarly = firstEventTime.minus(2, ChronoUnit.HOURS); // strictly before last event
            CreateEventRequest request = new CreateEventRequest(
                    EventType.GATE_OUT, "Rotterdam, NL", null, tooEarly);

            UUID shipmentId = shipment.getId();
            when(shipmentRepository.findByIdAndTenantId(shipmentId, tenantId)).thenReturn(Optional.of(shipment));

            // Act & Assert
            assertThatThrownBy(() -> eventService.create(shipmentId, request, tenantId, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("cannot be before last event");
        }

        @Test
        @DisplayName("should_notCreateEvent_when_shipmentBelongsToAnotherTenant")
        void should_notCreateEvent_when_shipmentBelongsToAnotherTenant() {
            UUID shipmentId = shipment.getId();
            CreateEventRequest request = new CreateEventRequest(
                    EventType.LOADED, "Santos, BR", "Container loaded onto vessel", Instant.now());

            when(shipmentRepository.findByIdAndTenantId(shipmentId, tenantId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.create(shipmentId, request, tenantId, null))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shipment");

            verify(shipmentRepository, never()).save(any(Shipment.class));
        }

        @Test
        @DisplayName("should_notCreateEvent_when_clientTargetsAnotherCustomer")
        void should_notCreateEvent_when_clientTargetsAnotherCustomer() {
            UUID shipmentId = shipment.getId();
            UUID customerId = UUID.randomUUID();
            CreateEventRequest request = new CreateEventRequest(
                    EventType.LOADED, "Santos, BR", "Container loaded onto vessel", Instant.now());

            when(shipmentRepository.findByIdAndTenantIdAndCustomerId(shipmentId, tenantId, customerId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.create(shipmentId, request, tenantId, customerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shipment");

            verify(shipmentRepository, never()).save(any(Shipment.class));
        }
    }

    // ── listByShipment() ─────────────────────────────────────────────────

    @Nested
    @DisplayName("listByShipment()")
    class ListByShipmentTests {

        @Test
        @DisplayName("should_returnEventHistory_orderedByOccurredAtAsc")
        void should_returnEventHistory_orderedByOccurredAtAsc() {
            // Arrange — repo returns events in chronological (ASC) order
            UUID shipmentId = shipment.getId();
            Instant t1 = Instant.now().minus(10, ChronoUnit.HOURS);
            Instant t2 = Instant.now().minus(5, ChronoUnit.HOURS);
            Instant t3 = Instant.now().minus(1, ChronoUnit.HOURS);

            Event ev1 = new Event(shipment, EventType.GATE_IN,  "Santos, BR",  t1);
            Event ev2 = new Event(shipment, EventType.LOADED,   "Santos, BR",  t2);
            Event ev3 = new Event(shipment, EventType.DEPARTED, "Santos, BR",  t3);

            when(shipmentRepository.findByIdAndTenantId(shipmentId, tenantId)).thenReturn(Optional.of(shipment));
            when(eventRepository.findByShipmentIdAndShipmentTenantIdOrderByOccurredAtAsc(shipmentId, tenantId))
                    .thenReturn(List.of(ev1, ev2, ev3));

            // Act
            List<EventResponse> result = eventService.listByShipment(shipmentId, tenantId, null);

            // Assert — chronological order enforced by the ASC query
            assertThat(result).hasSize(3);
            assertThat(result.get(0).type()).isEqualTo(EventType.GATE_IN);
            assertThat(result.get(1).type()).isEqualTo(EventType.LOADED);
            assertThat(result.get(2).type()).isEqualTo(EventType.DEPARTED);
        }

        @Test
        @DisplayName("should_throwResourceNotFoundException_when_shipmentNotFound")
        void should_throwResourceNotFoundException_when_shipmentNotFound() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(shipmentRepository.findByIdAndTenantId(nonExistentId, tenantId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> eventService.listByShipment(nonExistentId, tenantId, null))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shipment");
        }

        @Test
        @DisplayName("should_notListEvents_when_shipmentBelongsToAnotherTenant")
        void should_notListEvents_when_shipmentBelongsToAnotherTenant() {
            UUID shipmentId = shipment.getId();
            when(shipmentRepository.findByIdAndTenantId(shipmentId, tenantId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.listByShipment(shipmentId, tenantId, null))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shipment");
        }

        @Test
        @DisplayName("should_notListEvents_when_clientTargetsAnotherCustomer")
        void should_notListEvents_when_clientTargetsAnotherCustomer() {
            UUID shipmentId = shipment.getId();
            UUID customerId = UUID.randomUUID();
            when(shipmentRepository.findByIdAndTenantIdAndCustomerId(shipmentId, tenantId, customerId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.listByShipment(shipmentId, tenantId, customerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shipment");
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("should_returnEvent_when_authorized")
        void should_returnEvent_when_authorized() {
            UUID eventId = UUID.randomUUID();
            Event event = new Event(shipment, EventType.LOADED, "Santos, BR", Instant.now());

            when(eventRepository.findByIdAndShipmentIdAndShipmentTenantId(eventId, shipment.getId(), tenantId))
                    .thenReturn(Optional.of(event));

            EventResponse result = eventService.getById(shipment.getId(), eventId, tenantId, null);

            assertThat(result.shipmentId()).isEqualTo(shipment.getId());
            assertThat(result.type()).isEqualTo(EventType.LOADED);
        }

        @Test
        @DisplayName("should_notReturnEvent_when_itBelongsToAnotherTenant")
        void should_notReturnEvent_when_itBelongsToAnotherTenant() {
            UUID eventId = UUID.randomUUID();
            when(eventRepository.findByIdAndShipmentIdAndShipmentTenantId(eventId, shipment.getId(), tenantId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.getById(shipment.getId(), eventId, tenantId, null))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Event");
        }

        @Test
        @DisplayName("should_notReturnEvent_when_clientTargetsAnotherCustomer")
        void should_notReturnEvent_when_clientTargetsAnotherCustomer() {
            UUID eventId = UUID.randomUUID();
            UUID customerId = UUID.randomUUID();
            when(eventRepository.findByIdAndShipmentIdAndShipmentTenantIdAndShipmentCustomerId(
                    eventId, shipment.getId(), tenantId, customerId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.getById(shipment.getId(), eventId, tenantId, customerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Event");
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("should_deleteEvent_when_authorized")
        void should_deleteEvent_when_authorized() {
            UUID eventId = UUID.randomUUID();
            Event event = new Event(shipment, EventType.LOADED, "Santos, BR", Instant.now());

            when(eventRepository.findByIdAndShipmentIdAndShipmentTenantId(eventId, shipment.getId(), tenantId))
                    .thenReturn(Optional.of(event));

            eventService.delete(shipment.getId(), eventId, tenantId, null);

            verify(eventRepository).delete(event);
        }

        @Test
        @DisplayName("should_notDeleteEvent_when_eventBelongsToAnotherTenant")
        void should_notDeleteEvent_when_eventBelongsToAnotherTenant() {
            UUID eventId = UUID.randomUUID();
            when(eventRepository.findByIdAndShipmentIdAndShipmentTenantId(eventId, shipment.getId(), tenantId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.delete(shipment.getId(), eventId, tenantId, null))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Event");

            verify(eventRepository, never()).delete(any(Event.class));
        }

        @Test
        @DisplayName("should_notDeleteEvent_when_clientTargetsAnotherCustomer")
        void should_notDeleteEvent_when_clientTargetsAnotherCustomer() {
            UUID eventId = UUID.randomUUID();
            UUID customerId = UUID.randomUUID();
            when(eventRepository.findByIdAndShipmentIdAndShipmentTenantIdAndShipmentCustomerId(
                    eventId, shipment.getId(), tenantId, customerId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.delete(shipment.getId(), eventId, tenantId, customerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Event");

            verify(eventRepository, never()).delete(any(Event.class));
        }
    }
}
