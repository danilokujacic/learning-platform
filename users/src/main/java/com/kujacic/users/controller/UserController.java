package com.kujacic.users.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")  // ← This path matters for Gateway routing!
public class UserController {

    /**
     * Public endpoint - No authentication required
     * URL: /api/users/health
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "service", "user-service",
                "message", "Service is running"
        );
    }

    /**
     * Get current user - Requires authentication
     * URL: /api/users/me
     */
    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> response = new HashMap<>();
        response.put("userId", jwt.getSubject());
        response.put("username", jwt.getClaimAsString("preferred_username"));
        response.put("email", jwt.getClaimAsString("email"));
        response.put("name", jwt.getClaimAsString("name"));
        response.put("roles", extractRoles(jwt));
        return response;
    }

    /**
     * Get all users - Requires ADMIN role
     * URL: /api/users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getAllUsers(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "message", "List of all users",
                "requestedBy", jwt.getClaimAsString("preferred_username"),
                "users", List.of(
                        Map.of("id", 1, "username", "john", "email", "john@example.com"),
                        Map.of("id", 2, "username", "jane", "email", "jane@example.com")
                )
        );
    }

    /**
     * Create user - Requires USER or ADMIN role
     * URL: /api/users
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Map<String, Object> createUser(
            @RequestBody Map<String, String> userData,
            @AuthenticationPrincipal Jwt jwt) {

        return Map.of(
                "message", "User created successfully",
                "createdBy", jwt.getClaimAsString("preferred_username"),
                "userData", userData
        );
    }

    /**
     * Update user - Requires ADMIN role
     * URL: /api/users/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, String> userData,
            @AuthenticationPrincipal Jwt jwt) {

        return Map.of(
                "message", "User updated successfully",
                "userId", id,
                "updatedBy", jwt.getClaimAsString("preferred_username")
        );
    }

    /**
     * Delete user - Requires ADMIN role
     * URL: /api/users/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        return Map.of(
                "message", "User deleted successfully",
                "userId", id,
                "deletedBy", jwt.getClaimAsString("preferred_username")
        );
    }

    // Helper method to extract roles
    private List<String> extractRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            return (List<String>) realmAccess.get("roles");
        }
        return List.of();
    }
}
