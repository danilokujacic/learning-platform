package com.kujacic.users.controller;

import com.kujacic.users.dto.progress.ProgressRequestDTO;
import com.kujacic.users.dto.progress.ProgressResponseDTO;
import com.kujacic.users.service.ProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @PostMapping("")
    public ResponseEntity<ProgressResponseDTO> createProgress(@Valid @RequestBody ProgressRequestDTO progressRequest, @AuthenticationPrincipal Jwt jwt) {
        String authenticatedUserId = jwt.getClaimAsString("sub");
        ProgressResponseDTO progressResponse = progressService.createProgress(authenticatedUserId, progressRequest);
        return new ResponseEntity<>(progressResponse, HttpStatus.OK);
    };
}
