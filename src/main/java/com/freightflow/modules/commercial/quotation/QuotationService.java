package com.freightflow.modules.commercial.quotation;

import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.auth.User;
import com.freightflow.modules.auth.UserRepository;
import com.freightflow.modules.commercial.quotation.dto.CreateQuotationItemRequest;
import com.freightflow.modules.commercial.quotation.dto.CreateQuotationRequest;
import com.freightflow.modules.commercial.quotation.dto.QuotationFilterParams;
import com.freightflow.modules.commercial.quotation.dto.QuotationResponse;
import com.freightflow.modules.commercial.quotation.dto.QuotationSummaryResponse;
import com.freightflow.modules.commercial.quotation.dto.UpdateQuotationItemRequest;
import com.freightflow.modules.commercial.quotation.dto.UpdateQuotationRequest;
import com.freightflow.modules.commercial.quotation.enums.QuotationStatus;
import com.freightflow.modules.commercial.rfq.RequestForQuotation;
import com.freightflow.modules.commercial.rfq.RfqRepository;
import com.freightflow.modules.commercial.rfq.enums.RfqStatus;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import com.freightflow.shared.pagination.PageResponse;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class QuotationService {

    private static final Logger log = LoggerFactory.getLogger(QuotationService.class);

    private final QuotationRepository quotationRepository;
    private final QuotationItemRepository quotationItemRepository;
    private final RfqRepository rfqRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final QuotationFinancialCalculator financialCalculator;

    public QuotationService(QuotationRepository quotationRepository,
                            QuotationItemRepository quotationItemRepository,
                            RfqRepository rfqRepository,
                            TenantRepository tenantRepository,
                            UserRepository userRepository,
                            QuotationFinancialCalculator financialCalculator) {
        this.quotationRepository = quotationRepository;
        this.quotationItemRepository = quotationItemRepository;
        this.rfqRepository = rfqRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.financialCalculator = financialCalculator;
    }

    public PageResponse<QuotationSummaryResponse> list(UUID tenantId, QuotationFilterParams filters, Pageable pageable) {
        var page = quotationRepository.findAll(buildSpec(tenantId, filters), pageable);
        return PageResponse.from(page.map(QuotationSummaryResponse::from));
    }

    public QuotationResponse getById(UUID id, UUID tenantId) {
        Quotation quotation = getScopedQuotation(id, tenantId);
        return QuotationResponse.from(quotation, quotationCountForRfq(tenantId, quotation.getRfq().getId()));
    }

    @Transactional
    public QuotationResponse create(UUID rfqId, CreateQuotationRequest request, UUID tenantId, UUID userId) {
        log.info("Creating quotation number={} for tenant={}", request.quotationNumber(), tenantId);
        RequestForQuotation rfq = rfqRepository.findByIdAndTenantId(rfqId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("RequestForQuotation", rfqId));

        if (rfq.getStatus() == RfqStatus.CANCELLED || rfq.getStatus() == RfqStatus.EXPIRED) {
            throw new BusinessException("Quotation cannot be created for cancelled or expired RFQ");
        }
        if (rfq.getStatus() == RfqStatus.SUBMITTED) {
            throw new BusinessException("RFQ must be moved to UNDER_ANALYSIS before creating quotations");
        }
        if (rfq.getStatus() != RfqStatus.UNDER_ANALYSIS) {
            throw new BusinessException("Quotation can only be created for RFQs in UNDER_ANALYSIS");
        }

        String quotationNumber = normalizeCode(request.quotationNumber());
        if (quotationRepository.existsByTenantIdAndQuotationNumberAndRevision(tenantId, quotationNumber, 1)) {
            throw new BusinessException("Quotation number '" + quotationNumber + "' already exists for this tenant");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));
        User createdBy = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Quotation quotation = new Quotation(
                tenant,
                rfq,
                quotationNumber,
                normalizeCurrency(request.sellingCurrency()),
                createdBy
        );
        applyUpdatableFields(quotation, request.validUntil(), request.carrierName(), request.transitTimeDays(),
                request.freeTimeDays(), request.estimatedDeparture(), request.estimatedArrival(),
                request.sellingCurrency(), request.exchangeRate(), request.exchangeRateDate(),
                request.exchangeRateSource(), request.commercialNotes(), request.internalNotes(), false);

        validateQuotation(quotation);
        Quotation saved = quotationRepository.save(quotation);
        return QuotationResponse.from(saved, quotationCountForRfq(tenantId, saved.getRfq().getId()));
    }

    @Transactional
    public QuotationResponse update(UUID id, UpdateQuotationRequest request, UUID tenantId) {
        Quotation quotation = getScopedQuotation(id, tenantId);
        ensureDraft(quotation);

        if (request.quotationNumber() != null) {
            String quotationNumber = normalizeCode(request.quotationNumber());
            if (quotationRepository.existsByTenantIdAndQuotationNumberAndRevisionAndIdNot(tenantId, quotationNumber, quotation.getRevision(), quotation.getId())) {
                throw new BusinessException("Quotation number '" + quotationNumber + "' already exists for this tenant");
            }
            quotation.setQuotationNumber(quotationNumber);
        }

        applyUpdatableFields(quotation, request.validUntil(), request.carrierName(), request.transitTimeDays(),
                request.freeTimeDays(), request.estimatedDeparture(), request.estimatedArrival(),
                request.sellingCurrency(), request.exchangeRate(), request.exchangeRateDate(),
                request.exchangeRateSource(), request.commercialNotes(), request.internalNotes(), true);

        validateQuotation(quotation);
        recalculateTotals(quotation);
        Quotation saved = quotationRepository.save(quotation);
        return QuotationResponse.from(saved, quotationCountForRfq(tenantId, saved.getRfq().getId()));
    }

    @Transactional
    public QuotationResponse addItem(UUID quotationId, CreateQuotationItemRequest request, UUID tenantId) {
        Quotation quotation = getScopedQuotation(quotationId, tenantId);
        ensureDraft(quotation);

        QuotationItem item = new QuotationItem(
                request.category(),
                request.description().trim(),
                request.scope(),
                normalizeCurrency(request.costCurrency()),
                financialCalculator.money(request.costAmount()),
                normalizeCurrency(request.sellingCurrency()),
                financialCalculator.money(request.sellingAmount()),
                request.quantity(),
                request.sortOrder() != null ? request.sortOrder() : quotation.getItems().size()
        );
        applyItemFields(quotation, item, request.exchangeRate(), request.unit(), request.included(), request.optional(),
                request.supplierName(), request.notes());
        quotation.addItem(item);
        sortItems(quotation);
        recalculateTotals(quotation);
        quotationItemRepository.save(item);
        Quotation saved = quotationRepository.save(quotation);
        return QuotationResponse.from(saved, quotationCountForRfq(tenantId, saved.getRfq().getId()));
    }

    @Transactional
    public QuotationResponse updateItem(UUID quotationId, UUID itemId, UpdateQuotationItemRequest request, UUID tenantId) {
        Quotation quotation = getScopedQuotation(quotationId, tenantId);
        ensureDraft(quotation);
        QuotationItem item = findItem(quotation, itemId);

        if (request.category() != null) item.setCategory(request.category());
        if (request.description() != null) item.setDescription(request.description().trim());
        if (request.scope() != null) item.setScope(request.scope());
        if (request.costCurrency() != null) item.setCostCurrency(normalizeCurrency(request.costCurrency()));
        if (request.costAmount() != null) item.setCostAmount(financialCalculator.money(request.costAmount()));
        if (request.exchangeRate() != null) item.setExchangeRate(financialCalculator.normalizeExchangeRate(request.exchangeRate()));
        if (request.sellingCurrency() != null) item.setSellingCurrency(normalizeCurrency(request.sellingCurrency()));
        if (request.sellingAmount() != null) item.setSellingAmount(financialCalculator.money(request.sellingAmount()));
        if (request.quantity() != null) item.setQuantity(request.quantity());
        if (request.unit() != null) item.setUnit(trimToNull(request.unit()));
        if (request.included() != null) item.setIncluded(request.included());
        if (request.optional() != null) item.setOptional(request.optional());
        if (request.supplierName() != null) item.setSupplierName(trimToNull(request.supplierName()));
        if (request.notes() != null) item.setNotes(request.notes());
        if (request.sortOrder() != null) {
            validateSortOrder(request.sortOrder());
            item.setSortOrder(request.sortOrder());
        }

        recalculateItem(quotation, item);
        sortItems(quotation);
        recalculateTotals(quotation);
        Quotation saved = quotationRepository.save(quotation);
        return QuotationResponse.from(saved, quotationCountForRfq(tenantId, saved.getRfq().getId()));
    }

    @Transactional
    public QuotationResponse deleteItem(UUID quotationId, UUID itemId, UUID tenantId) {
        Quotation quotation = getScopedQuotation(quotationId, tenantId);
        ensureDraft(quotation);
        QuotationItem item = findItem(quotation, itemId);
        quotation.removeItem(item);
        sortItems(quotation);
        recalculateTotals(quotation);
        Quotation saved = quotationRepository.save(quotation);
        return QuotationResponse.from(saved, quotationCountForRfq(tenantId, saved.getRfq().getId()));
    }

    @Transactional
    public QuotationResponse readyForReview(UUID id, UUID tenantId) {
        Quotation quotation = getScopedQuotation(id, tenantId);
        ensureDraft(quotation);
        if (quotation.getItems().isEmpty()) {
            throw new BusinessException("Quotation requires at least one item before review");
        }
        validateQuotation(quotation);
        recalculateTotals(quotation);
        quotation.setStatus(QuotationStatus.READY_FOR_REVIEW);
        quotation.setSubmittedAt(java.time.Instant.now());
        Quotation saved = quotationRepository.save(quotation);
        return QuotationResponse.from(saved, quotationCountForRfq(tenantId, saved.getRfq().getId()));
    }

    @Transactional
    public QuotationResponse cancel(UUID id, UUID tenantId) {
        Quotation quotation = getScopedQuotation(id, tenantId);
        if (quotation.getStatus() == QuotationStatus.CANCELLED || quotation.getStatus() == QuotationStatus.EXPIRED) {
            throw new BusinessException("Quotation is already not actionable");
        }
        if (quotation.getStatus() != QuotationStatus.DRAFT && quotation.getStatus() != QuotationStatus.READY_FOR_REVIEW) {
            throw new BusinessException("Quotation cannot be cancelled from current status");
        }
        quotation.setStatus(QuotationStatus.CANCELLED);
        Quotation saved = quotationRepository.save(quotation);
        return QuotationResponse.from(saved, quotationCountForRfq(tenantId, saved.getRfq().getId()));
    }

    @Transactional
    public QuotationResponse approve(UUID id, UUID tenantId, UUID userId) {
        Quotation quotation = getScopedQuotation(id, tenantId);
        if (quotation.getStatus() != QuotationStatus.READY_FOR_REVIEW) {
            throw new BusinessException("Only quotations in READY_FOR_REVIEW can be approved");
        }

        User approver = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        quotation.setStatus(QuotationStatus.APPROVED);
        quotation.setApprovedAt(java.time.Instant.now());
        quotation.setApprovedBy(approver);

        Quotation saved = quotationRepository.save(quotation);
        return QuotationResponse.from(saved, quotationCountForRfq(tenantId, saved.getRfq().getId()));
    }

    @Transactional
    public QuotationResponse send(UUID id, UUID tenantId, UUID userId) {
        Quotation quotation = getScopedQuotation(id, tenantId);
        if (quotation.getStatus() != QuotationStatus.APPROVED) {
            throw new BusinessException("Only quotations in APPROVED can be sent");
        }

        RequestForQuotation rfq = quotation.getRfq();
        if (rfq.getStatus() != RfqStatus.UNDER_ANALYSIS) {
            throw new BusinessException("Only quotations linked to RFQs in UNDER_ANALYSIS can be sent");
        }

        User sender = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        quotation.setStatus(QuotationStatus.SENT);
        quotation.setSentAt(java.time.Instant.now());
        quotation.setSentBy(sender);

        rfq.setStatus(RfqStatus.QUOTED);

        Quotation saved = quotationRepository.save(quotation);
        return QuotationResponse.from(saved, quotationCountForRfq(tenantId, saved.getRfq().getId()));
    }

    private Specification<Quotation> buildSpec(UUID tenantId, QuotationFilterParams filters) {
        return Specification.where(hasTenant(tenantId))
                .and(hasSearch(filters.search()))
                .and(hasStatus(filters.status()))
                .and(hasRfq(filters.rfqId()))
                .and(hasCustomer(filters.customerId()))
                .and(hasCreatedBy(filters.createdBy()))
                .and(validFrom(filters.validFrom()))
                .and(validTo(filters.validTo()));
    }

    private Specification<Quotation> hasTenant(UUID tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenant").get("id"), tenantId);
    }

    private Specification<Quotation> hasSearch(String search) {
        if (search == null || search.isBlank()) return null;
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("quotationNumber")), pattern),
                cb.like(cb.lower(root.join("rfq", JoinType.LEFT).get("reference")), pattern),
                cb.like(cb.lower(root.get("carrierName")), pattern)
        );
    }

    private Specification<Quotation> hasStatus(QuotationStatus status) {
        return status == null ? null : (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private Specification<Quotation> hasRfq(UUID rfqId) {
        return rfqId == null ? null : (root, query, cb) -> cb.equal(root.get("rfq").get("id"), rfqId);
    }

    private Specification<Quotation> hasCustomer(UUID customerId) {
        return customerId == null ? null : (root, query, cb) -> cb.equal(root.get("rfq").get("customer").get("id"), customerId);
    }

    private Specification<Quotation> hasCreatedBy(UUID createdBy) {
        return createdBy == null ? null : (root, query, cb) -> cb.equal(root.get("createdBy").get("id"), createdBy);
    }

    private Specification<Quotation> validFrom(java.time.Instant validFrom) {
        return validFrom == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("validUntil"), validFrom);
    }

    private Specification<Quotation> validTo(java.time.Instant validTo) {
        return validTo == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("validUntil"), validTo);
    }

    private Quotation getScopedQuotation(UUID id, UUID tenantId) {
        return quotationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation", id));
    }

    private QuotationItem findItem(Quotation quotation, UUID itemId) {
        return quotation.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("QuotationItem", itemId));
    }

    private void applyUpdatableFields(Quotation quotation,
                                      java.time.Instant validUntil,
                                      String carrierName,
                                      Integer transitTimeDays,
                                      Integer freeTimeDays,
                                      java.time.Instant estimatedDeparture,
                                      java.time.Instant estimatedArrival,
                                      String sellingCurrency,
                                      BigDecimal exchangeRate,
                                      java.time.Instant exchangeRateDate,
                                      String exchangeRateSource,
                                      String commercialNotes,
                                      String internalNotes,
                                      boolean partial) {
        if (!partial || validUntil != null) quotation.setValidUntil(validUntil);
        if (!partial || carrierName != null) quotation.setCarrierName(trimToNull(carrierName));
        if (!partial || transitTimeDays != null) quotation.setTransitTimeDays(transitTimeDays);
        if (!partial || freeTimeDays != null) quotation.setFreeTimeDays(freeTimeDays);
        if (!partial || estimatedDeparture != null) quotation.setEstimatedDeparture(estimatedDeparture);
        if (!partial || estimatedArrival != null) quotation.setEstimatedArrival(estimatedArrival);
        if (!partial || sellingCurrency != null) quotation.setSellingCurrency(normalizeCurrency(sellingCurrency != null ? sellingCurrency : quotation.getSellingCurrency()));
        if (!partial || exchangeRate != null) quotation.setExchangeRate(exchangeRate != null ? financialCalculator.normalizeExchangeRate(exchangeRate) : null);
        if (!partial || exchangeRateDate != null) quotation.setExchangeRateDate(exchangeRateDate);
        if (!partial || exchangeRateSource != null) quotation.setExchangeRateSource(trimToNull(exchangeRateSource));
        if (!partial || commercialNotes != null) quotation.setCommercialNotes(commercialNotes);
        if (!partial || internalNotes != null) quotation.setInternalNotes(internalNotes);
    }

    private void validateQuotation(Quotation quotation) {
        if (quotation.getValidUntil() != null && quotation.getValidUntil().isBefore(quotation.getCreatedAt())) {
            throw new BusinessException("Quotation validity cannot be earlier than creation time");
        }
        if (quotation.getEstimatedDeparture() != null && quotation.getEstimatedArrival() != null
                && quotation.getEstimatedDeparture().isAfter(quotation.getEstimatedArrival())) {
            throw new BusinessException("Estimated departure cannot be after estimated arrival");
        }
        if (quotation.getExchangeRate() != null && quotation.getExchangeRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Exchange rate must be greater than zero");
        }
        if (quotation.getTransitTimeDays() != null && quotation.getTransitTimeDays() < 0) {
            throw new BusinessException("Transit time days cannot be negative");
        }
        if (quotation.getFreeTimeDays() != null && quotation.getFreeTimeDays() < 0) {
            throw new BusinessException("Free time days cannot be negative");
        }
    }

    private void applyItemFields(Quotation quotation, QuotationItem item, BigDecimal exchangeRate,
                                 String unit, Boolean included, Boolean optional, String supplierName, String notes) {
        validateSortOrder(item.getSortOrder());
        item.setUnit(trimToNull(unit));
        item.setIncluded(included == null || included);
        item.setOptional(optional != null && optional);
        item.setSupplierName(trimToNull(supplierName));
        item.setNotes(notes);
        if (exchangeRate != null) {
            item.setExchangeRate(financialCalculator.normalizeExchangeRate(exchangeRate));
        }
        recalculateItem(quotation, item);
    }

    private void recalculateTotals(Quotation quotation) {
        quotation.getItems().forEach(item -> recalculateItem(quotation, item));
        var totals = financialCalculator.calculateQuotationTotals(quotation.getItems());
        quotation.setTotals(
                totals.costTotal(),
                totals.sellingTotal(),
                totals.profitAmount(),
                totals.marginPercentage(),
                totals.markupPercentage()
        );
    }

    private void recalculateItem(Quotation quotation, QuotationItem item) {
        validateSortOrder(item.getSortOrder());
        if (!item.getSellingCurrency().equalsIgnoreCase(quotation.getSellingCurrency())) {
            throw new BusinessException("Quotation item selling currency must match quotation selling currency");
        }

        BigDecimal costInSellingCurrency;
        if (item.getCostCurrency().equalsIgnoreCase(quotation.getSellingCurrency())) {
            costInSellingCurrency = financialCalculator.money(item.getCostAmount());
            item.setExchangeRate(null);
        } else {
            BigDecimal applicableRate = item.getExchangeRate() != null ? item.getExchangeRate() : quotation.getExchangeRate();
            if (applicableRate == null || applicableRate.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Exchange rate is required when cost currency differs from selling currency");
            }
            item.setExchangeRate(financialCalculator.normalizeExchangeRate(applicableRate));
            costInSellingCurrency = financialCalculator.convertCost(item.getCostAmount(), applicableRate);
        }

        item.setCostAmountInSellingCurrency(costInSellingCurrency);
        var totals = financialCalculator.calculateItemTotals(costInSellingCurrency, item.getSellingAmount(), item.getQuantity());
        item.setTotals(
                totals.totalCost(),
                totals.totalSelling(),
                totals.profitAmount(),
                totals.marginPercentage(),
                totals.markupPercentage()
        );
    }

    private void sortItems(Quotation quotation) {
        quotation.getItems().sort(Comparator.comparing(QuotationItem::getSortOrder).thenComparing(QuotationItem::getCreatedAt));
    }

    private void ensureDraft(Quotation quotation) {
        if (quotation.getStatus() != QuotationStatus.DRAFT) {
            throw new BusinessException("Only quotations in DRAFT can be structurally changed");
        }
    }

    private String normalizeCurrency(String currency) {
        return currency.trim().toUpperCase();
    }

    private String normalizeCode(String value) {
        return value.trim().toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void validateSortOrder(Integer sortOrder) {
        if (sortOrder != null && sortOrder < 0) {
            throw new BusinessException("Sort order cannot be negative");
        }
    }

    private long quotationCountForRfq(UUID tenantId, UUID rfqId) {
        return quotationRepository.countByRfqIdsAndTenantId(java.util.List.of(rfqId), tenantId).stream()
                .findFirst()
                .map(QuotationRepository.RfqQuotationCountView::getQuotationCount)
                .orElse(0L);
    }
}
