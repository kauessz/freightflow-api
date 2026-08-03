package com.freightflow.modules.commercial.rfq;

import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.auth.User;
import com.freightflow.modules.auth.UserRepository;
import com.freightflow.modules.commercial.quotation.QuotationRepository;
import com.freightflow.modules.commercial.rfq.dto.CreateRfqRequest;
import com.freightflow.modules.commercial.rfq.dto.RfqCargoItemRequest;
import com.freightflow.modules.commercial.rfq.dto.RfqContainerRequest;
import com.freightflow.modules.commercial.rfq.dto.RfqFilterParams;
import com.freightflow.modules.commercial.rfq.dto.RfqResponse;
import com.freightflow.modules.commercial.rfq.dto.RfqSummaryResponse;
import com.freightflow.modules.commercial.rfq.dto.UpdateRfqRequest;
import com.freightflow.modules.commercial.rfq.enums.RfqContainerType;
import com.freightflow.modules.commercial.rfq.enums.RfqServiceType;
import com.freightflow.modules.commercial.rfq.enums.RfqStatus;
import com.freightflow.modules.commercial.rfq.enums.RfqTransportMode;
import com.freightflow.modules.commercial.shared.IncotermCode;
import com.freightflow.modules.platform.entitlement.EntitlementEnforcementService;
import com.freightflow.modules.customer.Customer;
import com.freightflow.modules.customer.CustomerRepository;
import com.freightflow.modules.port.Port;
import com.freightflow.modules.port.PortRepository;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RfqService {

    private static final Logger log = LoggerFactory.getLogger(RfqService.class);
    private static final String COMMERCIAL_RFQ_FEATURE_KEY = "COMMERCIAL_RFQ";

    private final RfqRepository rfqRepository;
    private final TenantRepository tenantRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PortRepository portRepository;
    private final QuotationRepository quotationRepository;
    private final EntitlementEnforcementService entitlementEnforcementService;

    public RfqService(RfqRepository rfqRepository,
                      TenantRepository tenantRepository,
                      CustomerRepository customerRepository,
                      UserRepository userRepository,
                      PortRepository portRepository,
                      QuotationRepository quotationRepository,
                      EntitlementEnforcementService entitlementEnforcementService) {
        this.rfqRepository = rfqRepository;
        this.tenantRepository = tenantRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.portRepository = portRepository;
        this.quotationRepository = quotationRepository;
        this.entitlementEnforcementService = entitlementEnforcementService;
    }

    public PageResponse<RfqSummaryResponse> list(UUID tenantId, RfqFilterParams filters, Pageable pageable) {
        requireCommercialRfqEnabled(tenantId);
        var page = rfqRepository.findAll(buildSpec(tenantId, filters), pageable);
        Map<UUID, Long> quotationCounts = countQuotations(tenantId, page.getContent().stream().map(RequestForQuotation::getId).toList());

        return PageResponse.from(page.map(rfq -> RfqSummaryResponse.from(rfq, quotationCounts.getOrDefault(rfq.getId(), 0L))));
    }

    public RfqResponse getById(UUID id, UUID tenantId) {
        requireCommercialRfqEnabled(tenantId);
        RequestForQuotation rfq = getScopedRfq(id, tenantId);
        return RfqResponse.from(rfq, countQuotations(tenantId, List.of(rfq.getId())).getOrDefault(rfq.getId(), 0L));
    }

    @Transactional
    public RfqResponse create(CreateRfqRequest request, UUID tenantId, UUID userId) {
        requireCommercialRfqEnabled(tenantId);
        log.info("Creating RFQ reference={} for tenant={}", request.reference(), tenantId);

        String reference = normalizeReference(request.reference());
        if (rfqRepository.existsByReferenceAndTenantId(reference, tenantId)) {
            throw new BusinessException("RFQ reference '" + reference + "' already exists for this tenant");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));
        User createdBy = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Port originPort = findPort(request.originPortId());
        Port destinationPort = findPort(request.destinationPortId());

        RequestForQuotation rfq = new RequestForQuotation(
                tenant,
                reference,
                request.contactName().trim(),
                request.direction(),
                request.transportMode(),
                request.serviceType(),
                originPort,
                destinationPort,
                createdBy
        );

        applySharedFields(rfq, request.customerId(), request.prospectCompanyName(), request.contactEmail(), request.contactPhone(),
                request.incotermCode(), request.incotermVersion(), request.incotermNamedPlace(),
                request.placeOfReceipt(), request.placeOfDelivery(), request.cargoReadyDate(),
                request.desiredDepartureDate(), request.assignedTo(), request.notes(), request.cargoItems(),
                request.containers(), tenantId, false);

        RequestForQuotation saved = rfqRepository.save(rfq);
        return RfqResponse.from(saved, 0);
    }

    @Transactional
    public RfqResponse update(UUID id, UpdateRfqRequest request, UUID tenantId) {
        requireCommercialRfqEnabled(tenantId);
        log.info("Updating RFQ id={} for tenant={}", id, tenantId);
        RequestForQuotation rfq = getScopedRfq(id, tenantId);
        if (rfq.getStatus() != RfqStatus.DRAFT) {
            throw new BusinessException("Only RFQs in DRAFT can be structurally updated");
        }

        if (request.reference() != null) {
            String reference = normalizeReference(request.reference());
            if (rfqRepository.existsByReferenceAndTenantIdAndIdNot(reference, tenantId, id)) {
                throw new BusinessException("RFQ reference '" + reference + "' already exists for this tenant");
            }
            rfq.setReference(reference);
        }

        if (request.direction() != null) rfq.setDirection(request.direction());
        if (request.transportMode() != null) rfq.setTransportMode(request.transportMode());
        if (request.serviceType() != null) rfq.setServiceType(request.serviceType());
        if (request.contactName() != null) rfq.setContactName(request.contactName().trim());
        if (request.originPortId() != null) rfq.setOriginPort(findPort(request.originPortId()));
        if (request.destinationPortId() != null) rfq.setDestinationPort(findPort(request.destinationPortId()));

        applySharedFields(rfq, request.customerId(), request.prospectCompanyName(), request.contactEmail(), request.contactPhone(),
                request.incotermCode(), request.incotermVersion(), request.incotermNamedPlace(),
                request.placeOfReceipt(), request.placeOfDelivery(), request.cargoReadyDate(),
                request.desiredDepartureDate(), request.assignedTo(), request.notes(), request.cargoItems(),
                request.containers(), tenantId, true);

        validateRfq(rfq);
        return RfqResponse.from(rfqRepository.save(rfq), countQuotations(tenantId, List.of(rfq.getId())).getOrDefault(rfq.getId(), 0L));
    }

    @Transactional
    public void delete(UUID id, UUID tenantId) {
        requireCommercialRfqEnabled(tenantId);
        RequestForQuotation rfq = getScopedRfq(id, tenantId);
        if (rfq.getStatus() != RfqStatus.DRAFT) {
            throw new BusinessException("Only RFQs in DRAFT can be deleted");
        }
        if (countQuotations(tenantId, List.of(rfq.getId())).getOrDefault(rfq.getId(), 0L) > 0) {
            throw new BusinessException("Cannot delete RFQ with quotations");
        }
        rfqRepository.delete(rfq);
    }

    @Transactional
    public RfqResponse submit(UUID id, UUID tenantId) {
        requireCommercialRfqEnabled(tenantId);
        RequestForQuotation rfq = getScopedRfq(id, tenantId);
        if (rfq.getStatus() == RfqStatus.CANCELLED || rfq.getStatus() == RfqStatus.EXPIRED) {
            throw new BusinessException("Cancelled or expired RFQ cannot be submitted");
        }
        if (rfq.getStatus() != RfqStatus.DRAFT) {
            throw new BusinessException("Only RFQs in DRAFT can be submitted");
        }
        validateRfq(rfq);
        rfq.setStatus(RfqStatus.SUBMITTED);
        rfq.setSubmittedAt(java.time.Instant.now());
        return RfqResponse.from(rfqRepository.save(rfq), countQuotations(tenantId, List.of(rfq.getId())).getOrDefault(rfq.getId(), 0L));
    }

    @Transactional
    public RfqResponse startAnalysis(UUID id, UUID tenantId) {
        requireCommercialRfqEnabled(tenantId);
        RequestForQuotation rfq = getScopedRfq(id, tenantId);
        if (rfq.getStatus() != RfqStatus.SUBMITTED) {
            throw new BusinessException("Only SUBMITTED RFQs can move to UNDER_ANALYSIS");
        }
        rfq.setStatus(RfqStatus.UNDER_ANALYSIS);
        return RfqResponse.from(rfqRepository.save(rfq), countQuotations(tenantId, List.of(rfq.getId())).getOrDefault(rfq.getId(), 0L));
    }

    @Transactional
    public RfqResponse cancel(UUID id, UUID tenantId) {
        requireCommercialRfqEnabled(tenantId);
        RequestForQuotation rfq = getScopedRfq(id, tenantId);
        if (rfq.getStatus() == RfqStatus.CANCELLED || rfq.getStatus() == RfqStatus.EXPIRED) {
            throw new BusinessException("RFQ is already not actionable");
        }
        rfq.setStatus(RfqStatus.CANCELLED);
        rfq.setCancelledAt(java.time.Instant.now());
        return RfqResponse.from(rfqRepository.save(rfq), countQuotations(tenantId, List.of(rfq.getId())).getOrDefault(rfq.getId(), 0L));
    }

    @Transactional
    public void markQuoted(UUID rfqId, UUID tenantId) {
        RequestForQuotation rfq = getScopedRfq(rfqId, tenantId);
        if (rfq.getStatus() != RfqStatus.CANCELLED && rfq.getStatus() != RfqStatus.EXPIRED) {
            rfq.setStatus(RfqStatus.QUOTED);
            rfqRepository.save(rfq);
        }
    }

    private Specification<RequestForQuotation> buildSpec(UUID tenantId, RfqFilterParams filters) {
        return Specification.where(hasTenant(tenantId))
                .and(hasSearch(filters.search()))
                .and(hasStatus(filters.status()))
                .and(hasDirection(filters.direction()))
                .and(hasServiceType(filters.serviceType()))
                .and(hasCustomer(filters.customerId()))
                .and(hasAssignedTo(filters.assignedTo()))
                .and(hasOrigin(filters.originPortId()))
                .and(hasDestination(filters.destinationPortId()))
                .and(createdFrom(filters.createdFrom()))
                .and(createdTo(filters.createdTo()));
    }

    private void requireCommercialRfqEnabled(UUID tenantId) {
        entitlementEnforcementService.requireEnabled(tenantId, COMMERCIAL_RFQ_FEATURE_KEY);
    }

    private Specification<RequestForQuotation> hasTenant(UUID tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenant").get("id"), tenantId);
    }

    private Specification<RequestForQuotation> hasSearch(String search) {
        if (search == null || search.isBlank()) return null;
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("reference")), pattern),
                cb.like(cb.lower(root.get("contactName")), pattern),
                cb.like(cb.lower(root.get("prospectCompanyName")), pattern),
                cb.like(cb.lower(root.join("customer", JoinType.LEFT).get("name")), pattern)
        );
    }

    private Specification<RequestForQuotation> hasStatus(RfqStatus status) {
        return status == null ? null : (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private Specification<RequestForQuotation> hasDirection(com.freightflow.modules.commercial.rfq.enums.RfqDirection direction) {
        return direction == null ? null : (root, query, cb) -> cb.equal(root.get("direction"), direction);
    }

    private Specification<RequestForQuotation> hasServiceType(RfqServiceType serviceType) {
        return serviceType == null ? null : (root, query, cb) -> cb.equal(root.get("serviceType"), serviceType);
    }

    private Specification<RequestForQuotation> hasCustomer(UUID customerId) {
        return customerId == null ? null : (root, query, cb) -> cb.equal(root.get("customer").get("id"), customerId);
    }

    private Specification<RequestForQuotation> hasAssignedTo(UUID assignedTo) {
        return assignedTo == null ? null : (root, query, cb) -> cb.equal(root.get("assignedTo").get("id"), assignedTo);
    }

    private Specification<RequestForQuotation> hasOrigin(UUID originPortId) {
        return originPortId == null ? null : (root, query, cb) -> cb.equal(root.get("originPort").get("id"), originPortId);
    }

    private Specification<RequestForQuotation> hasDestination(UUID destinationPortId) {
        return destinationPortId == null ? null : (root, query, cb) -> cb.equal(root.get("destinationPort").get("id"), destinationPortId);
    }

    private Specification<RequestForQuotation> createdFrom(java.time.Instant createdFrom) {
        return createdFrom == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom);
    }

    private Specification<RequestForQuotation> createdTo(java.time.Instant createdTo) {
        return createdTo == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), createdTo);
    }

    private RequestForQuotation getScopedRfq(UUID id, UUID tenantId) {
        return rfqRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("RequestForQuotation", id));
    }

    private void applySharedFields(RequestForQuotation rfq,
                                   UUID customerId,
                                   String prospectCompanyName,
                                   String contactEmail,
                                   String contactPhone,
                                   IncotermCode incotermCode,
                                   String incotermVersion,
                                   String incotermNamedPlace,
                                   String placeOfReceipt,
                                   String placeOfDelivery,
                                   java.time.Instant cargoReadyDate,
                                   java.time.Instant desiredDepartureDate,
                                   UUID assignedToId,
                                   String notes,
                                   List<RfqCargoItemRequest> cargoRequests,
                                   List<RfqContainerRequest> containerRequests,
                                   UUID tenantId,
                                   boolean partial) {
        if (!partial || customerId != null) {
            rfq.setCustomer(customerId != null ? findCustomer(customerId, tenantId) : null);
        }
        if (!partial || prospectCompanyName != null) rfq.setProspectCompanyName(trimToNull(prospectCompanyName));
        if (!partial || contactEmail != null) rfq.setContactEmail(trimToNull(contactEmail));
        if (!partial || contactPhone != null) rfq.setContactPhone(trimToNull(contactPhone));
        if (!partial || incotermCode != null || incotermVersion != null || incotermNamedPlace != null) {
            rfq.setIncotermCode(incotermCode);
            rfq.setIncotermVersion(trimToNull(incotermVersion));
            rfq.setIncotermNamedPlace(trimToNull(incotermNamedPlace));
        }
        if (!partial || placeOfReceipt != null) rfq.setPlaceOfReceipt(trimToNull(placeOfReceipt));
        if (!partial || placeOfDelivery != null) rfq.setPlaceOfDelivery(trimToNull(placeOfDelivery));
        if (!partial || cargoReadyDate != null) rfq.setCargoReadyDate(cargoReadyDate);
        if (!partial || desiredDepartureDate != null) rfq.setDesiredDepartureDate(desiredDepartureDate);
        if (!partial || assignedToId != null) rfq.setAssignedTo(assignedToId != null ? findUser(assignedToId, tenantId) : null);
        if (!partial || notes != null) rfq.setNotes(notes);
        if (cargoRequests != null) rfq.replaceCargoItems(cargoRequests.stream().map(this::mapCargoItem).toList());
        if (containerRequests != null) rfq.replaceContainerRequirements(containerRequests.stream().map(this::mapContainer).toList());

        validateRfq(rfq);
    }

    private void validateRfq(RequestForQuotation rfq) {
        if ((rfq.getCustomer() == null) && isBlank(rfq.getProspectCompanyName())) {
            throw new BusinessException("RFQ must have a customer or prospect company name");
        }
        if (isBlank(rfq.getContactEmail()) && isBlank(rfq.getContactPhone())) {
            throw new BusinessException("RFQ must have at least contact email or phone");
        }
        if (rfq.getOriginPort().getId().equals(rfq.getDestinationPort().getId())) {
            throw new BusinessException("Origin and destination ports must be different");
        }
        if (rfq.getCargoReadyDate() != null && rfq.getDesiredDepartureDate() != null
                && rfq.getCargoReadyDate().isAfter(rfq.getDesiredDepartureDate())) {
            throw new BusinessException("Cargo ready date cannot be after desired departure date");
        }
        validateIncoterm(rfq);
        validateCargoItems(rfq);
        validateContainers(rfq);
    }

    private void validateIncoterm(RequestForQuotation rfq) {
        if (rfq.getIncotermCode() == null) {
            if (rfq.getIncotermVersion() != null || rfq.getIncotermNamedPlace() != null) {
                throw new BusinessException("Incoterm version and named place require an Incoterm code");
            }
            return;
        }

        if (!"2020".equals(rfq.getIncotermVersion())) {
            throw new BusinessException("Only Incoterms 2020 are supported in this phase");
        }
        if (isBlank(rfq.getIncotermNamedPlace())) {
            throw new BusinessException("Incoterm named place is required");
        }
        if (List.of(IncotermCode.FAS, IncotermCode.FOB, IncotermCode.CFR, IncotermCode.CIF).contains(rfq.getIncotermCode())
                && rfq.getTransportMode() != RfqTransportMode.OCEAN) {
            throw new BusinessException("Selected Incoterm is only valid for ocean context");
        }
    }

    private void validateCargoItems(RequestForQuotation rfq) {
        if (rfq.getCargoItems().isEmpty()) {
            throw new BusinessException("RFQ must include at least one cargo item");
        }
        for (RfqCargoItem item : rfq.getCargoItems()) {
            if (item.getVolume() != null && item.getVolumeUnit() == null) {
                throw new BusinessException("Volume unit is required when volume is informed");
            }
            if (item.isDangerousGoods() && isBlank(item.getUnNumber())) {
                throw new BusinessException("UN number is required for dangerous goods");
            }
            if (item.isTemperatureControlled()) {
                if (item.getMinimumTemperature() == null || item.getMaximumTemperature() == null) {
                    throw new BusinessException("Temperature-controlled cargo requires minimum and maximum temperatures");
                }
                if (item.getMinimumTemperature().compareTo(item.getMaximumTemperature()) > 0) {
                    throw new BusinessException("Minimum temperature cannot exceed maximum temperature");
                }
            } else if (item.getMinimumTemperature() != null || item.getMaximumTemperature() != null) {
                throw new BusinessException("Temperature range is only allowed for temperature-controlled cargo");
            }
        }
    }

    private void validateContainers(RequestForQuotation rfq) {
        boolean hasContainers = !rfq.getContainerRequirements().isEmpty();
        if (rfq.getServiceType() == RfqServiceType.FCL && !hasContainers) {
            throw new BusinessException("FCL RFQ requires at least one container");
        }
        if (rfq.getServiceType() != RfqServiceType.FCL && hasContainers) {
            throw new BusinessException("Containers are only allowed for FCL RFQs in this phase");
        }

        if (rfq.getServiceType() == RfqServiceType.FCL) {
            boolean reeferContainer = rfq.getContainerRequirements().stream()
                    .map(RfqContainerRequirement::getContainerType)
                    .anyMatch(type -> type == RfqContainerType.REEFER_20 || type == RfqContainerType.REEFER_40);
            boolean temperatureControlledCargo = rfq.getCargoItems().stream().anyMatch(RfqCargoItem::isTemperatureControlled);
            if (temperatureControlledCargo && !reeferContainer) {
                throw new BusinessException("Temperature-controlled FCL cargo requires at least one reefer container");
            }
        }
    }

    private RfqCargoItem mapCargoItem(RfqCargoItemRequest request) {
        RfqCargoItem item = new RfqCargoItem(
                request.description().trim(),
                request.packageQuantity(),
                request.grossWeight(),
                request.weightUnit()
        );
        item.setPackageType(trimToNull(request.packageType()));
        item.setVolume(request.volume());
        item.setVolumeUnit(request.volumeUnit());
        item.setHsCode(trimToNull(request.hsCode()));
        item.setDangerousGoods(request.dangerousGoods());
        item.setUnNumber(trimToNull(request.unNumber()));
        item.setTemperatureControlled(request.temperatureControlled());
        item.setMinimumTemperature(request.minimumTemperature());
        item.setMaximumTemperature(request.maximumTemperature());
        item.setStackable(request.stackable());
        item.setNotes(request.notes());
        return item;
    }

    private RfqContainerRequirement mapContainer(RfqContainerRequest request) {
        RfqContainerRequirement item = new RfqContainerRequirement(request.containerType(), request.quantity());
        item.setWeightPerContainer(request.weightPerContainer());
        item.setWeightUnit(request.weightUnit());
        item.setNotes(request.notes());
        return item;
    }

    private Customer findCustomer(UUID customerId, UUID tenantId) {
        return customerRepository.findByIdAndTenantId(customerId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));
    }

    private User findUser(UUID userId, UUID tenantId) {
        return userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private Port findPort(UUID portId) {
        return portRepository.findById(portId)
                .orElseThrow(() -> new ResourceNotFoundException("Port", portId));
    }

    private Map<UUID, Long> countQuotations(UUID tenantId, List<UUID> rfqIds) {
        if (rfqIds.isEmpty()) {
            return Map.of();
        }
        return quotationRepository.countByRfqIdsAndTenantId(rfqIds, tenantId).stream()
                .collect(Collectors.toMap(
                        QuotationRepository.RfqQuotationCountView::getRfqId,
                        QuotationRepository.RfqQuotationCountView::getQuotationCount
                ));
    }

    private String normalizeReference(String reference) {
        return reference.trim().toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
