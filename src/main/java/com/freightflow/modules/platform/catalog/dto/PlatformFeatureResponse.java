package com.freightflow.modules.platform.catalog.dto;

import com.freightflow.modules.platform.catalog.PlatformFeatureImplementationStatus;
import com.freightflow.modules.platform.catalog.PlatformFeatureValueType;

import java.util.List;

public record PlatformFeatureResponse(
        String key,
        String name,
        String description,
        PlatformFeatureValueType valueType,
        String unit,
        boolean active,
        PlatformFeatureImplementationStatus implementationStatus,
        List<String> dependencies
) {
}
