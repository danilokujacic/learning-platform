package com.kujacic.courses.dto.courseLevel;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateCourseLevelDTO {

    @NotNull
    @Length(min = 3, max = 55, message = "Must be between 3 and 55 characters")
    private String name;

    @NotNull
    @Range(min = 0, max = 100, message = "Must be between 0 and 100")
    private Integer progress;
}
