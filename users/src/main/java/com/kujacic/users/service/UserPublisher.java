package com.kujacic.users.service;

import com.kujacic.users.dto.rabbitmq.CourseCertificateRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void requestCertificate(Integer courseId, String userId) {
        CourseCertificateRequest request = CourseCertificateRequest.builder().userId(userId).courseId(courseId).build();

        try {
            String correlationId = UUID.randomUUID().toString();
            MDC.put("correlationId", correlationId);
            MDC.put("courseId", courseId + "");
            rabbitTemplate.convertAndSend("users-exchange", "course-certificate.requested", request, message -> {
                message.getMessageProperties().setCorrelationId(correlationId);
                message.getMessageProperties().setHeader("courseId", courseId + "");
                return message;
            });
        } finally {
            MDC.clear();
        }

    }
}
