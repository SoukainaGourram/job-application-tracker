package com.jobtrack.service;

import com.jobtrack.dto.request.CompanyContactCreateRequest;
import com.jobtrack.dto.request.CompanyContactUpdateRequest;
import com.jobtrack.dto.request.CompanyCreateRequest;
import com.jobtrack.dto.request.CompanyUpdateRequest;
import com.jobtrack.dto.response.CompanyContactResponse;
import com.jobtrack.dto.response.CompanyResponse;
import com.jobtrack.dto.response.CompanySummaryResponse;
import com.jobtrack.entity.Company;
import com.jobtrack.entity.CompanyContact;
import com.jobtrack.entity.User;
import com.jobtrack.exception.ResourceNotFoundException;
import com.jobtrack.mapper.CompanyContactMapper;
import com.jobtrack.mapper.CompanyMapper;
import com.jobtrack.repository.CompanyContactRepository;
import com.jobtrack.repository.CompanyRepository;
import com.jobtrack.repository.CompanySpecification;
import com.jobtrack.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyContactRepository companyContactRepository;
    private final OfferRepository offerRepository;
    private final CompanyMapper companyMapper;
    private final CompanyContactMapper companyContactMapper;

    // ── Company CRUD ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<CompanySummaryResponse> getCompanies(
            User user,
            String search,
            String industry,
            String location,
            String size,
            Pageable pageable) {

        Specification<Company> spec = CompanySpecification.withFilters(
                user.getId(), search, industry, location, size
        );

        return companyRepository.findAll(spec, pageable)
                .map(company -> {
                    CompanySummaryResponse summary = companyMapper.toSummaryResponse(company);
                    summary.setOffersCount(offerRepository.countByCompanyId(company.getId()));
                    summary.setContactsCount(companyContactRepository.countByCompanyId(company.getId()));
                    return summary;
                });
    }

    @Transactional(readOnly = true)
    public List<CompanySummaryResponse> getAllCompaniesForSelect(User user) {
        return companyRepository.findAllByUserIdOrderByNameAsc(user.getId())
                .stream()
                .map(company -> {
                    CompanySummaryResponse summary = companyMapper.toSummaryResponse(company);
                    summary.setOffersCount(offerRepository.countByCompanyId(company.getId()));
                    summary.setContactsCount(companyContactRepository.countByCompanyId(company.getId()));
                    return summary;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(Long id, User user) {
        Company company = companyRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));

        CompanyResponse response = companyMapper.toResponse(company);
        response.setOffersCount(offerRepository.countByCompanyId(id));

        List<CompanyContactResponse> contacts = companyContactRepository
                .findAllByCompanyIdAndCompanyUserIdOrderByCreatedAtDesc(id, user.getId())
                .stream()
                .map(companyContactMapper::toResponse)
                .toList();
        response.setContacts(contacts);

        return response;
    }

    @Transactional
    public CompanyResponse createCompany(CompanyCreateRequest request, User user) {
        Company company = companyMapper.toEntity(request);
        company.setUser(user);

        Company saved = companyRepository.save(company);
        log.info("Created company ID {} '{}' for user ID {}", saved.getId(), saved.getName(), user.getId());

        CompanyResponse response = companyMapper.toResponse(saved);
        response.setOffersCount(0);
        return response;
    }

    @Transactional
    public CompanyResponse updateCompany(Long id, CompanyUpdateRequest request, User user) {
        Company company = companyRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));

        companyMapper.updateEntityFromRequest(request, company);
        Company updated = companyRepository.save(company);
        log.info("Updated company ID {} for user ID {}", updated.getId(), user.getId());

        CompanyResponse response = companyMapper.toResponse(updated);
        response.setOffersCount(offerRepository.countByCompanyId(id));

        List<CompanyContactResponse> contacts = companyContactRepository
                .findAllByCompanyIdAndCompanyUserIdOrderByCreatedAtDesc(id, user.getId())
                .stream()
                .map(companyContactMapper::toResponse)
                .toList();
        response.setContacts(contacts);

        return response;
    }

    @Transactional
    public void deleteCompany(Long id, User user) {
        Company company = companyRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));

        // Critical safety rule: Clear company reference in offers first.
        // Offers and their applications are preserved!
        offerRepository.clearCompanyReference(company.getId());

        companyRepository.delete(company);
        log.info("Deleted company ID {} for user ID {}. Associated offers set to company=null.", id, user.getId());
    }

    // ── Contacts CRUD (CRM) ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CompanyContactResponse> getContacts(Long companyId, User user) {
        // Verify company belongs to user
        companyRepository.findByIdAndUserId(companyId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        return companyContactRepository
                .findAllByCompanyIdAndCompanyUserIdOrderByCreatedAtDesc(companyId, user.getId())
                .stream()
                .map(companyContactMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyContactResponse getContactById(Long companyId, Long contactId, User user) {
        // Verify company belongs to user
        companyRepository.findByIdAndUserId(companyId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        CompanyContact contact = companyContactRepository
                .findByIdAndCompanyIdAndCompanyUserId(contactId, companyId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("CompanyContact", contactId));

        return companyContactMapper.toResponse(contact);
    }

    @Transactional
    public CompanyContactResponse addContact(Long companyId, CompanyContactCreateRequest request, User user) {
        Company company = companyRepository.findByIdAndUserId(companyId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        CompanyContact contact = companyContactMapper.toEntity(request);
        contact.setCompany(company);

        CompanyContact saved = companyContactRepository.save(contact);
        log.info("Added contact ID {} '{} {}' to company ID {}", saved.getId(), saved.getFirstName(), saved.getLastName(), companyId);

        return companyContactMapper.toResponse(saved);
    }

    @Transactional
    public CompanyContactResponse updateContact(
            Long companyId, Long contactId, CompanyContactUpdateRequest request, User user) {

        // Verify company belongs to user
        companyRepository.findByIdAndUserId(companyId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        CompanyContact contact = companyContactRepository
                .findByIdAndCompanyIdAndCompanyUserId(contactId, companyId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("CompanyContact", contactId));

        companyContactMapper.updateEntityFromRequest(request, contact);
        CompanyContact updated = companyContactRepository.save(contact);
        log.info("Updated contact ID {} for company ID {}", contactId, companyId);

        return companyContactMapper.toResponse(updated);
    }

    @Transactional
    public void deleteContact(Long companyId, Long contactId, User user) {
        // Verify company belongs to user
        companyRepository.findByIdAndUserId(companyId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        CompanyContact contact = companyContactRepository
                .findByIdAndCompanyIdAndCompanyUserId(contactId, companyId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("CompanyContact", contactId));

        companyContactRepository.delete(contact);
        log.info("Deleted contact ID {} from company ID {}", contactId, companyId);
    }
}
