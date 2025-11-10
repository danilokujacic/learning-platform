package com.kujacic.users.service;

import com.kujacic.users.dto.PagedResponse;
import com.kujacic.users.dto.course.CourseResponseDTO;
import com.kujacic.users.dto.progress.ProgressResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final RestClient restClient;
    private final ProgressService progressService;
    public PagedResponse<CourseResponseDTO> getUserCourses(String userId, int page, int size) {
        Map<String, List<Integer>> requestBody = new HashMap<>();
        List<ProgressResponseDTO> progressResponse = progressService.getAllProgressesByUserId(userId);

        List<Integer> progresses = progressResponse.stream().map(ProgressResponseDTO::getCourseId).toList();
        log.info("User course ids {}", progresses);
        requestBody.put("course_ids", progresses);
        PagedResponse<CourseResponseDTO> response =  restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/courses/query")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(new ParameterizedTypeReference<PagedResponse<CourseResponseDTO>>() {});


        // Attach progress to course
        List<CourseResponseDTO> coursesWithProgress = new ArrayList<>();
        for(CourseResponseDTO courseResponse : response.getContent()) {
            for(ProgressResponseDTO progress : progressResponse) {
                if(courseResponse.getId().equals(progress.getCourseId())) {
                    courseResponse.setProgress(progress.getProgress());
                }
                coursesWithProgress.add(courseResponse);
            }

        }
        response.setContent(coursesWithProgress);
        return response;
    }
}
