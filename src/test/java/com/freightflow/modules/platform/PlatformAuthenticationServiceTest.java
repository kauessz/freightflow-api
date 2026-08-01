package com.freightflow.modules.platform;

import com.freightflow.config.PlatformBootstrapProperties;
import com.freightflow.config.PlatformJwtProperties;
import com.freightflow.modules.platform.dto.PlatformAuthResponse;
import com.freightflow.modules.platform.dto.PlatformLoginRequest;
import com.freightflow.shared.exception.UnauthorizedException;
import com.freightflow.shared.security.platform.PlatformJwtService;
import com.freightflow.shared.security.platform.PlatformPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlatformAuthenticationService")
class PlatformAuthenticationServiceTest {

    @Mock private PlatformUserRepository platformUserRepository;
    @Mock private PlatformBootstrapStateRepository platformBootstrapStateRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PlatformJwtService platformJwtService;
    @Mock private PlatformJwtProperties platformJwtProperties;

    @InjectMocks private PlatformAuthenticationService platformAuthenticationService;

    private PlatformUser activeUser() {
        PlatformUser user = new PlatformUser(
                "platform@freightflow.com",
                "$2a$10$hashedPasswordPlaceholder",
                PlatformRole.PLATFORM_ADMIN,
                PlatformUserStatus.ACTIVE
        );
        setEntityId(user, UUID.fromString("11111111-1111-1111-1111-111111111111"));
        return user;
    }

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("deveAutenticarComEmailCaseInsensitive")
        void deveAutenticarComEmailCaseInsensitive() {
            PlatformUser user = activeUser();
            when(platformUserRepository.findByEmailIgnoreCase("platform@freightflow.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("Password123", user.getPasswordHash())).thenReturn(true);
            when(platformUserRepository.save(any(PlatformUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(platformJwtService.generateAccessToken(any(PlatformPrincipal.class))).thenReturn("platform-access-token");
            when(platformJwtProperties.getExpirationMs()).thenReturn(3_600_000L);

            PlatformAuthResponse response = platformAuthenticationService.login(
                    new PlatformLoginRequest("Platform@FreightFlow.com", "Password123")
            );

            assertThat(response.accessToken()).isEqualTo("platform-access-token");
            assertThat(response.user().email()).isEqualTo("platform@freightflow.com");
            assertThat(response.user().platformRole()).isEqualTo("PLATFORM_ADMIN");

            ArgumentCaptor<PlatformUser> savedCaptor = ArgumentCaptor.forClass(PlatformUser.class);
            verify(platformUserRepository).save(savedCaptor.capture());
            assertThat(savedCaptor.getValue().getLastLoginAt()).isNotNull();
        }

        @Test
        @DisplayName("deveFalharQuandoSenhaInvalida")
        void deveFalharQuandoSenhaInvalida() {
            PlatformUser user = activeUser();
            when(platformUserRepository.findByEmailIgnoreCase("platform@freightflow.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrong-password", user.getPasswordHash())).thenReturn(false);

            assertThatThrownBy(() -> platformAuthenticationService.login(
                    new PlatformLoginRequest("platform@freightflow.com", "wrong-password")
            )).isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Invalid email or password");

            verify(platformUserRepository, never()).save(any());
        }

        @Test
        @DisplayName("deveFalharQuandoUsuarioNaoExiste")
        void deveFalharQuandoUsuarioNaoExiste() {
            when(platformUserRepository.findByEmailIgnoreCase("missing@freightflow.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> platformAuthenticationService.login(
                    new PlatformLoginRequest("missing@freightflow.com", "Password123")
            )).isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Invalid email or password");
        }

        @Test
        @DisplayName("deveFalharQuandoContaEstaDesabilitada")
        void deveFalharQuandoContaEstaDesabilitada() {
            PlatformUser user = activeUser();
            user.setStatus(PlatformUserStatus.DISABLED);
            when(platformUserRepository.findByEmailIgnoreCase("platform@freightflow.com")).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> platformAuthenticationService.login(
                    new PlatformLoginRequest("platform@freightflow.com", "Password123")
            )).isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("disabled");
        }
    }

    @Test
    @DisplayName("bootstrapNaoCriaUsuarioQuandoDesabilitado")
    void bootstrapNaoCriaUsuarioQuandoDesabilitado() {
        PlatformBootstrapProperties bootstrapProperties = new PlatformBootstrapProperties();
        bootstrapProperties.setEnabled(false);
        PlatformBootstrapService bootstrapService = new PlatformBootstrapService(
                bootstrapProperties,
                platformUserRepository,
                platformBootstrapStateRepository,
                passwordEncoder
        );

        bootstrapService.bootstrapIfEnabled();

        verify(platformUserRepository, never()).save(any());
        verify(platformBootstrapStateRepository, never()).save(any());
    }

    @Test
    @DisplayName("bootstrapCriaPrimeiroPlatformUserQuandoHabilitado")
    void bootstrapCriaPrimeiroPlatformUserQuandoHabilitado() {
        PlatformBootstrapProperties bootstrapProperties = new PlatformBootstrapProperties();
        bootstrapProperties.setEnabled(true);
        bootstrapProperties.setEmail("Bootstrap@FreightFlow.com");
        bootstrapProperties.setPassword("Bootstrap123");
        PlatformBootstrapService bootstrapService = new PlatformBootstrapService(
                bootstrapProperties,
                platformUserRepository,
                platformBootstrapStateRepository,
                passwordEncoder
        );
        when(platformBootstrapStateRepository.existsById(PlatformBootstrapService.INITIAL_PLATFORM_ADMIN_BOOTSTRAP)).thenReturn(false);
        when(platformUserRepository.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Bootstrap123")).thenReturn("$2a$10$bootstrap");
        when(platformUserRepository.save(any(PlatformUser.class))).thenAnswer(invocation -> {
            PlatformUser saved = invocation.getArgument(0);
            setEntityId(saved, UUID.fromString("22222222-2222-2222-2222-222222222222"));
            return saved;
        });

        bootstrapService.bootstrapIfEnabled();

        ArgumentCaptor<PlatformUser> captor = ArgumentCaptor.forClass(PlatformUser.class);
        verify(platformUserRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("bootstrap@freightflow.com");
        assertThat(captor.getValue().getRole()).isEqualTo(PlatformRole.PLATFORM_ADMIN);
        assertThat(captor.getValue().getStatus()).isEqualTo(PlatformUserStatus.ACTIVE);
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("$2a$10$bootstrap");
        verify(platformBootstrapStateRepository).save(any(PlatformBootstrapState.class));
    }

    @Test
    @DisplayName("bootstrapEhIdempotenteQuandoJaExistePlatformUser")
    void bootstrapEhIdempotenteQuandoJaExistePlatformUser() {
        PlatformBootstrapProperties bootstrapProperties = new PlatformBootstrapProperties();
        bootstrapProperties.setEnabled(true);
        bootstrapProperties.setEmail("bootstrap@freightflow.com");
        bootstrapProperties.setPassword("Bootstrap123");
        PlatformBootstrapService bootstrapService = new PlatformBootstrapService(
                bootstrapProperties,
                platformUserRepository,
                platformBootstrapStateRepository,
                passwordEncoder
        );
        when(platformBootstrapStateRepository.existsById(PlatformBootstrapService.INITIAL_PLATFORM_ADMIN_BOOTSTRAP)).thenReturn(true);

        bootstrapService.bootstrapIfEnabled();

        verify(platformUserRepository, never()).save(any());
        verify(platformBootstrapStateRepository, never()).save(any());
    }

    @Test
    @DisplayName("bootstrapNaoRecriaContaRemovidaQuandoMarkerJaExiste")
    void bootstrapNaoRecriaContaRemovidaQuandoMarkerJaExiste() {
        PlatformBootstrapProperties bootstrapProperties = new PlatformBootstrapProperties();
        bootstrapProperties.setEnabled(true);
        bootstrapProperties.setEmail("bootstrap@freightflow.com");
        bootstrapProperties.setPassword("Bootstrap123");
        PlatformBootstrapService bootstrapService = new PlatformBootstrapService(
                bootstrapProperties,
                platformUserRepository,
                platformBootstrapStateRepository,
                passwordEncoder
        );
        when(platformBootstrapStateRepository.existsById(PlatformBootstrapService.INITIAL_PLATFORM_ADMIN_BOOTSTRAP)).thenReturn(true);

        bootstrapService.bootstrapIfEnabled();

        verify(platformUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("bootstrapFalhaClaramenteSemConfiguracaoObrigatoria")
    void bootstrapFalhaClaramenteSemConfiguracaoObrigatoria() {
        PlatformBootstrapProperties bootstrapProperties = new PlatformBootstrapProperties();
        bootstrapProperties.setEnabled(true);
        bootstrapProperties.setEmail("");
        bootstrapProperties.setPassword("");
        PlatformBootstrapService bootstrapService = new PlatformBootstrapService(
                bootstrapProperties,
                platformUserRepository,
                platformBootstrapStateRepository,
                passwordEncoder
        );

        assertThatThrownBy(bootstrapService::bootstrapIfEnabled)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("email/password");

        verify(platformBootstrapStateRepository, never()).existsById(any());
    }

    @Test
    @DisplayName("bootstrapComUsuarioExistentePersisteMarkerSemDuplicar")
    void bootstrapComUsuarioExistentePersisteMarkerSemDuplicar() {
        PlatformBootstrapProperties bootstrapProperties = new PlatformBootstrapProperties();
        bootstrapProperties.setEnabled(true);
        bootstrapProperties.setEmail("bootstrap@freightflow.com");
        bootstrapProperties.setPassword("Bootstrap123");
        PlatformBootstrapService bootstrapService = new PlatformBootstrapService(
                bootstrapProperties,
                platformUserRepository,
                platformBootstrapStateRepository,
                passwordEncoder
        );
        PlatformUser existing = activeUser();
        when(platformBootstrapStateRepository.existsById(PlatformBootstrapService.INITIAL_PLATFORM_ADMIN_BOOTSTRAP)).thenReturn(false);
        when(platformUserRepository.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.of(existing));

        bootstrapService.bootstrapIfEnabled();

        verify(platformUserRepository, never()).save(any(PlatformUser.class));
        ArgumentCaptor<PlatformBootstrapState> stateCaptor = ArgumentCaptor.forClass(PlatformBootstrapState.class);
        verify(platformBootstrapStateRepository).save(stateCaptor.capture());
        assertThat(stateCaptor.getValue().getBootstrapKey()).isEqualTo(PlatformBootstrapService.INITIAL_PLATFORM_ADMIN_BOOTSTRAP);
        assertThat(stateCaptor.getValue().getPlatformUserId()).isEqualTo(existing.getId());
    }

    private static void setEntityId(PlatformUser user, UUID id) {
        try {
            var field = PlatformUser.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}
