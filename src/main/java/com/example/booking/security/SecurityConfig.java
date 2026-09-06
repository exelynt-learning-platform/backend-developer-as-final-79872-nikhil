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

                // Disable CSRF
                .csrf(csrf -> csrf.disable())


                // Stateless JWT authentication
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )


                // Authorization rules
                .authorizeHttpRequests(auth -> auth


                        // =========================================
                        // PUBLIC
                        // =========================================

                        .requestMatchers("/auth/**")
                        .permitAll()

                        .requestMatchers("/error")
                        .permitAll()


                        // =========================================
                        // RESOURCES
                        // =========================================

                        // USER + ADMIN → GET
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/resources",
                                "/api/resources/**"
                        )
                        .hasAnyRole("USER", "ADMIN")


                        // ADMIN → CREATE
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/resources",
                                "/api/resources/**"
                        )
                        .hasRole("ADMIN")


                        // ADMIN → UPDATE
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/resources",
                                "/api/resources/**"
                        )
                        .hasRole("ADMIN")


                        // ADMIN → DELETE
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/resources",
                                "/api/resources/**"
                        )
                        .hasRole("ADMIN")


                        // =========================================
                        // RESERVATIONS
                        // =========================================

                        // USER + ADMIN → CREATE
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/reservations"
                        )
                        .hasAnyRole("USER", "ADMIN")


                        // USER + ADMIN → MY RESERVATIONS
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/reservations/my"
                        )
                        .hasAnyRole("USER", "ADMIN")


                        // ADMIN → ALL RESERVATIONS
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/reservations"
                        )
                        .hasRole("ADMIN")


                        // ADMIN → FILTER
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/reservations/filter"
                        )
                        .hasRole("ADMIN")


                        // USER + ADMIN → VIEW BY ID
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/reservations/*"
                        )
                        .hasAnyRole("USER", "ADMIN")


                        // USER + ADMIN → CANCEL
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/reservations/*/cancel"
                        )
                        .hasAnyRole("USER", "ADMIN")


                        // ADMIN → UPDATE
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/reservations/*"
                        )
                        .hasRole("ADMIN")


                        // ADMIN → DELETE
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/reservations/*"
                        )
                        .hasRole("ADMIN")


                        // =========================================
                        // EVERYTHING ELSE
                        // =========================================

                        .anyRequest()
                        .authenticated()
                )


                // JWT filter
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();
    }
}