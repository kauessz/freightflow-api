package com.freightflow.modules.commercial.rfq;

import com.freightflow.modules.commercial.shared.VolumeUnit;
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
@Table(name = "commercial_rfq_cargo_items")
public class RfqCargoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rfq_id", nullable = false)
    private RequestForQuotation rfq;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(length = 100)
    private String packageType;

    @Column(nullable = false)
    private Integer packageQuantity;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal grossWeight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WeightUnit weightUnit;

    @Column(precision = 14, scale = 3)
    private BigDecimal volume;

    @Enumerated(EnumType.STRING)
    private VolumeUnit volumeUnit;

    @Column(length = 50)
    private String hsCode;

    @Column(nullable = false)
    private boolean dangerousGoods;

    @Column(length = 20)
    private String unNumber;

    @Column(nullable = false)
    private boolean temperatureControlled;

    @Column(precision = 8, scale = 2)
    private BigDecimal minimumTemperature;

    @Column(precision = 8, scale = 2)
    private BigDecimal maximumTemperature;

    @Column
    private Boolean stackable;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected RfqCargoItem() {
    }

    public RfqCargoItem(String description, Integer packageQuantity, BigDecimal grossWeight, WeightUnit weightUnit) {
        this.description = description;
        this.packageQuantity = packageQuantity;
        this.grossWeight = grossWeight;
        this.weightUnit = weightUnit;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public RequestForQuotation getRfq() { return rfq; }
    public String getDescription() { return description; }
    public String getPackageType() { return packageType; }
    public Integer getPackageQuantity() { return packageQuantity; }
    public BigDecimal getGrossWeight() { return grossWeight; }
    public WeightUnit getWeightUnit() { return weightUnit; }
    public BigDecimal getVolume() { return volume; }
    public VolumeUnit getVolumeUnit() { return volumeUnit; }
    public String getHsCode() { return hsCode; }
    public boolean isDangerousGoods() { return dangerousGoods; }
    public String getUnNumber() { return unNumber; }
    public boolean isTemperatureControlled() { return temperatureControlled; }
    public BigDecimal getMinimumTemperature() { return minimumTemperature; }
    public BigDecimal getMaximumTemperature() { return maximumTemperature; }
    public Boolean getStackable() { return stackable; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    void setRfq(RequestForQuotation rfq) {
        this.rfq = rfq;
        touch();
    }

    public void setDescription(String description) { this.description = description; touch(); }
    public void setPackageType(String packageType) { this.packageType = packageType; touch(); }
    public void setPackageQuantity(Integer packageQuantity) { this.packageQuantity = packageQuantity; touch(); }
    public void setGrossWeight(BigDecimal grossWeight) { this.grossWeight = grossWeight; touch(); }
    public void setWeightUnit(WeightUnit weightUnit) { this.weightUnit = weightUnit; touch(); }
    public void setVolume(BigDecimal volume) { this.volume = volume; touch(); }
    public void setVolumeUnit(VolumeUnit volumeUnit) { this.volumeUnit = volumeUnit; touch(); }
    public void setHsCode(String hsCode) { this.hsCode = hsCode; touch(); }
    public void setDangerousGoods(boolean dangerousGoods) { this.dangerousGoods = dangerousGoods; touch(); }
    public void setUnNumber(String unNumber) { this.unNumber = unNumber; touch(); }
    public void setTemperatureControlled(boolean temperatureControlled) { this.temperatureControlled = temperatureControlled; touch(); }
    public void setMinimumTemperature(BigDecimal minimumTemperature) { this.minimumTemperature = minimumTemperature; touch(); }
    public void setMaximumTemperature(BigDecimal maximumTemperature) { this.maximumTemperature = maximumTemperature; touch(); }
    public void setStackable(Boolean stackable) { this.stackable = stackable; touch(); }
    public void setNotes(String notes) { this.notes = notes; touch(); }

    private void touch() {
        this.updatedAt = Instant.now();
    }
}
