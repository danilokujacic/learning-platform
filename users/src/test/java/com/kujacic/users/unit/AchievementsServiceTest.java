package com.kujacic.users.unit;

import com.kujacic.users.model.Achievement;
import com.kujacic.users.repository.AchievementRepository;
import com.kujacic.users.service.AchievementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AchievementsServiceTest {

    @Mock
    private AchievementRepository achievementRepository;

    @InjectMocks
    private AchievementService achievementService;

    @Test
    public void achievementsService_shouldCreateAchievementWithProperData() {
        String userId = UUID.randomUUID().toString();
        Achievement achievement = Achievement.builder().userId(userId).id(1L).certificateId(1L).build();

        when(achievementRepository.save(any())).thenReturn(achievement);

        Achievement returnAchievement = achievementService.createAchievement(achievement.getCertificateId(), achievement.getUserId());

        assertEquals(returnAchievement.getUserId(), achievement.getUserId());
        assertEquals(returnAchievement.getCertificateId(), achievement.getCertificateId());
        assertEquals(returnAchievement.getId(), achievement.getId());
    }
}
