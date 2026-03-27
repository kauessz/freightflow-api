package com.freightflow.config;

import com.freightflow.shared.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.mockito.Mockito;
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
