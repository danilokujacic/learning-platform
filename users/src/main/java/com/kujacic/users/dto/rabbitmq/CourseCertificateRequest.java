package com.kujacic.users.dto.rabbitmq;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CourseCertificateRequest {

    private String userId;
    private Integer courseId;
}
