package com.freightflow.modules.auth;

import com.freightflow.modules.auth.dto.CreateUserRequest;
import com.freightflow.modules.auth.dto.UpdateUserRequest;
import com.freightflow.modules.auth.dto.UserResponse;
import com.freightflow.modules.customer.Customer;
import com.freightflow.modules.customer.CustomerRepository;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import com.freightflow.shared.pagination.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       TenantRepository tenantRepository,
                       CustomerRepository customerRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ==================== Queries ====================

    public PageResponse<UserResponse> list(UUID tenantId, Pageable pageable) {
        log.debug("Listing users for tenant={}", tenantId);
        Page<User> page = userRepository.findByTenantId(tenantId, pageable);
        return PageResponse.from(page.map(UserResponse::from));
    }

    public UserResponse getById(UUID id, UUID tenantId) {
        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return UserResponse.from(user);
    }

    // ==================== Commands ====================

    @Transactional
    public UserResponse create(CreateUserRequest request, UUID tenantId) {
        log.info("Creating user email='{}' role='{}' for tenant={}", request.email(), request.role(), tenantId);

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email " + request.email() + " is already in use");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        User.UserRole role = User.UserRole.valueOf(request.role());

        // CLIENT role requires customerId
        Customer customer = null;
        if (role == User.UserRole.CLIENT) {
            if (request.customerId() == null || request.customerId().isBlank()) {
                throw new BusinessException("customerId is required for CLIENT role");
            }
            UUID customerId = UUID.fromString(request.customerId());
            customer = customerRepository.findByIdAndTenantId(customerId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));
        }

        String hashedPassword = passwordEncoder.encode(request.password());
        User user = new User(request.name(), request.email(), hashedPassword, role, tenant);
        user.setCustomer(customer);

        User saved = userRepository.save(user);
        log.info("User created: id={}, email={}", saved.getId(), saved.getEmail());
        return UserResponse.from(saved);
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request, UUID tenantId) {
        log.info("Updating user id={}", id);
        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (request.name() != null) user.setName(request.name());
        if (request.active() != null) user.setActive(request.active());

        if (request.role() != null) {
            User.UserRole newRole = User.UserRole.valueOf(request.role());
            user.setRole(newRole);

            if (newRole == User.UserRole.CLIENT) {
                if (request.customerId() == null || request.customerId().isBlank()) {
                    throw new BusinessException("customerId is required when changing role to CLIENT");
                }
                UUID customerId = UUID.fromString(request.customerId());
                Customer customer = customerRepository.findByIdAndTenantId(customerId, tenantId)
                        .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));
                user.setCustomer(customer);
            } else {
                // Non-CLIENT roles don't need customer link
                user.setCustomer(null);
            }
        } else if (request.customerId() != null && !request.customerId().isBlank()) {
            // Update only customer (keep existing role)
            UUID customerId = UUID.fromString(request.customerId());
            Customer customer = customerRepository.findByIdAndTenantId(customerId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));
            user.setCustomer(customer);
        }

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void delete(UUID id, UUID tenantId) {
        log.info("Deleting user id={}", id);
        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        // Soft delete — desativa em vez de apagar
        user.setActive(false);
        userRepository.save(user);
    }
}
