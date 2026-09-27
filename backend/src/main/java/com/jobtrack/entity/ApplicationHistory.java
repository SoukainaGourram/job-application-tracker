package com.jobtrack.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "application_history", indexes = {
        @Index(name = "idx_history_app_id", columnList = "application_id"),
        @Index(name = "idx_history_changed_at", columnList = "changed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 30)
    private ApplicationStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private ApplicationStatus newStatus;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @Column(columnDefinition = "TEXT")
    private String note;
}
