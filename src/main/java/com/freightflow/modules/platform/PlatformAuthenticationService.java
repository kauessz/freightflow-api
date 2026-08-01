package com.freightflow.modules.platform;

import com.freightflow.config.PlatformJwtProperties;
import com.freightflow.modules.platform.dto.PlatformAuthResponse;
import com.freightflow.modules.platform.dto.PlatformLoginRequest;
import com.freightflow.modules.platform.dto.PlatformMeResponse;
import com.freightflow.shared.exception.ResourceNotFoundException;
import com.freightflow.shared.exception.UnauthorizedException;
import com.freightflow.shared.security.platform.PlatformJwtService;
import com.freightflow.shared.security.platform.PlatformPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PlatformAuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(PlatformAuthenticationService.class);

    private final PlatformUserRepository platformUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlatformJwtService platformJwtService;
    private final PlatformJwtProperties platformJwtProperties;

    public PlatformAuthenticationService(PlatformUserRepository platformUserRepository,
                                         PasswordEncoder passwordEncoder,
                                         PlatformJwtService platformJwtService,
                                         PlatformJwtProperties platformJwtProperties) {
        this.platformUserRepository = platformUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.platformJwtService = platformJwtService;
        this.platformJwtProperties = platformJwtProperties;
    }

    @Transactional
    public PlatformAuthResponse login(PlatformLoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        log.info("Platform login attempt for: {}", normalizedEmail);

        PlatformUser user = platformUserRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.getStatus() != PlatformUserStatus.ACTIVE) {
            throw new UnauthorizedException("Platform account is disabled. Contact another platform administrator.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Failed platform login attempt for: {}", normalizedEmail);
            throw new UnauthorizedException("Invalid email or password");
        }

        user.setLastLoginAt(Instant.now());
        platformUserRepository.save(user);

        PlatformPrincipal principal = new PlatformPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole().name(),
                user.getStatus().name()
        );
        String accessToken = platformJwtService.generateAccessToken(principal);

        return new PlatformAuthResponse(
                accessToken,
                platformJwtProperties.getExpirationMs() / 1000,
                new PlatformAuthResponse.PlatformUserInfo(
                        user.getId().toString(),
                        user.getEmail(),
                        user.getRole().name(),
                        user.getStatus().name(),
                        user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : null
                )
        );
    }

    public PlatformMeResponse me(UUID platformUserId) {
        PlatformUser user = platformUserRepository.findById(platformUserId)
                .orElseThrow(() -> new ResourceNotFoundException("PlatformUser", platformUserId));
        return PlatformMeResponse.from(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

