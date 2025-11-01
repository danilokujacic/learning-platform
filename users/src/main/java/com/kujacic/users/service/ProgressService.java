package com.kujacic.users.service;

import com.kujacic.users.dto.progress.ProgressResponseDTO;
import com.kujacic.users.dto.rabbitmq.CourseLevelPassEvent;
import com.kujacic.users.exception.CouldNotParseExcelException;
import com.kujacic.users.model.Progress;
import com.kujacic.users.repository.ProgressRepository;
import com.kujacic.users.util.DocumentUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class ProgressService {
    private final ProgressRepository progressRepository;


    public byte[] exportProgress(String userId) {
        List<Progress> progresses = progressRepository.findByUserId(userId);
        try {
            return DocumentUtils.generateProgressDocument(progresses);
        } catch(IOException e) {
            throw new CouldNotParseExcelException();
        }


    }

    public Progress createProgress(CourseLevelPassEvent courseLevel) {
       Optional<Progress> progress = progressRepository
                .findByCourseIdAndUserId(courseLevel.getCourseId(), courseLevel.getUserId());

        if(progress.isPresent()) {
            Progress foundProgress = progress.get();
            progressRepository.updateProgressByCourseIdAndUserId(foundProgress.getProgress() + courseLevel.getProgress(), courseLevel.getCourseId(), courseLevel.getUserId());

           return foundProgress;
        }

        Progress new_progress = Progress.builder().userId(courseLevel.getUserId()).courseName(courseLevel.getCourseName()).courseId(courseLevel.getCourseId()).progress(courseLevel.getProgress()).build();
        progressRepository.save(new_progress);

        ProgressResponseDTO progressResponse = new ProgressResponseDTO();
        BeanUtils.copyProperties(new_progress, progressResponse);

        return new_progress;
    }

}
