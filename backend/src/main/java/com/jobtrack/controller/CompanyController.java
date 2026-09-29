package com.jobtrack.controller;

import com.jobtrack.dto.request.CompanyContactCreateRequest;
import com.jobtrack.dto.request.CompanyContactUpdateRequest;
import com.jobtrack.dto.request.CompanyCreateRequest;
import com.jobtrack.dto.request.CompanyUpdateRequest;
import com.jobtrack.dto.response.CompanyContactResponse;
import com.jobtrack.dto.response.CompanyResponse;
import com.jobtrack.dto.response.CompanySummaryResponse;
import com.jobtrack.entity.User;
import com.jobtrack.service.CompanyService;
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
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Gestion des entreprises et du CRM recrutement")
@SecurityRequirement(name = "bearerAuth")
public class CompanyController {

    private final CompanyService companyService;

    // ── Company Endpoints ────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Lister les entreprises", description = "Retourne la liste paginée des entreprises de l'utilisateur avec filtres optionnels")
    public ResponseEntity<Page<CompanySummaryResponse>> getCompanies(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String size,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User user) {

        Page<CompanySummaryResponse> companies = companyService.getCompanies(user, search, industry, location, size, pageable);
        return ResponseEntity.ok(companies);
    }

    @GetMapping("/all")
    @Operation(summary = "Lister toutes les entreprises pour sélection", description = "Retourne la liste complète des entreprises triées par nom")
    public ResponseEntity<List<CompanySummaryResponse>> getAllCompaniesForSelect(@AuthenticationPrincipal User user) {
        List<CompanySummaryResponse> list = companyService.getAllCompaniesForSelect(user);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une entreprise", description = "Retourne les détails complets d'une entreprise avec ses contacts et métriques")
    public ResponseEntity<CompanyResponse> getCompanyById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        CompanyResponse company = companyService.getCompanyById(id, user);
        return ResponseEntity.ok(company);
    }

    @PostMapping
    @Operation(summary = "Créer une entreprise", description = "Ajoute une nouvelle entreprise rattachée à l'utilisateur courant")
    public ResponseEntity<CompanyResponse> createCompany(
            @Valid @RequestBody CompanyCreateRequest request,
            @AuthenticationPrincipal User user) {

        CompanyResponse created = companyService.createCompany(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une entreprise", description = "Met à jour les informations d'une entreprise")
    public ResponseEntity<CompanyResponse> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyUpdateRequest request,
            @AuthenticationPrincipal User user) {

        CompanyResponse updated = companyService.updateCompany(id, request, user);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une entreprise", description = "Supprime l'entreprise sans supprimer les offres associées (company_id devient NULL)")
    public ResponseEntity<Void> deleteCompany(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        companyService.deleteCompany(id, user);
        return ResponseEntity.noContent().build();
    }

    // ── Contacts Endpoints (CRM) ─────────────────────────────────────────────

    @GetMapping("/{companyId}/contacts")
    @Operation(summary = "Lister les contacts", description = "Retourne la liste des contacts recruteurs pour une entreprise")
    public ResponseEntity<List<CompanyContactResponse>> getContacts(
            @PathVariable Long companyId,
            @AuthenticationPrincipal User user) {

        List<CompanyContactResponse> contacts = companyService.getContacts(companyId, user);
        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/{companyId}/contacts/{contactId}")
    @Operation(summary = "Obtenir un contact", description = "Retourne les détails d'un contact recruteur")
    public ResponseEntity<CompanyContactResponse> getContactById(
            @PathVariable Long companyId,
            @PathVariable Long contactId,
            @AuthenticationPrincipal User user) {

        CompanyContactResponse contact = companyService.getContactById(companyId, contactId, user);
        return ResponseEntity.ok(contact);
    }

    @PostMapping("/{companyId}/contacts")
    @Operation(summary = "Ajouter un contact", description = "Associe un nouveau contact recruteur à une entreprise")
    public ResponseEntity<CompanyContactResponse> addContact(
            @PathVariable Long companyId,
            @Valid @RequestBody CompanyContactCreateRequest request,
            @AuthenticationPrincipal User user) {

        CompanyContactResponse created = companyService.addContact(companyId, request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{companyId}/contacts/{contactId}")
    @Operation(summary = "Modifier un contact", description = "Met à jour les coordonnées d'un contact recruteur")
    public ResponseEntity<CompanyContactResponse> updateContact(
            @PathVariable Long companyId,
            @PathVariable Long contactId,
            @Valid @RequestBody CompanyContactUpdateRequest request,
            @AuthenticationPrincipal User user) {

        CompanyContactResponse updated = companyService.updateContact(companyId, contactId, request, user);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{companyId}/contacts/{contactId}")
    @Operation(summary = "Supprimer un contact", description = "Supprime un contact recruteur de l'entreprise")
    public ResponseEntity<Void> deleteContact(
            @PathVariable Long companyId,
            @PathVariable Long contactId,
            @AuthenticationPrincipal User user) {

        companyService.deleteContact(companyId, contactId, user);
        return ResponseEntity.noContent().build();
    }
}
