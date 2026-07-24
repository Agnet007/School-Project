package com.school.platform.authorization.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile({"dev", "test"})
class DevelopmentSecurityConfiguration {
    @Bean
    @Order(1)
    SecurityFilterChain developmentApiSecurity(HttpSecurity http) throws Exception {
        return http.securityMatcher("/api/v1/lessons/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .build();
    }
}