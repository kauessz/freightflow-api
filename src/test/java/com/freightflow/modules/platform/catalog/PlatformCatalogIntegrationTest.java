package com.freightflow.modules.platform.catalog;

import com.freightflow.AbstractIntegrationTest;
import com.freightflow.config.PlatformBootstrapProperties;
import com.freightflow.modules.platform.PlatformBootstrapService;
import com.freightflow.modules.platform.PlatformBootstrapState;
import com.freightflow.modules.platform.PlatformBootstrapStateRepository;
import com.freightflow.modules.platform.PlatformRole;
import com.freightflow.modules.platform.PlatformUser;
import com.freightflow.modules.platform.PlatformUserRepository;
import com.freightflow.modules.platform.PlatformUserStatus;
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

@DisplayName("Platform catalog integration")
class PlatformCatalogIntegrationTest extends AbstractIntegrationTest {

    private static final String INITIAL_PLATFORM_ADMIN_BOOTSTRAP = "INITIAL_PLATFORM_ADMIN";

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private PlatformCatalogService platformCatalogService;
    @Autowired private PlatformUserRepository platformUserRepository;
    @Autowired private PlatformBootstrapStateRepository platformBootstrapStateRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("deveAplicarV28ESeedarCatalogoInicial")
    void deveAplicarV28ESeedarCatalogoInicial() {
        Integer featureTableCount = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_name = 'platform_features'",
                Integer.class
        );
        Integer planTableCount = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_name = 'subscription_plans'",
                Integer.class
        );
        Integer features = jdbcTemplate.queryForObject("select count(*) from platform_features", Integer.class);
        Integer dependencies = jdbcTemplate.queryForObject("select count(*) from platform_feature_dependencies", Integer.class);
        Integer plans = jdbcTemplate.queryForObject("select count(*) from subscription_plans", Integer.class);
        Integer starterClientLimit = jdbcTemplate.queryForObject("""
                select count(*)
                from plan_entitlements pe
                join subscription_plans sp on sp.id = pe.plan_id
                where sp.code = 'STARTER' and pe.feature_key = 'MAX_CLIENT_USERS'
                """, Integer.class);
        Integer plannedInEnterprise = jdbcTemplate.queryForObject("""
                select count(*)
                from plan_entitlements pe
                join subscription_plans sp on sp.id = pe.plan_id
                join platform_features pf on pf.feature_key = pe.feature_key
                where sp.code = 'ENTERPRISE' and pf.implementation_status = 'PLANNED'
                """, Integer.class);

        assertThat(featureTableCount).isEqualTo(1);
        assertThat(planTableCount).isEqualTo(1);
        assertThat(features).isEqualTo(19);
        assertThat(dependencies).isEqualTo(5);
        assertThat(plans).isEqualTo(4);
        assertThat(starterClientLimit).isZero();
        assertThat(plannedInEnterprise).isZero();
        assertThat(jdbcTemplate.queryForObject("select count(*) from tenants", Integer.class)).isZero();
    }

    @Test
    @DisplayName("constraintsCanonicasDoCatalogoSaoAplicadasPeloPostgreSQL")
    void constraintsCanonicasDoCatalogoSaoAplicadasPeloPostgreSQL() {
        Timestamp now = Timestamp.from(Instant.now());

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into platform_features (feature_key, name, description, value_type, unit, implementation_status, active, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                "fleet_map", "Invalid", "desc", "BOOLEAN", null, "AVAILABLE", true, now, now
        )).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into subscription_plans (id, code, name, description, status, display_order, custom, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(), "LEGACY_TEST", "   ", "desc", "ACTIVE", 0, false, now, now
        )).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into subscription_plans (id, code, name, description, status, display_order, custom, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(), "INVALID_PLAN", "Invalid", "desc", "ACTIVE", -1, false, now, now
        )).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into platform_feature_dependencies (feature_key, required_feature_key)
                values (?, ?)
                """,
                "TRACKING", "TRACKING"
        )).isInstanceOf(DataIntegrityViolationException.class);

        UUID customPlanId = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into subscription_plans (id, code, name, description, status, display_order, custom, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                customPlanId, "CUSTOM_TEST", "Custom Test", "desc", "DRAFT", 9, true, now, now
        );

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into plan_entitlements (plan_id, feature_key, enabled, limit_value)
                values (?, ?, ?, ?)
                """,
                customPlanId, "MAX_ACTIVE_USERS", true, -1
        )).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into plan_entitlements (plan_id, feature_key, enabled, limit_value)
                values (?, ?, ?, ?)
                """,
                customPlanId, "MAX_ACTIVE_USERS", false, 10
        )).isInstanceOf(DataIntegrityViolationException.class);

        jdbcTemplate.update("""
                insert into plan_entitlements (plan_id, feature_key, enabled, limit_value)
                values (?, ?, ?, ?)
                """,
                customPlanId, "MAX_ACTIVE_USERS", true, 0
        );

        jdbcTemplate.update("""
                insert into plan_entitlements (plan_id, feature_key, enabled, limit_value)
                values (?, ?, ?, ?)
                """,
                customPlanId, "MAX_MONTHLY_API_REQUESTS", true, null
        );
    }

    @Test
    @DisplayName("booleanComLimitValueInvalidoEhRejeitadoPeloService")
    void booleanComLimitValueInvalidoEhRejeitadoPeloService() {
        UUID customPlanId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        Timestamp now = Timestamp.from(Instant.now());

        jdbcTemplate.update("""
                insert into subscription_plans (id, code, name, description, status, display_order, custom, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                customPlanId, "CUSTOM_TEST", "Custom Test", "desc", "DRAFT", 9, true, now, now
        );
        jdbcTemplate.update("""
                insert into plan_entitlements (plan_id, feature_key, enabled, limit_value)
                values (?, ?, ?, ?)
                """,
                customPlanId, "SHIPMENT_MANAGEMENT", true, 1
        );

        assertThatThrownBy(() -> platformCatalogService.getPlanByCode("CUSTOM_TEST"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("BOOLEAN feature");
    }

    @Test
    @DisplayName("bootstrapDaV27PermaneceIntactoAposV28")
    void bootstrapDaV27PermaneceIntactoAposV28() {
        PlatformUser user = platformUserRepository.saveAndFlush(new PlatformUser(
                "platform@freightflow.com",
                passwordEncoder.encode("Password123"),
                PlatformRole.PLATFORM_ADMIN,
                PlatformUserStatus.ACTIVE
        ));

        platformBootstrapStateRepository.saveAndFlush(new PlatformBootstrapState(
                INITIAL_PLATFORM_ADMIN_BOOTSTRAP,
                Instant.now(),
                user.getId()
        ));

        platformUserRepository.delete(user);
        platformUserRepository.flush();

        PlatformBootstrapState marker = platformBootstrapStateRepository.findById(
                INITIAL_PLATFORM_ADMIN_BOOTSTRAP
        ).orElseThrow();

        PlatformBootstrapProperties properties = new PlatformBootstrapProperties();
        properties.setEnabled(true);
        properties.setEmail("platform@freightflow.com");
        properties.setPassword("Password123");

        PlatformBootstrapService bootstrapService = new PlatformBootstrapService(
                properties,
                platformUserRepository,
                platformBootstrapStateRepository,
                passwordEncoder
        );
        bootstrapService.bootstrapIfEnabled();

        assertThat(marker.getPlatformUserId()).isNull();
        assertThat(platformUserRepository.count()).isZero();
        assertThat(platformBootstrapStateRepository.count()).isEqualTo(1);
    }
}
