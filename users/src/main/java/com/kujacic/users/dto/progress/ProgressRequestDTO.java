package com.kujacic.users.dto.progress;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProgressRequestDTO {
    @NotNull(message = "Course id must be provdied")
    @JsonProperty("course_id")
    private Integer courseId;

    @NotNull(message = "Progress must be provdied")
    @Min(value = 1, message = "Progress must be greater than 0")
    @Max(value = 100, message = "Progress must be less than or equal to 100")
    private Integer progress;
}
