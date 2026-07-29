package com.freightflow.modules.customer;

import com.freightflow.config.TestSecurityConfig;
import com.freightflow.modules.customer.dto.CustomerResponse;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.GlobalExceptionHandler;
import com.freightflow.shared.rbac.RoleCheckAspect;
import com.freightflow.shared.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CustomerController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class, CustomerControllerTest.RoleAspectTestConfig.class})
@AutoConfigureMockMvc(addFilters = true)
@DisplayName("Customer controller")
class CustomerControllerTest {

    @TestConfiguration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class RoleAspectTestConfig {
        @Bean
        RoleCheckAspect roleCheckAspect() {
            return new RoleCheckAspect();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Test
    @DisplayName("deleteRetorna409QuandoCustomerPossuiUserVinculado")
    void deleteRetorna409QuandoCustomerPossuiUserVinculado() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        doThrow(new BusinessException("Customer cannot be deleted while it is linked to active users"))
                .when(customerService).delete(customerId, tenantId);

        mockMvc.perform(delete("/api/v1/customers/{id}", customerId)
                        .with(csrf())
                        .with(user(principal("ADMIN", tenantId))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Business Rule Violation"))
                .andExpect(jsonPath("$.detail").value("Customer cannot be deleted while it is linked to active users"));
    }

    private UserPrincipal principal(String role, UUID tenantId) {
        return new UserPrincipal(UUID.randomUUID(), role.toLowerCase() + "@tenant.com", null, tenantId, role, null);
    }
}
