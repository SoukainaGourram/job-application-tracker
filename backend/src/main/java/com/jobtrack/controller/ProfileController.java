package com.jobtrack.controller;

import com.jobtrack.dto.request.UpdateProfileRequest;
import com.jobtrack.dto.response.ProfileResponse;
import com.jobtrack.entity.User;
import com.jobtrack.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Endpoints de gestion du profil de l'utilisateur connecté")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    @Operation(
            summary = "Consulter son profil",
            description = "Retourne les informations du profil de l'utilisateur authentifié (sans accepter d'ID externe)"
    )
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(profileService.getProfile(user));
    }

    @PutMapping
    @Operation(
            summary = "Modifier son profil",
            description = "Met à jour le prénom et le nom de l'utilisateur authentifié"
    )
    public ResponseEntity<ProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(profileService.updateProfile(request, user));
    }
}
