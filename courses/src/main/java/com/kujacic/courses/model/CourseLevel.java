package com.kujacic.courses.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

    @OneToMany(mappedBy = "courseLevel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseContent> courseContents;

    @Column
    private String name;

    @Column(nullable = false)
    private Integer progress = 0;
}
