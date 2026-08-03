package com.freightflow.modules.platform.entitlement;

public class FeatureNotAvailableException extends RuntimeException {

    private final String featureKey;

    public FeatureNotAvailableException(String featureKey) {
        super("This feature is not available for the current tenant.");
        this.featureKey = featureKey;
    }

    public String getFeatureKey() {
        return featureKey;
    }
}
