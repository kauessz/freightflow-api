package com.freightflow.modules.commercial.quotation;

import com.freightflow.modules.commercial.quotation.enums.ChargeCategory;
import com.freightflow.modules.commercial.quotation.enums.ChargeScope;
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
@Table(name = "commercial_quotation_items")
public class QuotationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", nullable = false)
    private Quotation quotation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChargeCategory category;

    @Column(nullable = false, length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChargeScope scope;

    @Column(nullable = false, length = 3)
    private String costCurrency;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal costAmount;

    @Column(precision = 18, scale = 6)
    private BigDecimal exchangeRate;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal costAmountInSellingCurrency;

    @Column(nullable = false, length = 3)
    private String sellingCurrency;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal sellingAmount;

    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal quantity;

    @Column(length = 50)
    private String unit;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalCost;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalSelling;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal profitAmount;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal marginPercentage;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal markupPercentage;

    @Column(nullable = false)
    private boolean included;

    @Column(nullable = false)
    private boolean optional;

    @Column(length = 255)
    private String supplierName;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected QuotationItem() {
    }

    public QuotationItem(ChargeCategory category, String description, ChargeScope scope,
                         String costCurrency, BigDecimal costAmount, String sellingCurrency,
                         BigDecimal sellingAmount, BigDecimal quantity, Integer sortOrder) {
        this.category = category;
        this.description = description;
        this.scope = scope;
        this.costCurrency = costCurrency;
        this.costAmount = costAmount;
        this.sellingCurrency = sellingCurrency;
        this.sellingAmount = sellingAmount;
        this.quantity = quantity;
        this.sortOrder = sortOrder;
        this.included = true;
        this.optional = false;
        this.costAmountInSellingCurrency = BigDecimal.ZERO;
        this.totalCost = BigDecimal.ZERO;
        this.totalSelling = BigDecimal.ZERO;
        this.profitAmount = BigDecimal.ZERO;
        this.marginPercentage = BigDecimal.ZERO;
        this.markupPercentage = BigDecimal.ZERO;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Quotation getQuotation() { return quotation; }
    public ChargeCategory getCategory() { return category; }
    public String getDescription() { return description; }
    public ChargeScope getScope() { return scope; }
    public String getCostCurrency() { return costCurrency; }
    public BigDecimal getCostAmount() { return costAmount; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public BigDecimal getCostAmountInSellingCurrency() { return costAmountInSellingCurrency; }
    public String getSellingCurrency() { return sellingCurrency; }
    public BigDecimal getSellingAmount() { return sellingAmount; }
    public BigDecimal getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public BigDecimal getTotalCost() { return totalCost; }
    public BigDecimal getTotalSelling() { return totalSelling; }
    public BigDecimal getProfitAmount() { return profitAmount; }
    public BigDecimal getMarginPercentage() { return marginPercentage; }
    public BigDecimal getMarkupPercentage() { return markupPercentage; }
    public boolean isIncluded() { return included; }
    public boolean isOptional() { return optional; }
    public String getSupplierName() { return supplierName; }
    public String getNotes() { return notes; }
    public Integer getSortOrder() { return sortOrder; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    void setQuotation(Quotation quotation) { this.quotation = quotation; touch(); }
    public void setCategory(ChargeCategory category) { this.category = category; touch(); }
    public void setDescription(String description) { this.description = description; touch(); }
    public void setScope(ChargeScope scope) { this.scope = scope; touch(); }
    public void setCostCurrency(String costCurrency) { this.costCurrency = costCurrency; touch(); }
    public void setCostAmount(BigDecimal costAmount) { this.costAmount = costAmount; touch(); }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; touch(); }
    public void setCostAmountInSellingCurrency(BigDecimal costAmountInSellingCurrency) { this.costAmountInSellingCurrency = costAmountInSellingCurrency; touch(); }
    public void setSellingCurrency(String sellingCurrency) { this.sellingCurrency = sellingCurrency; touch(); }
    public void setSellingAmount(BigDecimal sellingAmount) { this.sellingAmount = sellingAmount; touch(); }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; touch(); }
    public void setUnit(String unit) { this.unit = unit; touch(); }
    public void setIncluded(boolean included) { this.included = included; touch(); }
    public void setOptional(boolean optional) { this.optional = optional; touch(); }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; touch(); }
    public void setNotes(String notes) { this.notes = notes; touch(); }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; touch(); }
    public void setTotals(BigDecimal totalCost, BigDecimal totalSelling, BigDecimal profitAmount,
                          BigDecimal marginPercentage, BigDecimal markupPercentage) {
        this.totalCost = totalCost;
        this.totalSelling = totalSelling;
        this.profitAmount = profitAmount;
        this.marginPercentage = marginPercentage;
        this.markupPercentage = markupPercentage;
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }
}
