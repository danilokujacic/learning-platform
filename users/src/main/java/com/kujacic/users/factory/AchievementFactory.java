package com.kujacic.users.factory;

import com.kujacic.users.model.Achievement;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AchievementFactory {

    public Achievement createAchievement(String userId) {
        return Achievement.builder()
                .userId(userId)
                .id(1L)
                .certificateId(1L)
                .build();
    }

    public Achievement createAchievement() {
        String userId = UUID.randomUUID().toString();
        return Achievement.builder()
                .userId(userId)
                .id(1L)
                .certificateId(1L)
                .build();
    }
}
