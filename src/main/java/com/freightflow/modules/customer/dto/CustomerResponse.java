package com.freightflow.modules.customer.dto;

import com.freightflow.modules.customer.Customer;

import java.time.Instant;

public record CustomerResponse(
        String id,
        String name,
        String taxId,
        String contactName,
        String contactEmail,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static CustomerResponse from(Customer c) {
        return new CustomerResponse(
                c.getId().toString(),
                c.getName(),
                c.getTaxId(),
                c.getContactName(),
                c.getContactEmail(),
                c.isActive(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
