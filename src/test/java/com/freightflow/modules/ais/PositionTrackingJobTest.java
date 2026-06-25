package com.freightflow.modules.ais;

import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.ais.dto.AisPositionResponse;
import com.freightflow.modules.event.Event;
import com.freightflow.modules.event.EventRepository;
import com.freightflow.modules.event.enums.EventType;
import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.voyage.Voyage;
import com.freightflow.modules.voyage.enums.VoyageStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PositionTrackingJob}.
 *
 * Uses Mockito only — no Spring context, no DB.
 * EntityManager is mocked to return controlled lists of voyages.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PositionTrackingJob")
class PositionTrackingJobTest {

    @Mock private VesselPositionResolver vesselPositionResolver;
    @Mock private EventRepository        eventRepository;
    @Mock private EntityManager          em;

    private PositionTrackingJob job;

    @BeforeEach
    void setUp() {
        job = new PositionTrackingJob(vesselPositionResolver, eventRepository, em);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void mockEntityManagerToReturn(List<Voyage> voyages) {
        TypedQuery<Voyage> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Voyage.class))).thenReturn(query);
        // Handles the two setParameter calls: "statuses" and the implicit chain
        lenient().when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(voyages);
    }

    /** Builds a voyage IN_TRANSIT with one active (BOOKED) shipment attached. */
    private Voyage voyageWithShipment(UUID voyageId) {
        Voyage voyage = TestDataFactory.voyage(voyageId, "MSC-2026-" + voyageId.toString().substring(0, 4));
        voyage.setStatus(VoyageStatus.IN_TRANSIT);

        Shipment shipment = TestDataFactory.shipment(UUID.randomUUID(), "A" + System.nanoTime());
        // Shipment default status is BOOKED — not in FINISHED_STATUSES
        voyage.getShipments().add(shipment);

        return voyage;
    }

    /**
     * Creates a POSITION_UPDATE event with the given lat,lon as location.
     *
     * Uses Locale.US to ensure the decimal separator is always "." regardless of
     * the JVM's default locale. PositionTrackingJob.shouldRecordPosition() splits
     * on "," and calls Double.parseDouble(), which requires English decimal format.
     */
    private Event positionEvent(Shipment shipment, double lat, double lon) {
        return new Event(
                shipment,
                EventType.POSITION_UPDATE,
                String.format(Locale.US, "%.6f,%.6f", lat, lon),
                "Position update — 14.0 kn, heading 270° (LIVE)",
                Instant.now().minusSeconds(300)
        );
    }

    /** A live AIS position at the given coordinates. */
    private AisPositionResponse liveAt(double lat, double lon) {
        return AisPositionResponse.live("9839012", lat, lon, 14.0, 270.0, "underway", Instant.now());
    }

    // ==================== Tests ====================

    @Nested
    @DisplayName("Quando nao ha evento anterior")
    class SemEventoAnterior {

        @Test
        @DisplayName("Deve criar evento POSITION_UPDATE quando voyage IN_TRANSIT sem historico")
        void deveCriarEvento_quandoSemHistorico() {
            UUID voyageId = UUID.randomUUID();
            Voyage voyage = voyageWithShipment(voyageId);
            Shipment shipment = voyage.getShipments().get(0);

            mockEntityManagerToReturn(List.of(voyage));
            when(vesselPositionResolver.resolveForVoyage(voyage))
                    .thenReturn(liveAt(-22.5, -43.2));
            when(eventRepository.findByShipmentIdOrderByOccurredAtDesc(shipment.getId()))
                    .thenReturn(List.of());  // no previous events
            when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

            job.trackActiveVoyages();

            verify(eventRepository, times(1)).save(argThat(
                    event -> event.getType() == EventType.POSITION_UPDATE
            ));
        }
    }

    @Nested
    @DisplayName("Quando ha evento anterior")
    class ComEventoAnterior {

        @Test
        @DisplayName("Nao deve criar evento quando posicao nao mudou (distancia < 0.05 graus)")
        void naoDeveCriarEvento_quandoPosicaoMesma() {
            UUID voyageId = UUID.randomUUID();
            Voyage voyage = voyageWithShipment(voyageId);
            Shipment shipment = voyage.getShipments().get(0);

            // Previous event at exactly the same position
            Event previousEvent = positionEvent(shipment, -22.5, -43.2);

            mockEntityManagerToReturn(List.of(voyage));
            when(vesselPositionResolver.resolveForVoyage(voyage))
                    .thenReturn(liveAt(-22.5, -43.2));
            when(eventRepository.findByShipmentIdOrderByOccurredAtDesc(shipment.getId()))
                    .thenReturn(List.of(previousEvent));

            job.trackActiveVoyages();

            // Threshold not exceeded (0.0 < 0.05) → no new save
            verify(eventRepository, never()).save(any(Event.class));
        }

        @Test
        @DisplayName("Deve criar evento quando posicao mudou mais de 0.05 graus")
        void deveCriarEvento_quandoPosicaoDiferente() {
            UUID voyageId = UUID.randomUUID();
            Voyage voyage = voyageWithShipment(voyageId);
            Shipment shipment = voyage.getShipments().get(0);

            // Previous event at different position (> 0.05 degree difference)
            Event previousEvent = positionEvent(shipment, -22.5, -43.2);

            mockEntityManagerToReturn(List.of(voyage));
            // New position has moved more than threshold in longitude: -43.2 + 0.1 = -43.1
            when(vesselPositionResolver.resolveForVoyage(voyage))
                    .thenReturn(liveAt(-22.5, -43.1));
            when(eventRepository.findByShipmentIdOrderByOccurredAtDesc(shipment.getId()))
                    .thenReturn(List.of(previousEvent));
            when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

            job.trackActiveVoyages();

            // Threshold exceeded (|(-43.1) - (-43.2)| = 0.1 > 0.05) → new event saved
            verify(eventRepository, times(1)).save(argThat(
                    event -> event.getType() == EventType.POSITION_UPDATE
            ));
        }

        @Test
        @DisplayName("Deve criar evento quando posicao anterior esta mal formatada")
        void deveCriarEvento_quandoPosicaoAnteriorMalFormatada() {
            UUID voyageId = UUID.randomUUID();
            Voyage voyage = voyageWithShipment(voyageId);
            Shipment shipment = voyage.getShipments().get(0);

            Event malformedPreviousEvent = new Event(
                    shipment,
                    EventType.POSITION_UPDATE,
                    "563150,41,362250",
                    "Position update — 14.0 kn, heading 270° (LIVE)",
                    Instant.now().minusSeconds(300)
            );

            mockEntityManagerToReturn(List.of(voyage));
            when(vesselPositionResolver.resolveForVoyage(voyage))
                    .thenReturn(liveAt(-22.5, -43.2));
            when(eventRepository.findByShipmentIdOrderByOccurredAtDesc(shipment.getId()))
                    .thenReturn(List.of(malformedPreviousEvent));
            when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

            job.trackActiveVoyages();

            verify(eventRepository, times(1)).save(argThat(
                    event -> event.getType() == EventType.POSITION_UPDATE
            ));
        }
    }

    @Nested
    @DisplayName("Quando AIS indisponivel")
    class AisIndisponivel {

        @Test
        @DisplayName("Nao deve criar evento quando posicao AIS retorna UNAVAILABLE")
        void naoDeveCriarEvento_quandoAisUnavailable() {
            UUID voyageId = UUID.randomUUID();
            Voyage voyage = voyageWithShipment(voyageId);

            mockEntityManagerToReturn(List.of(voyage));
            when(vesselPositionResolver.resolveForVoyage(voyage))
                    .thenReturn(AisPositionResponse.unavailable("9839012"));

            job.trackActiveVoyages();

            // UNAVAILABLE → early return, no event saved, no exception
            verify(eventRepository, never()).save(any(Event.class));
        }
    }

    @Nested
    @DisplayName("Resiliencia a falhas")
    class Resiliencia {

        @Test
        @DisplayName("Deve continuar processando voyages restantes quando uma lanca excecao")
        void deveContinuarProcessando_quandoUmaVoyageFalha() {
            UUID idFailing = UUID.randomUUID();
            UUID idOk      = UUID.randomUUID();

            Voyage failingVoyage = voyageWithShipment(idFailing);
            Voyage okVoyage      = voyageWithShipment(idOk);
            Shipment okShipment  = okVoyage.getShipments().get(0);

            mockEntityManagerToReturn(List.of(failingVoyage, okVoyage));

            // First voyage: VesselPositionResolver throws RuntimeException
            when(vesselPositionResolver.resolveForVoyage(failingVoyage))
                    .thenThrow(new RuntimeException("Simulated AIS timeout"));

            // Second voyage: normal processing
            when(vesselPositionResolver.resolveForVoyage(okVoyage))
                    .thenReturn(liveAt(-23.0, -43.5));
            when(eventRepository.findByShipmentIdOrderByOccurredAtDesc(okShipment.getId()))
                    .thenReturn(List.of());
            when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

            // Must not throw — the per-voyage try/catch absorbs the failure
            job.trackActiveVoyages();

            // The OK voyage must have produced an event despite the failure of the first one
            verify(eventRepository, times(1)).save(argThat(
                    event -> event.getType() == EventType.POSITION_UPDATE
            ));
        }
    }
}
