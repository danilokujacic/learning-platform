package com.kujacic.users.service;

import com.fasterxml.jackson.databind.util.BeanUtil;
import com.kujacic.users.config.CourseClient;
import com.kujacic.users.dto.course.CourseResponseDTO;
import com.kujacic.users.dto.progress.ProgressRequestDTO;
import com.kujacic.users.dto.progress.ProgressResponseDTO;
import com.kujacic.users.exception.ProgressNotFoundException;
import com.kujacic.users.model.Progress;
import com.kujacic.users.repository.ProgressRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Log4j2
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final CourseClient courseClient;

    public ProgressService(ProgressRepository progressRepository, CourseClient courseClient) {
        this.progressRepository = progressRepository;
        this.courseClient = courseClient;
    }

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
            progress = new Progress();
            progress.setUserId(userId);
            progress.setCourseId(progressRequest.getCourseId());
            progress.setProgress(progressRequest.getProgress());
            progress = progressRepository.save(progress);
        }

        ProgressResponseDTO progressResponse = new ProgressResponseDTO();
        BeanUtils.copyProperties(progress, progressResponse);

        return progressResponse;
    }

}
