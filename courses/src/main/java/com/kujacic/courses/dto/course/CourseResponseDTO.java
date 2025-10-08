package com.kujacic.courses.dto.course;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kujacic.courses.enums.CourseLevels;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CourseResponseDTO {
    private Integer id;
    private String name;
    private CourseLevels level;
}
