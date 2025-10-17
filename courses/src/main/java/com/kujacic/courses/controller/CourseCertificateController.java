package com.kujacic.courses.controller;

import com.kujacic.courses.dto.courseCertificate.CourseCertificateResponse;
import com.kujacic.courses.dto.courseCertificate.CreateCertificateRequest;
import com.kujacic.courses.service.CourseCertificateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseCertificateController {
    private final CourseCertificateService certificateService;


    @PostMapping("{id}/certificates")
    public ResponseEntity<CourseCertificateResponse> createSertificate(@PathVariable("id") Integer id, @Valid @RequestBody CreateCertificateRequest createCertificateRequest) {
        CourseCertificateResponse certificateResponse = certificateService.createCourseCertificate(id, createCertificateRequest);
        return new ResponseEntity<>(certificateResponse, HttpStatus.CREATED);
    }

    @GetMapping("{id}/certificates/{certificateId}")
    public ResponseEntity<CourseCertificateResponse> getCertifiacte(@PathVariable("id") Integer courseId, @PathVariable("certificateId") Long certificateId) {
        CourseCertificateResponse certificateResponse = certificateService.getCertificate(certificateId);

        return new ResponseEntity<>(certificateResponse, HttpStatus.OK);
    }

    @DeleteMapping("{id}/certificates/{certificateId}")
    public ResponseEntity<Void> deleteCertificate(@PathVariable("id") Integer courseId, @PathVariable("certificateId") Long certificateId) {
        return new ResponseEntity<>(certificateService.deleteCertificate(certificateId), HttpStatus.OK);
    }

    public void updateCertificate() {
        // to be implemented
    }

    public void patchCertificate() {
        // to be implemented
    }
}
