package com.kujacic.users.service;

import com.kujacic.users.dto.progress.ProgressResponseDTO;
import com.kujacic.users.dto.rabbitmq.CourseCertificateIssuedEvent;
import com.kujacic.users.dto.rabbitmq.CourseLevelPassEvent;
import com.kujacic.users.model.Progress;
import com.kujacic.users.repository.ProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserListener {

    private final ProgressRepository progressRepository;
    private final AchievementService achievementService;
    private final ProgressService progressService;
    private final UserPublisher userPublisher;

    @RabbitListener(queues = "courses-queue")
    public void handleCourseLevelEvent(CourseLevelPassEvent courseLevel, @Header(AmqpHeaders.CORRELATION_ID) String correlationId,
                                       @Header("levelId") String levelId) {
        MDC.put("correlationId", correlationId);
        MDC.put("levelId", levelId);

        try {
            log.info("Processing user progress");
            ProgressResponseDTO progress = progressService.createProgress(courseLevel);
            if(progress.getProgress() >= 100) {
                log.info("Course {} passed, requesting certificate for user {}", courseLevel.getCourseId(), courseLevel.getUserId());
                userPublisher.requestCertificate(courseLevel.getCourseId(), courseLevel.getUserId());

            }
            log.info("Added progress: {}", progress.getId());
        } finally {
            MDC.clear();
        }



    }

    @RabbitListener(queues = "course-certificates-queue")
    public void handleCertificateReceivedEvent(CourseCertificateIssuedEvent courseCertificateIssuedEvent,  @Header(AmqpHeaders.CORRELATION_ID) String correlationId,
                                               @Header("levelId") String levelId) {
        MDC.put("correlationId", correlationId);
        MDC.put("levelId", levelId);
        try {
            log.info("Processing achievement...");
            achievementService.createAchievement(courseCertificateIssuedEvent.getId(), courseCertificateIssuedEvent.getUserId());

            log.info("Achievement processed...");
        } catch(Exception e) {
            throw e; // Cause queue job to retry
        } finally {
            MDC.clear();
        }
    }
}
