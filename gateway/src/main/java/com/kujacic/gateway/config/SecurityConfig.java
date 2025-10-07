<<<<<<< Updated upstream
package com.kujacic.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges -> exchanges
                        // Allow actuator endpoints without authentication
                        .pathMatchers("/actuator/**").permitAll()
                        // Allow OAuth2 login endpoints
                        .pathMatchers("/login/**", "/oauth2/**").permitAll()
                        // All other requests require authentication
                        .anyExchange().authenticated()
                )
                // Enable OAuth2 login for browser-based access
                .oauth2Login(Customizer.withDefaults())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(
                                new RedirectServerAuthenticationEntryPoint("/oauth2/authorization/keycloak")
                        )
                )
                // Disable CSRF for API Gateway
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
=======
package com.kujacic.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges -> exchanges
                        // Allow actuator endpoints without authentication
                        .pathMatchers("/actuator/**").permitAll()
                        // Allow OAuth2 login endpoints
                        .pathMatchers("/login/**", "/oauth2/**").permitAll()
                        // All other requests require authentication
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt())
                // Enable OAuth2 login for browser-based access
                .oauth2Login(Customizer.withDefaults())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(
                                new RedirectServerAuthenticationEntryPoint("/oauth2/authorization/keycloak")
                        )
                )
                // Disable CSRF for API Gateway
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
>>>>>>> Stashed changes
}