package com.kujacic.courses.dto.courseCertificate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kujacic.courses.dto.course.CourseResponseDTO;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CourseCertificateResponse {

    private Long id;

    private String name;

    private CourseResponseDTO course;

    @JsonProperty("reference_url")
    private String referenceUrl;
}
