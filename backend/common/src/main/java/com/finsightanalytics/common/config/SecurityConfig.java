package com.finsightanalytics.common.config;

import com.finsightanalytics.common.filter.JwtRequestFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())  // Updated CSRF configuration
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        // Allow login, registration endpoints to be public
                        .requestMatchers("/auth/**").permitAll()

                        // Securing User Management Service (CUSTOMER, BANKER, ADMIN roles)
                        .requestMatchers(HttpMethod.GET, "/users/**").hasAnyRole("CUSTOMER", "BANKER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/users/**").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "/users/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")

                        // Securing Transaction Service (CUSTOMER, BANKER, ADMIN roles)
                        .requestMatchers(HttpMethod.GET, "/transactions/**").hasAnyRole("CUSTOMER", "BANKER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/transactions/**").hasRole("CUSTOMER")

                        // Securing Recommendation Engine (CUSTOMER, BANKER, ADMIN roles)
                        .requestMatchers(HttpMethod.GET, "/recommendations/**").hasAnyRole("CUSTOMER", "BANKER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/recommendations/**").hasRole("BANKER")

                        // Securing Notification Service (CUSTOMER, BANKER, ADMIN roles)
                        .requestMatchers(HttpMethod.GET, "/notifications/**").hasAnyRole("CUSTOMER", "BANKER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/notifications/**").hasRole("BANKER")

                        // Securing Analytics Service (CUSTOMER, BANKER, ADMIN roles)
                        .requestMatchers(HttpMethod.GET, "/analytics/**").hasAnyRole("CUSTOMER", "BANKER", "ADMIN")

                        // Securing Admin Service (ADMIN only)
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        // Add JWT filter before processing requests
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
