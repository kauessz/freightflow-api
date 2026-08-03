package com.freightflow.modules.platform.entitlement;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.freightflow.modules.platform.catalog.PlatformFeatureImplementationStatus;
import com.freightflow.modules.platform.catalog.PlatformFeatureValueType;
import com.freightflow.shared.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EntitlementEnforcementService")
class EntitlementEnforcementServiceTest {

    private static final UUID TENANT_ID = UUID.randomUUID();

    @Mock
    private TenantEntitlementResolverService tenantEntitlementResolverService;

    private EntitlementEnforcementProperties properties;
    private EntitlementEnforcementService service;

    @BeforeEach
    void setUp() {
        properties = new EntitlementEnforcementProperties();
        service = new EntitlementEnforcementService(tenantEntitlementResolverService, properties);
    }

    @Test
    @DisplayName("disabledPermitePorRolloutMesmoSemSubscription")
    void disabledPermitePorRolloutMesmoSemSubscription() {
        properties.setEnforcementMode(EntitlementEnforcementMode.DISABLED);
        when(tenantEntitlementResolverService.resolveTenantEntitlements(TENANT_ID))
                .thenReturn(resolution(TenantEntitlementAccessStatus.NO_SUBSCRIPTION, List.of()));

        EntitlementDecision decision = service.check(TENANT_ID, "commercial_rfq");

        assertThat(decision.featureKey()).isEqualTo("COMMERCIAL_RFQ");
        assertThat(decision.entitled()).isFalse();
        assertThat(decision.allowedByRollout()).isTrue();
        assertThat(decision.allowed()).isTrue();
        assertThat(decision.denialReason()).isEqualTo(EntitlementDenialReason.NO_SUBSCRIPTION);
    }

    @Test
    @DisplayName("auditNaoBloqueiaMasRegistraNegacaoReal")
    void auditNaoBloqueiaMasRegistraNegacaoReal() {
        properties.setEnforcementMode(EntitlementEnforcementMode.AUDIT);
        when(tenantEntitlementResolverService.resolveTenantEntitlements(TENANT_ID))
                .thenReturn(resolution(
                        TenantEntitlementAccessStatus.ACTIVE,
                        List.of(feature("COMMERCIAL_RFQ", false, false))
                ));

        Logger logger = (Logger) LoggerFactory.getLogger(EntitlementEnforcementService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            EntitlementDecision decision = service.check(TENANT_ID, "COMMERCIAL_RFQ");

            assertThat(decision.allowed()).isTrue();
            assertThat(decision.allowedByRollout()).isTrue();
            assertThat(decision.denialReason()).isEqualTo(EntitlementDenialReason.FEATURE_NOT_GRANTED);
            assertThat(appender.list).hasSize(1);
            assertThat(appender.list.getFirst().getFormattedMessage())
                    .contains(TENANT_ID.toString(), "COMMERCIAL_RFQ", "FEATURE_NOT_GRANTED", "AUDIT")
                    .doesNotContain("@");
        } finally {
            logger.detachAppender(appender);
        }
    }

    @Test
    @DisplayName("enforcePermiteQuandoFeatureEstaEfetiva")
    void enforcePermiteQuandoFeatureEstaEfetiva() {
        properties.setEnforcementMode(EntitlementEnforcementMode.ENFORCE);
        when(tenantEntitlementResolverService.resolveTenantEntitlements(TENANT_ID))
                .thenReturn(resolution(
                        TenantEntitlementAccessStatus.ACTIVE,
                        List.of(feature("COMMERCIAL_RFQ", true, true))
                ));

        EntitlementDecision decision = service.check(TENANT_ID, "COMMERCIAL_RFQ");

        assertThat(decision.entitled()).isTrue();
        assertThat(decision.allowed()).isTrue();
        assertThat(decision.allowedByRollout()).isFalse();
        assertThat(decision.denialReason()).isEqualTo(EntitlementDenialReason.NONE);
    }

    @Test
    @DisplayName("checkAllPermiteQuandoTodasAsFeaturesEstaoEfetivasComUmaResolucao")
    void checkAllPermiteQuandoTodasAsFeaturesEstaoEfetivasComUmaResolucao() {
        properties.setEnforcementMode(EntitlementEnforcementMode.ENFORCE);
        when(tenantEntitlementResolverService.resolveTenantEntitlements(TENANT_ID))
                .thenReturn(resolution(
                        TenantEntitlementAccessStatus.ACTIVE,
                        List.of(
                                feature("COMMERCIAL_RFQ", true, true),
                                feature("QUOTATION_WORKFLOW", true, true)
                        )
                ));

        EntitlementBatchDecision decision = service.checkAll(
                TENANT_ID,
                List.of(" quotation_workflow ", "commercial_rfq")
        );

        assertThat(decision.featureKeys()).containsExactly("COMMERCIAL_RFQ", "QUOTATION_WORKFLOW");
        assertThat(decision.decisions()).hasSize(2);
        assertThat(decision.entitled()).isTrue();
        assertThat(decision.allowed()).isTrue();
        assertThat(decision.allowedByRollout()).isFalse();
        assertThat(decision.firstDeniedFeatureKey()).isNull();
        verify(tenantEntitlementResolverService, times(1)).resolveTenantEntitlements(TENANT_ID);
    }

    @Test
    @DisplayName("checkAllDeduplicaOrdenaEDefinePrimeiraNegacaoDeterministica")
    void checkAllDeduplicaOrdenaEDefinePrimeiraNegacaoDeterministica() {
        properties.setEnforcementMode(EntitlementEnforcementMode.ENFORCE);
        when(tenantEntitlementResolverService.resolveTenantEntitlements(TENANT_ID))
                .thenReturn(resolution(
                        TenantEntitlementAccessStatus.ACTIVE,
                        List.of(
                                feature("COMMERCIAL_RFQ", true, true),
                                feature("QUOTATION_WORKFLOW", false, false)
                        )
                ));

        EntitlementBatchDecision decision = service.checkAll(
                TENANT_ID,
                List.of("quotation_workflow", "COMMERCIAL_RFQ", " quotation_workflow ")
        );

        assertThat(decision.featureKeys()).containsExactly("COMMERCIAL_RFQ", "QUOTATION_WORKFLOW");
        assertThat(decision.decisions()).extracting(EntitlementDecision::featureKey)
                .containsExactly("COMMERCIAL_RFQ", "QUOTATION_WORKFLOW");
        assertThat(decision.decisions()).extracting(EntitlementDecision::entitled)
                .containsExactly(true, false);
        assertThat(decision.entitled()).isFalse();
        assertThat(decision.allowed()).isFalse();
        assertThat(decision.allowedByRollout()).isFalse();
        assertThat(decision.firstDeniedFeatureKey()).isEqualTo("QUOTATION_WORKFLOW");
        verify(tenantEntitlementResolverService, times(1)).resolveTenantEntitlements(TENANT_ID);
    }

    @Test
    @DisplayName("checkAllEmAuditRegistraCadaFeatureNegadaUmaVez")
    void checkAllEmAuditRegistraCadaFeatureNegadaUmaVez() {
        properties.setEnforcementMode(EntitlementEnforcementMode.AUDIT);
        when(tenantEntitlementResolverService.resolveTenantEntitlements(TENANT_ID))
                .thenReturn(resolution(
                        TenantEntitlementAccessStatus.ACTIVE,
                        List.of(
                                feature("COMMERCIAL_RFQ", false, false),
                                feature("QUOTATION_WORKFLOW", true, false)
                        )
                ));

        Logger logger = (Logger) LoggerFactory.getLogger(EntitlementEnforcementService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            EntitlementBatchDecision decision = service.checkAll(
                    TENANT_ID,
                    new LinkedHashSet<>(List.of("quotation_workflow", "COMMERCIAL_RFQ", "commercial_rfq"))
            );

            assertThat(decision.allowed()).isTrue();
            assertThat(decision.allowedByRollout()).isTrue();
            assertThat(decision.firstDeniedFeatureKey()).isEqualTo("COMMERCIAL_RFQ");
            assertThat(appender.list).hasSize(2);
            assertThat(appender.list.get(0).getFormattedMessage()).contains("COMMERCIAL_RFQ");
            assertThat(appender.list.get(1).getFormattedMessage()).contains("QUOTATION_WORKFLOW");
        } finally {
            logger.detachAppender(appender);
        }
    }

    @Test
    @DisplayName("enforceNegaSemSubscription")
    void enforceNegaSemSubscription() {
        properties.setEnforcementMode(EntitlementEnforcementMode.ENFORCE);
        when(tenantEntitlementResolverService.resolveTenantEntitlements(TENANT_ID))
                .thenReturn(resolution(TenantEntitlementAccessStatus.NO_SUBSCRIPTION, List.of()));

        EntitlementDecision decision = service.check(TENANT_ID, "COMMERCIAL_RFQ");

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.denialReason()).isEqualTo(EntitlementDenialReason.NO_SUBSCRIPTION);
    }

    @Test
    @DisplayName("enforceNegaSubscriptionSuspended")
    void enforceNegaSubscriptionSuspended() {
        properties.setEnforcementMode(EntitlementEnforcementMode.ENFORCE);
        when(tenantEntitlementResolverService.resolveTenantEntitlements(TENANT_ID))
                .thenReturn(resolution(
                        TenantEntitlementAccessStatus.SUSPENDED,
                        List.of(feature("COMMERCIAL_RFQ", true, false))
                ));

        EntitlementDecision decision = service.check(TENANT_ID, "COMMERCIAL_RFQ");

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.denialReason()).isEqualTo(EntitlementDenialReason.SUBSCRIPTION_SUSPENDED);
    }

    @Test
    @DisplayName("enforceNegaSubscriptionInconsistente")
    void enforceNegaSubscriptionInconsistente() {
        properties.setEnforcementMode(EntitlementEnforcementMode.ENFORCE);
        when(tenantEntitlementResolverService.resolveTenantEntitlements(TENANT_ID))
                .thenReturn(resolution(TenantEntitlementAccessStatus.INCONSISTENT_SUBSCRIPTION, List.of()));

        EntitlementDecision decision = service.check(TENANT_ID, "COMMERCIAL_RFQ");

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.denialReason()).isEqualTo(EntitlementDenialReason.INCONSISTENT_SUBSCRIPTION);
    }

    @Test
    @DisplayName("enforceNegaFeatureNaoConcedida")
    void enforceNegaFeatureNaoConcedida() {
        properties.setEnforcementMode(EntitlementEnforcementMode.ENFORCE);
        when(tenantEntitlementResolverService.resolveTenantEntitlements(TENANT_ID))
                .thenReturn(resolution(
                        TenantEntitlementAccessStatus.ACTIVE,
                        List.of(feature("COMMERCIAL_RFQ", false, false))
                ));

        EntitlementDecision decision = service.check(TENANT_ID, "COMMERCIAL_RFQ");

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.denialReason()).isEqualTo(EntitlementDenialReason.FEATURE_NOT_GRANTED);
    }

    @Test
    @DisplayName("enforceNegaFeatureNaoEfetiva")
    void enforceNegaFeatureNaoEfetiva() {
        properties.setEnforcementMode(EntitlementEnforcementMode.ENFORCE);
        when(tenantEntitlementResolverService.resolveTenantEntitlements(TENANT_ID))
                .thenReturn(resolution(
                        TenantEntitlementAccessStatus.ACTIVE,
                        List.of(feature("COMMERCIAL_RFQ", true, false))
                ));

        EntitlementDecision decision = service.check(TENANT_ID, "COMMERCIAL_RFQ");

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.denialReason()).isEqualTo(EntitlementDenialReason.FEATURE_NOT_EFFECTIVE);
    }

    @Test
    @DisplayName("featureInexistenteNaoGeraErroInterno")
    void featureInexistenteNaoGeraErroInterno() {
        properties.setEnforcementMode(EntitlementEnforcementMode.ENFORCE);
        when(tenantEntitlementResolverService.resolveTenantEntitlements(TENANT_ID))
                .thenReturn(resolution(
                        TenantEntitlementAccessStatus.ACTIVE,
                        List.of(feature("TRACKING", true, true))
                ));

        EntitlementDecision decision = service.check(TENANT_ID, "UNKNOWN_FEATURE");

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.denialReason()).isEqualTo(EntitlementDenialReason.FEATURE_NOT_FOUND);
    }

    @Test
    @DisplayName("featureKeyBlankEhRejeitada")
    void featureKeyBlankEhRejeitada() {
        assertThatThrownBy(() -> service.check(TENANT_ID, "   "))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("featureKey");
    }

    @Test
    @DisplayName("featureKeysVaziasSaoRejeitadas")
    void featureKeysVaziasSaoRejeitadas() {
        assertThatThrownBy(() -> service.checkAll(TENANT_ID, List.of()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("featureKeys");
    }

    @Test
    @DisplayName("requireEnabledLancaSomenteQuandoModoEfetivoBloqueia")
    void requireEnabledLancaSomenteQuandoModoEfetivoBloqueia() {
        when(tenantEntitlementResolverService.resolveTenantEntitlements(TENANT_ID))
                .thenReturn(resolution(TenantEntitlementAccessStatus.NO_SUBSCRIPTION, List.of()));

        properties.setEnforcementMode(EntitlementEnforcementMode.DISABLED);
        service.requireEnabled(TENANT_ID, "COMMERCIAL_RFQ");

        properties.setEnforcementMode(EntitlementEnforcementMode.AUDIT);
        service.requireEnabled(TENANT_ID, "COMMERCIAL_RFQ");

        properties.setEnforcementMode(EntitlementEnforcementMode.ENFORCE);
        assertThatThrownBy(() -> service.requireEnabled(TENANT_ID, "COMMERCIAL_RFQ"))
                .isInstanceOf(FeatureNotAvailableException.class)
                .hasMessage("This feature is not available for the current tenant.");
    }

    @Test
    @DisplayName("requireAllEnabledLancaSomentePrimeiraFeatureNegada")
    void requireAllEnabledLancaSomentePrimeiraFeatureNegada() {
        properties.setEnforcementMode(EntitlementEnforcementMode.ENFORCE);
        when(tenantEntitlementResolverService.resolveTenantEntitlements(TENANT_ID))
                .thenReturn(resolution(
                        TenantEntitlementAccessStatus.ACTIVE,
                        List.of(
                                feature("COMMERCIAL_RFQ", false, false),
                                feature("QUOTATION_WORKFLOW", true, false)
                        )
                ));

        assertThatThrownBy(() -> service.requireAllEnabled(
                TENANT_ID,
                List.of("QUOTATION_WORKFLOW", "COMMERCIAL_RFQ")
        )).isInstanceOf(FeatureNotAvailableException.class)
                .extracting("featureKey")
                .isEqualTo("COMMERCIAL_RFQ");
    }

    @Test
    @DisplayName("erroTecnicoDoResolverEhPropagado")
    void erroTecnicoDoResolverEhPropagado() {
        when(tenantEntitlementResolverService.resolveTenantEntitlements(TENANT_ID))
                .thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> service.checkAll(TENANT_ID, List.of("COMMERCIAL_RFQ", "QUOTATION_WORKFLOW")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");
    }

    @Test
    @DisplayName("startupFalhaComModoInvalido")
    void startupFalhaComModoInvalido() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
                .withUserConfiguration(EnforcementPropertiesConfig.class)
                .withPropertyValues("freightflow.entitlements.enforcement-mode=NOPE")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(IllegalArgumentException.class)
                            .hasRootCauseMessage("No enum constant com.freightflow.modules.platform.entitlement.EntitlementEnforcementMode.NOPE");
                });
    }

    private TenantEntitlementResolution resolution(TenantEntitlementAccessStatus accessStatus,
                                                   List<ResolvedFeatureEntitlement> features) {
        return new TenantEntitlementResolution(
                TENANT_ID,
                accessStatus,
                null,
                features,
                List.of(),
                Instant.parse("2026-08-02T12:00:00Z")
        );
    }

    private ResolvedFeatureEntitlement feature(String featureKey, boolean grantedByPlan, boolean effectiveEnabled) {
        return new ResolvedFeatureEntitlement(
                featureKey,
                featureKey,
                PlatformFeatureValueType.BOOLEAN,
                PlatformFeatureImplementationStatus.AVAILABLE,
                true,
                grantedByPlan,
                effectiveEnabled,
                null,
                false,
                List.of(),
                List.of(),
                List.of()
        );
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(EntitlementEnforcementProperties.class)
    static class EnforcementPropertiesConfig {
    }
}
