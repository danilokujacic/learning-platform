package com.kujacic.users.service;

import com.kujacic.users.config.CourseClient;
import com.kujacic.users.dto.progress.ProgressRequestDTO;
import com.kujacic.users.dto.progress.ProgressResponseDTO;
import com.kujacic.users.exception.ProgressNotFoundException;
import com.kujacic.users.model.Progress;
import com.kujacic.users.repository.ProgressRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Log4j2
@AllArgsConstructor
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final CourseClient courseClient;

    public ProgressResponseDTO createProgress(String userId, ProgressRequestDTO progressRequest) {
        log.info("USER ID: {}", userId);
        // Make sure course exists in course service
        this.courseClient.getCourse(progressRequest.getCourseId());

        int updatedRows = progressRepository.updateProgressByCourseIdAndUserId(progressRequest.getProgress(), progressRequest.getCourseId(), userId);

        Progress progress;
        if(updatedRows > 0) {
            progress = progressRepository
                    .findByCourseIdAndUserId(progressRequest.getCourseId(), userId)
                    .orElseThrow(() -> new ProgressNotFoundException("Progress resource not found"));
        }else {
            progress = Progress.builder().userId(userId).courseId(progressRequest.getCourseId()).progress(progressRequest.getProgress()).build();
            progress = progressRepository.save(progress);
        }

        ProgressResponseDTO progressResponse = new ProgressResponseDTO();
        BeanUtils.copyProperties(progress, progressResponse);

        return progressResponse;
    }

}
