package com.kujacic.courses.dto.course;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QueryCourseDTO {
    @JsonProperty("course_ids")
    private List<Integer> courseIds;
}
