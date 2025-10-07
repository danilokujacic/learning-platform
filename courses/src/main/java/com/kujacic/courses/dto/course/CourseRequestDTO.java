package com.kujacic.courses.dto.course;

import com.kujacic.courses.enums.CourseLevels;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CourseRequestDTO {

    @NotNull
    @Length(min = 5, max = 55, message = "Must be between 5 and 55 characters")
    private String name;

    @NotNull
    private CourseLevels level;
}
