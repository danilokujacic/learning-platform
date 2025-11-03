package com.kujacic.users.factory;

import com.kujacic.users.dto.progress.ProgressResponseDTO;
import com.kujacic.users.model.Progress;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProgressFactory {

    public Progress createProgress(String userUUID) {
        return Progress.builder()
                .id(1)
                .courseId(1)
                .userId(userUUID)
                .progress(25)
                .courseName("Test Course")
                .build();
    }

    public Progress createProgress() {
        String userUUID = UUID.randomUUID().toString();
        return Progress.builder()
                .id(1)
                .courseId(1)
                .userId(userUUID)
                .progress(25)
                .courseName("Test Course")
                .build();
    }

    public ProgressResponseDTO createProgressResponse(Integer progress, Integer courseId) {
        ProgressResponseDTO response = new ProgressResponseDTO();
        response.setId(1);
        response.setProgress(progress);
        response.setUserId("user123");
        response.setCourseId(courseId);
        return response;
    }

    public ProgressResponseDTO createProgressResponse() {
        ProgressResponseDTO response = new ProgressResponseDTO();
        response.setId(1);
        response.setProgress(25);
        response.setUserId("user123");
        response.setCourseId(1);
        return response;
    }

    public ProgressResponseDTO createProgressResponse(Integer progress) {
        ProgressResponseDTO response = new ProgressResponseDTO();
        response.setId(1);
        response.setProgress(progress);
        response.setUserId("user123");
        response.setCourseId(1);
        return response;
    }
}
