package com.kujacic.users.controller;

import com.kujacic.users.dto.PagedResponse;
import com.kujacic.users.dto.course.CourseResponseDTO;
import com.kujacic.users.dto.progress.ProgressResponseDTO;
import com.kujacic.users.service.ProgressService;
import com.kujacic.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public void getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        // TO BE IMPLEMENTED
    }

    @GetMapping("/my-courses")
    public PagedResponse<CourseResponseDTO> getCourses(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String userId = jwt.getClaimAsString("sub");

        return userService.getUserCourses(userId, page, size);
    }
}
