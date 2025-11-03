package com.kujacic.courses.dto.course;

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
    private List<Integer> courseIds;
}
