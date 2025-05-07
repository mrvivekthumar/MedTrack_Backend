package com.medtrack.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // disable for development only
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/user/signup", "/api/v1/user/signin").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(withDefaults()); // Or formLogin(withDefaults());

        return http.build();
    }
}
