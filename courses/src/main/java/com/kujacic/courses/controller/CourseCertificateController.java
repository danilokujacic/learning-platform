package com.kujacic.courses.controller;

import com.kujacic.courses.dto.courseCertificate.CourseCertificateResponse;
import com.kujacic.courses.dto.courseCertificate.CreateCertificateRequest;
import com.kujacic.courses.service.CertificateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses/{courseId}/certificates")
@RequiredArgsConstructor
public class CourseCertificateController {
    private final CertificateService certificateService;

    @PostMapping()
    public ResponseEntity<CourseCertificateResponse> createSertificate(@PathVariable("courseId") Integer courseId, @Valid @RequestBody CreateCertificateRequest createCertificateRequest) {
        CourseCertificateResponse certificateResponse = certificateService.createCourseCertificate(courseId, createCertificateRequest);
        return new ResponseEntity<>(certificateResponse, HttpStatus.CREATED);
    }

    @GetMapping("{certificateId}")
    public ResponseEntity<CourseCertificateResponse> getCertifiacte(@PathVariable("courseId") Integer courseId, @PathVariable("certificateId") Long certificateId) {
        CourseCertificateResponse certificateResponse = certificateService.getCertificate(certificateId);

        return new ResponseEntity<>(certificateResponse, HttpStatus.OK);
    }

    @DeleteMapping("{certificateId}")
    public ResponseEntity<Void> deleteCertificate(@PathVariable("courseId") Integer courseId, @PathVariable("certificateId") Long certificateId) {
        return new ResponseEntity<>(certificateService.deleteCertificate(certificateId), HttpStatus.OK);
    }
}
