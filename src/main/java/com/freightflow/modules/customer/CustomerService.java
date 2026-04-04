package com.freightflow.modules.customer;

import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.customer.dto.CreateCustomerRequest;
import com.freightflow.modules.customer.dto.CustomerResponse;
import com.freightflow.modules.customer.dto.UpdateCustomerRequest;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import com.freightflow.shared.pagination.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final TenantRepository tenantRepository;

    public CustomerService(CustomerRepository customerRepository, TenantRepository tenantRepository) {
        this.customerRepository = customerRepository;
        this.tenantRepository = tenantRepository;
    }

    // ==================== Queries ====================

    public PageResponse<CustomerResponse> list(UUID tenantId, Pageable pageable) {
        log.debug("Listing customers for tenant={}", tenantId);
        var page = customerRepository.findByTenantId(tenantId, pageable);
        return PageResponse.from(page.map(CustomerResponse::from));
    }

    public CustomerResponse getById(UUID id, UUID tenantId) {
        log.debug("Fetching customer id={}", id);
        Customer customer = customerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        return CustomerResponse.from(customer);
    }

    // ==================== Commands ====================

    @Transactional
    public CustomerResponse create(CreateCustomerRequest request, UUID tenantId) {
        log.info("Creating customer name='{}' for tenant={}", request.name(), tenantId);

        if (customerRepository.existsByNameAndTenantId(request.name(), tenantId)) {
            throw new BusinessException("Customer '" + request.name() + "' already exists");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        Customer customer = new Customer(tenant, request.name());
        customer.setTaxId(request.taxId());
        customer.setContactName(request.contactName());
        customer.setContactEmail(request.contactEmail());

        Customer saved = customerRepository.save(customer);
        log.info("Customer created: id={}, name={}", saved.getId(), saved.getName());
        return CustomerResponse.from(saved);
    }

    @Transactional
    public CustomerResponse update(UUID id, UpdateCustomerRequest request, UUID tenantId) {
        log.info("Updating customer id={}", id);
        Customer customer = customerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));

        if (request.name() != null) customer.setName(request.name());
        if (request.taxId() != null) customer.setTaxId(request.taxId());
        if (request.contactName() != null) customer.setContactName(request.contactName());
        if (request.contactEmail() != null) customer.setContactEmail(request.contactEmail());
        if (request.active() != null) customer.setActive(request.active());

        return CustomerResponse.from(customerRepository.save(customer));
    }

    @Transactional
    public void delete(UUID id, UUID tenantId) {
        log.info("Deleting customer id={}", id);
        Customer customer = customerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        customerRepository.delete(customer);
    }
}
