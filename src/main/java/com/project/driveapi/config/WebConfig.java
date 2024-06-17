package com.project.driveapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebConfig {

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(_ -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOrigins(List.of("*"));
                    configuration.setAllowedMethods(List.of("*"));
                    configuration.setAllowedHeaders(List.of(
                            "Content-Type",
                            "Access-Control-Allow-Headers",
                            "Access-Control-Expose-Headers",
                            "Content-Disposition",
                            "Authorization",
                            "X-Requested-With")
                    );
                    configuration.setExposedHeaders(List.of("Content-Disposition"));
                    return configuration;
                }))
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
