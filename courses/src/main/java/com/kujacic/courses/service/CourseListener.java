package com.kujacic.courses.service;

import com.kujacic.courses.dto.course.CourseResponseDTO;
import com.kujacic.courses.dto.rabbitmq.CourseCertificateIssuedEvent;
import com.kujacic.courses.dto.rabbitmq.CertificateRequest;
import com.kujacic.courses.model.Course;
import com.kujacic.courses.model.CourseCertificate;
import com.kujacic.courses.repository.CourseCertificateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class CourseListener {
    private final CourseCertificateRepository courseCertificateRepository;
    private final CoursePublisher coursesPublisher;

    @RabbitListener(queues = "certificate-request-queue")
    public void certificateRequestListener(
        CertificateRequest certificateRequest
    ) {
        Optional<CourseCertificate> certificate = courseCertificateRepository.findByCourseId(certificateRequest.getCourseId());

        if(certificate.isPresent()) {
            log.info("Issuing certificate for user: {}", certificateRequest.getUserId());

            CourseCertificate resolvedCert = certificate.get();
            Course course = resolvedCert.getCourse();

            CourseResponseDTO courseResponse = CourseResponseDTO.builder()
                    .id(course.getId())
                    .name(course.getName())
                    .build();

            CourseCertificateIssuedEvent courseCertificateResponse = CourseCertificateIssuedEvent.builder()
                    .id(resolvedCert.getId())
                    .course(courseResponse)
                    .userId(certificateRequest.getUserId())
                    .build();
            coursesPublisher.courseCertificatePublisher(courseCertificateResponse);
        }else {
            log.info("No certificate for this course :(");
        }

    }
}
