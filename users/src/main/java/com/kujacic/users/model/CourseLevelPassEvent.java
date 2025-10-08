package com.kujacic.users.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CourseLevelPassEvent {

    private String userId;
    private Integer courseId;
    private Long levelId;
    private Integer progress;
}
