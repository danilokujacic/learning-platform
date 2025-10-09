package com.kujacic.courses.repository;

import com.kujacic.courses.model.CourseLevel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseLevelsRepository extends JpaRepository<CourseLevel, Long> {
}
