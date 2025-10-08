package com.kujacic.users.config;

import com.kujacic.users.dto.course.CourseResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "course-service", configuration = FeignConfig.class)
public interface CourseClient {
    @GetMapping("/api/courses/{id}")
    CourseResponseDTO getCourse(@PathVariable Integer id);
}