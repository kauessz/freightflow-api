package com.freightflow.modules.commercial.rfq.dto;

import com.freightflow.modules.commercial.rfq.enums.RfqDirection;
import com.freightflow.modules.commercial.rfq.enums.RfqServiceType;
import com.freightflow.modules.commercial.rfq.enums.RfqTransportMode;
import com.freightflow.modules.commercial.shared.IncotermCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UpdateRfqRequest(
        @Size(max = 80) String reference,
        UUID customerId,
        @Size(max = 255) String prospectCompanyName,
        @Size(max = 255) String contactName,
        @Size(max = 255) String contactEmail,
        @Size(max = 50) String contactPhone,
        RfqDirection direction,
        RfqTransportMode transportMode,
        RfqServiceType serviceType,
        IncotermCode incotermCode,
        @Size(max = 10) String incotermVersion,
        @Size(max = 255) String incotermNamedPlace,
        UUID originPortId,
        UUID destinationPortId,
        @Size(max = 255) String placeOfReceipt,
        @Size(max = 255) String placeOfDelivery,
        Instant cargoReadyDate,
        Instant desiredDepartureDate,
        UUID assignedTo,
        String notes,
        List<@Valid RfqCargoItemRequest> cargoItems,
        List<@Valid RfqContainerRequest> containers
) {}
