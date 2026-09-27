import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { OfferService } from '../../../core/services/offer.service';
import { ApplicationService } from '../../../core/services/application.service';
import { Offer, OfferStatus } from '../../../core/models/offer.models';

@Component({
  selector: 'app-offer-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './offer-detail.component.html',
  styleUrl: './offer-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OfferDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private offerService = inject(OfferService);
  private applicationService = inject(ApplicationService);

  offer = signal<Offer | null>(null);
  loading = signal<boolean>(true);
  applying = signal<boolean>(false);
  errorMessage = signal<string | null>(null);
  conflictMessage = signal<string | null>(null);
  showDeleteConfirm = signal<boolean>(false);

  readonly statusOptions: { value: OfferStatus; label: string }[] = [
    { value: 'SAVED', label: 'Sauvegardée' },
    { value: 'TO_APPLY', label: 'À postuler' },
    { value: 'ARCHIVED', label: 'Archivée' },
  ];

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = parseInt(idParam, 10);
      if (!isNaN(id)) {
        this.loadOffer(id);
      } else {
        this.errorMessage.set('Identifiant d\'offre invalide.');
        this.loading.set(false);
      }
    }
  }

  loadOffer(id: number): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.offerService.getOfferById(id).pipe(
      finalize(() => this.loading.set(false))
    ).subscribe({
      next: (data) => {
        this.offer.set(data);
      },
      error: () => {
        this.errorMessage.set('Offre introuvable ou vous n\'avez pas les permissions requises.');
      },
    });
  }

  onStatusChange(newStatus: string): void {
    const current = this.offer();
    if (!current) return;

    const status = newStatus as OfferStatus;
    this.offerService.updateStatus(current.id, status).subscribe({
      next: (updated) => {
        this.offer.set(updated);
      },
      error: () => {
        alert('Erreur lors de la mise à jour du statut.');
      },
    });
  }

  onApplyClick(): void {
    const current = this.offer();
    if (!current || this.applying()) return;

    this.applying.set(true);
    this.conflictMessage.set(null);

    this.applicationService.createApplicationFromOffer(current.id).pipe(
      finalize(() => this.applying.set(false))
    ).subscribe({
      next: (app) => {
        this.router.navigate(['/applications', app.id]);
      },
      error: (err) => {
        if (err.status === 409) {
          this.conflictMessage.set('Une candidature active existe déjà pour cette offre.');
        } else {
          alert(err?.error?.message || 'Erreur lors de la création de la candidature.');
        }
      },
    });
  }

  closeConflictMessage(): void {
    this.conflictMessage.set(null);
  }

  promptDelete(): void {
    this.showDeleteConfirm.set(true);
  }

  cancelDelete(): void {
    this.showDeleteConfirm.set(false);
  }

  executeDelete(): void {
    const current = this.offer();
    if (!current) return;

    this.offerService.deleteOffer(current.id).subscribe({
      next: () => {
        this.router.navigate(['/offers']);
      },
      error: () => {
        alert('Erreur lors de la suppression de l\'offre.');
        this.showDeleteConfirm.set(false);
      },
    });
  }

  getStatusBadgeClass(status: OfferStatus): string {
    switch (status) {
      case 'SAVED': return 'badge-saved';
      case 'TO_APPLY': return 'badge-to-apply';
      case 'ARCHIVED': return 'badge-archived';
      default: return '';
    }
  }

  getStatusLabel(status: OfferStatus): string {
    switch (status) {
      case 'SAVED': return 'Sauvegardée';
      case 'TO_APPLY': return 'À postuler';
      case 'ARCHIVED': return 'Archivée';
      default: return status;
    }
  }
}
