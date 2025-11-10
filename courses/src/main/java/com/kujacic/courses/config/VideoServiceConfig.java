package com.kujacic.courses.config;

import com.kujacic.courses.service.StorageService;
import com.kujacic.courses.service.impl.LocalVideoService;
import com.kujacic.courses.service.impl.VideoService;
import com.kujacic.courses.utils.VideoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@RequiredArgsConstructor
public class VideoServiceConfig {
    private final VideoUtils videoUtils;
    private final S3Client s3Client;

    @Bean
    public StorageService localVideoService() {
        return new LocalVideoService();
    }

    @Bean
    @Profile("prod")
    public StorageService s3VideoService() {
        return new VideoService(videoUtils, s3Client);
    }

}
