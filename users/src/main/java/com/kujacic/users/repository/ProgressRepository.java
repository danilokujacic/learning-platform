package com.kujacic.users.repository;


import com.kujacic.users.model.Progress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressRepository extends JpaRepository<Progress, Integer> {
}
