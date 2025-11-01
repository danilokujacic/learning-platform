package com.kujacic.courses.dto.rabbitmq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseLevelPassEvent {

    private String userId;
    private Integer courseId;
    private Long levelId;
    private Integer progress;
    private String courseName;
}
