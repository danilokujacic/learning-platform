package com.kujacic.courses.dto.courseLevel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kujacic.courses.enums.CourseLevelPassedStatus;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CourseLevelUpdatedResponse {
    @JsonProperty("status")
    public CourseLevelPassedStatus courseLevelPassedStatus;
}
