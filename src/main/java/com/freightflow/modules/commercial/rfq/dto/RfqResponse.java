package com.freightflow.modules.commercial.rfq.dto;

import com.freightflow.modules.commercial.rfq.RequestForQuotation;
import com.freightflow.modules.commercial.rfq.enums.RfqDirection;
import com.freightflow.modules.commercial.rfq.enums.RfqServiceType;
import com.freightflow.modules.commercial.rfq.enums.RfqStatus;
import com.freightflow.modules.commercial.rfq.enums.RfqTransportMode;
import com.freightflow.modules.commercial.shared.IncotermCode;

import java.time.Instant;
import java.util.List;

public record RfqResponse(
        String id,
        String reference,
        String customerId,
        String customerName,
        String prospectCompanyName,
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
        String assignedToId,
        String assignedToName,
        String notes,
        String createdById,
        String createdByName,
        Instant submittedAt,
        Instant cancelledAt,
        Instant createdAt,
        Instant updatedAt,
        long quotationCount,
        List<RfqCargoItemResponse> cargoItems,
        List<RfqContainerResponse> containers
) {
    public static RfqResponse from(RequestForQuotation rfq, long quotationCount) {
        String incotermDisplay = rfq.getIncotermCode() == null
                ? null
                : rfq.getIncotermCode().name() + " " + rfq.getIncotermVersion() + " - " + rfq.getIncotermNamedPlace();

        return new RfqResponse(
                rfq.getId().toString(),
                rfq.getReference(),
                rfq.getCustomer() != null ? rfq.getCustomer().getId().toString() : null,
                rfq.getCustomer() != null ? rfq.getCustomer().getName() : null,
                rfq.getProspectCompanyName(),
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
                rfq.getAssignedTo() != null ? rfq.getAssignedTo().getId().toString() : null,
                rfq.getAssignedTo() != null ? rfq.getAssignedTo().getName() : null,
                rfq.getNotes(),
                rfq.getCreatedBy().getId().toString(),
                rfq.getCreatedBy().getName(),
                rfq.getSubmittedAt(),
                rfq.getCancelledAt(),
                rfq.getCreatedAt(),
                rfq.getUpdatedAt(),
                quotationCount,
                rfq.getCargoItems().stream().map(RfqCargoItemResponse::from).toList(),
                rfq.getContainerRequirements().stream().map(RfqContainerResponse::from).toList()
        );
    }
}
