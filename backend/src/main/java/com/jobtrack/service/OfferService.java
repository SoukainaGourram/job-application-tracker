package com.jobtrack.service;

import com.jobtrack.dto.request.OfferCreateRequest;
import com.jobtrack.dto.request.OfferUpdateRequest;
import com.jobtrack.dto.response.OfferResponse;
import com.jobtrack.entity.*;
import com.jobtrack.exception.ResourceNotFoundException;
import com.jobtrack.mapper.OfferMapper;
import com.jobtrack.repository.CompanyRepository;
import com.jobtrack.repository.OfferRepository;
import com.jobtrack.repository.OfferSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferService {

    private final OfferRepository offerRepository;
    private final CompanyRepository companyRepository;
    private final OfferMapper offerMapper;

    @Transactional(readOnly = true)
    public Page<OfferResponse> getOffers(
            User user,
            String search,
            OfferStatus status,
            ContractType contractType,
            String city,
            String country,
            String technology,
            Pageable pageable) {

        Specification<Offer> spec = OfferSpecification.withFilters(
                user.getId(),
                search,
                status,
                contractType,
                city,
                country,
                technology
        );

        return offerRepository.findAll(spec, pageable)
                .map(offerMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OfferResponse getOfferById(Long id, User user) {
        Offer offer = offerRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Offer", id));
        return offerMapper.toResponse(offer);
    }

    @Transactional
    public OfferResponse createOffer(OfferCreateRequest request, User user) {
        Offer offer = offerMapper.toEntity(request);
        offer.setUser(user);

        // Optional company association with strict user isolation
        if (request.getCompanyId() != null) {
            Company company = companyRepository.findByIdAndUserId(request.getCompanyId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company", request.getCompanyId()));
            offer.setCompany(company);
        }

        if (offer.getStatus() == null) {
            offer.setStatus(OfferStatus.SAVED);
        }

        Offer saved = offerRepository.save(offer);
        log.info("Created offer ID {} '{}' for user ID {}", saved.getId(), saved.getTitle(), user.getId());
        return offerMapper.toResponse(saved);
    }

    @Transactional
    public OfferResponse updateOffer(Long id, OfferUpdateRequest request, User user) {
        Offer offer = offerRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Offer", id));

        offerMapper.updateEntityFromRequest(request, offer);

        if (request.getCompanyId() != null) {
            Company company = companyRepository.findByIdAndUserId(request.getCompanyId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company", request.getCompanyId()));
            offer.setCompany(company);
        }

        Offer updated = offerRepository.save(offer);
        log.info("Updated offer ID {} for user ID {}", updated.getId(), user.getId());
        return offerMapper.toResponse(updated);
    }

    @Transactional
    public OfferResponse updateOfferStatus(Long id, OfferStatus status, User user) {
        Offer offer = offerRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Offer", id));

        offer.setStatus(status);
        Offer updated = offerRepository.save(offer);
        log.info("Updated status of offer ID {} to {} for user ID {}", updated.getId(), status, user.getId());
        return offerMapper.toResponse(updated);
    }

    @Transactional
    public void deleteOffer(Long id, User user) {
        Offer offer = offerRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Offer", id));

        offerRepository.delete(offer);
        log.info("Deleted offer ID {} for user ID {}", id, user.getId());
    }
}
