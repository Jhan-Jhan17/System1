package com.example.system1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disabled to allow your existing forms to work smoothly
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Let your FoundItController handle the manual security!
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/") // If Google login fails, send them back to the landing page
                .defaultSuccessUrl("/google-success", true) // Where to go when Google says "Yes!"
            );
        
        return http.build();
    }
}