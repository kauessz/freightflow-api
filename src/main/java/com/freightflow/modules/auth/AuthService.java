package com.freightflow.modules.auth;

import com.freightflow.config.JwtProperties;
import com.freightflow.modules.auth.dto.AuthResponse;
import com.freightflow.modules.auth.dto.LoginRequest;
import com.freightflow.modules.auth.dto.RefreshRequest;
import com.freightflow.modules.auth.dto.RegisterRequest;
import com.freightflow.modules.auth.dto.UserResponse;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import com.freightflow.shared.exception.UnauthorizedException;
import com.freightflow.shared.security.JwtTokenProvider;
import com.freightflow.shared.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    public AuthService(UserRepository userRepository,
                       TenantRepository tenantRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }

    // ==================== Register ====================

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.email());

        // Verifica se email ja existe
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email " + request.email() + " is already registered");
        }

        // Cria tenant automaticamente no registro
        String slug = generateSlug(request.companyName());
        if (tenantRepository.existsBySlug(slug)) {
            throw new BusinessException("Company name '" + request.companyName() + "' is already taken");
        }

        Tenant tenant = new Tenant(request.companyName(), slug, request.email(), "FREE");
        tenant = tenantRepository.save(tenant);

        // Cria usuario como ADMIN do tenant
        String hashedPassword = passwordEncoder.encode(request.password());
        User user = new User(request.name(), request.email(), hashedPassword, User.UserRole.ADMIN, tenant);
        user = userRepository.save(user);

        log.info("User registered: id={}, email={}, tenant={}", user.getId(), user.getEmail(), tenant.getSlug());

        // Gera tokens
        return buildAuthResponse(user);
    }

    // ==================== Login ====================

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.email());

        User user = userRepository.findByEmailWithTenant(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is disabled. Contact your administrator.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Failed login attempt for: {}", request.email());
            throw new UnauthorizedException("Invalid email or password");
        }

        // Atualiza ultimo login
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        log.info("User logged in: id={}, email={}", user.getId(), user.getEmail());
        return buildAuthResponse(user);
    }

    // ==================== Refresh Token ====================

    public AuthResponse refresh(RefreshRequest request) {
        log.debug("Refreshing token");

        Claims claims = jwtTokenProvider.validateAndGetClaims(request.refreshToken());
        if (claims == null) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        // Verifica se eh um refresh token (tem claim "type": "refresh")
        String tokenType = claims.get("type", String.class);
        if (!"refresh".equals(tokenType)) {
            throw new UnauthorizedException("Token is not a refresh token");
        }

        UUID userId = UUID.fromString(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is disabled");
        }

        log.debug("Token refreshed for user: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    // ==================== Me (current user) ====================

    public UserResponse me(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return UserResponse.from(user);
    }

    // ==================== Helpers ====================

    private AuthResponse buildAuthResponse(User user) {
        UserPrincipal principal = new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getTenant().getId(),
                user.getRole().name()
        );

        String accessToken = jwtTokenProvider.generateAccessToken(principal);
        String refreshToken = jwtTokenProvider.generateRefreshToken(principal);

        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getTenant().getId().toString(),
                user.getTenant().getName()
        );

        return new AuthResponse(accessToken, refreshToken, jwtProperties.getExpirationMs() / 1000, userInfo);
    }

    /**
     * Gera um slug URL-friendly a partir do nome da empresa.
     * Ex: "Mercosul Line Ltda" -> "mercosul-line-ltda"
     */
    private String generateSlug(String companyName) {
        return companyName
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
    }
}
