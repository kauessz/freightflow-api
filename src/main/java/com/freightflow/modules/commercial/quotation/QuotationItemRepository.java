package com.freightflow.modules.commercial.quotation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuotationItemRepository extends JpaRepository<QuotationItem, UUID> {
}
