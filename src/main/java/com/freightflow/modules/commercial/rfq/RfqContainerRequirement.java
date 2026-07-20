package com.freightflow.modules.commercial.rfq;

import com.freightflow.modules.commercial.rfq.enums.RfqContainerType;
import com.freightflow.modules.commercial.shared.WeightUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "commercial_rfq_containers")
public class RfqContainerRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rfq_id", nullable = false)
    private RequestForQuotation rfq;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RfqContainerType containerType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 14, scale = 3)
    private BigDecimal weightPerContainer;

    @Enumerated(EnumType.STRING)
    private WeightUnit weightUnit;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected RfqContainerRequirement() {
    }

    public RfqContainerRequirement(RfqContainerType containerType, Integer quantity) {
        this.containerType = containerType;
        this.quantity = quantity;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public RequestForQuotation getRfq() { return rfq; }
    public RfqContainerType getContainerType() { return containerType; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getWeightPerContainer() { return weightPerContainer; }
    public WeightUnit getWeightUnit() { return weightUnit; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    void setRfq(RequestForQuotation rfq) { this.rfq = rfq; touch(); }
    public void setContainerType(RfqContainerType containerType) { this.containerType = containerType; touch(); }
    public void setQuantity(Integer quantity) { this.quantity = quantity; touch(); }
    public void setWeightPerContainer(BigDecimal weightPerContainer) { this.weightPerContainer = weightPerContainer; touch(); }
    public void setWeightUnit(WeightUnit weightUnit) { this.weightUnit = weightUnit; touch(); }
    public void setNotes(String notes) { this.notes = notes; touch(); }

    private void touch() {
        this.updatedAt = Instant.now();
    }
}
