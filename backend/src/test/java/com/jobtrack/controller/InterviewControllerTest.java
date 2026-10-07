package com.jobtrack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtrack.config.ApplicationConfig;
import com.jobtrack.config.SecurityConfig;
import com.jobtrack.dto.request.InterviewCreateRequest;
import com.jobtrack.dto.request.InterviewStatusUpdateRequest;
import com.jobtrack.dto.request.InterviewUpdateRequest;
import com.jobtrack.dto.response.InterviewResponse;
import com.jobtrack.entity.*;
import com.jobtrack.exception.GlobalExceptionHandler;
import com.jobtrack.exception.ResourceNotFoundException;
import com.jobtrack.security.JwtAuthenticationFilter;
import com.jobtrack.security.JwtService;
import com.jobtrack.service.InterviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InterviewController.class)
@Import({SecurityConfig.class, ApplicationConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = {
        "spring.security.filter.order=0",
        "application.security.jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
        "application.security.jwt.expiration=86400000",
        "application.cors.allowed-origins=http://localhost:4200"
})
@DisplayName("InterviewController — Tests d'intégration")
class InterviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InterviewService interviewService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private User authUser;
    private InterviewResponse sampleResponse;

    @BeforeEach
    void setUp() {
        authUser = User.builder()
                .id(1L)
                .email("alice@jobtrack.dev")
                .firstName("Alice")
                .role(Role.USER)
                .password("encodedPassword")
                .build();

        sampleResponse = InterviewResponse.builder()
                .id(10L)
                .applicationId(100L)
                .type(InterviewType.TECHNICAL)
                .status(InterviewStatus.SCHEDULED)
                .scheduledAt(Instant.parse("2026-10-15T10:00:00Z"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // ── POST ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/applications/{id}/interviews — création valide → 201 Created")
    void createInterview_valid_returns201() throws Exception {
        InterviewCreateRequest request = InterviewCreateRequest.builder()
                .type(InterviewType.TECHNICAL)
                .scheduledAt(Instant.parse("2026-10-15T10:00:00Z"))
                .location("Paris, présentiel")
                .build();

        when(interviewService.createInterview(eq(100L), any(InterviewCreateRequest.class), any(User.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/applications/100/interviews")
                        .with(user(authUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.type").value("TECHNICAL"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("POST — champs obligatoires manquants (type null) → 400")
    void createInterview_missingType_returns400() throws Exception {
        InterviewCreateRequest request = InterviewCreateRequest.builder()
                .scheduledAt(Instant.parse("2026-10-15T10:00:00Z"))
                // type missing
                .build();

        mockMvc.perform(post("/api/applications/100/interviews")
                        .with(user(authUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST — scheduledAt manquant → 400")
    void createInterview_missingScheduledAt_returns400() throws Exception {
        InterviewCreateRequest request = InterviewCreateRequest.builder()
                .type(InterviewType.HR)
                // scheduledAt missing
                .build();

        mockMvc.perform(post("/api/applications/100/interviews")
                        .with(user(authUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST — application inexistante ou d'un autre user → 404")
    void createInterview_applicationNotFound_returns404() throws Exception {
        InterviewCreateRequest request = InterviewCreateRequest.builder()
                .type(InterviewType.HR)
                .scheduledAt(Instant.now())
                .build();

        when(interviewService.createInterview(eq(999L), any(), any()))
                .thenThrow(new ResourceNotFoundException("Application", 999L));

        mockMvc.perform(post("/api/applications/999/interviews")
                        .with(user(authUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST — non authentifié → 403")
    void createInterview_unauthenticated_returns403() throws Exception {
        mockMvc.perform(post("/api/applications/100/interviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // ── GET list ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/applications/{id}/interviews — liste → 200 OK")
    void getInterviews_returns200() throws Exception {
        when(interviewService.getInterviews(eq(100L), any(User.class)))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/applications/100/interviews")
                        .with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].type").value("TECHNICAL"));
    }

    @Test
    @DisplayName("GET — application IDOR → 404")
    void getInterviews_idor_returns404() throws Exception {
        when(interviewService.getInterviews(eq(100L), any(User.class)))
                .thenThrow(new ResourceNotFoundException("Application", 100L));

        mockMvc.perform(get("/api/applications/100/interviews")
                        .with(user(authUser)))
                .andExpect(status().isNotFound());
    }

    // ── GET single ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/applications/{id}/interviews/{iid} — détail → 200 OK")
    void getInterview_returns200() throws Exception {
        when(interviewService.getInterview(eq(100L), eq(10L), any(User.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/applications/100/interviews/10")
                        .with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("GET interview — IDOR : interview d'une autre application → 404")
    void getInterview_idor_returns404() throws Exception {
        when(interviewService.getInterview(eq(100L), eq(10L), any(User.class)))
                .thenThrow(new ResourceNotFoundException("Interview", 10L));

        mockMvc.perform(get("/api/applications/100/interviews/10")
                        .with(user(authUser)))
                .andExpect(status().isNotFound());
    }

    // ── PUT ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/applications/{id}/interviews/{iid} — modification → 200 OK")
    void updateInterview_valid_returns200() throws Exception {
        InterviewUpdateRequest request = InterviewUpdateRequest.builder()
                .scheduledAt(Instant.parse("2026-11-01T14:00:00Z"))
                .location("Distanciel")
                .build();

        when(interviewService.updateInterview(eq(100L), eq(10L), any(InterviewUpdateRequest.class), any(User.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/api/applications/100/interviews/10")
                        .with(user(authUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    // ── PATCH /status ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /status — SCHEDULED→COMPLETED → 200 OK")
    void updateStatus_valid_returns200() throws Exception {
        InterviewStatusUpdateRequest request = InterviewStatusUpdateRequest.builder()
                .status(InterviewStatus.COMPLETED)
                .notes("Bien passé")
                .build();

        InterviewResponse completed = InterviewResponse.builder()
                .id(10L)
                .applicationId(100L)
                .status(InterviewStatus.COMPLETED)
                .build();

        when(interviewService.updateStatus(eq(100L), eq(10L), eq(InterviewStatus.COMPLETED), eq("Bien passé"), any(User.class)))
                .thenReturn(completed);

        mockMvc.perform(patch("/api/applications/100/interviews/10/status")
                        .with(user(authUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("PATCH /status — transition invalide (COMPLETED→CANCELLED) → 400")
    void updateStatus_invalidTransition_returns400() throws Exception {
        InterviewStatusUpdateRequest request = InterviewStatusUpdateRequest.builder()
                .status(InterviewStatus.CANCELLED)
                .build();

        when(interviewService.updateStatus(eq(100L), eq(10L), eq(InterviewStatus.CANCELLED), any(), any(User.class)))
                .thenThrow(new IllegalStateException("Cannot change status of an interview that is already COMPLETED"));

        mockMvc.perform(patch("/api/applications/100/interviews/10/status")
                        .with(user(authUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PATCH /status — status manquant → 400")
    void updateStatus_missingStatus_returns400() throws Exception {
        mockMvc.perform(patch("/api/applications/100/interviews/10/status")
                        .with(user(authUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notes\":\"some note\"}"))
                .andExpect(status().isBadRequest());
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/applications/{id}/interviews/{iid} — suppression → 204 No Content")
    void deleteInterview_owned_returns204() throws Exception {
        doNothing().when(interviewService).deleteInterview(eq(100L), eq(10L), any(User.class));

        mockMvc.perform(delete("/api/applications/100/interviews/10")
                        .with(user(authUser))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE — IDOR : interview inconnue → 404")
    void deleteInterview_idor_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Interview", 10L))
                .when(interviewService).deleteInterview(eq(100L), eq(10L), any(User.class));

        mockMvc.perform(delete("/api/applications/100/interviews/10")
                        .with(user(authUser))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
