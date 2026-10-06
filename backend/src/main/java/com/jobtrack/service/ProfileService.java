package com.jobtrack.service;

import com.jobtrack.dto.request.UpdateProfileRequest;
import com.jobtrack.dto.response.ProfileResponse;
import com.jobtrack.entity.User;
import com.jobtrack.exception.ResourceNotFoundException;
import com.jobtrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final UserRepository userRepository;

    /**
     * Retourne le profil de l'utilisateur actuellement connecté.
     */
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(User authUser) {
        log.info("Fetching profile for user ID {}", authUser.getId());
        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", authUser.getId()));

        return toProfileResponse(user);
    }

    /**
     * Met à jour les informations du profil de l'utilisateur connecté.
     * L'email est conservé immuable.
     */
    @Transactional
    public ProfileResponse updateProfile(UpdateProfileRequest request, User authUser) {
        log.info("Updating profile for user ID {}", authUser.getId());
        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", authUser.getId()));

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());

        User updated = userRepository.save(user);
        log.info("Profile updated successfully for user ID {}", updated.getId());

        return toProfileResponse(updated);
    }

    private ProfileResponse toProfileResponse(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }
}
