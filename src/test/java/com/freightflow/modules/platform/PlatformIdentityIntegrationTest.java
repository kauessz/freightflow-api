package com.freightflow.modules.platform;

import com.freightflow.AbstractIntegrationTest;
import com.freightflow.config.PlatformBootstrapProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Platform identity integration")
class PlatformIdentityIntegrationTest extends AbstractIntegrationTest {

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private PlatformUserRepository platformUserRepository;
    @Autowired private PlatformBootstrapStateRepository platformBootstrapStateRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("deveAplicarMigrationEPersistirPlatformUserSemTenant")
    void deveAplicarMigrationEPersistirPlatformUserSemTenant() {
        Integer tableCount = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_name = 'platform_users'",
                Integer.class
        );

        PlatformUser saved = platformUserRepository.saveAndFlush(new PlatformUser(
                "platform@freightflow.com",
                passwordEncoder.encode("Password123"),
                PlatformRole.PLATFORM_ADMIN,
                PlatformUserStatus.ACTIVE
        ));

        assertThat(tableCount).isEqualTo(1);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("platform@freightflow.com");
    }

    @Test
    @DisplayName("emailCanonicoEhUnicoEValoresNaoCanonicosSaoRejeitados")
    void emailCanonicoEhUnicoEValoresNaoCanonicosSaoRejeitados() {
        platformUserRepository.saveAndFlush(new PlatformUser(
                "admin@freightflow.com",
                passwordEncoder.encode("Password123"),
                PlatformRole.PLATFORM_ADMIN,
                PlatformUserStatus.ACTIVE
        ));

        assertThatThrownBy(() -> platformUserRepository.saveAndFlush(new PlatformUser(
                "admin@freightflow.com",
                passwordEncoder.encode("Password456"),
                PlatformRole.PLATFORM_ADMIN,
                PlatformUserStatus.ACTIVE
        ))).isInstanceOf(DataIntegrityViolationException.class);

        Instant now = Instant.now();
        Timestamp timestamp = Timestamp.from(now);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into platform_users (id, email, password_hash, role, status, last_login_at, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(), "Admin@FreightFlow.com", "$2a$10$hash", "PLATFORM_ADMIN", "ACTIVE", timestamp, timestamp, timestamp
        )).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into platform_users (id, email, password_hash, role, status, last_login_at, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(), " admin@freightflow.com ", "$2a$10$hash", "PLATFORM_ADMIN", "ACTIVE", timestamp, timestamp, timestamp
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("roleEStatusInvalidosSaoRejeitadosPeloBanco")
    void roleEStatusInvalidosSaoRejeitadosPeloBanco() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Timestamp timestamp = Timestamp.from(now);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into platform_users (id, email, password_hash, role, status, last_login_at, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                "invalid-role@freightflow.com",
                "$2a$10$hash",
                "INVALID_ROLE",
                "ACTIVE",
                timestamp,
                timestamp,
                timestamp
        )).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into platform_users (id, email, password_hash, role, status, last_login_at, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                "invalid-status@freightflow.com",
                "$2a$10$hash",
                "PLATFORM_ADMIN",
                "INVALID_STATUS",
                timestamp,
                timestamp,
                timestamp
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("emailNaoPodeSerVazioEspacosOuNaoNormalizadoEHashNaoPodeSerVazio")
    void emailNaoPodeSerVazioEspacosOuNaoNormalizadoEHashNaoPodeSerVazio() {
        Instant now = Instant.now();
        Timestamp timestamp = Timestamp.from(now);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into platform_users (id, email, password_hash, role, status, last_login_at, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(), "", "$2a$10$hash", "PLATFORM_ADMIN", "ACTIVE", timestamp, timestamp, timestamp
        )).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into platform_users (id, email, password_hash, role, status, last_login_at, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(), "   ", "$2a$10$hash", "PLATFORM_ADMIN", "ACTIVE", timestamp, timestamp, timestamp
        )).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into platform_users (id, email, password_hash, role, status, last_login_at, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(), "Admin@FreightFlow.com", "$2a$10$hash", "PLATFORM_ADMIN", "ACTIVE", timestamp, timestamp, timestamp
        )).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into platform_users (id, email, password_hash, role, status, last_login_at, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(), "admin@freightflow.com", "   ", "PLATFORM_ADMIN", "ACTIVE", timestamp, timestamp, timestamp
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("markerDeBootstrapPermaneceAposExcluirUsuarioEBootstrapNaoRecriaConta")
    void markerDeBootstrapPermaneceAposExcluirUsuarioEBootstrapNaoRecriaConta() {
        PlatformUser user = platformUserRepository.saveAndFlush(new PlatformUser(
                "bootstrap@freightflow.com",
                passwordEncoder.encode("Password123"),
                PlatformRole.PLATFORM_ADMIN,
                PlatformUserStatus.ACTIVE
        ));

        platformBootstrapStateRepository.saveAndFlush(new PlatformBootstrapState(
                PlatformBootstrapService.INITIAL_PLATFORM_ADMIN_BOOTSTRAP,
                Instant.now(),
                user.getId()
        ));

        user.setStatus(PlatformUserStatus.DISABLED);
        platformUserRepository.saveAndFlush(user);

        platformUserRepository.delete(user);
        platformUserRepository.flush();

        PlatformBootstrapState markerAfterDelete = platformBootstrapStateRepository.findById(
                PlatformBootstrapService.INITIAL_PLATFORM_ADMIN_BOOTSTRAP
        ).orElseThrow();

        assertThat(platformUserRepository.count()).isZero();
        assertThat(markerAfterDelete.getPlatformUserId()).isNull();

        PlatformBootstrapProperties properties = new PlatformBootstrapProperties();
        properties.setEnabled(true);
        properties.setEmail("bootstrap@freightflow.com");
        properties.setPassword("Bootstrap123");

        PlatformBootstrapService bootstrapService = new PlatformBootstrapService(
                properties,
                platformUserRepository,
                platformBootstrapStateRepository,
                passwordEncoder
        );

        bootstrapService.bootstrapIfEnabled();

        assertThat(platformUserRepository.count()).isZero();
        assertThat(platformBootstrapStateRepository.count()).isEqualTo(1);
        assertThat(platformBootstrapStateRepository.findById(
                PlatformBootstrapService.INITIAL_PLATFORM_ADMIN_BOOTSTRAP
        )).get().extracting(PlatformBootstrapState::getPlatformUserId).isNull();
    }
}
