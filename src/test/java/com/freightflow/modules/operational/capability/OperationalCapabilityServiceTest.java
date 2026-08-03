package com.freightflow.modules.operational.capability;

import com.freightflow.modules.platform.entitlement.EntitlementBatchDecision;
import com.freightflow.modules.platform.entitlement.EntitlementDecision;
import com.freightflow.modules.platform.entitlement.EntitlementDenialReason;
import com.freightflow.modules.platform.entitlement.EntitlementEnforcementMode;
import com.freightflow.modules.platform.entitlement.EntitlementEnforcementService;
import com.freightflow.modules.platform.entitlement.TenantEntitlementAccessStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OperationalCapabilityService")
class OperationalCapabilityServiceTest {

    private static final UUID TENANT_ID = UUID.randomUUID();

    @Mock
    private EntitlementEnforcementService entitlementEnforcementService;

    private OperationalCapabilityService service;

    @BeforeEach
    void setUp() {
        service = new OperationalCapabilityService(entitlementEnforcementService);
    }

    @Test
    @DisplayName("expõeSomenteAsTresKeysEmOrdemDeterministicaEComUmaResolucao")
    void expoeSomenteAsTresKeysEmOrdemDeterministicaEComUmaResolucao() {
        when(entitlementEnforcementService.inspectAll(TENANT_ID, OperationalCapabilityService.EXPOSED_CAPABILITY_KEYS))
                .thenReturn(batchDecision(
                        EntitlementEnforcementMode.ENFORCE,
                        decision("CLIENT_PORTAL", false, TenantEntitlementAccessStatus.ACTIVE, EntitlementDenialReason.FEATURE_NOT_GRANTED),
                        decision("COMMERCIAL_RFQ", true, TenantEntitlementAccessStatus.ACTIVE, EntitlementDenialReason.NONE),
                        decision("QUOTATION_WORKFLOW", false, TenantEntitlementAccessStatus.ACTIVE, EntitlementDenialReason.FEATURE_NOT_EFFECTIVE)
                ));

        OperationalCapabilitySnapshot snapshot = service.getCapabilities(TENANT_ID);

        assertThat(OperationalCapabilityService.EXPOSED_CAPABILITY_KEYS)
                .containsExactly("CLIENT_PORTAL", "COMMERCIAL_RFQ", "QUOTATION_WORKFLOW");
        assertThat(snapshot.capabilities()).extracting(OperationalCapabilityAvailability::key)
                .containsExactly("CLIENT_PORTAL", "COMMERCIAL_RFQ", "QUOTATION_WORKFLOW");
        assertThat(snapshot.capabilities()).extracting(OperationalCapabilityAvailability::available)
                .containsExactly(false, true, false);
        verify(entitlementEnforcementService, times(1))
                .inspectAll(TENANT_ID, OperationalCapabilityService.EXPOSED_CAPABILITY_KEYS);
    }

    @Test
    @DisplayName("disabledRetornaTodasAsCapabilitiesComoDisponiveis")
    void disabledRetornaTodasAsCapabilitiesComoDisponiveis() {
        when(entitlementEnforcementService.inspectAll(TENANT_ID, OperationalCapabilityService.EXPOSED_CAPABILITY_KEYS))
                .thenReturn(batchDecision(
                        EntitlementEnforcementMode.DISABLED,
                        decision("CLIENT_PORTAL", true, TenantEntitlementAccessStatus.NO_SUBSCRIPTION, EntitlementDenialReason.NO_SUBSCRIPTION),
                        decision("COMMERCIAL_RFQ", true, TenantEntitlementAccessStatus.NO_SUBSCRIPTION, EntitlementDenialReason.NO_SUBSCRIPTION),
                        decision("QUOTATION_WORKFLOW", true, TenantEntitlementAccessStatus.NO_SUBSCRIPTION, EntitlementDenialReason.NO_SUBSCRIPTION)
                ));

        OperationalCapabilitySnapshot snapshot = service.getCapabilities(TENANT_ID);

        assertThat(snapshot.capabilities()).extracting(OperationalCapabilityAvailability::available)
                .containsExactly(true, true, true);
    }

    @Test
    @DisplayName("auditRetornaTodasAsCapabilitiesComoDisponiveis")
    void auditRetornaTodasAsCapabilitiesComoDisponiveis() {
        when(entitlementEnforcementService.inspectAll(TENANT_ID, OperationalCapabilityService.EXPOSED_CAPABILITY_KEYS))
                .thenReturn(batchDecision(
                        EntitlementEnforcementMode.AUDIT,
                        decision("CLIENT_PORTAL", true, TenantEntitlementAccessStatus.NO_SUBSCRIPTION, EntitlementDenialReason.NO_SUBSCRIPTION),
                        decision("COMMERCIAL_RFQ", true, TenantEntitlementAccessStatus.NO_SUBSCRIPTION, EntitlementDenialReason.NO_SUBSCRIPTION),
                        decision("QUOTATION_WORKFLOW", true, TenantEntitlementAccessStatus.NO_SUBSCRIPTION, EntitlementDenialReason.NO_SUBSCRIPTION)
                ));

        OperationalCapabilitySnapshot snapshot = service.getCapabilities(TENANT_ID);

        assertThat(snapshot.capabilities()).extracting(OperationalCapabilityAvailability::available)
                .containsExactly(true, true, true);
    }

    @Test
    @DisplayName("enforceMapeiaMatrizRealPorFeature")
    void enforceMapeiaMatrizRealPorFeature() {
        when(entitlementEnforcementService.inspectAll(TENANT_ID, OperationalCapabilityService.EXPOSED_CAPABILITY_KEYS))
                .thenReturn(batchDecision(
                        EntitlementEnforcementMode.ENFORCE,
                        decision("CLIENT_PORTAL", false, TenantEntitlementAccessStatus.ACTIVE, EntitlementDenialReason.FEATURE_NOT_GRANTED),
                        decision("COMMERCIAL_RFQ", true, TenantEntitlementAccessStatus.ACTIVE, EntitlementDenialReason.NONE),
                        decision("QUOTATION_WORKFLOW", false, TenantEntitlementAccessStatus.ACTIVE, EntitlementDenialReason.FEATURE_NOT_EFFECTIVE)
                ));

        OperationalCapabilitySnapshot snapshot = service.getCapabilities(TENANT_ID);

        assertThat(snapshot.capabilities()).extracting(OperationalCapabilityAvailability::available)
                .containsExactly(false, true, false);
    }

    @Test
    @DisplayName("enforceSemSubscriptionRetornaTodasComoFalse")
    void enforceSemSubscriptionRetornaTodasComoFalse() {
        when(entitlementEnforcementService.inspectAll(TENANT_ID, OperationalCapabilityService.EXPOSED_CAPABILITY_KEYS))
                .thenReturn(batchDecision(
                        EntitlementEnforcementMode.ENFORCE,
                        decision("CLIENT_PORTAL", false, TenantEntitlementAccessStatus.NO_SUBSCRIPTION, EntitlementDenialReason.NO_SUBSCRIPTION),
                        decision("COMMERCIAL_RFQ", false, TenantEntitlementAccessStatus.NO_SUBSCRIPTION, EntitlementDenialReason.NO_SUBSCRIPTION),
                        decision("QUOTATION_WORKFLOW", false, TenantEntitlementAccessStatus.NO_SUBSCRIPTION, EntitlementDenialReason.NO_SUBSCRIPTION)
                ));

        OperationalCapabilitySnapshot snapshot = service.getCapabilities(TENANT_ID);

        assertThat(snapshot.capabilities()).extracting(OperationalCapabilityAvailability::available)
                .containsExactly(false, false, false);
    }

    @Test
    @DisplayName("subscriptionSuspendedRetornaTodasComoFalse")
    void subscriptionSuspendedRetornaTodasComoFalse() {
        when(entitlementEnforcementService.inspectAll(TENANT_ID, OperationalCapabilityService.EXPOSED_CAPABILITY_KEYS))
                .thenReturn(batchDecision(
                        EntitlementEnforcementMode.ENFORCE,
                        decision("CLIENT_PORTAL", false, TenantEntitlementAccessStatus.SUSPENDED, EntitlementDenialReason.SUBSCRIPTION_SUSPENDED),
                        decision("COMMERCIAL_RFQ", false, TenantEntitlementAccessStatus.SUSPENDED, EntitlementDenialReason.SUBSCRIPTION_SUSPENDED),
                        decision("QUOTATION_WORKFLOW", false, TenantEntitlementAccessStatus.SUSPENDED, EntitlementDenialReason.SUBSCRIPTION_SUSPENDED)
                ));

        OperationalCapabilitySnapshot snapshot = service.getCapabilities(TENANT_ID);

        assertThat(snapshot.capabilities()).extracting(OperationalCapabilityAvailability::available)
                .containsExactly(false, false, false);
    }

    @Test
    @DisplayName("dependenciaQuebradaDesabilitaSomenteAsFeaturesDependentes")
    void dependenciaQuebradaDesabilitaSomenteAsFeaturesDependentes() {
        when(entitlementEnforcementService.inspectAll(TENANT_ID, OperationalCapabilityService.EXPOSED_CAPABILITY_KEYS))
                .thenReturn(batchDecision(
                        EntitlementEnforcementMode.ENFORCE,
                        decision("CLIENT_PORTAL", false, TenantEntitlementAccessStatus.ACTIVE, EntitlementDenialReason.FEATURE_NOT_EFFECTIVE),
                        decision("COMMERCIAL_RFQ", true, TenantEntitlementAccessStatus.ACTIVE, EntitlementDenialReason.NONE),
                        decision("QUOTATION_WORKFLOW", false, TenantEntitlementAccessStatus.ACTIVE, EntitlementDenialReason.FEATURE_NOT_EFFECTIVE)
                ));

        OperationalCapabilitySnapshot snapshot = service.getCapabilities(TENANT_ID);

        assertThat(snapshot.capabilities()).extracting(OperationalCapabilityAvailability::available)
                .containsExactly(false, true, false);
    }

    @Test
    @DisplayName("erroTecnicoRealEhPropagado")
    void erroTecnicoRealEhPropagado() {
        when(entitlementEnforcementService.inspectAll(TENANT_ID, OperationalCapabilityService.EXPOSED_CAPABILITY_KEYS))
                .thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> service.getCapabilities(TENANT_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");
    }

    private EntitlementBatchDecision batchDecision(EntitlementEnforcementMode mode,
                                                   EntitlementDecision... decisions) {
        List<EntitlementDecision> decisionList = List.of(decisions);
        boolean entitled = decisionList.stream().allMatch(EntitlementDecision::entitled);
        boolean allowedByRollout = !entitled && mode != EntitlementEnforcementMode.ENFORCE;
        boolean allowed = entitled || allowedByRollout;
        String firstDenied = decisionList.stream()
                .filter(decision -> !decision.entitled())
                .map(EntitlementDecision::featureKey)
                .findFirst()
                .orElse(null);

        return new EntitlementBatchDecision(
                TENANT_ID,
                mode,
                OperationalCapabilityService.EXPOSED_CAPABILITY_KEYS,
                decisionList,
                entitled,
                allowedByRollout,
                allowed,
                firstDenied
        );
    }

    private EntitlementDecision decision(String featureKey,
                                         boolean allowed,
                                         TenantEntitlementAccessStatus accessStatus,
                                         EntitlementDenialReason denialReason) {
        boolean entitled = allowed && denialReason == EntitlementDenialReason.NONE;
        boolean allowedByRollout = allowed && !entitled;

        return new EntitlementDecision(
                TENANT_ID,
                featureKey,
                allowed ? (allowedByRollout ? EntitlementEnforcementMode.AUDIT : EntitlementEnforcementMode.ENFORCE) : EntitlementEnforcementMode.ENFORCE,
                entitled,
                allowedByRollout,
                allowed,
                accessStatus,
                denialReason
        );
    }
}
