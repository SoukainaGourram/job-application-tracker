package com.jobtrack.service;

import com.jobtrack.dto.request.ApplicationCreateRequest;
import com.jobtrack.dto.request.ApplicationUpdateRequest;
import com.jobtrack.dto.response.ApplicationHistoryResponse;
import com.jobtrack.dto.response.ApplicationResponse;
import com.jobtrack.dto.response.ApplicationSummaryResponse;
import com.jobtrack.entity.*;
import com.jobtrack.exception.ApplicationConflictException;
import com.jobtrack.exception.ResourceNotFoundException;
import com.jobtrack.mapper.ApplicationHistoryMapper;
import com.jobtrack.mapper.ApplicationMapper;
import com.jobtrack.repository.ApplicationHistoryRepository;
import com.jobtrack.repository.ApplicationRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationService — Tests unitaires")
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationHistoryRepository applicationHistoryRepository;

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private ApplicationMapper applicationMapper;

    @Mock
    private ApplicationHistoryMapper applicationHistoryMapper;

    @InjectMocks
    private ApplicationService applicationService;

    private User userA;
    private User userB;
    private Offer offerA;
    private Application applicationA;
    private ApplicationResponse applicationResponseA;

    @BeforeEach
    void setUp() {
        userA = User.builder()
                .id(1L)
                .email("userA@jobtrack.dev")
                .firstName("Alice")
                .role(Role.USER)
                .build();

        userB = User.builder()
                .id(2L)
                .email("userB@jobtrack.dev")
                .firstName("Bob")
                .role(Role.USER)
                .build();

        offerA = Offer.builder()
                .id(10L)
                .user(userA)
                .title("Fullstack Developer")
                .companyName("Airbus")
                .contractType(ContractType.CDI)
                .build();

        applicationA = Application.builder()
                .id(100L)
                .user(userA)
                .offer(offerA)
                .status(ApplicationStatus.TO_APPLY)
                .notes("Interesting position")
                .build();

        applicationResponseA = ApplicationResponse.builder()
                .id(100L)
                .status(ApplicationStatus.TO_APPLY)
                .notes("Interesting position")
                .build();
    }

    @Test
    @DisplayName("createApplication — succès et création automatique de l'historique initial")
    void createApplication_success() {
        ApplicationCreateRequest request = ApplicationCreateRequest.builder()
                .offerId(10L)
                .status(ApplicationStatus.TO_APPLY)
                .notes("Initial notes")
                .build();

        when(offerRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(offerA));
        when(applicationRepository.existsByOfferIdAndUserIdAndStatusNotIn(eq(10L), eq(1L), any())).thenReturn(false);

        Application mapped = Application.builder()
                .status(ApplicationStatus.TO_APPLY)
                .notes("Initial notes")
                .build();

        when(applicationMapper.toEntity(request)).thenReturn(mapped);
        when(applicationRepository.save(mapped)).thenReturn(applicationA);
        when(applicationMapper.toResponse(applicationA)).thenReturn(applicationResponseA);

        ApplicationResponse result = applicationService.createApplication(request, userA);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        // Verify automatic history creation
        verify(applicationHistoryRepository).save(any(ApplicationHistory.class));
    }

    @Test
    @DisplayName("createApplication — doublon : candidature active déjà existante → 409 ApplicationConflictException")
    void createApplication_duplicateActive_throwsConflict() {
        ApplicationCreateRequest request = ApplicationCreateRequest.builder()
                .offerId(10L)
                .build();

        when(offerRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(offerA));
        when(applicationRepository.existsByOfferIdAndUserIdAndStatusNotIn(eq(10L), eq(1L), any())).thenReturn(true);

        assertThatThrownBy(() -> applicationService.createApplication(request, userA))
                .isInstanceOf(ApplicationConflictException.class);

        verify(applicationRepository, never()).save(any());
        verify(applicationHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("createApplication — offre n'appartenant pas à l'utilisateur → 404 ResourceNotFoundException")
    void createApplication_offerNotOwned_throwsNotFound() {
        ApplicationCreateRequest request = ApplicationCreateRequest.builder()
                .offerId(10L)
                .build();

        when(offerRepository.findByIdAndUserId(10L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.createApplication(request, userB))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createApplicationFromOffer — création directe depuis une offre")
    void createApplicationFromOffer_success() {
        when(offerRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(offerA));
        when(applicationRepository.existsByOfferIdAndUserIdAndStatusNotIn(eq(10L), eq(1L), any())).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenReturn(applicationA);
        when(applicationMapper.toResponse(applicationA)).thenReturn(applicationResponseA);

        ApplicationResponse result = applicationService.createApplicationFromOffer(10L, userA);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.TO_APPLY);
        verify(applicationHistoryRepository).save(any(ApplicationHistory.class));
    }

    @Test
    @DisplayName("getApplicationById — succès pour le propriétaire")
    void getApplicationById_owned_returnsResponse() {
        when(applicationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(applicationA));
        when(applicationMapper.toResponse(applicationA)).thenReturn(applicationResponseA);

        ApplicationResponse result = applicationService.getApplicationById(100L, userA);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("getApplicationById — isolation : User B ne peut pas lire la candidature de User A")
    void getApplicationById_notOwned_throwsNotFound() {
        when(applicationRepository.findByIdAndUserId(100L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.getApplicationById(100L, userB))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateApplicationStatus — changement de statut génère automatiquement une entrée d'historique")
    void updateApplicationStatus_generatesHistory() {
        when(applicationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(applicationA));
        when(applicationRepository.save(applicationA)).thenReturn(applicationA);
        when(applicationMapper.toResponse(applicationA)).thenReturn(
                ApplicationResponse.builder().id(100L).status(ApplicationStatus.APPLIED).build()
        );

        ApplicationResponse result = applicationService.updateApplicationStatus(
                100L, ApplicationStatus.APPLIED, "CV envoyé via LinkedIn", userA
        );

        assertThat(result).isNotNull();
        assertThat(applicationA.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(applicationA.getAppliedAt()).isNotNull();

        verify(applicationHistoryRepository).save(argThat(hist ->
                hist.getOldStatus() == ApplicationStatus.TO_APPLY &&
                hist.getNewStatus() == ApplicationStatus.APPLIED &&
                hist.getNote().equals("CV envoyé via LinkedIn")
        ));
    }

    @Test
    @DisplayName("deleteApplication — supprime d'abord les interviews, puis l'historique, puis la candidature")
    void deleteApplication_deletesInterviewsThenHistoryThenApplication() {
        when(applicationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(applicationA));
        when(applicationHistoryRepository.findAllByApplicationIdOrderByChangedAtDesc(100L)).thenReturn(List.of());

        applicationService.deleteApplication(100L, userA);

        // Verify order: interviews first, then history, then application
        var inOrder = inOrder(interviewRepository, applicationHistoryRepository, applicationRepository);
        inOrder.verify(interviewRepository).deleteAllByApplicationId(100L);
        inOrder.verify(applicationHistoryRepository).deleteAll(any());
        inOrder.verify(applicationRepository).delete(applicationA);
    }

    @Test
    @DisplayName("deleteApplication — l'Offer reste intacte après la suppression de la candidature")
    void deleteApplication_offerNotDeleted() {
        when(applicationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(applicationA));
        when(applicationHistoryRepository.findAllByApplicationIdOrderByChangedAtDesc(100L)).thenReturn(List.of());

        applicationService.deleteApplication(100L, userA);

        // Offer repository must NOT be touched
        verifyNoInteractions(offerRepository);
    }

    @Test
    @DisplayName("getApplicationHistory — retourne l'historique pour le propriétaire")
    void getApplicationHistory_owned_returnsList() {
        when(applicationRepository.existsByIdAndUserId(100L, 1L)).thenReturn(true);
        ApplicationHistory hist = ApplicationHistory.builder()
                .id(1L)
                .application(applicationA)
                .oldStatus(ApplicationStatus.TO_APPLY)
                .newStatus(ApplicationStatus.APPLIED)
                .changedAt(Instant.now())
                .note("Sent")
                .build();

        when(applicationHistoryRepository.findAllByApplicationIdOrderByChangedAtDesc(100L))
                .thenReturn(List.of(hist));
        when(applicationHistoryMapper.toResponse(hist)).thenReturn(
                ApplicationHistoryResponse.builder().id(1L).oldStatus(ApplicationStatus.TO_APPLY).newStatus(ApplicationStatus.APPLIED).build()
        );

        List<ApplicationHistoryResponse> list = applicationService.getApplicationHistory(100L, userA);

        assertThat(list).hasSize(1);
    }
}
