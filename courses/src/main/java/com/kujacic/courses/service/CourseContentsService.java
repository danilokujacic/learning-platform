package com.kujacic.courses.service;

import com.kujacic.courses.dto.content.ContentResponseDTO;
import com.kujacic.courses.dto.video.VideoMetadata;
import com.kujacic.courses.exception.CourseContentExistForLevel;
import com.kujacic.courses.exception.CourseLevelNotFoundException;
import com.kujacic.courses.exception.VideoProcessingError;
import com.kujacic.courses.model.CourseContent;
import com.kujacic.courses.model.CourseLevel;
import com.kujacic.courses.repository.CourseContentRepository;
import com.kujacic.courses.repository.CourseLevelsRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class CourseContentsService {
    private final CourseContentRepository courseContentRepository;
    private final CourseLevelsRepository courseLevelsRepository;
    private final VideoService videoService;



    public ContentResponseDTO createContent(Long courseLevelId, MultipartFile file){
            Boolean contentExistForLevel = courseContentRepository.existByCourseLevelId(courseLevelId);
            if(contentExistForLevel) throw new CourseContentExistForLevel();
            CourseLevel courseLevel = courseLevelsRepository.findById(courseLevelId).orElseThrow(() -> new CourseLevelNotFoundException("Course level not found"));

            try {
                UUID contentId = UUID.randomUUID();
                String url = videoService.uploadVideo(contentId.toString(), file);
                CourseContent courseContent = CourseContent.builder()
                        .id(contentId)
                        .courseLevel(courseLevel)
                        .url(url)
                        .build();



                CourseContent savedCourseContent = courseContentRepository.save(courseContent);

                return ContentResponseDTO.builder()
                        .id(savedCourseContent.getId().toString())
                        .courseLevelId(savedCourseContent.getCourseLevel().getId())
                        .url(savedCourseContent.getUrl())
                        .build();
            }catch(IOException err){
                throw new VideoProcessingError();
            }


    }

}
