package com.freightflow.shared.security.platform;

import com.freightflow.modules.platform.PlatformUser;
import com.freightflow.modules.platform.PlatformUserRepository;
import com.freightflow.modules.platform.PlatformUserStatus;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class PlatformAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(PlatformAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final PlatformJwtService platformJwtService;
    private final PlatformUserRepository platformUserRepository;

    public PlatformAuthenticationFilter(PlatformJwtService platformJwtService,
                                        PlatformUserRepository platformUserRepository) {
        this.platformJwtService = platformJwtService;
        this.platformUserRepository = platformUserRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = extractTokenFromRequest(request);

        if (token != null) {
            Claims claims = platformJwtService.validateAndGetClaims(token);
            if (claims != null) {
                PlatformPrincipal principal = platformJwtService.getPrincipalFromClaims(claims);
                if (principal != null) {
                    PlatformUser currentUser = platformUserRepository.findById(principal.getId()).orElse(null);
                    if (currentUser != null
                            && currentUser.getStatus() == PlatformUserStatus.ACTIVE
                            && currentUser.getRole().name().equals(principal.getRole())
                            && currentUser.getEmail().equals(principal.getEmail())) {
                        var authentication = new UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("Authenticated platform user: {}", principal.getEmail());
                    } else {
                        SecurityContextHolder.clearContext();
                        log.warn("Rejected platform JWT for inactive, removed or mismatched platform user");
                    }
                } else {
                    SecurityContextHolder.clearContext();
                }
            } else {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
