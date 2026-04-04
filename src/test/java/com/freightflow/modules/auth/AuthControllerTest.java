package com.freightflow.modules.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightflow.config.TestSecurityConfig;
import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.auth.dto.AuthResponse;
import com.freightflow.modules.auth.dto.LoginRequest;
import com.freightflow.modules.auth.dto.RefreshRequest;
import com.freightflow.modules.auth.dto.RegisterRequest;
import com.freightflow.modules.auth.dto.UserResponse;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.GlobalExceptionHandler;
import com.freightflow.shared.exception.UnauthorizedException;
import com.freightflow.shared.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = true)
@DisplayName("AuthController")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;

    private AuthResponse sampleAuthResponse() {
        return new AuthResponse(
                "access-token-jwt", "refresh-token-jwt", 86400,
                new AuthResponse.UserInfo(
                        UUID.randomUUID().toString(), "Kaue", "kaue@mercosul.com",
                        "ADMIN", UUID.randomUUID().toString(), "Mercosul Line"
                )
        );
    }

    // ==================== POST /api/v1/auth/register ====================

    @Nested
    @DisplayName("POST /api/v1/auth/register")
    class RegisterEndpoint {

        @Test
        @DisplayName("Deve retornar 201 ao registrar com sucesso")
        void deveRetornar201() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "Kaue", "kaue@newcompany.com", "senha1234", "New Company"
            );
            when(authService.register(any(RegisterRequest.class))).thenReturn(sampleAuthResponse());

            mockMvc.perform(post("/api/v1/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.accessToken").value("access-token-jwt"))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.user.name").value("Kaue"));
        }

        @Test
        @DisplayName("Deve retornar 400 com dados invalidos")
        void deveRetornar400() throws Exception {
            RegisterRequest invalid = new RegisterRequest("", "", "123", "");

            mockMvc.perform(post("/api/v1/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Validation Error"));
        }

        @Test
        @DisplayName("Deve retornar 409 quando email ja registrado")
        void deveRetornar409() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "Kaue", "existing@test.com", "senha1234", "Company"
            );
            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new BusinessException("Email existing@test.com is already registered"));

            mockMvc.perform(post("/api/v1/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").value("Email existing@test.com is already registered"));
        }
    }

    // ==================== POST /api/v1/auth/login ====================

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class LoginEndpoint {

        @Test
        @DisplayName("Deve retornar 200 com credenciais validas")
        void deveRetornar200() throws Exception {
            LoginRequest request = new LoginRequest("kaue@mercosul.com", "senha1234");
            when(authService.login(any(LoginRequest.class))).thenReturn(sampleAuthResponse());

            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.refreshToken").exists())
                    .andExpect(jsonPath("$.user.email").value("kaue@mercosul.com"));
        }

        @Test
        @DisplayName("Deve retornar 401 com credenciais invalidas")
        void deveRetornar401() throws Exception {
            LoginRequest request = new LoginRequest("kaue@mercosul.com", "wrongpassword");
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new UnauthorizedException("Invalid email or password"));

            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.detail").value("Invalid email or password"));
        }
    }

    // ==================== POST /api/v1/auth/refresh ====================

    @Nested
    @DisplayName("POST /api/v1/auth/refresh")
    class RefreshEndpoint {

        @Test
        @DisplayName("Deve retornar 200 com refresh token valido")
        void deveRetornar200() throws Exception {
            RefreshRequest request = new RefreshRequest("valid-refresh-token");
            when(authService.refresh(any(RefreshRequest.class))).thenReturn(sampleAuthResponse());

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists());
        }

        @Test
        @DisplayName("Deve retornar 401 com refresh token invalido")
        void deveRetornar401() throws Exception {
            RefreshRequest request = new RefreshRequest("expired-token");
            when(authService.refresh(any(RefreshRequest.class)))
                    .thenThrow(new UnauthorizedException("Invalid or expired refresh token"));

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== GET /api/v1/auth/me ====================

    @Nested
    @DisplayName("GET /api/v1/auth/me")
    class MeEndpoint {

        @Test
        @DisplayName("Deve retornar 200 com perfil do usuario autenticado")
        void deveRetornar200() throws Exception {
            UserPrincipal principal = TestDataFactory.principal();
            UserResponse userResponse = new UserResponse(
                    principal.getId().toString(), "Kaue", "kaue@mercosul.com", "ADMIN",
                    principal.getTenantId().toString(), "Mercosul Line",
                    null, null, true,
                    Instant.now(), Instant.now()
            );
            when(authService.me(principal.getId())).thenReturn(userResponse);

            mockMvc.perform(get("/api/v1/auth/me")
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Kaue"))
                    .andExpect(jsonPath("$.role").value("ADMIN"));
        }

        @Test
        @DisplayName("Deve retornar 401 sem autenticacao")
        void deveRetornar401() throws Exception {
            mockMvc.perform(get("/api/v1/auth/me"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
