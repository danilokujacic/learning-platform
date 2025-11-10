package com.kujacic.courses.unit;

import com.kujacic.courses.dto.courseCertificate.CourseCertificateResponse;
import com.kujacic.courses.dto.courseCertificate.CreateCertificateRequest;
import com.kujacic.courses.exception.CertificateAlreadyExistsException;
import com.kujacic.courses.exception.CourseNotFoundException;
import com.kujacic.courses.model.Course;
import com.kujacic.courses.model.Certificate;
import com.kujacic.courses.repository.CertificateRepository;
import com.kujacic.courses.repository.CourseRepository;
import com.kujacic.courses.service.CertificateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificateServiceTests {

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CertificateService certificateService;

    @Test
    void shouldCreateCourseCertificateSuccessfully() {
        Integer courseId = 1;
        CreateCertificateRequest request = createCertificateRequest("Certificate of Completion", "https://example.com/cert");
        Course course = createCourse(courseId, "Spring Boot Course");

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(certificateRepository.findByCourseId(courseId)).thenReturn(Optional.empty());
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(invocation -> {
            Certificate cert = invocation.getArgument(0);
            cert.setId(1L);
            return cert;
        });

        CourseCertificateResponse response = certificateService.createCourseCertificate(courseId, request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Certificate of Completion", response.getName());

        verify(courseRepository).findById(courseId);
        verify(certificateRepository).findByCourseId(courseId);
        verify(certificateRepository).save(any(Certificate.class));
    }

    @Test
    void shouldThrowExceptionWhenCourseNotFound() {
        Integer courseId = 999;
        CreateCertificateRequest request = createCertificateRequest("Test Certificate", "https://example.com");

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        CourseNotFoundException exception = assertThrows(CourseNotFoundException.class, () -> {
            certificateService.createCourseCertificate(courseId, request);
        });

        assertEquals("Course doesn't exist", exception.getMessage());
        verify(courseRepository).findById(courseId);
        verify(certificateRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCertificateAlreadyExists() {
        Integer courseId = 1;
        CreateCertificateRequest request = createCertificateRequest("Test Certificate", "https://example.com");
        Course course = createCourse(courseId, "Test Course");
        Certificate existingCertificate = createCourseCertificate(1L, "Existing Certificate", course);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(certificateRepository.findByCourseId(courseId)).thenReturn(Optional.of(existingCertificate));

        assertThrows(CertificateAlreadyExistsException.class, () -> {
            certificateService.createCourseCertificate(courseId, request);
        });

        verify(courseRepository).findById(courseId);
        verify(certificateRepository).findByCourseId(courseId);
        verify(certificateRepository, never()).save(any());
    }

    @Test
    void shouldSaveCertificateWithCorrectData() {
        Integer courseId = 1;
        String certificateName = "Advanced Java Certificate";
        String referenceUrl = "https://example.com/advanced-java";
        CreateCertificateRequest request = createCertificateRequest(certificateName, referenceUrl);
        Course course = createCourse(courseId, "Java Course");

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(certificateRepository.findByCourseId(courseId)).thenReturn(Optional.empty());
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(invocation -> {
            Certificate cert = invocation.getArgument(0);
            cert.setId(5L);
            return cert;
        });

        certificateService.createCourseCertificate(courseId, request);

        ArgumentCaptor<Certificate> certificateCaptor = ArgumentCaptor.forClass(Certificate.class);
        verify(certificateRepository).save(certificateCaptor.capture());

        Certificate savedCertificate = certificateCaptor.getValue();
        assertEquals(certificateName, savedCertificate.getName());
        assertEquals(course, savedCertificate.getCourse());
    }

    @Test
    void shouldReturnCertificateResponseWithCorrectFields() {
        Integer courseId = 1;
        CreateCertificateRequest request = createCertificateRequest("Test Certificate", "https://test.com");
        Course course = createCourse(courseId, "Test Course");

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(certificateRepository.findByCourseId(courseId)).thenReturn(Optional.empty());
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(invocation -> {
            Certificate cert = invocation.getArgument(0);
            cert.setId(10L);
            cert.setReferenceUrl("https://test.com");
            return cert;
        });

        CourseCertificateResponse response = certificateService.createCourseCertificate(courseId, request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Test Certificate", response.getName());
        assertEquals("https://test.com", response.getReferenceUrl());
    }

    @Test
    void shouldGetCertificateById() {
        Long certificateId = 1L;
        Course course = createCourse(1, "Test Course");
        Certificate certificate = createCourseCertificate(certificateId, "Certificate", course);
        certificate.setReferenceUrl("https://example.com/cert");

        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificate));

        CourseCertificateResponse response = certificateService.getCertificate(certificateId);

        assertNotNull(response);
        assertEquals(certificateId, response.getId());
        assertEquals("Certificate", response.getName());
        assertEquals("https://example.com/cert", response.getReferenceUrl());

        verify(certificateRepository).findById(certificateId);
    }

    @Test
    void shouldThrowExceptionWhenCertificateNotFound() {
        Long certificateId = 999L;

        when(certificateRepository.findById(certificateId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            certificateService.getCertificate(certificateId);
        });

        assertEquals("Certificate not found", exception.getMessage());
        verify(certificateRepository).findById(certificateId);
    }

    @Test
    void shouldCopyAllPropertiesWhenGettingCertificate() {
        Long certificateId = 5L;
        Course course = createCourse(1, "Java Course");
        Certificate certificate = createCourseCertificate(certificateId, "Java Certificate", course);
        certificate.setReferenceUrl("https://java-certs.com/5");

        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificate));

        CourseCertificateResponse response = certificateService.getCertificate(certificateId);

        assertEquals(certificateId, response.getId());
        assertEquals("Java Certificate", response.getName());
        assertEquals("https://java-certs.com/5", response.getReferenceUrl());
    }

    @Test
    void shouldDeleteCertificateById() {
        Long certificateId = 1L;

        Void result = certificateService.deleteCertificate(certificateId);

        assertNull(result);
        verify(certificateRepository).deleteById(certificateId);
    }

    @Test
    void shouldDeleteMultipleCertificates() {
        Long certificateId1 = 1L;
        Long certificateId2 = 2L;
        Long certificateId3 = 3L;

        certificateService.deleteCertificate(certificateId1);
        certificateService.deleteCertificate(certificateId2);
        certificateService.deleteCertificate(certificateId3);

        verify(certificateRepository).deleteById(certificateId1);
        verify(certificateRepository).deleteById(certificateId2);
        verify(certificateRepository).deleteById(certificateId3);
    }

    @Test
    void shouldCallDeleteByIdOnce() {
        Long certificateId = 10L;

        certificateService.deleteCertificate(certificateId);

        verify(certificateRepository, times(1)).deleteById(certificateId);
    }

    @Test
    void shouldCreateCertificateForDifferentCourses() {
        CreateCertificateRequest request = createCertificateRequest("Certificate", "https://example.com");

        Course course1 = createCourse(1, "Course 1");
        Course course2 = createCourse(2, "Course 2");

        when(courseRepository.findById(1)).thenReturn(Optional.of(course1));
        when(courseRepository.findById(2)).thenReturn(Optional.of(course2));
        when(certificateRepository.findByCourseId(any())).thenReturn(Optional.empty());
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(invocation -> {
            Certificate cert = invocation.getArgument(0);
            cert.setId(1L);
            return cert;
        });

        certificateService.createCourseCertificate(1, request);
        certificateService.createCourseCertificate(2, request);

        verify(certificateRepository, times(2)).save(any(Certificate.class));
    }


    @Test
    void shouldHandleRepositoryExceptionDuringCreate() {
        Integer courseId = 1;
        CreateCertificateRequest request = createCertificateRequest("Test", "https://test.com");
        Course course = createCourse(courseId, "Test Course");

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(certificateRepository.findByCourseId(courseId)).thenReturn(Optional.empty());
        when(certificateRepository.save(any())).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            certificateService.createCourseCertificate(courseId, request);
        });

        verify(courseRepository).findById(courseId);
    }

    @Test
    void shouldHandleRepositoryExceptionDuringGet() {
        Long certificateId = 1L;

        when(certificateRepository.findById(certificateId))
                .thenThrow(new RuntimeException("Database connection failed"));

        assertThrows(RuntimeException.class, () -> {
            certificateService.getCertificate(certificateId);
        });
    }

    @Test
    void shouldCreateCertificateWithMinimumNameLength() {
        Integer courseId = 1;
        CreateCertificateRequest request = createCertificateRequest("ABC", "https://example.com");
        Course course = createCourse(courseId, "Test Course");

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(certificateRepository.findByCourseId(courseId)).thenReturn(Optional.empty());
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(invocation -> {
            Certificate cert = invocation.getArgument(0);
            cert.setId(1L);
            return cert;
        });

        CourseCertificateResponse response = certificateService.createCourseCertificate(courseId, request);

        assertEquals("ABC", response.getName());
    }

    @Test
    void shouldCreateCertificateWithMaximumNameLength() {
        Integer courseId = 1;
        String longName = "A".repeat(55);
        CreateCertificateRequest request = createCertificateRequest(longName, "https://example.com");
        Course course = createCourse(courseId, "Test Course");

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(certificateRepository.findByCourseId(courseId)).thenReturn(Optional.empty());
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(invocation -> {
            Certificate cert = invocation.getArgument(0);
            cert.setId(1L);
            return cert;
        });

        CourseCertificateResponse response = certificateService.createCourseCertificate(courseId, request);

        assertEquals(longName, response.getName());
    }

    @Test
    void shouldVerifyFindByCourseIdCalled() {
        Integer courseId = 1;
        CreateCertificateRequest request = createCertificateRequest("Test", "https://test.com");
        Course course = createCourse(courseId, "Test Course");

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(certificateRepository.findByCourseId(courseId)).thenReturn(Optional.empty());
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(invocation -> {
            Certificate cert = invocation.getArgument(0);
            cert.setId(1L);
            return cert;
        });

        certificateService.createCourseCertificate(courseId, request);

        verify(certificateRepository).findByCourseId(eq(courseId));
    }

    @Test
    void shouldReturnNullReferenceUrlWhenNotSet() {
        Long certificateId = 1L;
        Course course = createCourse(1, "Test Course");
        Certificate certificate = createCourseCertificate(certificateId, "Certificate", course);
        certificate.setReferenceUrl(null);

        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificate));

        CourseCertificateResponse response = certificateService.getCertificate(certificateId);

        assertNull(response.getReferenceUrl());
    }

    private CreateCertificateRequest createCertificateRequest(String name, String referenceUrl) {
        CreateCertificateRequest request = new CreateCertificateRequest();
        request.setName(name);
        request.setReferenceUrl(referenceUrl);
        return request;
    }

    private Course createCourse(Integer id, String name) {
        Course course = new Course();
        course.setId(id);
        course.setName(name);
        return course;
    }

    private Certificate createCourseCertificate(Long id, String name, Course course) {
        return Certificate.builder()
                .id(id)
                .name(name)
                .course(course)
                .build();
    }
}
