package com.jobtrack.service;

import com.jobtrack.dto.request.UpdateProfileRequest;
import com.jobtrack.dto.response.ProfileResponse;
import com.jobtrack.entity.Role;
import com.jobtrack.entity.User;
import com.jobtrack.exception.ResourceNotFoundException;
import com.jobtrack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileService — Tests unitaires")
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfileService profileService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("Soukaina")
                .lastName("Gourram")
                .email("soukaina@example.com")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("getProfile() — retourne le profil de l'utilisateur connecté")
    void getProfile_returnsAuthenticatedUserProfile() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        ProfileResponse response = profileService.getProfile(testUser);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFirstName()).isEqualTo("Soukaina");
        assertThat(response.getLastName()).isEqualTo("Gourram");
        assertThat(response.getEmail()).isEqualTo("soukaina@example.com");
    }

    @Test
    @DisplayName("getProfile() — lance ResourceNotFoundException si l'utilisateur n'existe plus")
    void getProfile_userNotFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getProfile(testUser))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateProfile() — met à jour le prénom et le nom de l'utilisateur connecté")
    void updateProfile_updatesFirstAndLastName() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("Sarah")
                .lastName("Benali")
                .build();

        User updatedUser = User.builder()
                .id(1L)
                .firstName("Sarah")
                .lastName("Benali")
                .email("soukaina@example.com")
                .role(Role.USER)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        ProfileResponse response = profileService.updateProfile(request, testUser);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFirstName()).isEqualTo("Sarah");
        assertThat(response.getLastName()).isEqualTo("Benali");
        assertThat(response.getEmail()).isEqualTo("soukaina@example.com");

        verify(userRepository).save(argThat(u ->
                u.getFirstName().equals("Sarah") &&
                u.getLastName().equals("Benali") &&
                u.getEmail().equals("soukaina@example.com")
        ));
    }
}
