package com.jobtrack.service;

import com.jobtrack.dto.request.OfferCreateRequest;
import com.jobtrack.dto.request.OfferUpdateRequest;
import com.jobtrack.dto.response.OfferResponse;
import com.jobtrack.entity.ContractType;
import com.jobtrack.entity.Offer;
import com.jobtrack.entity.OfferStatus;
import com.jobtrack.entity.Role;
import com.jobtrack.entity.User;
import com.jobtrack.exception.ResourceNotFoundException;
import com.jobtrack.mapper.OfferMapper;
import com.jobtrack.repository.CompanyRepository;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OfferService — Tests unitaires")
class OfferServiceTest {

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private OfferMapper offerMapper;

    @InjectMocks
    private OfferService offerService;

    private User userA;
    private User userB;
    private Offer offerA;
    private OfferResponse offerResponseA;

    @BeforeEach
    void setUp() {
        userA = User.builder()
                .id(1L)
                .email("userA@example.com")
                .firstName("Alice")
                .lastName("Smith")
                .role(Role.USER)
                .build();

        userB = User.builder()
                .id(2L)
                .email("userB@example.com")
                .firstName("Bob")
                .lastName("Jones")
                .role(Role.USER)
                .build();

        offerA = Offer.builder()
                .id(10L)
                .user(userA)
                .title("Full Stack Developer")
                .companyName("TechCorp")
                .contractType(ContractType.CDI)
                .status(OfferStatus.SAVED)
                .technologies(new ArrayList<>(List.of("Angular", "Java", "Spring Boot")))
                .city("Paris")
                .country("France")
                .salary("50k-55k")
                .applicationDeadline(LocalDate.now().plusMonths(1))
                .build();

        offerResponseA = OfferResponse.builder()
                .id(10L)
                .title("Full Stack Developer")
                .companyName("TechCorp")
                .contractType(ContractType.CDI)
                .status(OfferStatus.SAVED)
                .technologies(List.of("Angular", "Java", "Spring Boot"))
                .city("Paris")
                .country("France")
                .salary("50k-55k")
                .build();
    }

    @Test
    @DisplayName("getOffers — retourne la liste paginée pour l'utilisateur")
    void getOffers_returnsPagedList() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Offer> page = new PageImpl<>(List.of(offerA));

        when(offerRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(offerMapper.toResponse(offerA)).thenReturn(offerResponseA);

        Page<OfferResponse> result = offerService.getOffers(
                userA, null, null, null, null, null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Full Stack Developer");
        verify(offerRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("getOfferById — succès quand l'offre appartient à l'utilisateur")
    void getOfferById_whenOwned_returnsOffer() {
        when(offerRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(offerA));
        when(offerMapper.toResponse(offerA)).thenReturn(offerResponseA);

        OfferResponse result = offerService.getOfferById(10L, userA);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("Full Stack Developer");
        verify(offerRepository).findByIdAndUserId(10L, 1L);
    }

    @Test
    @DisplayName("getOfferById — isolation utilisateur : User B ne peut pas lire l'offre de User A")
    void getOfferById_whenNotOwned_throwsResourceNotFound() {
        when(offerRepository.findByIdAndUserId(10L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> offerService.getOfferById(10L, userB))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(offerRepository).findByIdAndUserId(10L, 2L);
    }

    @Test
    @DisplayName("createOffer — crée l'offre et l'associe à l'utilisateur")
    void createOffer_associatesUser_returnsResponse() {
        OfferCreateRequest request = OfferCreateRequest.builder()
                .title("Java Developer")
                .companyName("Cloud Inc")
                .contractType(ContractType.CDI)
                .status(OfferStatus.SAVED)
                .technologies(List.of("Java", "PostgreSQL"))
                .build();

        Offer unmapped = Offer.builder()
                .title("Java Developer")
                .companyName("Cloud Inc")
                .contractType(ContractType.CDI)
                .build();

        Offer saved = Offer.builder()
                .id(11L)
                .user(userA)
                .title("Java Developer")
                .companyName("Cloud Inc")
                .contractType(ContractType.CDI)
                .status(OfferStatus.SAVED)
                .build();

        when(offerMapper.toEntity(request)).thenReturn(unmapped);
        when(offerRepository.save(unmapped)).thenReturn(saved);
        when(offerMapper.toResponse(saved)).thenReturn(
                OfferResponse.builder().id(11L).title("Java Developer").status(OfferStatus.SAVED).build()
        );

        OfferResponse result = offerService.createOffer(request, userA);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(11L);
        assertThat(unmapped.getUser()).isEqualTo(userA);
        assertThat(unmapped.getStatus()).isEqualTo(OfferStatus.SAVED);
        verify(offerRepository).save(unmapped);
    }

    @Test
    @DisplayName("updateOffer — succès pour le propriétaire")
    void updateOffer_whenOwned_updatesAndReturns() {
        OfferUpdateRequest request = OfferUpdateRequest.builder()
                .title("Senior Developer")
                .companyName("TechCorp")
                .contractType(ContractType.CDI)
                .status(OfferStatus.TO_APPLY)
                .technologies(List.of("Java", "Spring"))
                .build();

        when(offerRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(offerA));
        when(offerRepository.save(offerA)).thenReturn(offerA);
        when(offerMapper.toResponse(offerA)).thenReturn(offerResponseA);

        OfferResponse result = offerService.updateOffer(10L, request, userA);

        assertThat(result).isNotNull();
        verify(offerMapper).updateEntityFromRequest(request, offerA);
        verify(offerRepository).save(offerA);
    }

    @Test
    @DisplayName("updateOffer — isolation utilisateur : User B ne peut pas modifier l'offre de User A")
    void updateOffer_whenNotOwned_throwsResourceNotFound() {
        OfferUpdateRequest request = OfferUpdateRequest.builder()
                .title("Malicious Edit")
                .companyName("Hacked")
                .contractType(ContractType.CDI)
                .status(OfferStatus.ARCHIVED)
                .build();

        when(offerRepository.findByIdAndUserId(10L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> offerService.updateOffer(10L, request, userB))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(offerRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateOfferStatus — succès pour le propriétaire")
    void updateOfferStatus_updatesStatus() {
        when(offerRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(offerA));
        when(offerRepository.save(offerA)).thenReturn(offerA);
        when(offerMapper.toResponse(offerA)).thenReturn(offerResponseA);

        OfferResponse result = offerService.updateOfferStatus(10L, OfferStatus.ARCHIVED, userA);

        assertThat(result).isNotNull();
        assertThat(offerA.getStatus()).isEqualTo(OfferStatus.ARCHIVED);
        verify(offerRepository).save(offerA);
    }

    @Test
    @DisplayName("deleteOffer — succès pour le propriétaire")
    void deleteOffer_whenOwned_deletes() {
        when(offerRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(offerA));

        offerService.deleteOffer(10L, userA);

        verify(offerRepository).delete(offerA);
    }

    @Test
    @DisplayName("deleteOffer — isolation utilisateur : User B ne peut pas supprimer l'offre de User A")
    void deleteOffer_whenNotOwned_throwsResourceNotFound() {
        when(offerRepository.findByIdAndUserId(10L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> offerService.deleteOffer(10L, userB))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(offerRepository, never()).delete(any(Offer.class));
    }

    @Test
    @DisplayName("createOffer — association avec Company du même utilisateur : succès")
    void createOffer_withOwnedCompany_associatesCompany() {
        com.jobtrack.entity.Company company = com.jobtrack.entity.Company.builder()
                .id(100L)
                .user(userA)
                .name("Acme Corp")
                .build();

        OfferCreateRequest request = OfferCreateRequest.builder()
                .title("Java Developer")
                .companyName("Acme Corp")
                .companyId(100L)
                .contractType(ContractType.CDI)
                .build();

        Offer unmapped = Offer.builder()
                .title("Java Developer")
                .companyName("Acme Corp")
                .contractType(ContractType.CDI)
                .build();

        when(offerMapper.toEntity(request)).thenReturn(unmapped);
        when(companyRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(company));
        when(offerRepository.save(unmapped)).thenReturn(unmapped);
        when(offerMapper.toResponse(unmapped)).thenReturn(OfferResponse.builder().id(12L).build());

        OfferResponse result = offerService.createOffer(request, userA);

        assertThat(result).isNotNull();
        assertThat(unmapped.getCompany()).isEqualTo(company);
        verify(companyRepository).findByIdAndUserId(100L, 1L);
    }

    @Test
    @DisplayName("createOffer — tentative d'associer une Company d'un autre utilisateur : lève ResourceNotFoundException")
    void createOffer_withCrossUserCompany_throwsResourceNotFound() {
        OfferCreateRequest request = OfferCreateRequest.builder()
                .title("Java Developer")
                .companyName("Hacked Corp")
                .companyId(999L)
                .contractType(ContractType.CDI)
                .build();

        Offer unmapped = Offer.builder().title("Java Developer").build();
        when(offerMapper.toEntity(request)).thenReturn(unmapped);
        when(companyRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> offerService.createOffer(request, userA))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company not found with id: 999");

        verify(offerRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateOffer — tentative d'associer une Company d'un autre utilisateur : lève ResourceNotFoundException")
    void updateOffer_withCrossUserCompany_throwsResourceNotFound() {
        OfferUpdateRequest request = OfferUpdateRequest.builder()
                .title("Updated Title")
                .companyName("Hacked Corp")
                .companyId(999L)
                .contractType(ContractType.CDI)
                .build();

        when(offerRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(offerA));
        when(companyRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> offerService.updateOffer(10L, request, userA))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company not found with id: 999");

        verify(offerRepository, never()).save(any());
    }
}
