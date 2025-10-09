package com.kujacic.courses.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_levels")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CourseLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column
    private String name;

    @Column(nullable = false)
    private Integer progress = 0;
}
