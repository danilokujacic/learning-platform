package com.kujacic.users.model;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "progress")
public class Progress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false, unique = false)
    private String userId;

    @Column(name = "course_id", nullable = false)
    private Integer courseId;

    @Column(name = "progress")
    private Integer progress;

    @Column(name = "course_name")
    private String courseName;
}
