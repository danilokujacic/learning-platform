package com.kujacic.courses.service;

import com.kujacic.courses.model.CourseLevelPassEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoursesPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void courseLevelPublisher(Integer courseId, Long levelId, String userId, Integer progress) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        MDC.put("levelId", levelId + "");
        try {
            log.info("Course level passed {}", levelId);
            CourseLevelPassEvent courseLevelPassEvent = CourseLevelPassEvent.builder().courseId(courseId).levelId(levelId).userId(userId).progress(progress).build();
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
