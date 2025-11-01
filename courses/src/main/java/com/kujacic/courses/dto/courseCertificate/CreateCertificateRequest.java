package com.kujacic.courses.dto.courseCertificate;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateCertificateRequest {
    @NotNull
    @Length(min = 3, max = 55, message = "Certificate name must be between 3 and 55 chars long")
    private String name;

    @JsonProperty("reference_url")
    @NotNull
    @Length(min = 3, max = 150, message = "Reference url must be between 3 and 150 chars long")
    private String referenceUrl;
}
