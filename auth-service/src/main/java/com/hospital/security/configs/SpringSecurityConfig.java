package com.hospital.security.configs;

import com.hospital.security.filters.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SpringSecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final CorsConfigurationSource corsConfigurationSource;
    private final TokenJwtConfig tokenJwtConfig;

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(authz -> authz
                        // Hay que tener encuenta que solo deberia estar permitido el endpoint de login.
                        // Los otros endpoints deberian estar en otro microservicio (clara separacion de responsabilidades).
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated())
                .addFilter(new JwtAuthenticationFilter(authenticationManager(), tokenJwtConfig))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(management ->
                        management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }
}
