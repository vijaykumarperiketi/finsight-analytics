package com.finsightanalytics.usermanagement;

import com.finsightanalytics.common.util.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
            // Allow login, registration endpoints to be public
            .antMatchers("/auth/**").permitAll()

                // Securing User Management Service (CUSTOMER, BANKER, ADMIN roles)
                .antMatchers(HttpMethod.GET, "/users/**").hasAnyRole("CUSTOMER", "BANKER", "ADMIN")
                .antMatchers(HttpMethod.POST, "/users/**").hasRole("CUSTOMER")
                .antMatchers(HttpMethod.PUT, "/users/**").hasAnyRole("CUSTOMER", "ADMIN")
                .antMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")

                // Securing Transaction Service (CUSTOMER, BANKER, ADMIN roles)
                .antMatchers(HttpMethod.GET, "/transactions/**").hasAnyRole("CUSTOMER", "BANKER", "ADMIN")
                .antMatchers(HttpMethod.POST, "/transactions/**").hasRole("CUSTOMER")
                
                // Securing Recommendation Engine (CUSTOMER, BANKER, ADMIN roles)
                .antMatchers(HttpMethod.GET, "/recommendations/**").hasAnyRole("CUSTOMER", "BANKER", "ADMIN")
                .antMatchers(HttpMethod.POST, "/recommendations/**").hasRole("BANKER")

                // Securing Notification Service (CUSTOMER, BANKER, ADMIN roles)
                .antMatchers(HttpMethod.GET, "/notifications/**").hasAnyRole("CUSTOMER", "BANKER", "ADMIN")
                .antMatchers(HttpMethod.POST, "/notifications/**").hasRole("BANKER")

                // Securing Analytics Service (CUSTOMER, BANKER, ADMIN roles)
                .antMatchers(HttpMethod.GET, "/analytics/**").hasAnyRole("CUSTOMER", "BANKER", "ADMIN")

                // Securing Admin Service (ADMIN only)
                .antMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // Add JWT filter before processing requests
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
