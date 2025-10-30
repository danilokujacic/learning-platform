package com.kujacic.users.controller;

import com.kujacic.users.dto.course.CourseResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final RestClient restClient;

    @GetMapping("/me")
    public void getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        // TO BE IMPLEMENTED
    }

    @GetMapping("/courses")
    public Page<CourseResponseDTO> getCourses(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String userId = jwt.getClaimAsString("sub");
        Map<String, String> requestBody = new HashMap<>();

        requestBody.put("userId", userId);


        return restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/courses/query")

                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .body(requestBody)
                .retrieve()
                .body(new ParameterizedTypeReference<Page<CourseResponseDTO>>() {});
    }
}
