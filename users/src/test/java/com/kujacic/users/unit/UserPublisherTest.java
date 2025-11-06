package com.kujacic.users.unit;

import com.kujacic.users.dto.rabbitmq.CourseCertificateRequest;
import com.kujacic.users.service.UserPublisher;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private UserPublisher userPublisher;

    @Test
    void shouldConvertAndSendProperCertificateRequest() {
        String exchange = "users-exchange";
        String exchangeKey = "course-certificate.requested";
        Integer courseId = 1;
        String userId = UUID.randomUUID().toString();
        userPublisher.requestCertificate(courseId, userId);

        ArgumentCaptor<CourseCertificateRequest> captor =
                ArgumentCaptor.forClass(CourseCertificateRequest.class);

        verify(rabbitTemplate).convertAndSend(eq(exchange), eq(exchangeKey),  captor.capture(), any(MessagePostProcessor.class) );

        CourseCertificateRequest captured = captor.getValue();

        assertEquals(userId, captured.getUserId());
        assertEquals(courseId, captured.getCourseId());
    }

    @Test
    void shouldSetProperMDCWhenCallingConvertAndSend() {
        Integer courseId = 1;
        String userId = UUID.randomUUID().toString();

        AtomicReference<String> courseIdReference = new AtomicReference<>();
        AtomicReference<String> correlationIdReference = new AtomicReference<>();

        doAnswer(invocation -> {
            correlationIdReference.set(MDC.get("correlationId"));
            courseIdReference.set(MDC.get("courseId"));
            return null;
        }).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(CourseCertificateRequest.class), any(MessagePostProcessor.class));

        userPublisher.requestCertificate(courseId, userId);

        assertNotNull(correlationIdReference.get(), "correlationId should be set in MDC");
        assertFalse(correlationIdReference.get().isEmpty());

        assertEquals("1", courseIdReference.get(), "courseId should be set in MDC");

        assertDoesNotThrow(() -> UUID.fromString(correlationIdReference.get()));

        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("courseId"));

    }
}
