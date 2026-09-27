package com.jobtrack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtrack.config.ApplicationConfig;
import com.jobtrack.config.SecurityConfig;
import com.jobtrack.dto.request.ApplicationCreateRequest;
import com.jobtrack.dto.request.ApplicationStatusUpdateRequest;
import com.jobtrack.dto.request.ApplicationUpdateRequest;
import com.jobtrack.dto.response.ApplicationHistoryResponse;
import com.jobtrack.dto.response.ApplicationResponse;
import com.jobtrack.dto.response.ApplicationSummaryResponse;
import com.jobtrack.entity.ApplicationStatus;
import com.jobtrack.entity.ContractType;
import com.jobtrack.entity.Role;
import com.jobtrack.entity.User;
import com.jobtrack.exception.ApplicationConflictException;
import com.jobtrack.exception.GlobalExceptionHandler;
import com.jobtrack.exception.ResourceNotFoundException;
import com.jobtrack.security.JwtAuthenticationFilter;
import com.jobtrack.security.JwtService;
import com.jobtrack.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplicationController.class)
@Import({SecurityConfig.class, ApplicationConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = {
        "spring.security.filter.order=0",
        "application.security.jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
        "application.security.jwt.expiration=86400000",
        "application.cors.allowed-origins=http://localhost:4200"
})
@DisplayName("ApplicationController — Tests d'intégration")
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private User authUser;
    private ApplicationResponse sampleResponse;
    private ApplicationSummaryResponse sampleSummary;

    @BeforeEach
    void setUp() {
        authUser = User.builder()
                .id(1L)
                .email("alice@jobtrack.dev")
                .firstName("Alice")
                .role(Role.USER)
                .password("encodedPassword")
                .build();

        sampleResponse = ApplicationResponse.builder()
                .id(100L)
                .status(ApplicationStatus.TO_APPLY)
                .notes("Some notes")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        sampleSummary = ApplicationSummaryResponse.builder()
                .id(100L)
                .offerId(10L)
                .offerTitle("Fullstack Dev")
                .companyName("Airbus")
                .contractType(ContractType.CDI)
                .status(ApplicationStatus.TO_APPLY)
                .build();
    }

    // ── POST /api/applications ────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/applications — création valide → 201 Created")
    void createApplication_valid_returns201() throws Exception {
        ApplicationCreateRequest request = ApplicationCreateRequest.builder()
                .offerId(10L)
                .status(ApplicationStatus.TO_APPLY)
                .notes("First note")
                .build();

        when(applicationService.createApplication(any(ApplicationCreateRequest.class), any(User.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/applications")
                        .with(user(authUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("TO_APPLY"));
    }

    @Test
    @DisplayName("POST /api/applications — candidature active déjà existante → 409 Conflict")
    void createApplication_conflict_returns409() throws Exception {
        ApplicationCreateRequest request = ApplicationCreateRequest.builder()
                .offerId(10L)
                .build();

        when(applicationService.createApplication(any(ApplicationCreateRequest.class), any(User.class)))
                .thenThrow(new ApplicationConflictException(10L));

        mockMvc.perform(post("/api/applications")
                        .with(user(authUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // ── GET /api/applications ─────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/applications — liste paginée pour utilisateur connecté → 200 OK")
    void getApplications_authenticated_returns200() throws Exception {
        when(applicationService.getApplications(any(User.class), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleSummary)));

        mockMvc.perform(get("/api/applications")
                        .with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(100))
                .andExpect(jsonPath("$.content[0].companyName").value("Airbus"));
    }

    @Test
    @DisplayName("GET /api/applications/kanban — liste pour Kanban → 200 OK")
    void getKanbanApplications_returns200() throws Exception {
        when(applicationService.getKanbanApplications(any(User.class)))
                .thenReturn(List.of(sampleSummary));

        mockMvc.perform(get("/api/applications/kanban")
                        .with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100));
    }

    // ── GET /api/applications/{id} ────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/applications/{id} — succès pour le propriétaire → 200 OK")
    void getApplicationById_owned_returns200() throws Exception {
        when(applicationService.getApplicationById(eq(100L), any(User.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/applications/100")
                        .with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    @DisplayName("GET /api/applications/{id} — candidature non possédée ou inexistante → 404")
    void getApplicationById_notOwned_returns404() throws Exception {
        when(applicationService.getApplicationById(eq(999L), any(User.class)))
                .thenThrow(new ResourceNotFoundException("Application", 999L));

        mockMvc.perform(get("/api/applications/999")
                        .with(user(authUser)))
                .andExpect(status().isNotFound());
    }

    // ── PUT /api/applications/{id} ────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/applications/{id} — modification valide → 200 OK")
    void updateApplication_valid_returns200() throws Exception {
        ApplicationUpdateRequest request = ApplicationUpdateRequest.builder()
                .notes("Updated notes")
                .cvVersion("CV_v2.pdf")
                .build();

        when(applicationService.updateApplication(eq(100L), any(ApplicationUpdateRequest.class), any(User.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/api/applications/100")
                        .with(user(authUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // ── PATCH /api/applications/{id}/status ───────────────────────────────────

    @Test
    @DisplayName("PATCH /api/applications/{id}/status — changement de statut valide → 200 OK")
    void updateStatus_valid_returns200() throws Exception {
        ApplicationStatusUpdateRequest request = ApplicationStatusUpdateRequest.builder()
                .status(ApplicationStatus.INTERVIEW)
                .note("Entretien RH prévu")
                .build();

        when(applicationService.updateApplicationStatus(eq(100L), eq(ApplicationStatus.INTERVIEW), eq("Entretien RH prévu"), any(User.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(patch("/api/applications/100/status")
                        .with(user(authUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // ── DELETE /api/applications/{id} ─────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/applications/{id} — suppression par propriétaire → 204 No Content")
    void deleteApplication_owned_returns204() throws Exception {
        doNothing().when(applicationService).deleteApplication(eq(100L), any(User.class));

        mockMvc.perform(delete("/api/applications/100")
                        .with(user(authUser))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    // ── GET /api/applications/{id}/history ────────────────────────────────────

    @Test
    @DisplayName("GET /api/applications/{id}/history — retourne l'historique → 200 OK")
    void getHistory_returns200() throws Exception {
        ApplicationHistoryResponse hist = ApplicationHistoryResponse.builder()
                .id(1L)
                .applicationId(100L)
                .oldStatus(ApplicationStatus.TO_APPLY)
                .newStatus(ApplicationStatus.APPLIED)
                .changedAt(Instant.now())
                .note("Envoyé")
                .build();

        when(applicationService.getApplicationHistory(eq(100L), any(User.class)))
                .thenReturn(List.of(hist));

        mockMvc.perform(get("/api/applications/100/history")
                        .with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].newStatus").value("APPLIED"));
    }
}
