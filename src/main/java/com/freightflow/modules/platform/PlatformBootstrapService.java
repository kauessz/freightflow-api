package com.freightflow.modules.platform;

import com.freightflow.config.PlatformBootstrapProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Locale;

@Service
public class PlatformBootstrapService {

    static final String INITIAL_PLATFORM_ADMIN_BOOTSTRAP = "INITIAL_PLATFORM_ADMIN";

    private static final Logger log = LoggerFactory.getLogger(PlatformBootstrapService.class);

    private final PlatformBootstrapProperties properties;
    private final PlatformUserRepository platformUserRepository;
    private final PlatformBootstrapStateRepository platformBootstrapStateRepository;
    private final PasswordEncoder passwordEncoder;

    public PlatformBootstrapService(PlatformBootstrapProperties properties,
                                    PlatformUserRepository platformUserRepository,
                                    PlatformBootstrapStateRepository platformBootstrapStateRepository,
                                    PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.platformUserRepository = platformUserRepository;
        this.platformBootstrapStateRepository = platformBootstrapStateRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void bootstrapIfEnabled() {
        if (!properties.isEnabled()) {
            return;
        }

        if (!StringUtils.hasText(properties.getEmail()) || !StringUtils.hasText(properties.getPassword())) {
            throw new IllegalStateException("Platform bootstrap is enabled but email/password were not provided.");
        }

        if (platformBootstrapStateRepository.existsById(INITIAL_PLATFORM_ADMIN_BOOTSTRAP)) {
            log.info("Platform bootstrap skipped because the one-time bootstrap was already completed.");
            return;
        }

        String normalizedEmail = properties.getEmail().trim().toLowerCase(Locale.ROOT);
        PlatformUser existingUser = platformUserRepository.findFirstByOrderByCreatedAtAsc().orElse(null);
        if (existingUser != null) {
            persistBootstrapMarker(existingUser.getId());
            log.info("Platform bootstrap skipped because platform users already exist.");
            return;
        }

        PlatformUser platformUser = new PlatformUser(
                normalizedEmail,
                passwordEncoder.encode(properties.getPassword()),
                PlatformRole.PLATFORM_ADMIN,
                PlatformUserStatus.ACTIVE
        );
        PlatformUser savedUser = platformUserRepository.save(platformUser);
        persistBootstrapMarker(savedUser.getId());
        log.info("Platform bootstrap created the first platform administrator for {}", normalizedEmail);
    }

    private void persistBootstrapMarker(java.util.UUID platformUserId) {
        platformBootstrapStateRepository.save(new PlatformBootstrapState(
                INITIAL_PLATFORM_ADMIN_BOOTSTRAP,
                Instant.now(),
                platformUserId
        ));
    }
}
