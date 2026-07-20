package com.freightflow.modules.commercial.rfq.dto;

import com.freightflow.modules.commercial.rfq.RequestForQuotation;
import com.freightflow.modules.commercial.rfq.enums.RfqDirection;
import com.freightflow.modules.commercial.rfq.enums.RfqServiceType;
import com.freightflow.modules.commercial.rfq.enums.RfqStatus;
import com.freightflow.modules.commercial.rfq.enums.RfqTransportMode;

import java.time.Instant;

public record RfqSummaryResponse(
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
        String originPortName,
        String originPortUnlocode,
        String destinationPortName,
        String destinationPortUnlocode,
        RfqStatus status,
        String assignedToId,
        String assignedToName,
        Instant createdAt,
        Instant updatedAt,
        long quotationCount
) {
    public static RfqSummaryResponse from(RequestForQuotation rfq, long quotationCount) {
        return new RfqSummaryResponse(
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
                rfq.getOriginPort().getName(),
                rfq.getOriginPort().getUnlocode(),
                rfq.getDestinationPort().getName(),
                rfq.getDestinationPort().getUnlocode(),
                rfq.getStatus(),
                rfq.getAssignedTo() != null ? rfq.getAssignedTo().getId().toString() : null,
                rfq.getAssignedTo() != null ? rfq.getAssignedTo().getName() : null,
                rfq.getCreatedAt(),
                rfq.getUpdatedAt(),
                quotationCount
        );
    }
}
