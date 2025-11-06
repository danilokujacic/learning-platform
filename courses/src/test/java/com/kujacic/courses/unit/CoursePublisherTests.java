package com.kujacic.courses.unit;

import com.kujacic.courses.dto.course.CourseResponseDTO;
import com.kujacic.courses.dto.rabbitmq.CourseCertificateIssuedEvent;
import com.kujacic.courses.dto.rabbitmq.CourseLevelPassEvent;
import com.kujacic.courses.service.CoursePublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoursePublisherTests {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private CoursePublisher coursePublisher;

    @AfterEach
    void cleanup() {
        MDC.clear();
    }

    @Test
    void shouldPublishCourseCertificateWithCorrectExchangeAndRoutingKey() {
        CourseCertificateIssuedEvent event = createCertificateEvent();

        coursePublisher.courseCertificatePublisher(event);

        verify(rabbitTemplate).convertAndSend(
                eq("course-exchange"),
                eq("course-certificate.issued"),
                eq(event),
                any(MessagePostProcessor.class)
        );
    }

    @Test
    void shouldSetMDCValuesForCertificatePublisher() {
        AtomicReference<String> capturedCorrelationId = new AtomicReference<>();
        AtomicReference<String> capturedCourseId = new AtomicReference<>();

        CourseCertificateIssuedEvent event = createCertificateEvent();

        doAnswer(invocation -> {
            capturedCorrelationId.set(MDC.get("correlationId"));
            capturedCourseId.set(MDC.get("courseId"));
            return null;
        }).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(), any(MessagePostProcessor.class));

        coursePublisher.courseCertificatePublisher(event);

        assertNotNull(capturedCorrelationId.get());
        assertEquals(36, capturedCorrelationId.get().length());
        assertDoesNotThrow(() -> UUID.fromString(capturedCorrelationId.get()));
        assertEquals("1", capturedCourseId.get());
    }

    @Test
    void shouldClearMDCAfterCertificatePublish() {
        CourseCertificateIssuedEvent event = createCertificateEvent();

        coursePublisher.courseCertificatePublisher(event);

        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("courseId"));
    }

    @Test
    void shouldClearMDCEvenWhenCertificatePublishFails() {
        CourseCertificateIssuedEvent event = createCertificateEvent();

        doThrow(new RuntimeException("RabbitMQ error"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(), any(MessagePostProcessor.class));

        assertThrows(RuntimeException.class, () -> {
            coursePublisher.courseCertificatePublisher(event);
        });

        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("courseId"));
    }

    @Test
    void shouldSetCorrectHeadersForCertificateMessage() {
        CourseCertificateIssuedEvent event = createCertificateEvent();
        AtomicReference<String> mdcCorrelationId = new AtomicReference<>();

        doAnswer(invocation -> {
            mdcCorrelationId.set(MDC.get("correlationId"));
            return null;
        }).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(), any(MessagePostProcessor.class));

        coursePublisher.courseCertificatePublisher(event);

        ArgumentCaptor<MessagePostProcessor> processorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                any(),
                processorCaptor.capture()
        );

        assertNotNull(mdcCorrelationId.get());
    }

    @Test
    void shouldPublishCourseLevelWithCorrectExchangeAndRoutingKey() {
        Integer courseId = 1;
        Long levelId = 10L;
        String userId = "user123";
        Integer progress = 50;
        String courseName = "Spring Boot Course";

        coursePublisher.courseLevelPublisher(courseId, levelId, userId, progress, courseName);

        ArgumentCaptor<CourseLevelPassEvent> eventCaptor = ArgumentCaptor.forClass(CourseLevelPassEvent.class);

        verify(rabbitTemplate).convertAndSend(
                eq("course-exchange"),
                eq("course-level.passed"),
                eventCaptor.capture(),
                any(MessagePostProcessor.class)
        );

        CourseLevelPassEvent capturedEvent = eventCaptor.getValue();
        assertEquals(courseId, capturedEvent.getCourseId());
        assertEquals(levelId, capturedEvent.getLevelId());
        assertEquals(userId, capturedEvent.getUserId());
        assertEquals(progress, capturedEvent.getProgress());
        assertEquals(courseName, capturedEvent.getCourseName());
    }

    @Test
    void shouldSetMDCValuesForCourseLevelPublisher() {
        AtomicReference<String> capturedCorrelationId = new AtomicReference<>();
        AtomicReference<String> capturedLevelId = new AtomicReference<>();

        doAnswer(invocation -> {
            capturedCorrelationId.set(MDC.get("correlationId"));
            capturedLevelId.set(MDC.get("levelId"));
            return null;
        }).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(), any(MessagePostProcessor.class));

        coursePublisher.courseLevelPublisher(1, 10L, "user123", 50, "Spring Boot");

        assertNotNull(capturedCorrelationId.get());
        assertEquals(36, capturedCorrelationId.get().length());
        assertDoesNotThrow(() -> UUID.fromString(capturedCorrelationId.get()));
        assertEquals("10", capturedLevelId.get());
    }

    @Test
    void shouldClearMDCAfterCourseLevelPublish() {
        coursePublisher.courseLevelPublisher(1, 10L, "user123", 50, "Spring Boot");

        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("levelId"));
    }

    @Test
    void shouldClearMDCEvenWhenCourseLevelPublishFails() {
        doThrow(new RuntimeException("RabbitMQ connection failed"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(), any(MessagePostProcessor.class));

        assertThrows(RuntimeException.class, () -> {
            coursePublisher.courseLevelPublisher(1, 10L, "user123", 50, "Spring Boot");
        });

        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("levelId"));
    }

    @Test
    void shouldBuildCourseLevelPassEventCorrectly() {
        Integer courseId = 5;
        Long levelId = 20L;
        String userId = "user456";
        Integer progress = 100;
        String courseName = "Advanced Java";

        coursePublisher.courseLevelPublisher(courseId, levelId, userId, progress, courseName);

        ArgumentCaptor<CourseLevelPassEvent> eventCaptor = ArgumentCaptor.forClass(CourseLevelPassEvent.class);

        verify(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                eventCaptor.capture(),
                any(MessagePostProcessor.class)
        );

        CourseLevelPassEvent event = eventCaptor.getValue();
        assertEquals(courseId, event.getCourseId());
        assertEquals(levelId, event.getLevelId());
        assertEquals(userId, event.getUserId());
        assertEquals(progress, event.getProgress());
        assertEquals(courseName, event.getCourseName());
    }

    @Test
    void shouldSetLevelIdHeaderForCourseLevelMessage() {
        Long levelId = 15L;

        coursePublisher.courseLevelPublisher(1, levelId, "user123", 75, "Test Course");

        ArgumentCaptor<MessagePostProcessor> processorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);

        verify(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                any(),
                processorCaptor.capture()
        );

        assertNotNull(processorCaptor.getValue());
    }

    @Test
    void shouldPublishCourseLevelWithZeroProgress() {
        coursePublisher.courseLevelPublisher(1, 1L, "user123", 0, "Beginner Course");

        ArgumentCaptor<CourseLevelPassEvent> eventCaptor = ArgumentCaptor.forClass(CourseLevelPassEvent.class);

        verify(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                eventCaptor.capture(),
                any(MessagePostProcessor.class)
        );

        assertEquals(0, eventCaptor.getValue().getProgress());
    }

    @Test
    void shouldPublishCourseLevelWithMaxProgress() {
        coursePublisher.courseLevelPublisher(1, 1L, "user123", 100, "Completed Course");

        ArgumentCaptor<CourseLevelPassEvent> eventCaptor = ArgumentCaptor.forClass(CourseLevelPassEvent.class);

        verify(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                eventCaptor.capture(),
                any(MessagePostProcessor.class)
        );

        assertEquals(100, eventCaptor.getValue().getProgress());
    }

    @Test
    void shouldHandleNullCourseName() {
        coursePublisher.courseLevelPublisher(1, 1L, "user123", 50, null);

        ArgumentCaptor<CourseLevelPassEvent> eventCaptor = ArgumentCaptor.forClass(CourseLevelPassEvent.class);

        verify(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                eventCaptor.capture(),
                any(MessagePostProcessor.class)
        );

        assertNull(eventCaptor.getValue().getCourseName());
    }

    @Test
    void shouldPublishCertificateWithDifferentCourseIds() {
        CourseCertificateIssuedEvent event1 = createCertificateEventWithCourseId(1);
        CourseCertificateIssuedEvent event2 = createCertificateEventWithCourseId(2);
        CourseCertificateIssuedEvent event3 = createCertificateEventWithCourseId(3);

        coursePublisher.courseCertificatePublisher(event1);
        coursePublisher.courseCertificatePublisher(event2);
        coursePublisher.courseCertificatePublisher(event3);

        verify(rabbitTemplate, times(3)).convertAndSend(
                eq("course-exchange"),
                eq("course-certificate.issued"),
                any(CourseCertificateIssuedEvent.class),
                any(MessagePostProcessor.class)
        );
    }

    @Test
    void shouldPublishMultipleCourseLevels() {
        coursePublisher.courseLevelPublisher(1, 1L, "user1", 25, "Course A");
        coursePublisher.courseLevelPublisher(2, 2L, "user2", 50, "Course B");
        coursePublisher.courseLevelPublisher(3, 3L, "user3", 75, "Course C");

        verify(rabbitTemplate, times(3)).convertAndSend(
                eq("course-exchange"),
                eq("course-level.passed"),
                any(CourseLevelPassEvent.class),
                any(MessagePostProcessor.class)
        );
    }

    @Test
    void shouldGenerateUniqueCorrelationIdsForCertificates() {
        AtomicReference<String> correlationId1 = new AtomicReference<>();
        AtomicReference<String> correlationId2 = new AtomicReference<>();

        doAnswer(invocation -> {
            if (correlationId1.get() == null) {
                correlationId1.set(MDC.get("correlationId"));
            } else {
                correlationId2.set(MDC.get("correlationId"));
            }
            return null;
        }).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(), any(MessagePostProcessor.class));

        CourseCertificateIssuedEvent event = createCertificateEvent();
        coursePublisher.courseCertificatePublisher(event);
        coursePublisher.courseCertificatePublisher(event);

        assertNotNull(correlationId1.get());
        assertNotNull(correlationId2.get());
        assertNotEquals(correlationId1.get(), correlationId2.get());
    }

    @Test
    void shouldGenerateUniqueCorrelationIdsForCourseLevels() {
        AtomicReference<String> correlationId1 = new AtomicReference<>();
        AtomicReference<String> correlationId2 = new AtomicReference<>();

        doAnswer(invocation -> {
            if (correlationId1.get() == null) {
                correlationId1.set(MDC.get("correlationId"));
            } else {
                correlationId2.set(MDC.get("correlationId"));
            }
            return null;
        }).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(), any(MessagePostProcessor.class));

        coursePublisher.courseLevelPublisher(1, 1L, "user123", 50, "Course");
        coursePublisher.courseLevelPublisher(1, 2L, "user123", 75, "Course");

        assertNotNull(correlationId1.get());
        assertNotNull(correlationId2.get());
        assertNotEquals(correlationId1.get(), correlationId2.get());
    }

    private CourseCertificateIssuedEvent createCertificateEvent() {
        return createCertificateEventWithCourseId(1);
    }

    private CourseCertificateIssuedEvent createCertificateEventWithCourseId(Integer courseId) {
        CourseResponseDTO course = new CourseResponseDTO();
        course.setId(courseId);
        course.setName("Spring Boot Course");

        return CourseCertificateIssuedEvent.builder()
                .id(1L)
                .name("Certificate of Completion")
                .userId("user123")
                .course(course)
                .referenceUrl("https://certificates.example.com/cert-123")
                .build();
    }
}