package com.freightflow.modules.voyage;

import com.freightflow.modules.port.PortController;
import com.freightflow.modules.vessel.VesselController;
import com.freightflow.shared.rbac.RequiresRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Master data controller roles")
class MasterDataControllerRoleTest {

    @Test
    @DisplayName("should_protect_masterDataWriteEndpoints_with_adminOrOperator")
    void should_protect_masterDataWriteEndpoints_with_adminOrOperator() throws Exception {
        assertThat(roles(PortController.class, "create")).containsExactly("ADMIN", "OPERATOR");
        assertThat(roles(PortController.class, "update")).containsExactly("ADMIN", "OPERATOR");
        assertThat(roles(VesselController.class, "create")).containsExactly("ADMIN", "OPERATOR");
        assertThat(roles(VesselController.class, "update")).containsExactly("ADMIN", "OPERATOR");
        assertThat(roles(VoyageController.class, "create")).containsExactly("ADMIN", "OPERATOR");
        assertThat(roles(VoyageController.class, "update")).containsExactly("ADMIN", "OPERATOR");
    }

    @Test
    @DisplayName("should_allow_client_only_on_readinessAndFleetMapReads")
    void should_allow_client_only_on_readinessAndFleetMapReads() throws Exception {
        assertThat(roles(VesselController.class, "getActiveWithShipments")).contains("CLIENT");
        assertThat(roles(VoyageController.class, "listFleetMapReadiness")).contains("CLIENT");
        assertThat(roles(PortController.class, "create")).doesNotContain("CLIENT");
        assertThat(roles(VesselController.class, "create")).doesNotContain("CLIENT");
        assertThat(roles(VoyageController.class, "create")).doesNotContain("CLIENT");
    }

    private String[] roles(Class<?> type, String methodName) throws Exception {
        Method method = Arrays.stream(type.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .findFirst()
                .orElseThrow();
        return method.getAnnotation(RequiresRole.class).value();
    }
}
