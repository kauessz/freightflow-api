package com.freightflow.shared.rbac;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restricts endpoint access to users with at least one of the listed roles.
 *
 * Usage:
 *   @RequiresRole({"ADMIN", "OPERATOR"})
 *   @RequiresRole("ADMIN")
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {
    String[] value();
}
