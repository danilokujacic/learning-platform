package com.kujacic.users.dto.course;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kujacic.users.dto.courseLevel.CourseLevelResponse;
import com.kujacic.users.enums.CourseLevels;
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
    private Integer progress;
}