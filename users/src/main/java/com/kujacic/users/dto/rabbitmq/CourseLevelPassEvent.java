package com.kujacic.users.dto.rabbitmq;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CourseLevelPassEvent {

    private String userId;
    private String courseName;
    private Integer courseId;
    private Long levelId;
    private Integer progress;
}
