package com.kujacic.users.service;

import com.kujacic.users.model.Achievement;
import com.kujacic.users.repository.AchievementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;


    public void createAchievement(Long certificateId, String userId) {
        Achievement achievement = Achievement.builder().userId(userId).certificateId(certificateId).build();
        achievementRepository.save(achievement);
    }

    public List<Achievement> getUserAchievements(String userId) {
        List<Achievement> achievements = achievementRepository.findAllAchievementsByUserId(userId).orElseThrow(() -> new RuntimeException("No achievements found"));
        return achievements;
    }
}
