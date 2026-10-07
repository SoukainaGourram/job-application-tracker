package com.jobtrack.service;

import com.jobtrack.dto.response.ApplicationSummaryResponse;
import com.jobtrack.dto.response.DashboardStatsResponse;
import com.jobtrack.entity.*;
import com.jobtrack.mapper.ApplicationMapper;
import com.jobtrack.repository.ApplicationRepository;
import com.jobtrack.repository.CompanyRepository;
import com.jobtrack.repository.InterviewRepository;
import com.jobtrack.repository.OfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService — Tests unitaires")
class DashboardServiceTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private OfferRepository       offerRepository;
    @Mock private CompanyRepository     companyRepository;
    @Mock private InterviewRepository   interviewRepository;
    @Mock private ApplicationMapper     applicationMapper;

    @InjectMocks
    private DashboardService dashboardService;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        userA = User.builder().id(1L).email("alice@jobtrack.dev").firstName("Alice").role(Role.USER).build();
        userB = User.builder().id(2L).email("bob@jobtrack.dev").firstName("Bob").role(Role.USER).build();
    }

    // ── getStats — happy path ────────────────────────────────────────────────

    @Test
    @DisplayName("getStats() retourne les totaux corrects pour l'utilisateur connecté")
    void getStats_returnsCorrectTotals() {
        // Arrange
        when(applicationRepository.countByUserId(1L)).thenReturn(10L);
        when(offerRepository.countByUserId(1L)).thenReturn(5L);
        when(companyRepository.countByUserId(1L)).thenReturn(3L);
        when(interviewRepository.countByApplication_UserId(1L)).thenReturn(2L);
        when(applicationRepository.countByUserIdAndStatus(eq(1L), any(ApplicationStatus.class))).thenReturn(0L);
        when(applicationRepository.findAllByUserId(eq(1L), any(Pageable.class))).thenReturn(Page.empty());
        when(interviewRepository.findAllByApplication_UserIdAndStatusAndScheduledAtAfterOrderByScheduledAtAsc(
                eq(1L), eq(InterviewStatus.SCHEDULED), any(Instant.class))).thenReturn(List.of());

        // Act
        DashboardStatsResponse stats = dashboardService.getStats(userA);

        // Assert
        assertThat(stats.getTotalApplications()).isEqualTo(10L);
        assertThat(stats.getTotalOffers()).isEqualTo(5L);
        assertThat(stats.getTotalCompanies()).isEqualTo(3L);
        assertThat(stats.getTotalInterviews()).isEqualTo(2L);
    }

    @Test
    @DisplayName("getStats() retourne une map avec tous les statuts ApplicationStatus")
    void getStats_returnsAllApplicationStatuses() {
        // Arrange
        when(applicationRepository.countByUserId(1L)).thenReturn(8L);
        when(offerRepository.countByUserId(1L)).thenReturn(0L);
        when(companyRepository.countByUserId(1L)).thenReturn(0L);
        when(interviewRepository.countByApplication_UserId(1L)).thenReturn(0L);
        // TO_APPLY = 3, others = 0
        when(applicationRepository.countByUserIdAndStatus(1L, ApplicationStatus.TO_APPLY)).thenReturn(3L);
        when(applicationRepository.countByUserIdAndStatus(1L, ApplicationStatus.APPLIED)).thenReturn(5L);
        when(applicationRepository.countByUserIdAndStatus(eq(1L), argThat(s ->
                s != ApplicationStatus.TO_APPLY && s != ApplicationStatus.APPLIED))).thenReturn(0L);
        when(applicationRepository.findAllByUserId(eq(1L), any(Pageable.class))).thenReturn(Page.empty());
        when(interviewRepository.findAllByApplication_UserIdAndStatusAndScheduledAtAfterOrderByScheduledAtAsc(
                any(), any(), any())).thenReturn(List.of());

        // Act
        DashboardStatsResponse stats = dashboardService.getStats(userA);

        // Assert — all 8 statuses present
        assertThat(stats.getApplicationsByStatus()).hasSize(ApplicationStatus.values().length);
        assertThat(stats.getApplicationsByStatus().get(ApplicationStatus.TO_APPLY)).isEqualTo(3L);
        assertThat(stats.getApplicationsByStatus().get(ApplicationStatus.APPLIED)).isEqualTo(5L);
        assertThat(stats.getApplicationsByStatus().get(ApplicationStatus.REJECTED)).isEqualTo(0L);
    }

    @Test
    @DisplayName("getStats() retourne les candidatures récentes")
    void getStats_returnsRecentApplications() {
        // Arrange
        Application app = Application.builder().id(10L).build();
        ApplicationSummaryResponse summary = ApplicationSummaryResponse.builder()
                .id(10L).offerTitle("Dev Java").companyName("Acme").build();

        Page<Application> page = new PageImpl<>(List.of(app));
        when(applicationRepository.countByUserId(1L)).thenReturn(1L);
        when(offerRepository.countByUserId(1L)).thenReturn(0L);
        when(companyRepository.countByUserId(1L)).thenReturn(0L);
        when(interviewRepository.countByApplication_UserId(1L)).thenReturn(0L);
        when(applicationRepository.countByUserIdAndStatus(eq(1L), any())).thenReturn(0L);
        when(applicationRepository.findAllByUserId(eq(1L), any(Pageable.class))).thenReturn(page);
        when(applicationMapper.toSummaryResponse(app)).thenReturn(summary);
        when(interviewRepository.findAllByApplication_UserIdAndStatusAndScheduledAtAfterOrderByScheduledAtAsc(
                any(), any(), any())).thenReturn(List.of());

        // Act
        DashboardStatsResponse stats = dashboardService.getStats(userA);

        // Assert
        assertThat(stats.getRecentApplications()).hasSize(1);
        assertThat(stats.getRecentApplications().get(0).getOfferTitle()).isEqualTo("Dev Java");
    }

    @Test
    @DisplayName("getStats() retourne les prochains entretiens enrichis")
    void getStats_returnsUpcomingInterviews() {
        // Arrange — interview linked to application → offer
        Offer offer = Offer.builder().id(1L).title("Dev Angular").companyName("TechCorp").build();
        Application application = Application.builder().id(5L).offer(offer).build();
        Interview interview = Interview.builder()
                .id(20L)
                .application(application)
                .type(InterviewType.TECHNICAL)
                .status(InterviewStatus.SCHEDULED)
                .scheduledAt(Instant.now().plusSeconds(86400))
                .location("Paris")
                .build();

        when(applicationRepository.countByUserId(1L)).thenReturn(0L);
        when(offerRepository.countByUserId(1L)).thenReturn(0L);
        when(companyRepository.countByUserId(1L)).thenReturn(0L);
        when(interviewRepository.countByApplication_UserId(1L)).thenReturn(1L);
        when(applicationRepository.countByUserIdAndStatus(eq(1L), any())).thenReturn(0L);
        when(applicationRepository.findAllByUserId(eq(1L), any(Pageable.class))).thenReturn(Page.empty());
        when(interviewRepository.findAllByApplication_UserIdAndStatusAndScheduledAtAfterOrderByScheduledAtAsc(
                eq(1L), eq(InterviewStatus.SCHEDULED), any(Instant.class)))
                .thenReturn(List.of(interview));

        // Act
        DashboardStatsResponse stats = dashboardService.getStats(userA);

        // Assert
        assertThat(stats.getUpcomingInterviews()).hasSize(1);
        assertThat(stats.getUpcomingInterviews().get(0).getOfferTitle()).isEqualTo("Dev Angular");
        assertThat(stats.getUpcomingInterviews().get(0).getCompanyName()).isEqualTo("TechCorp");
        assertThat(stats.getUpcomingInterviews().get(0).getLocation()).isEqualTo("Paris");
        assertThat(stats.getUpcomingInterviews().get(0).getType()).isEqualTo(InterviewType.TECHNICAL);
    }

    // ── Isolation — user B cannot see user A data ────────────────────────────

    @Test
    @DisplayName("getStats() — isolation : userB ne voit pas les données de userA")
    void getStats_isolation_userBDoesNotSeeUserAData() {
        // Arrange — userA has data, userB has none
        when(applicationRepository.countByUserId(1L)).thenReturn(15L);
        when(applicationRepository.countByUserId(2L)).thenReturn(0L);
        when(offerRepository.countByUserId(anyLong())).thenReturn(0L);
        when(companyRepository.countByUserId(anyLong())).thenReturn(0L);
        when(interviewRepository.countByApplication_UserId(anyLong())).thenReturn(0L);
        when(applicationRepository.countByUserIdAndStatus(anyLong(), any())).thenReturn(0L);
        when(applicationRepository.findAllByUserId(anyLong(), any(Pageable.class))).thenReturn(Page.empty());
        when(interviewRepository.findAllByApplication_UserIdAndStatusAndScheduledAtAfterOrderByScheduledAtAsc(
                anyLong(), any(), any())).thenReturn(List.of());

        // Act
        DashboardStatsResponse statsA = dashboardService.getStats(userA);
        DashboardStatsResponse statsB = dashboardService.getStats(userB);

        // Assert
        assertThat(statsA.getTotalApplications()).isEqualTo(15L);
        assertThat(statsB.getTotalApplications()).isEqualTo(0L);

        // Verify that each call queried strictly its respective user ID
        verify(applicationRepository, times(1)).countByUserId(1L);
        verify(applicationRepository, times(1)).countByUserId(2L);
        verify(offerRepository, times(1)).countByUserId(1L);
        verify(offerRepository, times(1)).countByUserId(2L);
        verify(companyRepository, times(1)).countByUserId(1L);
        verify(companyRepository, times(1)).countByUserId(2L);
        verify(interviewRepository, times(1)).countByApplication_UserId(1L);
        verify(interviewRepository, times(1)).countByApplication_UserId(2L);
    }

    @Test
    @DisplayName("getStats() — empty state : 0 données retourne des listes vides et des totaux à 0")
    void getStats_emptyState_returnsZerosAndEmptyLists() {
        // Arrange
        when(applicationRepository.countByUserId(anyLong())).thenReturn(0L);
        when(offerRepository.countByUserId(anyLong())).thenReturn(0L);
        when(companyRepository.countByUserId(anyLong())).thenReturn(0L);
        when(interviewRepository.countByApplication_UserId(anyLong())).thenReturn(0L);
        when(applicationRepository.countByUserIdAndStatus(anyLong(), any())).thenReturn(0L);
        when(applicationRepository.findAllByUserId(anyLong(), any(Pageable.class))).thenReturn(Page.empty());
        when(interviewRepository.findAllByApplication_UserIdAndStatusAndScheduledAtAfterOrderByScheduledAtAsc(
                anyLong(), any(), any())).thenReturn(List.of());

        // Act
        DashboardStatsResponse stats = dashboardService.getStats(userA);

        // Assert
        assertThat(stats.getTotalApplications()).isZero();
        assertThat(stats.getTotalOffers()).isZero();
        assertThat(stats.getTotalCompanies()).isZero();
        assertThat(stats.getTotalInterviews()).isZero();
        assertThat(stats.getRecentApplications()).isEmpty();
        assertThat(stats.getUpcomingInterviews()).isEmpty();
        assertThat(stats.getApplicationsByStatus()).hasSize(ApplicationStatus.values().length);
        assertThat(stats.getApplicationsByStatus().values()).allMatch(count -> count == 0L);
    }
}
