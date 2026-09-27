package com.jobtrack.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "offers", indexes = {
        @Index(name = "idx_offer_user_id", columnList = "user_id"),
        @Index(name = "idx_offer_status", columnList = "status"),
        @Index(name = "idx_offer_contract_type", columnList = "contract_type"),
        @Index(name = "idx_offer_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false, length = 30)
    private ContractType contractType;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "offer_technologies", joinColumns = @JoinColumn(name = "offer_id"))
    @Column(name = "technology", length = 50)
    @Builder.Default
    private List<String> technologies = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "job_url", length = 500)
    private String jobUrl;

    @Column(length = 100)
    private String salary;

    @Column(length = 100)
    private String source;

    @Column(name = "application_deadline")
    private LocalDate applicationDeadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private OfferStatus status = OfferStatus.SAVED;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
