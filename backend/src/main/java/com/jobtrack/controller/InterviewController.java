package com.jobtrack.controller;

import com.jobtrack.dto.request.InterviewCreateRequest;
import com.jobtrack.dto.request.InterviewStatusUpdateRequest;
import com.jobtrack.dto.request.InterviewUpdateRequest;
import com.jobtrack.dto.response.InterviewResponse;
import com.jobtrack.entity.User;
import com.jobtrack.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications/{applicationId}/interviews")
@RequiredArgsConstructor
@Tag(name = "Interviews", description = "Gestion des entretiens liés aux candidatures")
@SecurityRequirement(name = "bearerAuth")
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping
    @Operation(summary = "Créer un entretien", description = "Crée un entretien pour une candidature de l'utilisateur")
    public ResponseEntity<InterviewResponse> createInterview(
            @PathVariable Long applicationId,
            @Valid @RequestBody InterviewCreateRequest request,
            @AuthenticationPrincipal User user) {

        InterviewResponse created = interviewService.createInterview(applicationId, request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Lister les entretiens", description = "Retourne tous les entretiens d'une candidature, triés par date croissante")
    public ResponseEntity<List<InterviewResponse>> getInterviews(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(interviewService.getInterviews(applicationId, user));
    }

    @GetMapping("/{interviewId}")
    @Operation(summary = "Détail d'un entretien", description = "Retourne les détails complets d'un entretien")
    public ResponseEntity<InterviewResponse> getInterview(
            @PathVariable Long applicationId,
            @PathVariable Long interviewId,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(interviewService.getInterview(applicationId, interviewId, user));
    }

    @PutMapping("/{interviewId}")
    @Operation(summary = "Modifier un entretien", description = "Met à jour les informations d'un entretien (date, lieu, notes, feedback…)")
    public ResponseEntity<InterviewResponse> updateInterview(
            @PathVariable Long applicationId,
            @PathVariable Long interviewId,
            @Valid @RequestBody InterviewUpdateRequest request,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(interviewService.updateInterview(applicationId, interviewId, request, user));
    }

    @PatchMapping("/{interviewId}/status")
    @Operation(summary = "Changer le statut d'un entretien", description = "Passe l'entretien à COMPLETED, CANCELLED ou NO_SHOW")
    public ResponseEntity<InterviewResponse> updateStatus(
            @PathVariable Long applicationId,
            @PathVariable Long interviewId,
            @Valid @RequestBody InterviewStatusUpdateRequest request,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(
                interviewService.updateStatus(applicationId, interviewId, request.getStatus(), request.getNotes(), user)
        );
    }

    @DeleteMapping("/{interviewId}")
    @Operation(summary = "Supprimer un entretien", description = "Supprime définitivement un entretien")
    public ResponseEntity<Void> deleteInterview(
            @PathVariable Long applicationId,
            @PathVariable Long interviewId,
            @AuthenticationPrincipal User user) {

        interviewService.deleteInterview(applicationId, interviewId, user);
        return ResponseEntity.noContent().build();
    }
}
