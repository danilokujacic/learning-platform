package com.kujacic.users.factory;

import com.kujacic.users.dto.course.CourseResponseDTO;
import com.kujacic.users.dto.rabbitmq.CourseCertificateIssuedEvent;
import com.kujacic.users.dto.rabbitmq.CourseLevelPassEvent;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CourseFactory {

    public CourseLevelPassEvent createCourseLevelPassEvent(String userUUID) {
        return CourseLevelPassEvent.builder()
                .userId(userUUID)
                .courseId(1)
                .levelId(1L)
                .courseName("Test course")
                .progress(25)
                .build();
    }

    public CourseLevelPassEvent createCourseLevelPassEvent(String userUUID, Integer progress) {
        return CourseLevelPassEvent.builder()
                .userId(userUUID)
                .courseId(1)
                .levelId(1L)
                .courseName("Test course")
                .progress(progress)
                .build();
    }

    public CourseLevelPassEvent createCourseLevelPassEvent() {
        String userUUID = UUID.randomUUID().toString();
        return CourseLevelPassEvent.builder()
                .userId(userUUID)
                .courseId(1)
                .levelId(1L)
                .courseName("Test course")
                .progress(25)
                .build();
    }

    public CourseCertificateIssuedEvent createCourseCertificateIssuedEvent() {
        CourseResponseDTO course = new CourseResponseDTO();
        course.setCourseId(1);

        CourseCertificateIssuedEvent event = new CourseCertificateIssuedEvent();
        event.setId(1L);
        event.setUserId("user123");
        event.setName("Spring Boot Certificate");
        event.setCourse(course);
        event.setReferenceUrl("https://certificates.example.com/cert-123");
        return event;
    }
}
