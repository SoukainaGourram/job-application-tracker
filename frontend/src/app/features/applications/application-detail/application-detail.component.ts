import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ApplicationService } from '../../../core/services/application.service';
import { Application, ApplicationHistory, ApplicationStatus } from '../../../core/models/application.models';

@Component({
  selector: 'app-application-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './application-detail.component.html',
  styleUrl: './application-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private applicationService = inject(ApplicationService);

  application = signal<Application | null>(null);
  history = signal<ApplicationHistory[]>([]);
  loading = signal<boolean>(true);
  errorMessage = signal<string | null>(null);
  showDeleteConfirm = signal<boolean>(false);

  readonly statusOptions: { value: ApplicationStatus; label: string }[] = [
    { value: 'TO_APPLY', label: 'À postuler' },
    { value: 'APPLIED', label: 'Postulé' },
    { value: 'SCREENING', label: 'Screening RH' },
    { value: 'INTERVIEW', label: 'Entretien' },
    { value: 'OFFER', label: 'Offre reçue' },
    { value: 'ACCEPTED', label: 'Acceptée' },
    { value: 'REJECTED', label: 'Refusée' },
    { value: 'WITHDRAWN', label: 'Retirée' },
  ];

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = parseInt(idParam, 10);
      if (!isNaN(id)) {
        this.loadApplication(id);
      } else {
        this.errorMessage.set('Identifiant de candidature invalide.');
        this.loading.set(false);
      }
    }
  }

  loadApplication(id: number): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.applicationService.getApplicationById(id).pipe(
      finalize(() => this.loading.set(false))
    ).subscribe({
      next: (app) => {
        this.application.set(app);
        this.loadHistory(id);
      },
      error: () => {
        this.errorMessage.set('Candidature introuvable.');
      },
    });
  }

  loadHistory(id: number): void {
    this.applicationService.getApplicationHistory(id).subscribe({
      next: (hist) => {
        this.history.set(hist || []);
      },
    });
  }

  onStatusChange(newStatus: string): void {
    const current = this.application();
    if (!current) return;

    const status = newStatus as ApplicationStatus;
    if (current.status === status) return;

    this.applicationService.updateStatus(current.id, status).subscribe({
      next: (updated) => {
        this.application.set(updated);
        this.loadHistory(current.id);
      },
      error: () => {
        alert('Erreur lors du changement de statut.');
      },
    });
  }

  promptDelete(): void {
    this.showDeleteConfirm.set(true);
  }

  cancelDelete(): void {
    this.showDeleteConfirm.set(false);
  }

  executeDelete(): void {
    const current = this.application();
    if (!current) return;

    this.applicationService.deleteApplication(current.id).subscribe({
      next: () => {
        this.router.navigate(['/applications']);
      },
      error: () => {
        alert('Erreur lors de la suppression.');
        this.showDeleteConfirm.set(false);
      },
    });
  }

  getStatusBadgeClass(status: ApplicationStatus): string {
    switch (status) {
      case 'TO_APPLY': return 'badge-to-apply';
      case 'APPLIED': return 'badge-applied';
      case 'SCREENING': return 'badge-screening';
      case 'INTERVIEW': return 'badge-interview';
      case 'OFFER': return 'badge-offer';
      case 'ACCEPTED': return 'badge-accepted';
      case 'REJECTED': return 'badge-rejected';
      case 'WITHDRAWN': return 'badge-withdrawn';
      default: return '';
    }
  }

  getStatusLabel(status: ApplicationStatus): string {
    const opt = this.statusOptions.find((s) => s.value === status);
    return opt ? opt.label : status;
  }
}
