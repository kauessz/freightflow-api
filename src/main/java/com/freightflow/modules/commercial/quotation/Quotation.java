package com.freightflow.modules.commercial.quotation;

import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.User;
import com.freightflow.modules.commercial.quotation.enums.QuotationStatus;
import com.freightflow.modules.commercial.rfq.RequestForQuotation;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "commercial_quotations")
public class Quotation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rfq_id", nullable = false)
    private RequestForQuotation rfq;

    @Column(nullable = false, length = 80)
    private String quotationNumber;

    @Column(nullable = false)
    private Integer revision;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuotationStatus status;

    @Column
    private Instant validUntil;

    @Column(length = 255)
    private String carrierName;

    @Column
    private Integer transitTimeDays;

    @Column
    private Integer freeTimeDays;

    @Column
    private Instant estimatedDeparture;

    @Column
    private Instant estimatedArrival;

    @Column(nullable = false, length = 3)
    private String sellingCurrency;

    @Column(precision = 18, scale = 6)
    private BigDecimal exchangeRate;

    @Column
    private Instant exchangeRateDate;

    @Column(length = 100)
    private String exchangeRateSource;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal costTotal;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal sellingTotal;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal profitAmount;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal marginPercentage;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal markupPercentage;

    @Column(columnDefinition = "TEXT")
    private String commercialNotes;

    @Column(columnDefinition = "TEXT")
    private String internalNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column
    private Instant submittedAt;

    @Column
    private Instant approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column
    private Instant rejectedAt;

    @Column
    private Instant expiredAt;

    @Column
    private Instant sentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sent_by")
    private User sentBy;

    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuotationItem> items = new ArrayList<>();

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Quotation() {
    }

    public Quotation(Tenant tenant, RequestForQuotation rfq, String quotationNumber, String sellingCurrency, User createdBy) {
        this.tenant = tenant;
        this.rfq = rfq;
        this.quotationNumber = quotationNumber;
        this.revision = 1;
        this.status = QuotationStatus.DRAFT;
        this.sellingCurrency = sellingCurrency;
        this.createdBy = createdBy;
        this.costTotal = BigDecimal.ZERO;
        this.sellingTotal = BigDecimal.ZERO;
        this.profitAmount = BigDecimal.ZERO;
        this.marginPercentage = BigDecimal.ZERO;
        this.markupPercentage = BigDecimal.ZERO;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Tenant getTenant() { return tenant; }
    public RequestForQuotation getRfq() { return rfq; }
    public String getQuotationNumber() { return quotationNumber; }
    public Integer getRevision() { return revision; }
    public QuotationStatus getStatus() { return status; }
    public Instant getValidUntil() { return validUntil; }
    public String getCarrierName() { return carrierName; }
    public Integer getTransitTimeDays() { return transitTimeDays; }
    public Integer getFreeTimeDays() { return freeTimeDays; }
    public Instant getEstimatedDeparture() { return estimatedDeparture; }
    public Instant getEstimatedArrival() { return estimatedArrival; }
    public String getSellingCurrency() { return sellingCurrency; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public Instant getExchangeRateDate() { return exchangeRateDate; }
    public String getExchangeRateSource() { return exchangeRateSource; }
    public BigDecimal getCostTotal() { return costTotal; }
    public BigDecimal getSellingTotal() { return sellingTotal; }
    public BigDecimal getProfitAmount() { return profitAmount; }
    public BigDecimal getMarginPercentage() { return marginPercentage; }
    public BigDecimal getMarkupPercentage() { return markupPercentage; }
    public String getCommercialNotes() { return commercialNotes; }
    public String getInternalNotes() { return internalNotes; }
    public User getCreatedBy() { return createdBy; }
    public Instant getSubmittedAt() { return submittedAt; }
    public Instant getApprovedAt() { return approvedAt; }
    public User getApprovedBy() { return approvedBy; }
    public Instant getRejectedAt() { return rejectedAt; }
    public Instant getExpiredAt() { return expiredAt; }
    public Instant getSentAt() { return sentAt; }
    public User getSentBy() { return sentBy; }
    public List<QuotationItem> getItems() { return items; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setQuotationNumber(String quotationNumber) { this.quotationNumber = quotationNumber; touch(); }
    public void setStatus(QuotationStatus status) { this.status = status; touch(); }
    public void setValidUntil(Instant validUntil) { this.validUntil = validUntil; touch(); }
    public void setCarrierName(String carrierName) { this.carrierName = carrierName; touch(); }
    public void setTransitTimeDays(Integer transitTimeDays) { this.transitTimeDays = transitTimeDays; touch(); }
    public void setFreeTimeDays(Integer freeTimeDays) { this.freeTimeDays = freeTimeDays; touch(); }
    public void setEstimatedDeparture(Instant estimatedDeparture) { this.estimatedDeparture = estimatedDeparture; touch(); }
    public void setEstimatedArrival(Instant estimatedArrival) { this.estimatedArrival = estimatedArrival; touch(); }
    public void setSellingCurrency(String sellingCurrency) { this.sellingCurrency = sellingCurrency; touch(); }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; touch(); }
    public void setExchangeRateDate(Instant exchangeRateDate) { this.exchangeRateDate = exchangeRateDate; touch(); }
    public void setExchangeRateSource(String exchangeRateSource) { this.exchangeRateSource = exchangeRateSource; touch(); }
    public void setCommercialNotes(String commercialNotes) { this.commercialNotes = commercialNotes; touch(); }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; touch(); }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; touch(); }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; touch(); }
    public void setApprovedBy(User approvedBy) { this.approvedBy = approvedBy; touch(); }
    public void setRejectedAt(Instant rejectedAt) { this.rejectedAt = rejectedAt; touch(); }
    public void setExpiredAt(Instant expiredAt) { this.expiredAt = expiredAt; touch(); }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; touch(); }
    public void setSentBy(User sentBy) { this.sentBy = sentBy; touch(); }
    public void setTotals(BigDecimal costTotal, BigDecimal sellingTotal, BigDecimal profitAmount,
                          BigDecimal marginPercentage, BigDecimal markupPercentage) {
        this.costTotal = costTotal;
        this.sellingTotal = sellingTotal;
        this.profitAmount = profitAmount;
        this.marginPercentage = marginPercentage;
        this.markupPercentage = markupPercentage;
        touch();
    }

    public void addItem(QuotationItem item) {
        item.setQuotation(this);
        this.items.add(item);
        touch();
    }

    public void removeItem(QuotationItem item) {
        this.items.remove(item);
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }
}
