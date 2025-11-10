package com.kujacic.courses.service;

import com.kujacic.courses.dto.content.ContentResponseDTO;
import com.kujacic.courses.exception.CourseContentExistForLevel;
import com.kujacic.courses.exception.CourseLevelNotFoundException;
import com.kujacic.courses.exception.VideoProcessingError;
import com.kujacic.courses.model.Content;
import com.kujacic.courses.model.Level;
import com.kujacic.courses.repository.ContentRepository;
import com.kujacic.courses.repository.LevelRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class ContentService {
    private final ContentRepository contentRepository;
    private final LevelRepository levelRepository;
    private final StorageService videoService;



    public ContentResponseDTO createContent(Long courseLevelId, MultipartFile file){
            Boolean contentExistForLevel = contentRepository.existByCourseLevelId(courseLevelId);
            if(contentExistForLevel) throw new CourseContentExistForLevel();
            Level level = levelRepository.findById(courseLevelId).orElseThrow(() -> new CourseLevelNotFoundException("Course level not found"));

            try {
                UUID contentId = UUID.randomUUID();
                String url = videoService.uploadVideo(contentId.toString(), file);
                Content content = Content.builder()
                        .id(contentId)
                        .level(level)
                        .url(url)
                        .build();



                Content savedContent = contentRepository.save(content);

                return ContentResponseDTO.builder()
                        .id(savedContent.getId().toString())
                        .courseLevelId(savedContent.getLevel().getId())
                        .url(savedContent.getUrl())
                        .build();
            }catch(IOException err){
                throw new VideoProcessingError();
            }


    }

}
