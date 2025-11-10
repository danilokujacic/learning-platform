package com.kujacic.courses.dto.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContentResponseDTO {
    String id;

    @JsonProperty("course_level_id")
    Long courseLevelId;

    String url;
}
