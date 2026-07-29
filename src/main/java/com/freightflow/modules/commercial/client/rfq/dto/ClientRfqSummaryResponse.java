package com.freightflow.modules.commercial.client.rfq.dto;

import com.freightflow.modules.commercial.rfq.RequestForQuotation;
import com.freightflow.modules.commercial.rfq.enums.RfqDirection;
import com.freightflow.modules.commercial.rfq.enums.RfqServiceType;
import com.freightflow.modules.commercial.rfq.enums.RfqStatus;
import com.freightflow.modules.commercial.rfq.enums.RfqTransportMode;

import java.time.Instant;

public record ClientRfqSummaryResponse(
        String id,
        String reference,
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
        Instant createdAt,
        Instant updatedAt,
        long quotationCount
) {
    public static ClientRfqSummaryResponse from(RequestForQuotation rfq, long quotationCount) {
        return new ClientRfqSummaryResponse(
                rfq.getId().toString(),
                rfq.getReference(),
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
                rfq.getCreatedAt(),
                rfq.getUpdatedAt(),
                quotationCount
        );
    }
}
