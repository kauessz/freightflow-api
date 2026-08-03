package com.freightflow.modules.platform.entitlement;

public enum EntitlementDenialReason {
    NONE,
    NO_SUBSCRIPTION,
    SUBSCRIPTION_SUSPENDED,
    INCONSISTENT_SUBSCRIPTION,
    FEATURE_NOT_GRANTED,
    FEATURE_NOT_EFFECTIVE,
    FEATURE_NOT_FOUND
}
