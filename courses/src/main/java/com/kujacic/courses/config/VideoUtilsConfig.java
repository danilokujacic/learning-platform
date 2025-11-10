package com.kujacic.courses.config;

import com.kujacic.courses.utils.VideoUtils;
import com.kujacic.courses.utils.impl.LocalVideoUtils;
import com.kujacic.courses.utils.impl.S3VideoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@RequiredArgsConstructor
public class VideoUtilsConfig {
    private final S3Client s3Client;

    @Bean
    @Profile("prod")
    public VideoUtils s3VideoUtils() {
        return new S3VideoUtils(s3Client);
    }

    @Bean
    public VideoUtils localVideoUtils() {
        return new LocalVideoUtils();
    }
}
