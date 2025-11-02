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
import java.util.stream.Collectors;

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

    public List<ProgressResponseDTO> getAllProgressesByUserId(String userId) {
        List<Progress> progressesByUser = progressRepository.findByUserId(userId);

        return progressesByUser.stream().map(progress -> ProgressResponseDTO.builder()
                .userId(progress.getUserId())
                .progress(progress.getProgress())
                .courseId(progress.getCourseId())
                .build()).collect(Collectors.toList());

    }

    public ProgressResponseDTO createProgress(CourseLevelPassEvent courseLevel) {
       Optional<Progress> progress = progressRepository
                .findByCourseIdAndUserId(courseLevel.getCourseId(), courseLevel.getUserId());

        if(progress.isPresent()) {
            Progress foundProgress = progress.get();
            progressRepository.updateProgressByCourseIdAndUserId(foundProgress.getProgress() + courseLevel.getProgress(), courseLevel.getCourseId(), courseLevel.getUserId());

            return ProgressResponseDTO.builder().id(foundProgress.getId()).progress(foundProgress.getProgress() + courseLevel.getProgress()).courseId(foundProgress.getCourseId()).userId(foundProgress.getUserId()).build();
        }

        Progress new_progress = Progress.builder().userId(courseLevel.getUserId()).courseName(courseLevel.getCourseName()).courseId(courseLevel.getCourseId()).progress(courseLevel.getProgress()).build();
        progressRepository.save(new_progress);

        return ProgressResponseDTO.builder().id(new_progress.getId()).progress(new_progress.getProgress()).courseId(new_progress.getCourseId()).userId(new_progress.getUserId()).build();
    }

}
