package com.kujacic.users.listener;

import com.kujacic.users.model.CourseLevelPassEvent;
import com.kujacic.users.model.Progress;
import com.kujacic.users.repository.ProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@RequiredArgsConstructor
public class CourseListener {

    private final ProgressRepository progressRepository;

    @RabbitListener(queues = "courses-queue")
    public void handleCourseLevelEvent(CourseLevelPassEvent courseLevel, @Header(AmqpHeaders.CORRELATION_ID) String correlationId,
                                       @Header("levelId") String levelId) {
        MDC.put("correlationId", correlationId);
        MDC.put("levelId", levelId);

        try {
            log.info("Processing user progress");
            Progress progress = Progress.builder().userId(courseLevel.getUserId()).courseId(courseLevel.getCourseId()).progress(courseLevel.getProgress()).build();
            Progress newProgress = progressRepository.save(progress);

            log.info("Added new progress: {}", newProgress.getId());
        } finally {
            MDC.clear();
        }

    }
}
