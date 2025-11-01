package com.kujacic.users.dto.rabbitmq;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kujacic.users.dto.course.CourseResponseDTO;
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
