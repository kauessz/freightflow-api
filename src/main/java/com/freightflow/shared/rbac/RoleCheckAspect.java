package com.freightflow.shared.rbac;

import com.freightflow.shared.exception.ForbiddenException;
import com.freightflow.shared.security.UserPrincipal;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * AOP interceptor that enforces @RequiresRole access control.
 *
 * Precedence:
 *  1. Method-level @RequiresRole overrides class-level
 *  2. If neither is present this aspect is a no-op
 */
@Aspect
@Component
public class RoleCheckAspect {

    private static final Logger log = LoggerFactory.getLogger(RoleCheckAspect.class);

    @Around("@within(com.freightflow.shared.rbac.RequiresRole) || " +
            "@annotation(com.freightflow.shared.rbac.RequiresRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {

        RequiresRole annotation = resolveAnnotation(joinPoint);
        if (annotation == null) {
            return joinPoint.proceed();
        }

        List<String> requiredRoles = Arrays.asList(annotation.value());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            log.warn("RBAC: unauthenticated access attempt to {}", joinPoint.getSignature());
            throw new ForbiddenException("Authentication required");
        }

        String userRole = principal.getRole();
        if (!requiredRoles.contains(userRole)) {
            log.warn("RBAC: user {} (role={}) denied access to {} — required: {}",
                    principal.getEmail(), userRole, joinPoint.getSignature(), requiredRoles);
            throw new ForbiddenException("Insufficient permissions. Required role(s): " + requiredRoles);
        }

        return joinPoint.proceed();
    }

    /**
     * Method-level annotation takes precedence over class-level.
     */
    private RequiresRole resolveAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        RequiresRole methodAnnotation = method.getAnnotation(RequiresRole.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }

        return joinPoint.getTarget().getClass().getAnnotation(RequiresRole.class);
    }
}
