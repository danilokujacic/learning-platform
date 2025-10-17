package com.kujacic.courses.service;

import com.kujacic.courses.dto.rabbitmq.CourseCertificateIssuedEvent;
import com.kujacic.courses.dto.rabbitmq.CourseLevelPassEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoursePublisher {
    private final RabbitTemplate rabbitTemplate;

    public void courseCertificatePublisher(CourseCertificateIssuedEvent certificate) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        MDC.put("courseId", certificate.getCourse().getId() + "");

        try {
            rabbitTemplate.convertAndSend("course-exchange", "course-certificate.issued", certificate, message -> {
                message.getMessageProperties().setCorrelationId(correlationId);
                message.getMessageProperties().setHeader("levelId", certificate.getCourse().getId() + "");
                return message;
            });
        } finally {
            MDC.clear();
        }
    }

    public void courseLevelPublisher(Integer courseId, Long levelId, String userId, Integer progress, String courseName) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        MDC.put("levelId", levelId + "");
        try {
            log.info("Course level passed {}", levelId);
            CourseLevelPassEvent courseLevelPassEvent = CourseLevelPassEvent.builder().courseName(courseName).courseId(courseId).levelId(levelId).userId(userId).progress(progress).build();
            rabbitTemplate.convertAndSend("course-exchange", "course-level.passed", courseLevelPassEvent, message -> {
                message.getMessageProperties().setCorrelationId(correlationId);
                message.getMessageProperties().setHeader("levelId", levelId);
                return message;
            });
        } finally {
            MDC.clear();
        }

    }
}
