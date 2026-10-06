package com.jobtrack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtrack.config.ApplicationConfig;
import com.jobtrack.config.SecurityConfig;
import com.jobtrack.dto.request.UpdateProfileRequest;
import com.jobtrack.dto.response.ProfileResponse;
import com.jobtrack.entity.Role;
import com.jobtrack.entity.User;
import com.jobtrack.exception.GlobalExceptionHandler;
import com.jobtrack.security.JwtAuthenticationFilter;
import com.jobtrack.security.JwtService;
import com.jobtrack.service.ProfileService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@Import({SecurityConfig.class, ApplicationConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = {
        "spring.security.filter.order=0",
        "application.security.jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
        "application.security.jwt.expiration=86400000",
        "application.cors.allowed-origins=http://localhost:4200"
})
@DisplayName("ProfileController — Tests d'intégration WebMvc")
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private User authUser;
    private ProfileResponse sampleProfile;

    @BeforeEach
    void setUp() {
        authUser = User.builder()
                .id(1L)
                .email("soukaina@example.com")
                .firstName("Soukaina")
                .lastName("Gourram")
                .role(Role.USER)
                .build();

        sampleProfile = ProfileResponse.builder()
                .id(1L)
                .firstName("Soukaina")
                .lastName("Gourram")
                .email("soukaina@example.com")
                .build();
    }

    // ── GET /api/profile ───────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/profile — 200 OK avec le profil de l'utilisateur connecté")
    void getProfile_authenticated_returns200() throws Exception {
        when(profileService.getProfile(any(User.class))).thenReturn(sampleProfile);

        mockMvc.perform(get("/api/profile")
                        .with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Soukaina"))
                .andExpect(jsonPath("$.lastName").value("Gourram"))
                .andExpect(jsonPath("$.email").value("soukaina@example.com"));
    }

    @Test
    @DisplayName("GET /api/profile — 403 Forbidden sans token d'authentification")
    void getProfile_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isForbidden());
    }

    // ── PUT /api/profile ───────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/profile — 200 OK et modification réussie")
    void updateProfile_validRequest_returns200() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("Sarah")
                .lastName("Benali")
                .build();

        ProfileResponse updatedProfile = ProfileResponse.builder()
                .id(1L)
                .firstName("Sarah")
                .lastName("Benali")
                .email("soukaina@example.com")
                .build();

        when(profileService.updateProfile(any(UpdateProfileRequest.class), any(User.class)))
                .thenReturn(updatedProfile);

        mockMvc.perform(put("/api/profile")
                        .with(user(authUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Sarah"))
                .andExpect(jsonPath("$.lastName").value("Benali"))
                .andExpect(jsonPath("$.email").value("soukaina@example.com"));
    }

    @Test
    @DisplayName("PUT /api/profile — 400 Bad Request si prénom ou nom est vide")
    void updateProfile_invalidRequest_returns400() throws Exception {
        UpdateProfileRequest invalidRequest = UpdateProfileRequest.builder()
                .firstName("")
                .lastName("Gourram")
                .build();

        mockMvc.perform(put("/api/profile")
                        .with(user(authUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("PUT /api/profile — 403 Forbidden sans token d'authentification")
    void updateProfile_unauthenticated_returns403() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("Sarah")
                .lastName("Benali")
                .build();

        mockMvc.perform(put("/api/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
