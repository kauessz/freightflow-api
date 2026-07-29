package com.freightflow.modules.commercial.client.rfq.dto;

import com.freightflow.modules.commercial.rfq.RequestForQuotation;
import com.freightflow.modules.commercial.rfq.enums.RfqDirection;
import com.freightflow.modules.commercial.rfq.enums.RfqServiceType;
import com.freightflow.modules.commercial.rfq.enums.RfqStatus;
import com.freightflow.modules.commercial.rfq.enums.RfqTransportMode;
import com.freightflow.modules.commercial.shared.IncotermCode;

import java.time.Instant;
import java.util.List;

public record ClientRfqResponse(
        String id,
        String reference,
        String contactName,
        String contactEmail,
        String contactPhone,
        RfqDirection direction,
        RfqTransportMode transportMode,
        RfqServiceType serviceType,
        IncotermCode incotermCode,
        String incotermVersion,
        String incotermNamedPlace,
        String incotermDisplay,
        String originPortId,
        String originPortName,
        String originPortUnlocode,
        String destinationPortId,
        String destinationPortName,
        String destinationPortUnlocode,
        String placeOfReceipt,
        String placeOfDelivery,
        Instant cargoReadyDate,
        Instant desiredDepartureDate,
        RfqStatus status,
        String notes,
        Instant submittedAt,
        Instant cancelledAt,
        Instant createdAt,
        Instant updatedAt,
        long quotationCount,
        List<ClientRfqCargoItemResponse> cargoItems,
        List<ClientRfqContainerResponse> containers
) {
    public static ClientRfqResponse from(RequestForQuotation rfq, long quotationCount) {
        String incotermDisplay = rfq.getIncotermCode() == null
                ? null
                : rfq.getIncotermCode().name() + " " + rfq.getIncotermVersion() + " - " + rfq.getIncotermNamedPlace();

        return new ClientRfqResponse(
                rfq.getId().toString(),
                rfq.getReference(),
                rfq.getContactName(),
                rfq.getContactEmail(),
                rfq.getContactPhone(),
                rfq.getDirection(),
                rfq.getTransportMode(),
                rfq.getServiceType(),
                rfq.getIncotermCode(),
                rfq.getIncotermVersion(),
                rfq.getIncotermNamedPlace(),
                incotermDisplay,
                rfq.getOriginPort().getId().toString(),
                rfq.getOriginPort().getName(),
                rfq.getOriginPort().getUnlocode(),
                rfq.getDestinationPort().getId().toString(),
                rfq.getDestinationPort().getName(),
                rfq.getDestinationPort().getUnlocode(),
                rfq.getPlaceOfReceipt(),
                rfq.getPlaceOfDelivery(),
                rfq.getCargoReadyDate(),
                rfq.getDesiredDepartureDate(),
                rfq.getStatus(),
                rfq.getNotes(),
                rfq.getSubmittedAt(),
                rfq.getCancelledAt(),
                rfq.getCreatedAt(),
                rfq.getUpdatedAt(),
                quotationCount,
                rfq.getCargoItems().stream().map(ClientRfqCargoItemResponse::from).toList(),
                rfq.getContainerRequirements().stream().map(ClientRfqContainerResponse::from).toList()
        );
    }
}
