import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { OfferService } from '../../../core/services/offer.service';
import { ContractType, Offer, OfferFilters, OfferStatus } from '../../../core/models/offer.models';

@Component({
  selector: 'app-offers-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './offers-list.component.html',
  styleUrl: './offers-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OffersListComponent implements OnInit {
  private offerService = inject(OfferService);

  offers = signal<Offer[]>([]);
  loading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  // Pagination
  currentPage = signal<number>(0);
  totalPages = signal<number>(0);
  totalElements = signal<number>(0);
  pageSize = signal<number>(9);

  // Filters
  searchTerm = signal<string>('');
  selectedStatus = signal<OfferStatus | ''>('');
  selectedContractType = signal<ContractType | ''>('');

  // Status options for quick update & filters
  readonly statusOptions: { value: OfferStatus; label: string }[] = [
    { value: 'SAVED', label: 'Sauvegardée' },
    { value: 'TO_APPLY', label: 'À postuler' },
    { value: 'ARCHIVED', label: 'Archivée' },
  ];

  readonly contractTypeOptions: { value: ContractType; label: string }[] = [
    { value: 'CDI', label: 'CDI' },
    { value: 'CDD', label: 'CDD' },
    { value: 'STAGE', label: 'Stage' },
    { value: 'ALTERNANCE', label: 'Alternance' },
    { value: 'FREELANCE', label: 'Freelance' },
  ];

  // Deletion confirmation
  deletingOfferId = signal<number | null>(null);

  ngOnInit(): void {
    this.loadOffers();
  }

  loadOffers(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    const filters: OfferFilters = {
      search: this.searchTerm() || undefined,
      status: this.selectedStatus() || undefined,
      contractType: this.selectedContractType() || undefined,
      page: this.currentPage(),
      size: this.pageSize(),
      sort: 'createdAt,desc',
    };

    this.offerService.getOffers(filters).subscribe({
      next: (response) => {
        this.offers.set(response.content || []);
        this.totalPages.set(response.totalPages || 0);
        this.totalElements.set(response.totalElements || 0);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Impossible de charger les offres. Veuillez réessayer.');
        this.loading.set(false);
      },
    });
  }

  onSearch(): void {
    this.currentPage.set(0);
    this.loadOffers();
  }

  onFilterChange(): void {
    this.currentPage.set(0);
    this.loadOffers();
  }

  resetFilters(): void {
    this.searchTerm.set('');
    this.selectedStatus.set('');
    this.selectedContractType.set('');
    this.currentPage.set(0);
    this.loadOffers();
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage.set(page);
      this.loadOffers();
    }
  }

  onQuickStatusChange(offer: Offer, newStatus: string): void {
    const status = newStatus as OfferStatus;
    if (offer.status === status) return;

    this.offerService.updateStatus(offer.id, status).subscribe({
      next: (updated) => {
        this.offers.update((list) =>
          list.map((o) => (o.id === updated.id ? { ...o, status: updated.status } : o))
        );
      },
      error: () => {
        alert('Erreur lors du changement de statut');
      },
    });
  }

  confirmDelete(id: number): void {
    this.deletingOfferId.set(id);
  }

  cancelDelete(): void {
    this.deletingOfferId.set(null);
  }

  executeDelete(id: number): void {
    this.offerService.deleteOffer(id).subscribe({
      next: () => {
        this.deletingOfferId.set(null);
        this.loadOffers();
      },
      error: () => {
        alert('Erreur lors de la suppression de l\'offre.');
        this.deletingOfferId.set(null);
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
