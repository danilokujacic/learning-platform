package com.kujacic.courses.repository;

import com.kujacic.courses.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate,Long> {

    @Query("SELECT cc FROM CourseCertificate cc JOIN FETCH cc.course WHERE cc.course.id = :courseId")
    Optional<Certificate> findByCourseId(Integer courseId);

    void deleteById(Long certificateId);
}
