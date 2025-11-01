package com.kujacic.courses.service;

import com.kujacic.courses.dto.courseCertificate.CourseCertificateResponse;
import com.kujacic.courses.dto.courseCertificate.CreateCertificateRequest;
import com.kujacic.courses.exception.CertificateAlreadyExistsException;
import com.kujacic.courses.exception.CourseNotFoundException;
import com.kujacic.courses.model.Course;
import com.kujacic.courses.model.CourseCertificate;
import com.kujacic.courses.repository.CourseCertificateRepository;
import com.kujacic.courses.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CourseCertificateService {
    public final CourseCertificateRepository courseCertificateRepository;
    public final CourseRepository courseRepository;


    public CourseCertificateResponse createCourseCertificate(Integer courseId, CreateCertificateRequest createCertificateRequest) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException("Course doesn't exist"));

        Optional<CourseCertificate> certificate = courseCertificateRepository.findByCourseId(courseId);
        if(certificate.isPresent()) throw new CertificateAlreadyExistsException();

        CourseCertificate newCertificate = CourseCertificate.builder()
                .name(createCertificateRequest.getName())
                .course(course)
                .build();
        CourseCertificate savedCertificate = courseCertificateRepository.save(newCertificate);

        return CourseCertificateResponse.builder()
                .id(savedCertificate.getId())
                .name(savedCertificate.getName())
                .referenceUrl(savedCertificate.getReferenceUrl())
                .build();
    }

    public CourseCertificateResponse getCertificate(Long certificateId) {
        CourseCertificate certificate = courseCertificateRepository.findById(certificateId).orElseThrow(() -> new RuntimeException("Certificate not found"));

        CourseCertificateResponse certificateResponse = new CourseCertificateResponse();

        BeanUtils.copyProperties(certificate, certificateResponse);

        return certificateResponse;
    }

    public Void deleteCertificate(Long certificateId) {
        courseCertificateRepository.deleteById(certificateId);

        return null;
    }

}
