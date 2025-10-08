package com.kujacic.users.dto.progress;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProgressResponseDTO {

    private Integer progress;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("course_id")
    private Integer courseId;
}
