package com.freightflow.modules.customer;

import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.customer.dto.CreateCustomerRequest;
import com.freightflow.modules.customer.dto.CustomerResponse;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import com.freightflow.shared.pagination.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private CustomerService customerService;

    private Tenant tenantA;
    private Tenant tenantB;
    private Customer customer;

    @BeforeEach
    void setUp() {
        tenantA  = TestDataFactory.tenant();
        tenantB  = TestDataFactory.tenant(UUID.randomUUID(), "Other Company", "other-company");
        customer = new Customer(tenantA, "Atlas Cargo");
        TestDataFactory.setEntityId(customer, UUID.randomUUID());
    }

    // ── list() ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("list()")
    class ListTests {

        @Test
        @DisplayName("should_returnOnlyCustomersFromCallerTenant")
        void should_returnOnlyCustomersFromCallerTenant() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            var page = new PageImpl<>(List.of(customer), pageable, 1);
            when(customerRepository.findByTenantId(tenantA.getId(), pageable)).thenReturn(page);

            // Act
            PageResponse<CustomerResponse> result = customerService.list(tenantA.getId(), pageable);

            // Assert
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().get(0).name()).isEqualTo("Atlas Cargo");
        }
    }

    // ── getById() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("should_returnCustomer_when_idAndTenantMatch")
        void should_returnCustomer_when_idAndTenantMatch() {
            // Arrange
            UUID customerId = customer.getId();
            when(customerRepository.findByIdAndTenantId(customerId, tenantA.getId()))
                    .thenReturn(Optional.of(customer));

            // Act
            CustomerResponse result = customerService.getById(customerId, tenantA.getId());

            // Assert
            assertThat(result.name()).isEqualTo("Atlas Cargo");
        }

        @Test
        @DisplayName("should_throwForbiddenException_when_tenantMismatch")
        void should_throwForbiddenException_when_tenantMismatch() {
            // Arrange — tenantB cannot see tenantA's customer (findByIdAndTenantId returns empty)
            UUID customerId = customer.getId();
            when(customerRepository.findByIdAndTenantId(customerId, tenantB.getId()))
                    .thenReturn(Optional.empty());

            // Act & Assert — returns 404 (not 403) to avoid revealing existence of resource
            assertThatThrownBy(() -> customerService.getById(customerId, tenantB.getId()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Customer");
        }

        @Test
        @DisplayName("should_throwResourceNotFoundException_when_customerNotFound")
        void should_throwResourceNotFoundException_when_customerNotFound() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(customerRepository.findByIdAndTenantId(nonExistentId, tenantA.getId()))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> customerService.getById(nonExistentId, tenantA.getId()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Customer");
        }
    }

    // ── create() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should_throwBusinessException_when_customerNameAlreadyExistsInTenant")
        void should_throwBusinessException_when_customerNameAlreadyExistsInTenant() {
            // Arrange
            CreateCustomerRequest request = new CreateCustomerRequest(
                    "Atlas Cargo", null, null, null);
            when(customerRepository.existsByNameAndTenantId("Atlas Cargo", tenantA.getId()))
                    .thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> customerService.create(request, tenantA.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Atlas Cargo");
        }

        @Test
        @DisplayName("should_persistAndReturnCustomer_when_dataIsValid")
        void should_persistAndReturnCustomer_when_dataIsValid() {
            // Arrange
            CreateCustomerRequest request = new CreateCustomerRequest(
                    "New Customer", "12.345.678/0001-99", "Contact Name", "contact@new.com");
            when(customerRepository.existsByNameAndTenantId(any(), any())).thenReturn(false);
            when(tenantRepository.findById(tenantA.getId())).thenReturn(Optional.of(tenantA));
            Customer saved = new Customer(tenantA, "New Customer");
            TestDataFactory.setEntityId(saved, UUID.randomUUID());
            when(customerRepository.save(any(Customer.class))).thenReturn(saved);

            // Act
            CustomerResponse result = customerService.create(request, tenantA.getId());

            // Assert
            assertThat(result.name()).isEqualTo("New Customer");
        }
    }
}
