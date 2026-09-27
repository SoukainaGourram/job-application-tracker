package com.jobtrack.controller;

import com.jobtrack.dto.request.OfferCreateRequest;
import com.jobtrack.dto.request.OfferStatusUpdateRequest;
import com.jobtrack.dto.request.OfferUpdateRequest;
import com.jobtrack.dto.response.OfferResponse;
import com.jobtrack.entity.ContractType;
import com.jobtrack.entity.OfferStatus;
import com.jobtrack.entity.User;
import com.jobtrack.service.OfferService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
@Tag(name = "Offers", description = "Endpoints de gestion des offres d'emploi et de stage")
@SecurityRequirement(name = "bearerAuth")
public class OfferController {

    private final OfferService offerService;

    @GetMapping
    @Operation(summary = "Lister les offres", description = "Retourne la liste paginée et filtrée des offres de l'utilisateur connecté")
    public ResponseEntity<Page<OfferResponse>> getOffers(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) OfferStatus status,
            @RequestParam(required = false) ContractType contractType,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String technology,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<OfferResponse> offers = offerService.getOffers(
                user,
                search,
                status,
                contractType,
                city,
                country,
                technology,
                pageable
        );
        return ResponseEntity.ok(offers);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une offre par ID", description = "Retourne les détails d'une offre appartenant à l'utilisateur connecté")
    public ResponseEntity<OfferResponse> getOfferById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(offerService.getOfferById(id, user));
    }

    @PostMapping
    @Operation(summary = "Créer une offre", description = "Enregistre une nouvelle offre pour l'utilisateur connecté")
    public ResponseEntity<OfferResponse> createOffer(
            @Valid @RequestBody OfferCreateRequest request,
            @AuthenticationPrincipal User user) {

        OfferResponse created = offerService.createOffer(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une offre", description = "Met à jour une offre existante de l'utilisateur connecté")
    public ResponseEntity<OfferResponse> updateOffer(
            @PathVariable Long id,
            @Valid @RequestBody OfferUpdateRequest request,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(offerService.updateOffer(id, request, user));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Modifier le statut d'une offre", description = "Met à jour uniquement le statut d'une offre de l'utilisateur connecté")
    public ResponseEntity<OfferResponse> updateOfferStatus(
            @PathVariable Long id,
            @Valid @RequestBody OfferStatusUpdateRequest request,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(offerService.updateOfferStatus(id, request.getStatus(), user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une offre", description = "Supprime une offre de l'utilisateur connecté")
    public ResponseEntity<Void> deleteOffer(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        offerService.deleteOffer(id, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/parse-url")
    @Operation(summary = "Stub parsing URL", description = "Stub non fonctionnel réservé pour une phase ultérieure")
    public ResponseEntity<Map<String, String>> parseUrl(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("message", "Le scraping d'offres sera disponible dans une version ultérieure."));
    }
}
