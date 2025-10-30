package com.kujacic.users.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {



    @Bean
    public RestClient restClient() {
        return RestClient.builder().baseUrl("http://localhost:8081").requestInterceptor((request, body, execution) -> {
            var authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                String token = jwtAuth.getToken().getTokenValue();
                request.getHeaders().add("Authorization", "Bearer " + token);
            }

            return execution.execute(request, body);
        }).defaultHeader("Content-Type", "application/json").build();
    }
}
