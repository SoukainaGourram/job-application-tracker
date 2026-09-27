import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApplicationService } from '../../../core/services/application.service';
import { ApplicationFilters, ApplicationStatus, ApplicationSummary } from '../../../core/models/application.models';

@Component({
  selector: 'app-applications-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './applications-list.component.html',
  styleUrl: './applications-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationsListComponent implements OnInit {
  private applicationService = inject(ApplicationService);

  applications = signal<ApplicationSummary[]>([]);
  loading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  // Pagination
  currentPage = signal<number>(0);
  totalPages = signal<number>(0);
  totalElements = signal<number>(0);
  pageSize = signal<number>(10);

  // Filters
  searchTerm = signal<string>('');
  selectedStatus = signal<ApplicationStatus | ''>('');
  companyFilter = signal<string>('');

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

  deletingId = signal<number | null>(null);

  ngOnInit(): void {
    this.loadApplications();
  }

  loadApplications(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    const filters: ApplicationFilters = {
      search: this.searchTerm() || undefined,
      status: this.selectedStatus() || undefined,
      company: this.companyFilter() || undefined,
      page: this.currentPage(),
      size: this.pageSize(),
      sort: 'createdAt,desc',
    };

    this.applicationService.getApplications(filters).subscribe({
      next: (res) => {
        this.applications.set(res.content || []);
        this.totalPages.set(res.totalPages || 0);
        this.totalElements.set(res.totalElements || 0);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Erreur lors du chargement des candidatures.');
        this.loading.set(false);
      },
    });
  }

  onFilterChange(): void {
    this.currentPage.set(0);
    this.loadApplications();
  }

  resetFilters(): void {
    this.searchTerm.set('');
    this.selectedStatus.set('');
    this.companyFilter.set('');
    this.currentPage.set(0);
    this.loadApplications();
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage.set(page);
      this.loadApplications();
    }
  }

  confirmDelete(id: number): void {
    this.deletingId.set(id);
  }

  cancelDelete(): void {
    this.deletingId.set(null);
  }

  executeDelete(id: number): void {
    this.applicationService.deleteApplication(id).subscribe({
      next: () => {
        this.deletingId.set(null);
        this.loadApplications();
      },
      error: () => {
        alert('Erreur lors de la suppression.');
        this.deletingId.set(null);
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
