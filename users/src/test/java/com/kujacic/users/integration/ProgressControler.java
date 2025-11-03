package com.kujacic.users.integration;

import com.kujacic.users.model.Progress;
import com.kujacic.users.repository.ProgressRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class ProgressControler extends BaseIntegrationTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProgressRepository progressRepository;

    @BeforeEach
    void setUp() {
        progressRepository.deleteAll();
    }

    @Test
    void shouldExportUserProgressAsXlsx() throws Exception {
        String userId = UUID.randomUUID().toString();
        createTestProgress(userId, 1, 50);
        createTestProgress(userId, 2, 75);
        createTestProgress(userId, 3, 100);

        byte[] result = mockMvc.perform(post("/api/users/progress/export")
                        .with(jwt().jwt(jwt -> jwt.subject(userId).claim("preferred_username", "test_user"))))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(header().string("Content-Disposition", containsString(".xlsx")))
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    private void createTestProgress(String userId, Integer courseId, Integer progress) {
        Progress p = new Progress();
        p.setUserId(userId);
        p.setCourseId(courseId);
        p.setProgress(progress);
        progressRepository.save(p);
    }
}
