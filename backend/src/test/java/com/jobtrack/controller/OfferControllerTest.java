package com.jobtrack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtrack.config.ApplicationConfig;
import com.jobtrack.config.SecurityConfig;
import com.jobtrack.dto.request.OfferCreateRequest;
import com.jobtrack.dto.request.OfferStatusUpdateRequest;
import com.jobtrack.dto.request.OfferUpdateRequest;
import com.jobtrack.dto.response.OfferResponse;
import com.jobtrack.entity.ContractType;
import com.jobtrack.entity.OfferStatus;
import com.jobtrack.entity.Role;
import com.jobtrack.entity.User;
import com.jobtrack.exception.GlobalExceptionHandler;
import com.jobtrack.exception.ResourceNotFoundException;
import com.jobtrack.security.JwtAuthenticationFilter;
import com.jobtrack.security.JwtService;
import com.jobtrack.service.OfferService;
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

@WebMvcTest(OfferController.class)
@Import({SecurityConfig.class, ApplicationConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = {
        "spring.security.filter.order=0",
        "application.security.jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
        "application.security.jwt.expiration=86400000",
        "application.cors.allowed-origins=http://localhost:4200"
})
@DisplayName("OfferController — Tests d'intégration")
class OfferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OfferService offerService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private com.jobtrack.service.ApplicationService applicationService;

    private User authUser;
    private OfferResponse sampleOfferResponse;

    @BeforeEach
    void setUp() {
        authUser = User.builder()
                .id(1L)
                .email("alice@jobtrack.dev")
                .firstName("Alice")
                .lastName("Smith")
                .role(Role.USER)
                .password("encodedPassword")
                .build();

        sampleOfferResponse = OfferResponse.builder()
                .id(100L)
                .title("Full Stack Engineer")
                .companyName("Acme Corp")
                .contractType(ContractType.CDI)
                .status(OfferStatus.SAVED)
                .technologies(List.of("Angular", "Java", "Docker"))
                .city("Paris")
                .country("France")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // ── GET /api/offers ───────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/offers — succès pour utilisateur connecté → 200 OK")
    void getOffers_authenticated_returns200() throws Exception {
        when(offerService.getOffers(any(User.class), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleOfferResponse)));

        mockMvc.perform(get("/api/offers")
                        .with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(100))
                .andExpect(jsonPath("$.content[0].title").value("Full Stack Engineer"))
                .andExpect(jsonPath("$.content[0].companyName").value("Acme Corp"));
    }

    @Test
    @DisplayName("GET /api/offers — sans authentification → 401 ou 403")
    void getOffers_unauthenticated_isProtected() throws Exception {
        mockMvc.perform(get("/api/offers"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertTrue(status == 401 || status == 403);
                });
    }

    // ── GET /api/offers/{id} ──────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/offers/{id} — succès pour le propriétaire → 200 OK")
    void getOfferById_owned_returns200() throws Exception {
        when(offerService.getOfferById(eq(100L), any(User.class))).thenReturn(sampleOfferResponse);

        mockMvc.perform(get("/api/offers/100")
                        .with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.title").value("Full Stack Engineer"));
    }

    @Test
    @DisplayName("GET /api/offers/{id} — offre non trouvée ou d'un autre utilisateur → 404")
    void getOfferById_notFound_returns404() throws Exception {
        when(offerService.getOfferById(eq(999L), any(User.class)))
                .thenThrow(new ResourceNotFoundException("Offer", 999L));

        mockMvc.perform(get("/api/offers/999")
                        .with(user(authUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ── POST /api/offers ──────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/offers — création valide → 201 Created")
    void createOffer_valid_returns201() throws Exception {
        OfferCreateRequest request = OfferCreateRequest.builder()
                .title("Angular Developer")
                .companyName("Innovatech")
                .contractType(ContractType.CDI)
                .status(OfferStatus.SAVED)
                .technologies(List.of("Angular", "TypeScript"))
                .jobUrl("https://example.com/jobs/123")
                .city("Lyon")
                .country("France")
                .applicationDeadline(LocalDate.now().plusMonths(2))
                .build();

        when(offerService.createOffer(any(OfferCreateRequest.class), any(User.class)))
                .thenReturn(sampleOfferResponse);

        mockMvc.perform(post("/api/offers")
                        .with(user(authUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    @DisplayName("POST /api/offers — payload invalide (titre manquant) → 400 Bad Request")
    void createOffer_invalid_returns400() throws Exception {
        OfferCreateRequest request = OfferCreateRequest.builder()
                .title("") // blank
                .companyName("Innovatech")
                .contractType(ContractType.CDI)
                .build();

        mockMvc.perform(post("/api/offers")
                        .with(user(authUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.title").exists());
    }

    // ── PUT /api/offers/{id} ──────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/offers/{id} — mise à jour valide → 200 OK")
    void updateOffer_valid_returns200() throws Exception {
        OfferUpdateRequest request = OfferUpdateRequest.builder()
                .title("Lead Developer")
                .companyName("Acme Corp")
                .contractType(ContractType.CDI)
                .status(OfferStatus.TO_APPLY)
                .technologies(List.of("Java", "Kotlin"))
                .build();

        when(offerService.updateOffer(eq(100L), any(OfferUpdateRequest.class), any(User.class)))
                .thenReturn(sampleOfferResponse);

        mockMvc.perform(put("/api/offers/100")
                        .with(user(authUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/offers/{id} — mise à jour d'une offre non possédée → 404")
    void updateOffer_notOwned_returns404() throws Exception {
        OfferUpdateRequest request = OfferUpdateRequest.builder()
                .title("Lead Developer")
                .companyName("Acme Corp")
                .contractType(ContractType.CDI)
                .status(OfferStatus.TO_APPLY)
                .build();

        when(offerService.updateOffer(eq(999L), any(OfferUpdateRequest.class), any(User.class)))
                .thenThrow(new ResourceNotFoundException("Offer", 999L));

        mockMvc.perform(put("/api/offers/999")
                        .with(user(authUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ── PATCH /api/offers/{id}/status ─────────────────────────────────────────

    @Test
    @DisplayName("PATCH /api/offers/{id}/status — changement de statut valide → 200 OK")
    void patchStatus_valid_returns200() throws Exception {
        OfferStatusUpdateRequest request = OfferStatusUpdateRequest.builder()
                .status(OfferStatus.ARCHIVED)
                .build();

        when(offerService.updateOfferStatus(eq(100L), eq(OfferStatus.ARCHIVED), any(User.class)))
                .thenReturn(sampleOfferResponse);

        mockMvc.perform(patch("/api/offers/100/status")
                        .with(user(authUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // ── DELETE /api/offers/{id} ───────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/offers/{id} — suppression par propriétaire → 204 No Content")
    void deleteOffer_owned_returns204() throws Exception {
        doNothing().when(offerService).deleteOffer(eq(100L), any(User.class));

        mockMvc.perform(delete("/api/offers/100")
                        .with(user(authUser))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(offerService).deleteOffer(eq(100L), any(User.class));
    }

    @Test
    @DisplayName("DELETE /api/offers/{id} — suppression d'une offre non possédée → 404")
    void deleteOffer_notOwned_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Offer", 999L))
                .when(offerService).deleteOffer(eq(999L), any(User.class));

        mockMvc.perform(delete("/api/offers/999")
                        .with(user(authUser))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/offers/parse-url ────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/offers/parse-url — stub non fonctionnel → 501 Not Implemented")
    void parseUrl_returns501() throws Exception {
        mockMvc.perform(post("/api/offers/parse-url")
                        .with(user(authUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com/job\"}"))
                .andExpect(status().isNotImplemented());
    }

    @Test
    @DisplayName("POST /api/offers/{offerId}/applications — création candidature depuis offre → 201 Created")
    void createApplicationFromOffer_returns201() throws Exception {
        com.jobtrack.dto.response.ApplicationResponse appResponse = com.jobtrack.dto.response.ApplicationResponse.builder()
                .id(50L)
                .status(com.jobtrack.entity.ApplicationStatus.TO_APPLY)
                .build();

        when(applicationService.createApplicationFromOffer(eq(100L), any(User.class)))
                .thenReturn(appResponse);

        mockMvc.perform(post("/api/offers/100/applications")
                        .with(user(authUser))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(50))
                .andExpect(jsonPath("$.status").value("TO_APPLY"));
    }
}
