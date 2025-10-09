package com.kujacic.courses.dto.courseLevel;

import com.kujacic.courses.enums.CourseLevelPassedStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateCourseLevelStatus {

    @NotNull
    private CourseLevelPassedStatus status;
}
