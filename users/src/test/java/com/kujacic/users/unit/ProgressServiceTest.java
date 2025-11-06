package com.kujacic.users.unit;

import com.kujacic.users.dto.progress.ProgressResponseDTO;
import com.kujacic.users.dto.rabbitmq.CourseLevelPassEvent;
import com.kujacic.users.exception.CouldNotParseExcelException;
import com.kujacic.users.factory.ProgressFactory;
import com.kujacic.users.model.Progress;
import com.kujacic.users.repository.ProgressRepository;
import com.kujacic.users.service.ProgressService;
import com.kujacic.users.util.DocumentUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProgressServiceTest {

    private final ProgressFactory progressFactory = new ProgressFactory();

    @Mock
    private ProgressRepository progressRepository;

    @InjectMocks
    private ProgressService progressService;

    @Test
    public void progressService_shouldReturnDocumentFromExportProgress() {
        try(MockedStatic<DocumentUtils> utils = mockStatic(DocumentUtils.class)) {
            byte[] expectedDocument = new byte[]{1, 2, 3};
            String uuid = UUID.randomUUID().toString();
            Progress progress = progressFactory.createProgress(uuid);

            when(progressRepository.findByUserId(uuid)).thenReturn(List.of(progress));
            utils.when(() -> DocumentUtils.generateProgressDocument(List.of(progress))).thenReturn(expectedDocument);

            byte[] result = progressService.exportProgress(uuid);
            assertArrayEquals(expectedDocument, result);
        }

    }
    @Test
    public void progressService_shouldThrowCouldNotParseErrorOnIOException() {
        try(MockedStatic<DocumentUtils> utils = mockStatic(DocumentUtils.class)) {
            String uuid = UUID.randomUUID().toString();
            Progress progress = progressFactory.createProgress(uuid);

            when(progressRepository.findByUserId(uuid)).thenReturn(List.of(progress));
            utils.when(() -> DocumentUtils.generateProgressDocument(List.of(progress))).thenThrow(new IOException("Failed to generate document"));

            assertThrows(CouldNotParseExcelException.class, () ->  progressService.exportProgress(uuid));
        }

    }

    @Test
    public void progressService_shouldUpdateProgressValueIfExistsOnCreateProgress() {
        String userUUID = UUID.randomUUID().toString();
        CourseLevelPassEvent courseLevelPassEvent = CourseLevelPassEvent.builder()
                .userId(userUUID)
                .courseId(1)
                .levelId(1L)
                .courseName("Test course")
                .progress(25)
                .build();

        Progress progress = progressFactory.createProgress(userUUID);

        ProgressResponseDTO foundProgress = ProgressResponseDTO.builder()
                .id(1)
                .courseId(1)
                .userId(userUUID)
                .progress(progress.getProgress() + courseLevelPassEvent.getProgress())
                .build();

        when(progressRepository.findByCourseIdAndUserId(courseLevelPassEvent.getCourseId(), courseLevelPassEvent.getUserId())).thenReturn(Optional.ofNullable(progress));

        ProgressResponseDTO returnProgress = progressService.createProgress(courseLevelPassEvent);
        assertEquals(returnProgress.getCourseId(), foundProgress.getCourseId());
        assertEquals(returnProgress.getProgress(),foundProgress.getProgress() );
        assertEquals(returnProgress.getUserId(), foundProgress.getUserId());

    }

    @Test
    public void progressService_shouldCreateNewProgressIfNoProgressExist() {
        String userUUID = UUID.randomUUID().toString();
        CourseLevelPassEvent courseLevelPassEvent = CourseLevelPassEvent.builder().userId(userUUID).courseId(1).levelId(1L).courseName("Test course").progress(25).build();
        ProgressResponseDTO expectedProgressResponse = ProgressResponseDTO.builder().userId(userUUID).courseId(1).progress(25).build();

        when(progressRepository.findByCourseIdAndUserId(courseLevelPassEvent.getCourseId(), courseLevelPassEvent.getUserId())).thenReturn(Optional.empty());

        ProgressResponseDTO progressResponse = progressService.createProgress(courseLevelPassEvent);

        assertEquals(progressResponse.getProgress(),expectedProgressResponse.getProgress() );
        assertEquals(progressResponse.getUserId(),expectedProgressResponse.getUserId() );
        assertEquals(progressResponse.getCourseId(),expectedProgressResponse.getCourseId() );
    }
}
