package com.kujacic.courses.unit;


import com.kujacic.courses.dto.course.CourseResponseDTO;
import com.kujacic.courses.dto.rabbitmq.CertificateRequest;
import com.kujacic.courses.dto.rabbitmq.CourseCertificateIssuedEvent;
import com.kujacic.courses.model.Course;
import com.kujacic.courses.model.CourseCertificate;
import com.kujacic.courses.repository.CourseCertificateRepository;
import com.kujacic.courses.service.CourseListener;
import com.kujacic.courses.service.CoursePublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseListenerTests {

    @Mock
    private CourseCertificateRepository courseCertificateRepository;

    @Mock
    private CoursePublisher coursesPublisher;

    @InjectMocks
    private CourseListener courseListener;

    @Test
    void shouldIssueCertificateWhenCertificateExists() {
        Integer courseId = 1;
        String userId = "user123";
        CertificateRequest request = createCertificateRequest(courseId, userId);
        CourseCertificate certificate = createCourseCertificate(1L, courseId, "Spring Boot Course");

        when(courseCertificateRepository.findByCourseId(courseId))
                .thenReturn(Optional.of(certificate));

        courseListener.certificateRequestListener(request);

        verify(courseCertificateRepository).findByCourseId(courseId);
        verify(coursesPublisher).courseCertificatePublisher(any(CourseCertificateIssuedEvent.class));
    }

    @Test
    void shouldNotIssueCertificateWhenCertificateDoesNotExist() {
        Integer courseId = 999;
        String userId = "user123";
        CertificateRequest request = createCertificateRequest(courseId, userId);

        when(courseCertificateRepository.findByCourseId(courseId))
                .thenReturn(Optional.empty());

        courseListener.certificateRequestListener(request);

        verify(courseCertificateRepository).findByCourseId(courseId);
        verify(coursesPublisher, never()).courseCertificatePublisher(any());
    }

    @Test
    void shouldBuildCourseCertificateIssuedEventCorrectly() {
        Integer courseId = 1;
        String userId = "user456";
        Long certificateId = 10L;
        String courseName = "Advanced Java";

        CertificateRequest request = createCertificateRequest(courseId, userId);
        CourseCertificate certificate = createCourseCertificate(certificateId, courseId, courseName);

        when(courseCertificateRepository.findByCourseId(courseId))
                .thenReturn(Optional.of(certificate));

        courseListener.certificateRequestListener(request);

        ArgumentCaptor<CourseCertificateIssuedEvent> eventCaptor =
                ArgumentCaptor.forClass(CourseCertificateIssuedEvent.class);

        verify(coursesPublisher).courseCertificatePublisher(eventCaptor.capture());

        CourseCertificateIssuedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(certificateId, capturedEvent.getId());
        assertEquals(userId, capturedEvent.getUserId());
        assertNotNull(capturedEvent.getCourse());
        assertEquals(courseId, capturedEvent.getCourse().getId());
        assertEquals(courseName, capturedEvent.getCourse().getName());
    }

    @Test
    void shouldMapCourseResponseDTOCorrectly() {
        Integer courseId = 5;
        String userId = "user789";
        String courseName = "Microservices Architecture";

        CertificateRequest request = createCertificateRequest(courseId, userId);
        CourseCertificate certificate = createCourseCertificate(1L, courseId, courseName);

        when(courseCertificateRepository.findByCourseId(courseId))
                .thenReturn(Optional.of(certificate));

        courseListener.certificateRequestListener(request);

        ArgumentCaptor<CourseCertificateIssuedEvent> eventCaptor =
                ArgumentCaptor.forClass(CourseCertificateIssuedEvent.class);

        verify(coursesPublisher).courseCertificatePublisher(eventCaptor.capture());

        CourseResponseDTO courseResponse = eventCaptor.getValue().getCourse();
        assertNotNull(courseResponse);
        assertEquals(courseId, courseResponse.getId());
        assertEquals(courseName, courseResponse.getName());
    }

    @Test
    void shouldHandleMultipleCertificateRequests() {
        CertificateRequest request1 = createCertificateRequest(1, "user1");
        CertificateRequest request2 = createCertificateRequest(2, "user2");
        CertificateRequest request3 = createCertificateRequest(3, "user3");

        CourseCertificate cert1 = createCourseCertificate(1L, 1, "Course 1");
        CourseCertificate cert2 = createCourseCertificate(2L, 2, "Course 2");
        CourseCertificate cert3 = createCourseCertificate(3L, 3, "Course 3");

        when(courseCertificateRepository.findByCourseId(1)).thenReturn(Optional.of(cert1));
        when(courseCertificateRepository.findByCourseId(2)).thenReturn(Optional.of(cert2));
        when(courseCertificateRepository.findByCourseId(3)).thenReturn(Optional.of(cert3));

        courseListener.certificateRequestListener(request1);
        courseListener.certificateRequestListener(request2);
        courseListener.certificateRequestListener(request3);

        verify(coursesPublisher, times(3)).courseCertificatePublisher(any(CourseCertificateIssuedEvent.class));
    }

    @Test
    void shouldNotPublishWhenSomeCertificatesDoNotExist() {
        CertificateRequest request1 = createCertificateRequest(1, "user1");
        CertificateRequest request2 = createCertificateRequest(999, "user2");
        CertificateRequest request3 = createCertificateRequest(3, "user3");

        CourseCertificate cert1 = createCourseCertificate(1L, 1, "Course 1");
        CourseCertificate cert3 = createCourseCertificate(3L, 3, "Course 3");

        when(courseCertificateRepository.findByCourseId(1)).thenReturn(Optional.of(cert1));
        when(courseCertificateRepository.findByCourseId(999)).thenReturn(Optional.empty());
        when(courseCertificateRepository.findByCourseId(3)).thenReturn(Optional.of(cert3));

        courseListener.certificateRequestListener(request1);
        courseListener.certificateRequestListener(request2);
        courseListener.certificateRequestListener(request3);

        verify(coursesPublisher, times(2)).courseCertificatePublisher(any(CourseCertificateIssuedEvent.class));
    }

    @Test
    void shouldPassCorrectUserIdToCertificateEvent() {
        Integer courseId = 1;
        String userId = "specificUser123";

        CertificateRequest request = createCertificateRequest(courseId, userId);
        CourseCertificate certificate = createCourseCertificate(1L, courseId, "Test Course");

        when(courseCertificateRepository.findByCourseId(courseId))
                .thenReturn(Optional.of(certificate));

        courseListener.certificateRequestListener(request);

        ArgumentCaptor<CourseCertificateIssuedEvent> eventCaptor =
                ArgumentCaptor.forClass(CourseCertificateIssuedEvent.class);

        verify(coursesPublisher).courseCertificatePublisher(eventCaptor.capture());

        assertEquals(userId, eventCaptor.getValue().getUserId());
    }

    @Test
    void shouldHandleNullUserId() {
        Integer courseId = 1;
        CertificateRequest request = createCertificateRequest(courseId, null);
        CourseCertificate certificate = createCourseCertificate(1L, courseId, "Test Course");

        when(courseCertificateRepository.findByCourseId(courseId))
                .thenReturn(Optional.of(certificate));

        courseListener.certificateRequestListener(request);

        ArgumentCaptor<CourseCertificateIssuedEvent> eventCaptor =
                ArgumentCaptor.forClass(CourseCertificateIssuedEvent.class);

        verify(coursesPublisher).courseCertificatePublisher(eventCaptor.capture());

        assertNull(eventCaptor.getValue().getUserId());
    }

    @Test
    void shouldVerifyRepositoryCalledOnce() {
        Integer courseId = 1;
        String userId = "user123";
        CertificateRequest request = createCertificateRequest(courseId, userId);
        CourseCertificate certificate = createCourseCertificate(1L, courseId, "Test Course");

        when(courseCertificateRepository.findByCourseId(courseId))
                .thenReturn(Optional.of(certificate));

        courseListener.certificateRequestListener(request);

        verify(courseCertificateRepository, times(1)).findByCourseId(courseId);
    }

    @Test
    void shouldVerifyPublisherCalledOnce() {
        Integer courseId = 1;
        String userId = "user123";
        CertificateRequest request = createCertificateRequest(courseId, userId);
        CourseCertificate certificate = createCourseCertificate(1L, courseId, "Test Course");

        when(courseCertificateRepository.findByCourseId(courseId))
                .thenReturn(Optional.of(certificate));

        courseListener.certificateRequestListener(request);

        verify(coursesPublisher, times(1)).courseCertificatePublisher(any(CourseCertificateIssuedEvent.class));
    }

    @Test
    void shouldHandleRepositoryException() {
        Integer courseId = 1;
        String userId = "user123";
        CertificateRequest request = createCertificateRequest(courseId, userId);

        when(courseCertificateRepository.findByCourseId(courseId))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            courseListener.certificateRequestListener(request);
        });

        verify(coursesPublisher, never()).courseCertificatePublisher(any());
    }

    @Test
    void shouldHandlePublisherException() {
        Integer courseId = 1;
        String userId = "user123";
        CertificateRequest request = createCertificateRequest(courseId, userId);
        CourseCertificate certificate = createCourseCertificate(1L, courseId, "Test Course");

        when(courseCertificateRepository.findByCourseId(courseId))
                .thenReturn(Optional.of(certificate));

        doThrow(new RuntimeException("RabbitMQ error"))
                .when(coursesPublisher).courseCertificatePublisher(any());

        assertThrows(RuntimeException.class, () -> {
            courseListener.certificateRequestListener(request);
        });

        verify(courseCertificateRepository).findByCourseId(courseId);
    }

    @Test
    void shouldMapAllCertificateFields() {
        Integer courseId = 1;
        String userId = "user123";
        Long certificateId = 99L;
        String courseName = "Complete Course";

        CertificateRequest request = createCertificateRequest(courseId, userId);
        CourseCertificate certificate = createCourseCertificateWithAllFields(
                certificateId,
                courseId,
                courseName,
                "Certificate of Excellence",
                "https://example.com/cert/99"
        );

        when(courseCertificateRepository.findByCourseId(courseId))
                .thenReturn(Optional.of(certificate));

        courseListener.certificateRequestListener(request);

        ArgumentCaptor<CourseCertificateIssuedEvent> eventCaptor =
                ArgumentCaptor.forClass(CourseCertificateIssuedEvent.class);

        verify(coursesPublisher).courseCertificatePublisher(eventCaptor.capture());

        CourseCertificateIssuedEvent event = eventCaptor.getValue();
        assertEquals(certificateId, event.getId());
        assertEquals(userId, event.getUserId());
        assertEquals(courseId, event.getCourse().getId());
        assertEquals(courseName, event.getCourse().getName());
    }

    @Test
    void shouldHandleDifferentCourseIds() {
        CertificateRequest request1 = createCertificateRequest(100, "user1");
        CertificateRequest request2 = createCertificateRequest(200, "user2");

        CourseCertificate cert1 = createCourseCertificate(1L, 100, "Course 100");
        CourseCertificate cert2 = createCourseCertificate(2L, 200, "Course 200");

        when(courseCertificateRepository.findByCourseId(100)).thenReturn(Optional.of(cert1));
        when(courseCertificateRepository.findByCourseId(200)).thenReturn(Optional.of(cert2));

        courseListener.certificateRequestListener(request1);
        courseListener.certificateRequestListener(request2);

        verify(courseCertificateRepository).findByCourseId(100);
        verify(courseCertificateRepository).findByCourseId(200);
        verify(coursesPublisher, times(2)).courseCertificatePublisher(any());
    }

    @Test
    void shouldNotPublishWhenCertificateIsNull() {
        Integer courseId = 1;
        String userId = "user123";
        CertificateRequest request = createCertificateRequest(courseId, userId);

        when(courseCertificateRepository.findByCourseId(courseId))
                .thenReturn(Optional.empty());

        courseListener.certificateRequestListener(request);

        verify(coursesPublisher, never()).courseCertificatePublisher(any());
    }

    @Test
    void shouldVerifyCorrectEventStructure() {
        Integer courseId = 1;
        String userId = "user123";
        CertificateRequest request = createCertificateRequest(courseId, userId);
        CourseCertificate certificate = createCourseCertificate(5L, courseId, "Test Course");

        when(courseCertificateRepository.findByCourseId(courseId))
                .thenReturn(Optional.of(certificate));

        courseListener.certificateRequestListener(request);

        ArgumentCaptor<CourseCertificateIssuedEvent> eventCaptor =
                ArgumentCaptor.forClass(CourseCertificateIssuedEvent.class);

        verify(coursesPublisher).courseCertificatePublisher(eventCaptor.capture());

        CourseCertificateIssuedEvent event = eventCaptor.getValue();
        assertNotNull(event);
        assertNotNull(event.getId());
        assertNotNull(event.getUserId());
        assertNotNull(event.getCourse());
        assertNotNull(event.getCourse().getId());
        assertNotNull(event.getCourse().getName());
    }

    private CertificateRequest createCertificateRequest(Integer courseId, String userId) {
        CertificateRequest request = new CertificateRequest();
        request.setCourseId(courseId);
        request.setUserId(userId);
        return request;
    }

    private CourseCertificate createCourseCertificate(Long id, Integer courseId, String courseName) {
        Course course = new Course();
        course.setId(courseId);
        course.setName(courseName);

        return CourseCertificate.builder()
                .id(id)
                .course(course)
                .build();
    }

    private CourseCertificate createCourseCertificateWithAllFields(
            Long id,
            Integer courseId,
            String courseName,
            String certificateName,
            String referenceUrl
    ) {
        Course course = new Course();
        course.setId(courseId);
        course.setName(courseName);

        return CourseCertificate.builder()
                .id(id)
                .name(certificateName)
                .course(course)
                .referenceUrl(referenceUrl)
                .build();
    }
}
