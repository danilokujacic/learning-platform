package com.kujacic.users.repository;


import com.kujacic.users.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    @Query("SELECT a FROM Achievement a WHERE a.userId = :userId")
    Optional<List<Achievement>> findAllAchievementsByUserId(@Param("userId") String userId);
}
