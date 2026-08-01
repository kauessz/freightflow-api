package com.freightflow.config;

import com.freightflow.modules.platform.PlatformUserRepository;
import com.freightflow.shared.security.JwtAuthenticationFilter;
import com.freightflow.shared.security.JwtTokenProvider;
import com.freightflow.shared.security.platform.PlatformAuthenticationFilter;
import com.freightflow.shared.security.platform.PlatformJwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.mockito.Mockito;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security config simplificada para testes @WebMvcTest.
 * Replica as regras do SecurityConfig real sem depender de beans
 * como JwtAuthenticationFilter, CorsProperties, etc.
 *
 * Também provê um mock de JwtTokenProvider para satisfazer a dependência
 * do JwtAuthenticationFilter, que é um @Component detectado pelo scan do @WebMvcTest.
 */
@TestConfiguration
public class TestSecurityConfig {

    /**
     * Mock de JwtTokenProvider necessário porque o @WebMvcTest carrega o
     * JwtAuthenticationFilter (@Component), que injeta JwtTokenProvider no construtor.
     * Sem esse bean, o ApplicationContext falha com UnsatisfiedDependencyException.
     */
    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        return Mockito.mock(JwtTokenProvider.class);
    }

    @Bean
    public PlatformJwtService platformJwtService() {
        return Mockito.mock(PlatformJwtService.class);
    }

    @Bean
    public PlatformUserRepository platformUserRepository() {
        return Mockito.mock(PlatformUserRepository.class);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }

    @Bean
    public PlatformAuthenticationFilter platformAuthenticationFilter(
            PlatformJwtService platformJwtService,
            PlatformUserRepository platformUserRepository) {
        return new PlatformAuthenticationFilter(platformJwtService, platformUserRepository);
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(jwtAuthenticationFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<PlatformAuthenticationFilter> platformAuthenticationFilterRegistration(
            PlatformAuthenticationFilter platformAuthenticationFilter) {
        FilterRegistrationBean<PlatformAuthenticationFilter> registration =
                new FilterRegistrationBean<>(platformAuthenticationFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN))
                )

                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos (mesmas regras do SecurityConfig real)
                        .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/api/v1/platform/auth/login").permitAll()
                        .requestMatchers("/api/v1/tracking/**").permitAll()
                        .requestMatchers("/api/v1/billing/webhook").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api-docs/**", "/api-docs").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // Tudo mais requer autenticação
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
