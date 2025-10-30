package com.kujacic.users.dto.progress;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProgressResponseDTO {

    private Integer id;

    private Integer progress;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("course_id")
    private Integer courseId;
}
