package com.kujacic.users.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "progress")
@EntityListeners(AuditingEntityListener.class)
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
