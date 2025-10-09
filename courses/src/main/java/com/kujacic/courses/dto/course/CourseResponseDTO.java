package com.kujacic.courses.dto.course;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kujacic.courses.dto.courseLevel.CourseLevelResponse;
import com.kujacic.courses.enums.CourseLevels;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CourseResponseDTO {
    private Integer id;
    private String name;
    private CourseLevels level;

    @JsonProperty("course_levels")
    private List<CourseLevelResponse> courseLevels;
}
