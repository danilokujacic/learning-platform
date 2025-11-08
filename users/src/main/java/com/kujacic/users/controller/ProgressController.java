package com.kujacic.users.controller;

import com.kujacic.users.dto.progress.ProgressRequestDTO;
import com.kujacic.users.dto.progress.ProgressResponseDTO;
import com.kujacic.users.enums.FileFormats;
import com.kujacic.users.service.ProgressService;
import com.kujacic.users.util.DocumentUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/users/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @PostMapping("export")
    public ResponseEntity<byte[]> exportUserProgress(@AuthenticationPrincipal Jwt jwt) {
        byte[] file = progressService.exportProgress(jwt.getClaimAsString("sub"));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\" " + DocumentUtils.formatDocumentName(jwt) + ".xlsx\"").contentType(MediaType.parseMediaType(
                "\" " + FileFormats.SPREADSHEET + " \"")).contentLength(file.length).body(file);
    }
}
