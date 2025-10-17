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
        Optional<Course> course = courseRepository.findById(courseId);
        if(course.isEmpty()) throw new CourseNotFoundException("Course doesn't exist");
        Optional<CourseCertificate> certificate = courseCertificateRepository.findByCourseId(courseId);
        if(certificate.isPresent()) throw new CertificateAlreadyExistsException();
        CourseCertificate certificateToSave = new CourseCertificate();
        BeanUtils.copyProperties(createCertificateRequest, certificateToSave);
        certificateToSave.setCourse(course.get());
        CourseCertificate newCertificate = courseCertificateRepository.save(certificateToSave);
        CourseCertificateResponse certificateResponse = new CourseCertificateResponse();
        BeanUtils.copyProperties(newCertificate, certificateResponse);
        return certificateResponse;

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
