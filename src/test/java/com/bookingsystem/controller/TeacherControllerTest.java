package com.bookingsystem.controller;

import com.bookingsystem.dto.request.CreateTeacherRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TeacherControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/teachers — creates teacher successfully")
    void createTeacher_success() throws Exception {
        var request = new CreateTeacherRequest("Alice Johnson", "alice@test.com", "America/New_York");

        mockMvc.perform(post("/api/v1/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Alice Johnson"))
                .andExpect(jsonPath("$.email").value("alice@test.com"))
                .andExpect(jsonPath("$.timezone").value("America/New_York"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("POST /api/v1/teachers — rejects invalid timezone")
    void createTeacher_invalidTimezone() throws Exception {
        var request = new CreateTeacherRequest("Bob", "bob@test.com", "Not/ATimezone");

        mockMvc.perform(post("/api/v1/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Not/ATimezone")));
    }

    @Test
    @DisplayName("POST /api/v1/teachers — rejects missing name")
    void createTeacher_missingName() throws Exception {
        var request = new CreateTeacherRequest("", "bob@test.com", "UTC");

        mockMvc.perform(post("/api/v1/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    @DisplayName("POST /api/v1/teachers — rejects duplicate email")
    void createTeacher_duplicateEmail() throws Exception {
        var request = new CreateTeacherRequest("Alice", "alice@test.com", "UTC");

        // First creation succeeds
        mockMvc.perform(post("/api/v1/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second creation with same email fails
        mockMvc.perform(post("/api/v1/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/v1/teachers/{id}/offerings — 404 for unknown teacher")
    void getOfferings_notFound() throws Exception {
        mockMvc.perform(get("/api/v1/teachers/9999/offerings"))
                .andExpect(status().isNotFound());
    }
}
