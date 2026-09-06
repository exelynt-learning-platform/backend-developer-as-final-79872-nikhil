package com.example.booking.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    // =========================================================
    // AUTHENTICATION MANAGER
    // =========================================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }


    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

                // -------------------------------------------------
                // Disable CSRF because this is a stateless REST API
                // -------------------------------------------------
                .csrf(csrf -> csrf.disable())


                // -------------------------------------------------
                // JWT authentication is stateless
                // -------------------------------------------------
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )


                // -------------------------------------------------
                // Authorization rules
                // -------------------------------------------------
                .authorizeHttpRequests(auth -> auth


                        // =========================================
                        // PUBLIC ENDPOINTS
                        // =========================================

                        // Login
                        .requestMatchers("/auth/**")
                        .permitAll()

                        // Spring error endpoint
                        .requestMatchers("/error")
                        .permitAll()


                        // =========================================
                        // RESOURCE ENDPOINTS
                        // =========================================

                        // USER + ADMIN can view resources
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/resources",
                                "/api/resources/**"
                        )
                        .hasAnyRole("USER", "ADMIN")


                        // ADMIN can create resources
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/resources",
                                "/api/resources/**"
                        )
                        .hasRole("ADMIN")


                        // ADMIN can update resources
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/resources",
                                "/api/resources/**"
                        )
                        .hasRole("ADMIN")


                        // ADMIN can delete resources
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/resources",
                                "/api/resources/**"
                        )
                        .hasRole("ADMIN")


                        // =========================================
                        // RESERVATION ENDPOINTS
                        // =========================================

                        // USER + ADMIN can access reservations
                        .requestMatchers(
                                "/api/reservations/**"
                        )
                        .hasAnyRole("USER", "ADMIN")


                        // =========================================
                        // EVERYTHING ELSE
                        // =========================================

                        .anyRequest()
                        .authenticated()
                )


                // -------------------------------------------------
                // Add JWT filter before Spring's authentication
                // filter
                // -------------------------------------------------
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();
    }
}