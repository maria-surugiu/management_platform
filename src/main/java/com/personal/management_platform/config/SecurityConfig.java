package com.personal.management_platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // I'll use JWT later
                .csrf(AbstractHttpConfigurer::disable)

                // access rules
                .authorizeHttpRequests(auth -> auth
                        // anyone can access this is endpoint to test it on Postman for now
                        .requestMatchers("/api/users/register").permitAll()

                        // any other requests require auth
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}