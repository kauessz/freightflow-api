package com.freightflow.modules.auth;

import com.freightflow.config.JwtProperties;
import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.auth.dto.AuthResponse;
import com.freightflow.modules.auth.dto.LoginRequest;
import com.freightflow.modules.auth.dto.RefreshRequest;
import com.freightflow.modules.auth.dto.RegisterRequest;
import com.freightflow.modules.auth.dto.UserResponse;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import com.freightflow.shared.exception.UnauthorizedException;
import com.freightflow.shared.security.JwtTokenProvider;
import com.freightflow.shared.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private JwtProperties jwtProperties;

    @InjectMocks private AuthService authService;

    private Tenant tenant;
    private User user;

    @BeforeEach
    void setUp() {
        tenant = TestDataFactory.tenant();
        user = TestDataFactory.userWithTenant(
                TestDataFactory.defaultUserId(), "Kaue", "kaue@mercosul.com",
                User.UserRole.ADMIN, tenant
        );
    }

    // ==================== Register ====================

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        private final RegisterRequest validRequest = new RegisterRequest(
                "Kaue", "kaue@newcompany.com", "senha1234", "New Company"
        );

        @Test
        @DisplayName("Deve registrar novo usuario e tenant com sucesso")
        void deveRegistrarComSucesso() {
            when(userRepository.existsByEmail("kaue@newcompany.com")).thenReturn(false);
            when(tenantRepository.existsBySlug("new-company")).thenReturn(false);
            when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> {
                Tenant t = inv.getArgument(0);
                // Simula a geracao de ID pelo JPA
                TestDataFactory.setEntityId(t, UUID.randomUUID());
                return t;
            });
            when(passwordEncoder.encode("senha1234")).thenReturn("$2a$10$encoded");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                // Simula a geracao de ID pelo JPA
                TestDataFactory.setEntityId(u, UUID.randomUUID());
                return u;
            });
            when(jwtTokenProvider.generateAccessToken(any(UserPrincipal.class))).thenReturn("access-token");
            when(jwtTokenProvider.generateRefreshToken(any(UserPrincipal.class))).thenReturn("refresh-token");
            when(jwtProperties.getExpirationMs()).thenReturn(86400000L);

            AuthResponse result = authService.register(validRequest);

            assertThat(result.accessToken()).isEqualTo("access-token");
            assertThat(result.refreshToken()).isEqualTo("refresh-token");
            assertThat(result.tokenType()).isEqualTo("Bearer");
            assertThat(result.user().name()).isEqualTo("Kaue");
            assertThat(result.user().email()).isEqualTo("kaue@newcompany.com");
            verify(tenantRepository).save(any(Tenant.class));
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Deve lancar BusinessException quando email ja existe")
        void deveLancarExcecaoQuandoEmailExiste() {
            when(userRepository.existsByEmail("kaue@newcompany.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(validRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already registered");

            verify(tenantRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lancar BusinessException quando slug do tenant ja existe")
        void deveLancarExcecaoQuandoSlugExiste() {
            when(userRepository.existsByEmail("kaue@newcompany.com")).thenReturn(false);
            when(tenantRepository.existsBySlug("new-company")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(validRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already taken");
        }
    }

    // ==================== Login ====================

    @Nested
    @DisplayName("login()")
    class LoginTests {

        private final LoginRequest validRequest = new LoginRequest("kaue@mercosul.com", "senha1234");

        @Test
        @DisplayName("Deve autenticar usuario com credenciais validas")
        void deveAutenticarComSucesso() {
            when(userRepository.findByEmailWithTenant("kaue@mercosul.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("senha1234", user.getPasswordHash())).thenReturn(true);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(jwtTokenProvider.generateAccessToken(any(UserPrincipal.class))).thenReturn("access-token");
            when(jwtTokenProvider.generateRefreshToken(any(UserPrincipal.class))).thenReturn("refresh-token");
            when(jwtProperties.getExpirationMs()).thenReturn(86400000L);

            AuthResponse result = authService.login(validRequest);

            assertThat(result.accessToken()).isEqualTo("access-token");
            assertThat(result.user().email()).isEqualTo("kaue@mercosul.com");
            assertThat(result.user().role()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("Deve lancar UnauthorizedException quando email nao encontrado")
        void deveLancarExcecaoQuandoEmailNaoEncontrado() {
            when(userRepository.findByEmailWithTenant("kaue@mercosul.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(validRequest))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Invalid email or password");
        }

        @Test
        @DisplayName("Deve lancar UnauthorizedException quando senha incorreta")
        void deveLancarExcecaoQuandoSenhaIncorreta() {
            when(userRepository.findByEmailWithTenant("kaue@mercosul.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("senha1234", user.getPasswordHash())).thenReturn(false);

            assertThatThrownBy(() -> authService.login(validRequest))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Invalid email or password");
        }

        @Test
        @DisplayName("Deve lancar UnauthorizedException quando conta desativada")
        void deveLancarExcecaoQuandoContaDesativada() {
            user.setActive(false);
            when(userRepository.findByEmailWithTenant("kaue@mercosul.com")).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> authService.login(validRequest))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("disabled");
        }
    }

    // ==================== Refresh ====================

    @Nested
    @DisplayName("refresh()")
    class RefreshTests {

        @Test
        @DisplayName("Deve renovar tokens com refresh token valido")
        void deveRenovarTokens() {
            Claims claims = mock(Claims.class);
            when(claims.get("type", String.class)).thenReturn("refresh");
            when(claims.getSubject()).thenReturn(user.getId().toString());

            when(jwtTokenProvider.validateAndGetClaims("valid-refresh")).thenReturn(claims);
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(jwtTokenProvider.generateAccessToken(any(UserPrincipal.class))).thenReturn("new-access");
            when(jwtTokenProvider.generateRefreshToken(any(UserPrincipal.class))).thenReturn("new-refresh");
            when(jwtProperties.getExpirationMs()).thenReturn(86400000L);

            AuthResponse result = authService.refresh(new RefreshRequest("valid-refresh"));

            assertThat(result.accessToken()).isEqualTo("new-access");
            assertThat(result.refreshToken()).isEqualTo("new-refresh");
        }

        @Test
        @DisplayName("Deve lancar UnauthorizedException quando refresh token invalido")
        void deveLancarExcecaoQuandoTokenInvalido() {
            when(jwtTokenProvider.validateAndGetClaims("invalid")).thenReturn(null);

            assertThatThrownBy(() -> authService.refresh(new RefreshRequest("invalid")))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Invalid or expired");
        }

        @Test
        @DisplayName("Deve lancar UnauthorizedException quando token nao eh refresh")
        void deveLancarExcecaoQuandoNaoEhRefreshToken() {
            Claims claims = mock(Claims.class);
            when(claims.get("type", String.class)).thenReturn("access");

            when(jwtTokenProvider.validateAndGetClaims("access-token")).thenReturn(claims);

            assertThatThrownBy(() -> authService.refresh(new RefreshRequest("access-token")))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("not a refresh token");
        }
    }

    // ==================== Me ====================

    @Nested
    @DisplayName("me()")
    class MeTests {

        @Test
        @DisplayName("Deve retornar perfil do usuario autenticado")
        void deveRetornarPerfil() {
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

            UserResponse result = authService.me(user.getId());

            assertThat(result.name()).isEqualTo("Kaue");
            assertThat(result.email()).isEqualTo("kaue@mercosul.com");
            assertThat(result.role()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("Deve lancar ResourceNotFoundException quando usuario nao encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.me(id))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
