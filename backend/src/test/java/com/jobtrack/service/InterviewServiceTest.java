package com.jobtrack.service;

import com.jobtrack.dto.request.InterviewCreateRequest;
import com.jobtrack.dto.request.InterviewUpdateRequest;
import com.jobtrack.dto.response.InterviewResponse;
import com.jobtrack.entity.*;
import com.jobtrack.exception.ResourceNotFoundException;
import com.jobtrack.mapper.InterviewMapper;
import com.jobtrack.repository.ApplicationRepository;
import com.jobtrack.repository.InterviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InterviewService — Tests unitaires")
class InterviewServiceTest {

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private InterviewMapper interviewMapper;

    @InjectMocks
    private InterviewService interviewService;

    private User userA;
    private User userB;
    private Application applicationA;
    private Application applicationB;
    private Interview interviewA;
    private InterviewResponse interviewResponseA;

    @BeforeEach
    void setUp() {
        userA = User.builder().id(1L).email("userA@test.dev").firstName("Alice").role(Role.USER).build();
        userB = User.builder().id(2L).email("userB@test.dev").firstName("Bob").role(Role.USER).build();

        applicationA = Application.builder()
                .id(100L)
                .user(userA)
                .status(ApplicationStatus.INTERVIEW)
                .build();

        applicationB = Application.builder()
                .id(200L)
                .user(userB)
                .status(ApplicationStatus.APPLIED)
                .build();

        interviewA = Interview.builder()
                .id(10L)
                .application(applicationA)
                .type(InterviewType.TECHNICAL)
                .status(InterviewStatus.SCHEDULED)
                .scheduledAt(Instant.parse("2026-10-15T10:00:00Z"))
                .build();

        interviewResponseA = InterviewResponse.builder()
                .id(10L)
                .applicationId(100L)
                .type(InterviewType.TECHNICAL)
                .status(InterviewStatus.SCHEDULED)
                .scheduledAt(Instant.parse("2026-10-15T10:00:00Z"))
                .build();
    }

    // ── createInterview ───────────────────────────────────────────────────────

    @Test
    @DisplayName("createInterview — succès : interview créée pour une candidature existante")
    void createInterview_success() {
        InterviewCreateRequest request = InterviewCreateRequest.builder()
                .type(InterviewType.TECHNICAL)
                .scheduledAt(Instant.parse("2026-10-15T10:00:00Z"))
                .location("Paris")
                .build();

        when(applicationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(applicationA));

        Interview mapped = Interview.builder()
                .type(InterviewType.TECHNICAL)
                .scheduledAt(Instant.parse("2026-10-15T10:00:00Z"))
                .build();
        when(interviewMapper.toEntity(request)).thenReturn(mapped);
        when(interviewRepository.save(any(Interview.class))).thenReturn(interviewA);
        when(interviewMapper.toResponse(interviewA)).thenReturn(interviewResponseA);

        InterviewResponse result = interviewService.createInterview(100L, request, userA);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getType()).isEqualTo(InterviewType.TECHNICAL);
        verify(interviewRepository).save(any(Interview.class));
    }

    @Test
    @DisplayName("createInterview — IDOR : Application inconnue ou d'un autre user → 404")
    void createInterview_applicationNotOwned_throwsNotFound() {
        InterviewCreateRequest request = InterviewCreateRequest.builder()
                .type(InterviewType.HR)
                .scheduledAt(Instant.now())
                .build();

        when(applicationRepository.findByIdAndUserId(100L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.createInterview(100L, request, userB))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Application");

        verify(interviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("createInterview — Application inexistante → 404")
    void createInterview_applicationNotFound_throwsNotFound() {
        InterviewCreateRequest request = InterviewCreateRequest.builder()
                .type(InterviewType.FINAL)
                .scheduledAt(Instant.now())
                .build();

        when(applicationRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.createInterview(999L, request, userA))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getInterviews ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getInterviews — liste des entretiens pour le propriétaire")
    void getInterviews_owned_returnsList() {
        when(applicationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(applicationA));
        when(interviewRepository.findAllByApplicationIdOrderByScheduledAtAsc(100L))
                .thenReturn(List.of(interviewA));
        when(interviewMapper.toResponseList(List.of(interviewA))).thenReturn(List.of(interviewResponseA));

        List<InterviewResponse> result = interviewService.getInterviews(100L, userA);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getInterviews — IDOR : User B ne peut pas lister les entretiens de User A → 404")
    void getInterviews_notOwned_throwsNotFound() {
        when(applicationRepository.findByIdAndUserId(100L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.getInterviews(100L, userB))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getInterview ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getInterview — succès pour le propriétaire")
    void getInterview_owned_returnsResponse() {
        when(applicationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(applicationA));
        when(interviewRepository.findByIdAndApplicationId(10L, 100L)).thenReturn(Optional.of(interviewA));
        when(interviewMapper.toResponse(interviewA)).thenReturn(interviewResponseA);

        InterviewResponse result = interviewService.getInterview(100L, 10L, userA);

        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getInterview — interview appartenant à une autre application → 404")
    void getInterview_wrongApplication_throwsNotFound() {
        when(applicationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(applicationA));
        when(interviewRepository.findByIdAndApplicationId(10L, 100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.getInterview(100L, 10L, userA))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Interview");
    }

    @Test
    @DisplayName("getInterview — IDOR : User B tente d'accéder à l'interview de User A → 404")
    void getInterview_idor_throwsNotFound() {
        when(applicationRepository.findByIdAndUserId(100L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.getInterview(100L, 10L, userB))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── updateInterview ───────────────────────────────────────────────────────

    @Test
    @DisplayName("updateInterview — report : modification de scheduledAt (reste SCHEDULED)")
    void updateInterview_reschedule_success() {
        Instant newDate = Instant.parse("2026-11-01T14:00:00Z");
        InterviewUpdateRequest request = InterviewUpdateRequest.builder()
                .type(InterviewType.TECHNICAL)
                .scheduledAt(newDate)
                .build();

        when(applicationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(applicationA));
        when(interviewRepository.findByIdAndApplicationId(10L, 100L)).thenReturn(Optional.of(interviewA));
        doNothing().when(interviewMapper).updateEntityFromRequest(request, interviewA);
        when(interviewRepository.save(interviewA)).thenReturn(interviewA);
        when(interviewMapper.toResponse(interviewA)).thenReturn(
                InterviewResponse.builder().id(10L).status(InterviewStatus.SCHEDULED).scheduledAt(newDate).build()
        );

        InterviewResponse result = interviewService.updateInterview(100L, 10L, request, userA);

        assertThat(result.getStatus()).isEqualTo(InterviewStatus.SCHEDULED);
        assertThat(result.getScheduledAt()).isEqualTo(newDate);
    }

    @Test
    @DisplayName("updateInterview — IDOR : User B ne peut pas modifier l'interview de User A → 404")
    void updateInterview_idor_throwsNotFound() {
        when(applicationRepository.findByIdAndUserId(100L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.updateInterview(100L, 10L, new InterviewUpdateRequest(), userB))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── updateStatus ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateStatus — SCHEDULED → COMPLETED : transition valide")
    void updateStatus_scheduledToCompleted_success() {
        when(applicationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(applicationA));
        when(interviewRepository.findByIdAndApplicationId(10L, 100L)).thenReturn(Optional.of(interviewA));
        when(interviewRepository.save(interviewA)).thenReturn(interviewA);
        when(interviewMapper.toResponse(interviewA)).thenReturn(
                InterviewResponse.builder().id(10L).status(InterviewStatus.COMPLETED).build()
        );

        InterviewResponse result = interviewService.updateStatus(100L, 10L, InterviewStatus.COMPLETED, "Bien passé", userA);

        assertThat(interviewA.getStatus()).isEqualTo(InterviewStatus.COMPLETED);
        assertThat(result.getStatus()).isEqualTo(InterviewStatus.COMPLETED);
    }

    @Test
    @DisplayName("updateStatus — SCHEDULED → CANCELLED : transition valide")
    void updateStatus_scheduledToCancelled_success() {
        when(applicationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(applicationA));
        when(interviewRepository.findByIdAndApplicationId(10L, 100L)).thenReturn(Optional.of(interviewA));
        when(interviewRepository.save(interviewA)).thenReturn(interviewA);
        when(interviewMapper.toResponse(interviewA)).thenReturn(
                InterviewResponse.builder().id(10L).status(InterviewStatus.CANCELLED).build()
        );

        interviewService.updateStatus(100L, 10L, InterviewStatus.CANCELLED, null, userA);

        assertThat(interviewA.getStatus()).isEqualTo(InterviewStatus.CANCELLED);
    }

    @Test
    @DisplayName("updateStatus — SCHEDULED → NO_SHOW : transition valide")
    void updateStatus_scheduledToNoShow_success() {
        when(applicationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(applicationA));
        when(interviewRepository.findByIdAndApplicationId(10L, 100L)).thenReturn(Optional.of(interviewA));
        when(interviewRepository.save(interviewA)).thenReturn(interviewA);
        when(interviewMapper.toResponse(interviewA)).thenReturn(
                InterviewResponse.builder().id(10L).status(InterviewStatus.NO_SHOW).build()
        );

        interviewService.updateStatus(100L, 10L, InterviewStatus.NO_SHOW, null, userA);

        assertThat(interviewA.getStatus()).isEqualTo(InterviewStatus.NO_SHOW);
    }

    @Test
    @DisplayName("updateStatus — COMPLETED → CANCELLED : transition invalide → IllegalStateException")
    void updateStatus_completedToCancelled_throwsIllegalState() {
        Interview completedInterview = Interview.builder()
                .id(10L)
                .application(applicationA)
                .type(InterviewType.TECHNICAL)
                .status(InterviewStatus.COMPLETED)
                .scheduledAt(Instant.now())
                .build();

        when(applicationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(applicationA));
        when(interviewRepository.findByIdAndApplicationId(10L, 100L)).thenReturn(Optional.of(completedInterview));

        assertThatThrownBy(() -> interviewService.updateStatus(100L, 10L, InterviewStatus.CANCELLED, null, userA))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("COMPLETED");
    }

    @Test
    @DisplayName("updateStatus — CANCELLED → SCHEDULED : transition invalide → IllegalStateException")
    void updateStatus_cancelledToScheduled_throwsIllegalState() {
        Interview cancelledInterview = Interview.builder()
                .id(10L)
                .application(applicationA)
                .type(InterviewType.HR)
                .status(InterviewStatus.CANCELLED)
                .scheduledAt(Instant.now())
                .build();

        when(applicationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(applicationA));
        when(interviewRepository.findByIdAndApplicationId(10L, 100L)).thenReturn(Optional.of(cancelledInterview));

        assertThatThrownBy(() -> interviewService.updateStatus(100L, 10L, InterviewStatus.SCHEDULED, null, userA))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("updateStatus — IDOR : User B tente de changer le statut de l'interview de User A → 404")
    void updateStatus_idor_throwsNotFound() {
        when(applicationRepository.findByIdAndUserId(100L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.updateStatus(100L, 10L, InterviewStatus.COMPLETED, null, userB))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── deleteInterview ───────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteInterview — succès : interview supprimée")
    void deleteInterview_success() {
        when(applicationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(applicationA));
        when(interviewRepository.findByIdAndApplicationId(10L, 100L)).thenReturn(Optional.of(interviewA));

        interviewService.deleteInterview(100L, 10L, userA);

        verify(interviewRepository).delete(interviewA);
    }

    @Test
    @DisplayName("deleteInterview — IDOR : User B tente de supprimer l'interview de User A → 404")
    void deleteInterview_idor_throwsNotFound() {
        when(applicationRepository.findByIdAndUserId(100L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.deleteInterview(100L, 10L, userB))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(interviewRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteInterview — interview inexistante → 404")
    void deleteInterview_notFound_throwsNotFound() {
        when(applicationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(applicationA));
        when(interviewRepository.findByIdAndApplicationId(999L, 100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.deleteInterview(100L, 999L, userA))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Interview");
    }

    // ── multiple interviews ───────────────────────────────────────────────────

    @Test
    @DisplayName("getInterviews — plusieurs interviews pour une candidature retournées triées")
    void getInterviews_multiple_returnsSorted() {
        Interview i1 = Interview.builder().id(11L).application(applicationA)
                .type(InterviewType.HR).status(InterviewStatus.COMPLETED)
                .scheduledAt(Instant.parse("2026-10-01T09:00:00Z")).build();
        Interview i2 = Interview.builder().id(12L).application(applicationA)
                .type(InterviewType.TECHNICAL).status(InterviewStatus.SCHEDULED)
                .scheduledAt(Instant.parse("2026-10-15T10:00:00Z")).build();

        InterviewResponse r1 = InterviewResponse.builder().id(11L).type(InterviewType.HR)
                .scheduledAt(Instant.parse("2026-10-01T09:00:00Z")).build();
        InterviewResponse r2 = InterviewResponse.builder().id(12L).type(InterviewType.TECHNICAL)
                .scheduledAt(Instant.parse("2026-10-15T10:00:00Z")).build();

        when(applicationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(applicationA));
        when(interviewRepository.findAllByApplicationIdOrderByScheduledAtAsc(100L)).thenReturn(List.of(i1, i2));
        when(interviewMapper.toResponseList(List.of(i1, i2))).thenReturn(List.of(r1, r2));

        List<InterviewResponse> result = interviewService.getInterviews(100L, userA);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(11L);
        assertThat(result.get(1).getId()).isEqualTo(12L);
    }
}
