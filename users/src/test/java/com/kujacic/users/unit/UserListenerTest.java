package com.kujacic.users.unit;
import com.kujacic.users.dto.course.CourseResponseDTO;
import com.kujacic.users.dto.progress.ProgressResponseDTO;
import com.kujacic.users.dto.rabbitmq.CourseCertificateIssuedEvent;
import com.kujacic.users.dto.rabbitmq.CourseLevelPassEvent;
import com.kujacic.users.factory.CourseFactory;
import com.kujacic.users.factory.ProgressFactory;
import com.kujacic.users.repository.ProgressRepository;
import com.kujacic.users.service.AchievementService;
import com.kujacic.users.service.ProgressService;
import com.kujacic.users.service.UserListener;
import com.kujacic.users.service.UserPublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserListenerTest {

    private final CourseFactory courseFactory = new CourseFactory();

    private final ProgressFactory progressFactory = new ProgressFactory();

    @Mock
    private ProgressRepository progressRepository;

    @Mock
    private AchievementService achievementService;

    @Mock
    private ProgressService progressService;

    @Mock
    private UserPublisher userPublisher;

    @InjectMocks
    private UserListener userListener;

    @AfterEach
    void cleanup() {
        MDC.clear();
    }

    @Test
    void shouldSetMDCValues() {
        AtomicReference<String> capturedCorrelationId = new AtomicReference<>();
        AtomicReference<String> capturedLevelId = new AtomicReference<>();

        CourseLevelPassEvent event = courseFactory.createCourseLevelPassEvent();
        ProgressResponseDTO response = progressFactory.createProgressResponse(1, 50);

        when(progressService.createProgress(any())).thenAnswer(invocation -> {
            capturedCorrelationId.set(MDC.get("correlationId"));
            capturedLevelId.set(MDC.get("levelId"));
            return response;
        });

        userListener.handleCourseLevelEvent(event, "test-correlation-id", "123");

        assertEquals("test-correlation-id", capturedCorrelationId.get());
        assertEquals("123", capturedLevelId.get());
    }

    @Test
    void shouldClearMDCAfterProcessing() {
        CourseLevelPassEvent event = courseFactory.createCourseLevelPassEvent();
        ProgressResponseDTO response = progressFactory.createProgressResponse(1, 50);

        when(progressService.createProgress(any())).thenReturn(response);

        userListener.handleCourseLevelEvent(event, "test-correlation-id", "123");

        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("levelId"));
    }

    @Test
    void shouldClearMDCOnException() {
        CourseLevelPassEvent event = courseFactory.createCourseLevelPassEvent();

        when(progressService.createProgress(any())).thenThrow(new RuntimeException("Test error"));

        assertThrows(RuntimeException.class, () ->
                userListener.handleCourseLevelEvent(event, "test-correlation-id", "123")
        );

        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("levelId"));
    }

    @Test
    void shouldCreateProgress() {
        CourseLevelPassEvent event = courseFactory.createCourseLevelPassEvent();
        ProgressResponseDTO response = progressFactory.createProgressResponse(1, 50);

        when(progressService.createProgress(any())).thenReturn(response);

        userListener.handleCourseLevelEvent(event, "test-correlation-id", "123");

        verify(progressService).createProgress(event);
    }

    @Test
    void shouldRequestCertificateWhenProgressIs100() {
        CourseLevelPassEvent event = courseFactory.createCourseLevelPassEvent("user123", 100);
        ProgressResponseDTO response = progressFactory.createProgressResponse(150, 1);

        when(progressService.createProgress(any())).thenReturn(response);

        userListener.handleCourseLevelEvent(event, "test-correlation-id", "123");

        verify(userPublisher).requestCertificate(eq(1), eq("user123"));
    }

    @Test
    void shouldRequestCertificateWhenProgressIsGreaterThan100() {
        CourseLevelPassEvent event = courseFactory.createCourseLevelPassEvent("user123", 150);
        ProgressResponseDTO response = progressFactory.createProgressResponse(150, 1);

        when(progressService.createProgress(any())).thenReturn(response);

        userListener.handleCourseLevelEvent(event, "test-correlation-id", "123");

        verify(userPublisher).requestCertificate(eq(1), eq("user123"));
    }

    @Test
    void shouldNotRequestCertificateWhenProgressIsLessThan100() {
        CourseLevelPassEvent event = courseFactory.createCourseLevelPassEvent("user123", 99);
        ProgressResponseDTO response = progressFactory.createProgressResponse(1, 99);

        when(progressService.createProgress(any())).thenReturn(response);

        userListener.handleCourseLevelEvent(event, "test-correlation-id", "123");

        verify(userPublisher, never()).requestCertificate(any(), any());
    }

    @Test
    void shouldNotRequestCertificateWhenProgressIs0() {
        CourseLevelPassEvent event = courseFactory.createCourseLevelPassEvent("user123", 0);
        ProgressResponseDTO response = progressFactory.createProgressResponse(1, 0);

        when(progressService.createProgress(any())).thenReturn(response);

        userListener.handleCourseLevelEvent(event, "test-correlation-id", "123");

        verify(userPublisher, never()).requestCertificate(any(), any());
    }

    @Test
    void shouldPassCorrectCourseIdAndUserIdToCertificateRequest() {
        CourseLevelPassEvent event = courseFactory.createCourseLevelPassEvent("user999", 150);
        event.setCourseId(999);
        ProgressResponseDTO response = progressFactory.createProgressResponse(999, 100);

        when(progressService.createProgress(any())).thenReturn(response);

        userListener.handleCourseLevelEvent(event, "test-correlation-id", "123");

        verify(userPublisher).requestCertificate(999, "user999");
    }

    @Test
    void shouldSetMDCValuesForCertificateEvent() {
        AtomicReference<String> capturedCorrelationId = new AtomicReference<>();
        AtomicReference<String> capturedLevelId = new AtomicReference<>();

        CourseCertificateIssuedEvent event = courseFactory.createCourseCertificateIssuedEvent();

        doAnswer(invocation -> {
            capturedCorrelationId.set(MDC.get("correlationId"));
            capturedLevelId.set(MDC.get("levelId"));
            return null;
        }).when(achievementService).createAchievement(any(), any());

        userListener.handleCertificateReceivedEvent(event, "cert-correlation-id", "456");

        assertEquals("cert-correlation-id", capturedCorrelationId.get());
        assertEquals("456", capturedLevelId.get());
    }

    @Test
    void shouldClearMDCAfterCertificateProcessing() {
        CourseCertificateIssuedEvent event = courseFactory.createCourseCertificateIssuedEvent();

        userListener.handleCertificateReceivedEvent(event, "cert-correlation-id", "456");

        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("levelId"));
    }

    @Test
    void shouldClearMDCOnCertificateException() {
        CourseCertificateIssuedEvent event = courseFactory.createCourseCertificateIssuedEvent();

        doThrow(new RuntimeException("Achievement error"))
                .when(achievementService).createAchievement(any(), any());

        assertThrows(RuntimeException.class, () ->
                userListener.handleCertificateReceivedEvent(event, "cert-correlation-id", "456")
        );

        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("levelId"));
    }

    @Test
    void shouldCreateAchievement() {
        CourseCertificateIssuedEvent event = courseFactory.createCourseCertificateIssuedEvent();
        event.setId(100L);
        event.setUserId("user456");

        userListener.handleCertificateReceivedEvent(event, "cert-correlation-id", "456");

        verify(achievementService).createAchievement(100L, "user456");
    }

    @Test
    void shouldRethrowException() {
        CourseCertificateIssuedEvent event = courseFactory.createCourseCertificateIssuedEvent();
        RuntimeException exception = new RuntimeException("Service unavailable");

        doThrow(exception).when(achievementService).createAchievement(any(), any());

        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                userListener.handleCertificateReceivedEvent(event, "cert-correlation-id", "456")
        );

        assertEquals("Service unavailable", thrown.getMessage());
    }

}
