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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                // JWT REST API - disable CSRF
                .csrf(csrf -> csrf.disable())

                // JWT = stateless authentication
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Login is public
                        .requestMatchers("/auth/**")
                        .permitAll()

                        // ADMIN + USER can view resources
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/resources",
                                "/api/resources/**"
                        )
                        .hasAnyRole("USER", "ADMIN")

                        // Only ADMIN can create resources
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/resources",
                                "/api/resources/**"
                        )
                        .hasRole("ADMIN")

                        // Only ADMIN can update resources
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/resources",
                                "/api/resources/**"
                        )
                        .hasRole("ADMIN")

                        // Only ADMIN can delete resources
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/resources",
                                "/api/resources/**"
                        )
                        .hasRole("ADMIN")

                        // Any other endpoint requires login
                        .anyRequest()
                        .authenticated()
                )

                // JWT filter runs before Spring authentication filter
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}