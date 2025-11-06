package com.kujacic.courses.dto.video;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoPost {
    @NotNull
    @JsonProperty("video_id")
    private String videoId;

}
