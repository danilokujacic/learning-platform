package com.kujacic.courses.dto.rabbitmq;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kujacic.courses.dto.course.CourseResponseDTO;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCertificateIssuedEvent {
    private Long id;
    private String name;
    private String userId;
    private CourseResponseDTO course;
    private String referenceUrl;
}
