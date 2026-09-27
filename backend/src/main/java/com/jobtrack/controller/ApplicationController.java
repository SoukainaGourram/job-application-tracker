package com.jobtrack.controller;

import com.jobtrack.dto.request.ApplicationCreateRequest;
import com.jobtrack.dto.request.ApplicationStatusUpdateRequest;
import com.jobtrack.dto.request.ApplicationUpdateRequest;
import com.jobtrack.dto.response.ApplicationHistoryResponse;
import com.jobtrack.dto.response.ApplicationResponse;
import com.jobtrack.dto.response.ApplicationSummaryResponse;
import com.jobtrack.entity.ApplicationStatus;
import com.jobtrack.entity.User;
import com.jobtrack.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "Applications", description = "Endpoints de gestion des candidatures et du pipeline")
@SecurityRequirement(name = "bearerAuth")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    @Operation(summary = "Créer une candidature", description = "Crée une candidature pour une offre donnée")
    public ResponseEntity<ApplicationResponse> createApplication(
            @Valid @RequestBody ApplicationCreateRequest request,
            @AuthenticationPrincipal User user) {

        ApplicationResponse created = applicationService.createApplication(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Lister les candidatures", description = "Retourne la liste paginée et filtrée des candidatures de l'utilisateur")
    public ResponseEntity<Page<ApplicationSummaryResponse>> getApplications(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) String company,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ApplicationSummaryResponse> page = applicationService.getApplications(
                user, search, status, company, pageable
        );
        return ResponseEntity.ok(page);
    }

    @GetMapping("/kanban")
    @Operation(summary = "Candidatures Kanban", description = "Retourne toutes les candidatures de l'utilisateur pour le pipeline Kanban")
    public ResponseEntity<List<ApplicationSummaryResponse>> getKanbanApplications(
            @AuthenticationPrincipal User user) {

        List<ApplicationSummaryResponse> list = applicationService.getKanbanApplications(user);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une candidature", description = "Retourne les détails complets d'une candidature")
    public ResponseEntity<ApplicationResponse> getApplicationById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(applicationService.getApplicationById(id, user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une candidature", description = "Met à jour les informations d'une candidature existante")
    public ResponseEntity<ApplicationResponse> updateApplication(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationUpdateRequest request,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(applicationService.updateApplication(id, request, user));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Modifier le statut d'une candidature", description = "Met à jour le statut et génère automatiquement une entrée d'historique")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationStatusUpdateRequest request,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(applicationService.updateApplicationStatus(id, request.getStatus(), request.getNote(), user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une candidature", description = "Supprime une candidature et son historique associé")
    public ResponseEntity<Void> deleteApplication(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        applicationService.deleteApplication(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Historique d'une candidature", description = "Retourne l'historique chronologique des changements de statut")
    public ResponseEntity<List<ApplicationHistoryResponse>> getApplicationHistory(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(applicationService.getApplicationHistory(id, user));
    }
}
