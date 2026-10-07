package com.jobtrack.controller;

import com.jobtrack.config.ApplicationConfig;
import com.jobtrack.config.SecurityConfig;
import com.jobtrack.dto.response.ApplicationSummaryResponse;
import com.jobtrack.dto.response.DashboardStatsResponse;
import com.jobtrack.dto.response.UpcomingInterviewDto;
import com.jobtrack.entity.ApplicationStatus;
import com.jobtrack.entity.InterviewStatus;
import com.jobtrack.entity.InterviewType;
import com.jobtrack.entity.Role;
import com.jobtrack.entity.User;
import com.jobtrack.exception.GlobalExceptionHandler;
import com.jobtrack.security.JwtAuthenticationFilter;
import com.jobtrack.security.JwtService;
import com.jobtrack.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@Import({SecurityConfig.class, ApplicationConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = {
        "spring.security.filter.order=0",
        "application.security.jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
        "application.security.jwt.expiration=86400000",
        "application.cors.allowed-origins=http://localhost:4200"
})
@DisplayName("DashboardController — Tests d'intégration")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private User authUser;
    private DashboardStatsResponse sampleStats;

    @BeforeEach
    void setUp() {
        authUser = User.builder()
                .id(1L)
                .email("alice@jobtrack.dev")
                .firstName("Alice")
                .lastName("Dupont")
                .role(Role.USER)
                .build();

        ApplicationSummaryResponse recentApp = ApplicationSummaryResponse.builder()
                .id(1L)
                .offerTitle("Dev Java")
                .companyName("Acme Corp")
                .status(ApplicationStatus.APPLIED)
                .build();

        UpcomingInterviewDto upcomingInterview = UpcomingInterviewDto.builder()
                .id(10L)
                .applicationId(1L)
                .offerTitle("Dev Java")
                .companyName("Acme Corp")
                .type(InterviewType.TECHNICAL)
                .status(InterviewStatus.SCHEDULED)
                .scheduledAt(Instant.now().plusSeconds(86400))
                .location("Paris")
                .build();

        sampleStats = DashboardStatsResponse.builder()
                .totalApplications(5L)
                .totalOffers(3L)
                .totalCompanies(2L)
                .totalInterviews(1L)
                .applicationsByStatus(Map.of(
                        ApplicationStatus.TO_APPLY, 2L,
                        ApplicationStatus.APPLIED, 3L
                ))
                .recentApplications(List.of(recentApp))
                .upcomingInterviews(List.of(upcomingInterview))
                .build();
    }

    // ── GET /api/dashboard/stats ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/dashboard/stats → 200 avec les statistiques correctes")
    void getStats_authenticated_returns200WithStats() throws Exception {
        when(dashboardService.getStats(any(User.class))).thenReturn(sampleStats);

        mockMvc.perform(get("/api/dashboard/stats")
                        .with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplications").value(5))
                .andExpect(jsonPath("$.totalOffers").value(3))
                .andExpect(jsonPath("$.totalCompanies").value(2))
                .andExpect(jsonPath("$.totalInterviews").value(1))
                .andExpect(jsonPath("$.recentApplications[0].offerTitle").value("Dev Java"))
                .andExpect(jsonPath("$.upcomingInterviews[0].companyName").value("Acme Corp"))
                .andExpect(jsonPath("$.upcomingInterviews[0].type").value("TECHNICAL"));
    }

    @Test
    @DisplayName("GET /api/dashboard/stats → 403 sans authentification")
    void getStats_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/dashboard/stats → 200 avec listes vides (empty state)")
    void getStats_emptyState_returns200WithZeros() throws Exception {
        DashboardStatsResponse emptyStats = DashboardStatsResponse.builder()
                .totalApplications(0L)
                .totalOffers(0L)
                .totalCompanies(0L)
                .totalInterviews(0L)
                .applicationsByStatus(Map.of())
                .recentApplications(List.of())
                .upcomingInterviews(List.of())
                .build();

        when(dashboardService.getStats(any(User.class))).thenReturn(emptyStats);

        mockMvc.perform(get("/api/dashboard/stats")
                        .with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplications").value(0))
                .andExpect(jsonPath("$.recentApplications").isEmpty())
                .andExpect(jsonPath("$.upcomingInterviews").isEmpty());
    }
}
