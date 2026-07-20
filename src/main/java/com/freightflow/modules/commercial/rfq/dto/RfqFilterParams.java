package com.freightflow.modules.commercial.rfq.dto;

import com.freightflow.modules.commercial.rfq.enums.RfqDirection;
import com.freightflow.modules.commercial.rfq.enums.RfqServiceType;
import com.freightflow.modules.commercial.rfq.enums.RfqStatus;

import java.time.Instant;
import java.util.UUID;

public record RfqFilterParams(
        String search,
        RfqStatus status,
        RfqDirection direction,
        RfqServiceType serviceType,
        UUID customerId,
        UUID assignedTo,
        UUID originPortId,
        UUID destinationPortId,
        Instant createdFrom,
        Instant createdTo
) {}
