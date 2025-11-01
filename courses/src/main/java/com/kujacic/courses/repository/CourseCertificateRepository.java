package com.kujacic.courses.repository;

import com.kujacic.courses.model.CourseCertificate;
import jakarta.ws.rs.Path;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseCertificateRepository extends JpaRepository<CourseCertificate,Long> {

    @Query("SELECT cc FROM CourseCertificate cc JOIN FETCH cc.course WHERE cc.course.id = :courseId")
    Optional<CourseCertificate> findByCourseId(Integer courseId);

    void deleteById(Long certificateId);
}
