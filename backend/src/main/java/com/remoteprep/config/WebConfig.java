package com.remoteprep.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Spring Web MVC Configuration for RemotePrep.
 * Configures restricted CORS mappings for REST endpoints (/api/**)
 * to ensure that origins are restricted in production while supporting
 * local development environments.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${remoteprep.cors.allowed-origins:}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            String[] origins = Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);

            if (origins.length > 0) {
                registry.addMapping("/api/**")
                        .allowedOrigins(origins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false);
            }
        }
    }
}
