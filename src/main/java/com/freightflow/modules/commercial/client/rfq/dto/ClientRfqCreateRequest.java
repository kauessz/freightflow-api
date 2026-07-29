package com.freightflow.modules.commercial.client.rfq.dto;

import com.freightflow.modules.commercial.rfq.dto.RfqCargoItemRequest;
import com.freightflow.modules.commercial.rfq.dto.RfqContainerRequest;
import com.freightflow.modules.commercial.rfq.enums.RfqDirection;
import com.freightflow.modules.commercial.rfq.enums.RfqServiceType;
import com.freightflow.modules.commercial.rfq.enums.RfqTransportMode;
import com.freightflow.modules.commercial.shared.IncotermCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ClientRfqCreateRequest(
        @NotBlank @Size(max = 80) String reference,
        @NotBlank @Size(max = 255) String contactName,
        @Size(max = 255) String contactEmail,
        @Size(max = 50) String contactPhone,
        @NotNull RfqDirection direction,
        @NotNull RfqTransportMode transportMode,
        @NotNull RfqServiceType serviceType,
        IncotermCode incotermCode,
        @Size(max = 10) String incotermVersion,
        @Size(max = 255) String incotermNamedPlace,
        @NotNull UUID originPortId,
        @NotNull UUID destinationPortId,
        @Size(max = 255) String placeOfReceipt,
        @Size(max = 255) String placeOfDelivery,
        Instant cargoReadyDate,
        Instant desiredDepartureDate,
        String notes,
        @NotEmpty List<@Valid RfqCargoItemRequest> cargoItems,
        List<@Valid RfqContainerRequest> containers
) {}
