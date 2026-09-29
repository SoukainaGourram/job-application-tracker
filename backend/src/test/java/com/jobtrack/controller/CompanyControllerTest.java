package com.jobtrack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtrack.config.ApplicationConfig;
import com.jobtrack.config.SecurityConfig;
import com.jobtrack.dto.request.CompanyContactCreateRequest;
import com.jobtrack.dto.request.CompanyContactUpdateRequest;
import com.jobtrack.dto.request.CompanyCreateRequest;
import com.jobtrack.dto.request.CompanyUpdateRequest;
import com.jobtrack.dto.response.CompanyContactResponse;
import com.jobtrack.dto.response.CompanyResponse;
import com.jobtrack.dto.response.CompanySummaryResponse;
import com.jobtrack.entity.Role;
import com.jobtrack.entity.User;
import com.jobtrack.exception.GlobalExceptionHandler;
import com.jobtrack.exception.ResourceNotFoundException;
import com.jobtrack.security.JwtAuthenticationFilter;
import com.jobtrack.security.JwtService;
import com.jobtrack.service.CompanyService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompanyController.class)
@Import({SecurityConfig.class, ApplicationConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = {
        "spring.security.filter.order=0",
        "application.security.jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
        "application.security.jwt.expiration=86400000",
        "application.cors.allowed-origins=http://localhost:4200"
})
@DisplayName("CompanyController — Tests d'intégration WebMvc")
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyService companyService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private User testUser;
    private CompanyResponse testCompanyResponse;
    private CompanySummaryResponse testCompanySummary;
    private CompanyContactResponse testContactResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("User")
                .lastName("Test")
                .role(Role.USER)
                .build();

        testContactResponse = CompanyContactResponse.builder()
                .id(100L)
                .companyId(10L)
                .firstName("Claire")
                .lastName("Martin")
                .jobTitle("Recruteuse Tech")
                .email("claire@techcorp.example.com")
                .phone("+33600000000")
                .linkedinUrl("https://linkedin.com/in/claire-martin")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        testCompanyResponse = CompanyResponse.builder()
                .id(10L)
                .name("TechCorp")
                .industry("Informatique")
                .location("Paris")
                .size("Grand Groupe")
                .website("https://techcorp.example.com")
                .description("Entreprise leader de la tech")
                .notes("Contactée via LinkedIn")
                .offersCount(2L)
                .contacts(List.of(testContactResponse))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        testCompanySummary = CompanySummaryResponse.builder()
                .id(10L)
                .name("TechCorp")
                .industry("Informatique")
                .location("Paris")
                .size("Grand Groupe")
                .offersCount(2L)
                .contactsCount(1L)
                .createdAt(Instant.now())
                .build();
    }

    // ── Company Endpoints ────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/companies — 200 OK avec page d'entreprises")
    void getCompanies_returnsPagedList() throws Exception {
        when(companyService.getCompanies(any(User.class), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testCompanySummary)));

        mockMvc.perform(get("/api/companies")
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.content[0].name").value("TechCorp"));
    }

    @Test
    @DisplayName("GET /api/companies/all — 200 OK avec liste pour select")
    void getAllCompaniesForSelect_returnsList() throws Exception {
        when(companyService.getAllCompaniesForSelect(any(User.class)))
                .thenReturn(List.of(testCompanySummary));

        mockMvc.perform(get("/api/companies/all")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("TechCorp"));
    }

    @Test
    @DisplayName("GET /api/companies/{id} — 200 OK avec détails")
    void getCompanyById_success() throws Exception {
        when(companyService.getCompanyById(eq(10L), any(User.class)))
                .thenReturn(testCompanyResponse);

        mockMvc.perform(get("/api/companies/10")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("TechCorp"))
                .andExpect(jsonPath("$.contacts[0].firstName").value("Claire"));
    }

    @Test
    @DisplayName("GET /api/companies/{id} — 404 NOT FOUND si inexistante ou appartenant à un tiers")
    void getCompanyById_notFound() throws Exception {
        when(companyService.getCompanyById(eq(999L), any(User.class)))
                .thenThrow(new ResourceNotFoundException("Company", 999L));

        mockMvc.perform(get("/api/companies/999")
                        .with(user(testUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("POST /api/companies — 201 CREATED avec nouvelle entreprise")
    void createCompany_success() throws Exception {
        CompanyCreateRequest request = CompanyCreateRequest.builder()
                .name("NewTech")
                .industry("Tech")
                .location("Paris")
                .website("https://newtech.example.com")
                .build();

        CompanyResponse createdResponse = CompanyResponse.builder()
                .id(11L)
                .name("NewTech")
                .industry("Tech")
                .build();

        when(companyService.createCompany(any(CompanyCreateRequest.class), any(User.class)))
                .thenReturn(createdResponse);

        mockMvc.perform(post("/api/companies")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.name").value("NewTech"));
    }

    @Test
    @DisplayName("POST /api/companies — 400 BAD REQUEST si le nom est manquant")
    void createCompany_validationFailure() throws Exception {
        CompanyCreateRequest invalidRequest = CompanyCreateRequest.builder()
                .name("")
                .build();

        mockMvc.perform(post("/api/companies")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("PUT /api/companies/{id} — 200 OK avec modification")
    void updateCompany_success() throws Exception {
        CompanyUpdateRequest updateRequest = CompanyUpdateRequest.builder()
                .name("TechCorp Updated")
                .industry("Informatique")
                .build();

        CompanyResponse updatedResponse = CompanyResponse.builder()
                .id(10L)
                .name("TechCorp Updated")
                .build();

        when(companyService.updateCompany(eq(10L), any(CompanyUpdateRequest.class), any(User.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/companies/10")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TechCorp Updated"));
    }

    @Test
    @DisplayName("DELETE /api/companies/{id} — 204 NO CONTENT")
    void deleteCompany_success() throws Exception {
        doNothing().when(companyService).deleteCompany(eq(10L), any(User.class));

        mockMvc.perform(delete("/api/companies/10")
                        .with(user(testUser))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(companyService).deleteCompany(eq(10L), any(User.class));
    }

    // ── Contacts Endpoints ───────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/companies/{companyId}/contacts — 200 OK avec liste des contacts")
    void getContacts_success() throws Exception {
        when(companyService.getContacts(eq(10L), any(User.class)))
                .thenReturn(List.of(testContactResponse));

        mockMvc.perform(get("/api/companies/10/contacts")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].firstName").value("Claire"));
    }

    @Test
    @DisplayName("GET /api/companies/{companyId}/contacts/{contactId} — 200 OK")
    void getContactById_success() throws Exception {
        when(companyService.getContactById(eq(10L), eq(100L), any(User.class)))
                .thenReturn(testContactResponse);

        mockMvc.perform(get("/api/companies/10/contacts/100")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Claire"));
    }

    @Test
    @DisplayName("POST /api/companies/{companyId}/contacts — 201 CREATED")
    void addContact_success() throws Exception {
        CompanyContactCreateRequest request = CompanyContactCreateRequest.builder()
                .firstName("Marc")
                .lastName("Directeur")
                .email("marc@techcorp.example.com")
                .build();

        CompanyContactResponse created = CompanyContactResponse.builder()
                .id(101L)
                .companyId(10L)
                .firstName("Marc")
                .lastName("Directeur")
                .build();

        when(companyService.addContact(eq(10L), any(CompanyContactCreateRequest.class), any(User.class)))
                .thenReturn(created);

        mockMvc.perform(post("/api/companies/10/contacts")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.firstName").value("Marc"));
    }

    @Test
    @DisplayName("POST /api/companies/{companyId}/contacts — 400 BAD REQUEST si prénom manquant")
    void addContact_validationFailure() throws Exception {
        CompanyContactCreateRequest invalid = CompanyContactCreateRequest.builder()
                .firstName("")
                .lastName("Test")
                .build();

        mockMvc.perform(post("/api/companies/10/contacts")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("PUT /api/companies/{companyId}/contacts/{contactId} — 200 OK")
    void updateContact_success() throws Exception {
        CompanyContactUpdateRequest request = CompanyContactUpdateRequest.builder()
                .firstName("Claire-Marie")
                .lastName("Martin")
                .build();

        CompanyContactResponse updated = CompanyContactResponse.builder()
                .id(100L)
                .companyId(10L)
                .firstName("Claire-Marie")
                .lastName("Martin")
                .build();

        when(companyService.updateContact(eq(10L), eq(100L), any(CompanyContactUpdateRequest.class), any(User.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/companies/10/contacts/100")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Claire-Marie"));
    }

    @Test
    @DisplayName("DELETE /api/companies/{companyId}/contacts/{contactId} — 204 NO CONTENT")
    void deleteContact_success() throws Exception {
        doNothing().when(companyService).deleteContact(eq(10L), eq(100L), any(User.class));

        mockMvc.perform(delete("/api/companies/10/contacts/100")
                        .with(user(testUser))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(companyService).deleteContact(eq(10L), eq(100L), any(User.class));
    }

    @Test
    @DisplayName("Requête non authentifiée — 403 FORBIDDEN")
    void unauthenticatedAccess_rejected() throws Exception {
        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isForbidden());
    }
}
