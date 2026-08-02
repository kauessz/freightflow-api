package com.freightflow.modules.platform.subscription;

import com.freightflow.AbstractIntegrationTest;
import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Platform tenant subscription integration")
class PlatformTenantSubscriptionIntegrationTest extends AbstractIntegrationTest {

    private static final UUID STARTER_PLAN_ID = UUID.fromString("0fbe6f06-6416-4c8a-9382-6a5ff1a4a101");
    private static final UUID PROFESSIONAL_PLAN_ID = UUID.fromString("3f7a13e2-1cf4-45c0-a5f2-3ff7d9bdb102");

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private TenantRepository tenantRepository;

    @Test
    @DisplayName("v29 sobe em postgresql e cria tabelas")
    void v29SobeEmPostgresqlECriaTabelas() {
        Integer subscriptions = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_name = 'tenant_subscriptions'",
                Integer.class
        );
        Integer events = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_name = 'tenant_subscription_events'",
                Integer.class
        );

        assertThat(subscriptions).isEqualTo(1);
        assertThat(events).isEqualTo(1);
    }

    @Test
    @DisplayName("fks e unique parcial one open sao aplicados")
    void fksEUniqueParcialOneOpenSaoAplicados() {
        Tenant tenant = tenantRepository.saveAndFlush(new Tenant("Tenant One", "tenant-one", "admin@tenant.com", "LEGACY"));
        Timestamp now = Timestamp.from(Instant.parse("2026-08-01T12:00:00Z"));
        UUID subscriptionId = UUID.randomUUID();

        jdbcTemplate.update("""
                insert into tenant_subscriptions (id, tenant_id, plan_id, status, started_at, ended_at, reason, internal_notes, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                subscriptionId, tenant.getId(), STARTER_PLAN_ID, "ACTIVE", now, null, "Initial", null, now, now
        );

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into tenant_subscriptions (id, tenant_id, plan_id, status, started_at, ended_at, reason, internal_notes, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(), tenant.getId(), PROFESSIONAL_PLAN_ID, "SUSPENDED", now, null, "Blocked", null, now, now
        )).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into tenant_subscriptions (id, tenant_id, plan_id, status, started_at, ended_at, reason, internal_notes, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(), tenant.getId(), PROFESSIONAL_PLAN_ID, "ACTIVE", now, null, "Duplicate", null, now, now
        )).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into tenant_subscriptions (id, tenant_id, plan_id, status, started_at, ended_at, reason, internal_notes, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(), UUID.randomUUID(), STARTER_PLAN_ID, "ACTIVE", now, null, "Invalid tenant", null, now, now
        )).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into tenant_subscriptions (id, tenant_id, plan_id, status, started_at, ended_at, reason, internal_notes, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(), tenant.getId(), UUID.randomUUID(), "ACTIVE", now, null, "Invalid plan", null, now, now
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("checks de status event type datas e reason sao aplicados")
    void checksDeStatusEventTypeDatasEReasonSaoAplicados() {
        Tenant tenant = tenantRepository.saveAndFlush(new Tenant("Tenant Two", "tenant-two", "ops@tenant.com", "LEGACY"));
        Timestamp now = Timestamp.from(Instant.parse("2026-08-01T13:00:00Z"));
        UUID subscriptionId = UUID.randomUUID();

        jdbcTemplate.update("""
                insert into tenant_subscriptions (id, tenant_id, plan_id, status, started_at, ended_at, reason, internal_notes, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                subscriptionId, tenant.getId(), STARTER_PLAN_ID, "ACTIVE", now, null, "Initial", null, now, now
        );

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into tenant_subscriptions (id, tenant_id, plan_id, status, started_at, ended_at, reason, internal_notes, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(), tenant.getId(), STARTER_PLAN_ID, "INVALID", now, null, "Invalid", null, now, now
        )).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into tenant_subscriptions (id, tenant_id, plan_id, status, started_at, ended_at, reason, internal_notes, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(), tenant.getId(), STARTER_PLAN_ID, "CANCELLED", now, Timestamp.from(Instant.parse("2026-08-01T11:59:59Z")), "Invalid", null, now, now
        )).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into tenant_subscriptions (id, tenant_id, plan_id, status, started_at, ended_at, reason, internal_notes, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(), tenant.getId(), STARTER_PLAN_ID, "CANCELLED", now, now, "   ", null, now, now
        )).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into tenant_subscription_events (id, tenant_subscription_id, tenant_id, event_type, previous_plan_id, new_plan_id, previous_status, new_status, reason, created_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(), subscriptionId, tenant.getId(), "INVALID_EVENT", STARTER_PLAN_ID, PROFESSIONAL_PLAN_ID, "ACTIVE", "ACTIVE", "Invalid", now
        )).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into tenant_subscription_events (id, tenant_subscription_id, tenant_id, event_type, previous_plan_id, new_plan_id, previous_status, new_status, reason, created_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(), subscriptionId, tenant.getId(), "PLAN_CHANGED", STARTER_PLAN_ID, PROFESSIONAL_PLAN_ID, "INVALID", "ACTIVE", "Invalid", now
        )).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into tenant_subscription_events (id, tenant_subscription_id, tenant_id, event_type, previous_plan_id, new_plan_id, previous_status, new_status, reason, created_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(), subscriptionId, tenant.getId(), "PLAN_CHANGED", STARTER_PLAN_ID, PROFESSIONAL_PLAN_ID, "ACTIVE", "ACTIVE", "   ", now
        )).isInstanceOf(DataIntegrityViolationException.class);
    }
}
