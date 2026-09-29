package com.jobtrack.service;

import com.jobtrack.dto.request.CompanyContactCreateRequest;
import com.jobtrack.dto.request.CompanyContactUpdateRequest;
import com.jobtrack.dto.request.CompanyCreateRequest;
import com.jobtrack.dto.request.CompanyUpdateRequest;
import com.jobtrack.dto.response.CompanyContactResponse;
import com.jobtrack.dto.response.CompanyResponse;
import com.jobtrack.dto.response.CompanySummaryResponse;
import com.jobtrack.entity.Company;
import com.jobtrack.entity.CompanyContact;
import com.jobtrack.entity.Role;
import com.jobtrack.entity.User;
import com.jobtrack.exception.ResourceNotFoundException;
import com.jobtrack.mapper.CompanyContactMapper;
import com.jobtrack.mapper.CompanyMapper;
import com.jobtrack.repository.CompanyContactRepository;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompanyService — Tests unitaires & Isolation")
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyContactRepository companyContactRepository;

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private CompanyMapper companyMapper;

    @Mock
    private CompanyContactMapper companyContactMapper;

    @InjectMocks
    private CompanyService companyService;

    private User userA;
    private User userB;
    private Company companyA;
    private Company companyB;
    private CompanyContact contactA;

    @BeforeEach
    void setUp() {
        userA = User.builder()
                .id(1L)
                .email("alice@example.com")
                .firstName("Alice")
                .lastName("Martin")
                .role(Role.USER)
                .build();

        userB = User.builder()
                .id(2L)
                .email("bob@example.com")
                .firstName("Bob")
                .lastName("Dupont")
                .role(Role.USER)
                .build();

        companyA = Company.builder()
                .id(10L)
                .user(userA)
                .name("TechCorp")
                .industry("Informatique")
                .location("Paris")
                .size("Grand Groupe")
                .website("https://techcorp.example.com")
                .contacts(new ArrayList<>())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        companyB = Company.builder()
                .id(20L)
                .user(userB)
                .name("InnoSoft")
                .industry("Logiciel")
                .location("Lyon")
                .size("Startup")
                .website("https://innosoft.example.com")
                .contacts(new ArrayList<>())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        contactA = CompanyContact.builder()
                .id(100L)
                .company(companyA)
                .firstName("Jean")
                .lastName("Recruteur")
                .jobTitle("Talent Acquisition Manager")
                .email("jean@techcorp.example.com")
                .phone("+33612345678")
                .linkedinUrl("https://linkedin.com/in/jean-recruteur")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // ── Company Tests ────────────────────────────────────────────────────────

    @Test
    @DisplayName("createCompany: crée et associe la company à l'utilisateur authentifié")
    void createCompany_success() {
        CompanyCreateRequest request = CompanyCreateRequest.builder()
                .name("TechCorp")
                .industry("Informatique")
                .location("Paris")
                .size("Grand Groupe")
                .website("https://techcorp.example.com")
                .build();

        Company entityToSave = Company.builder().name("TechCorp").build();
        Company savedEntity = Company.builder().id(10L).user(userA).name("TechCorp").build();
        CompanyResponse expectedResponse = CompanyResponse.builder().id(10L).name("TechCorp").build();

        when(companyMapper.toEntity(request)).thenReturn(entityToSave);
        when(companyRepository.save(any(Company.class))).thenReturn(savedEntity);
        when(companyMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

        CompanyResponse result = companyService.createCompany(request, userA);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("TechCorp");
        assertThat(entityToSave.getUser()).isEqualTo(userA);
        verify(companyRepository).save(entityToSave);
    }

    @Test
    @DisplayName("getCompanyById: succès quand l'entreprise appartient à l'utilisateur")
    void getCompanyById_success() {
        when(companyRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(companyA));
        when(offerRepository.countByCompanyId(10L)).thenReturn(3L);
        when(companyContactRepository.findAllByCompanyIdAndCompanyUserIdOrderByCreatedAtDesc(10L, 1L))
                .thenReturn(List.of(contactA));

        CompanyResponse response = CompanyResponse.builder().id(10L).name("TechCorp").build();
        CompanyContactResponse contactResponse = CompanyContactResponse.builder().id(100L).firstName("Jean").build();

        when(companyMapper.toResponse(companyA)).thenReturn(response);
        when(companyContactMapper.toResponse(contactA)).thenReturn(contactResponse);

        CompanyResponse result = companyService.getCompanyById(10L, userA);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getOffersCount()).isEqualTo(3L);
        assertThat(result.getContacts()).hasSize(1);
    }

    @Test
    @DisplayName("getCompanyById: lève ResourceNotFoundException si l'entreprise n'existe pas ou appartient à autrui (IDOR)")
    void getCompanyById_notFoundOrCrossUser() {
        when(companyRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.getCompanyById(20L, userA))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company not found with id: 20");
    }

    @Test
    @DisplayName("getCompanies: liste paginée avec filtres et calcul des compteurs")
    void getCompanies_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Company> page = new PageImpl<>(List.of(companyA), pageable, 1);

        when(companyRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        CompanySummaryResponse summary = CompanySummaryResponse.builder().id(10L).name("TechCorp").build();
        when(companyMapper.toSummaryResponse(companyA)).thenReturn(summary);
        when(offerRepository.countByCompanyId(10L)).thenReturn(2L);
        when(companyContactRepository.countByCompanyId(10L)).thenReturn(1L);

        Page<CompanySummaryResponse> result = companyService.getCompanies(
                userA, "tech", "Informatique", "Paris", "Grand Groupe", pageable
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getOffersCount()).isEqualTo(2L);
        assertThat(result.getContent().get(0).getContactsCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getAllCompaniesForSelect: retourne la liste complète triée pour l'utilisateur")
    void getAllCompaniesForSelect_success() {
        when(companyRepository.findAllByUserIdOrderByNameAsc(1L)).thenReturn(List.of(companyA));
        CompanySummaryResponse summary = CompanySummaryResponse.builder().id(10L).name("TechCorp").build();
        when(companyMapper.toSummaryResponse(companyA)).thenReturn(summary);
        when(offerRepository.countByCompanyId(10L)).thenReturn(1L);
        when(companyContactRepository.countByCompanyId(10L)).thenReturn(1L);

        List<CompanySummaryResponse> list = companyService.getAllCompaniesForSelect(userA);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getName()).isEqualTo("TechCorp");
    }

    @Test
    @DisplayName("updateCompany: met à jour les informations pour le propriétaire")
    void updateCompany_success() {
        CompanyUpdateRequest updateRequest = CompanyUpdateRequest.builder()
                .name("TechCorp Updated")
                .location("Lyon")
                .build();

        when(companyRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(companyA));
        when(companyRepository.save(companyA)).thenReturn(companyA);
        when(offerRepository.countByCompanyId(10L)).thenReturn(2L);
        when(companyContactRepository.findAllByCompanyIdAndCompanyUserIdOrderByCreatedAtDesc(10L, 1L))
                .thenReturn(List.of());

        CompanyResponse response = CompanyResponse.builder().id(10L).name("TechCorp Updated").build();
        when(companyMapper.toResponse(companyA)).thenReturn(response);

        CompanyResponse result = companyService.updateCompany(10L, updateRequest, userA);

        assertThat(result.getName()).isEqualTo("TechCorp Updated");
        verify(companyMapper).updateEntityFromRequest(updateRequest, companyA);
        verify(companyRepository).save(companyA);
    }

    @Test
    @DisplayName("updateCompany: lève ResourceNotFoundException si tentative de modification d'une Company d'un autre utilisateur")
    void updateCompany_crossUser_forbidden() {
        CompanyUpdateRequest updateRequest = CompanyUpdateRequest.builder().name("Hacked").build();
        when(companyRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.updateCompany(20L, updateRequest, userA))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(companyRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteCompany: DÉTACHE les offres (clearCompanyReference) avant de supprimer l'entreprise")
    void deleteCompany_preservesOffers() {
        when(companyRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(companyA));

        companyService.deleteCompany(10L, userA);

        // Vérification cruciale : les offres associées sont déliées avant la suppression
        verify(offerRepository).clearCompanyReference(10L);
        verify(companyRepository).delete(companyA);
    }

    @Test
    @DisplayName("deleteCompany: lève ResourceNotFoundException si tentative de suppression par un autre utilisateur")
    void deleteCompany_crossUser_forbidden() {
        when(companyRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.deleteCompany(20L, userA))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(offerRepository, never()).clearCompanyReference(any());
        verify(companyRepository, never()).delete(any(Company.class));
    }

    // ── Contacts Tests (CRM) ─────────────────────────────────────────────────

    @Test
    @DisplayName("addContact: ajoute un contact à une entreprise appartenant à l'utilisateur")
    void addContact_success() {
        CompanyContactCreateRequest request = CompanyContactCreateRequest.builder()
                .firstName("Jean")
                .lastName("Recruteur")
                .email("jean@techcorp.example.com")
                .build();

        when(companyRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(companyA));
        CompanyContact contactEntity = CompanyContact.builder().firstName("Jean").lastName("Recruteur").build();
        CompanyContact savedContact = CompanyContact.builder().id(100L).company(companyA).firstName("Jean").build();
        CompanyContactResponse contactResponse = CompanyContactResponse.builder().id(100L).firstName("Jean").build();

        when(companyContactMapper.toEntity(request)).thenReturn(contactEntity);
        when(companyContactRepository.save(any(CompanyContact.class))).thenReturn(savedContact);
        when(companyContactMapper.toResponse(savedContact)).thenReturn(contactResponse);

        CompanyContactResponse result = companyService.addContact(10L, request, userA);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(contactEntity.getCompany()).isEqualTo(companyA);
        verify(companyContactRepository).save(contactEntity);
    }

    @Test
    @DisplayName("addContact: lève ResourceNotFoundException si la Company appartient à un autre utilisateur")
    void addContact_crossUserCompany_throwsNotFound() {
        CompanyContactCreateRequest request = CompanyContactCreateRequest.builder()
                .firstName("Infiltré")
                .lastName("Test")
                .build();

        when(companyRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.addContact(20L, request, userA))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(companyContactRepository, never()).save(any());
    }

    @Test
    @DisplayName("getContacts: liste les contacts pour l'entreprise du propriétaire")
    void getContacts_success() {
        when(companyRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(companyA));
        when(companyContactRepository.findAllByCompanyIdAndCompanyUserIdOrderByCreatedAtDesc(10L, 1L))
                .thenReturn(List.of(contactA));

        CompanyContactResponse contactResponse = CompanyContactResponse.builder().id(100L).firstName("Jean").build();
        when(companyContactMapper.toResponse(contactA)).thenReturn(contactResponse);

        List<CompanyContactResponse> result = companyService.getContacts(10L, userA);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Jean");
    }

    @Test
    @DisplayName("getContacts: lève ResourceNotFoundException si la Company appartient à un autre utilisateur")
    void getContacts_crossUser_throwsNotFound() {
        when(companyRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.getContacts(20L, userA))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateContact: modifie un contact avec vérification de l'appartenance utilisateur")
    void updateContact_success() {
        CompanyContactUpdateRequest request = CompanyContactUpdateRequest.builder()
                .firstName("Jean-Paul")
                .lastName("Recruteur")
                .build();

        when(companyRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(companyA));
        when(companyContactRepository.findByIdAndCompanyIdAndCompanyUserId(100L, 10L, 1L))
                .thenReturn(Optional.of(contactA));
        when(companyContactRepository.save(contactA)).thenReturn(contactA);

        CompanyContactResponse response = CompanyContactResponse.builder().id(100L).firstName("Jean-Paul").build();
        when(companyContactMapper.toResponse(contactA)).thenReturn(response);

        CompanyContactResponse result = companyService.updateContact(10L, 100L, request, userA);

        assertThat(result.getFirstName()).isEqualTo("Jean-Paul");
        verify(companyContactMapper).updateEntityFromRequest(request, contactA);
    }

    @Test
    @DisplayName("updateContact: lève ResourceNotFoundException si le contact appartient à une Company d'un autre utilisateur")
    void updateContact_crossUser_throwsNotFound() {
        CompanyContactUpdateRequest request = CompanyContactUpdateRequest.builder().firstName("Test").build();

        when(companyRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.updateContact(20L, 100L, request, userA))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(companyContactRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteContact: supprime un contact avec succès")
    void deleteContact_success() {
        when(companyRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(companyA));
        when(companyContactRepository.findByIdAndCompanyIdAndCompanyUserId(100L, 10L, 1L))
                .thenReturn(Optional.of(contactA));

        companyService.deleteContact(10L, 100L, userA);

        verify(companyContactRepository).delete(contactA);
    }

    @Test
    @DisplayName("deleteContact: lève ResourceNotFoundException si le contact appartient à autrui")
    void deleteContact_crossUser_throwsNotFound() {
        when(companyRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(companyA));
        when(companyContactRepository.findByIdAndCompanyIdAndCompanyUserId(999L, 10L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.deleteContact(10L, 999L, userA))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(companyContactRepository, never()).delete(any());
    }
}
