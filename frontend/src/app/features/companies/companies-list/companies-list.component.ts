import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CompanyService } from '../../../core/services/company.service';
import { CompanyFilters, CompanySummary } from '../../../core/models/company.models';

@Component({
  selector: 'app-companies-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './companies-list.component.html',
  styleUrl: './companies-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CompaniesListComponent implements OnInit {
  private companyService = inject(CompanyService);

  companies = signal<CompanySummary[]>([]);
  loading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  // Pagination
  currentPage = signal<number>(0);
  totalPages = signal<number>(0);
  totalElements = signal<number>(0);
  pageSize = signal<number>(9);

  // Filters
  searchTerm = signal<string>('');
  selectedIndustry = signal<string>('');
  selectedSize = signal<string>('');

  readonly sizeOptions: string[] = [
    'Startup',
    'PME',
    'ETI',
    'Grand Groupe',
  ];

  // Deletion confirmation
  deletingCompanyId = signal<number | null>(null);

  ngOnInit(): void {
    this.loadCompanies();
  }

  loadCompanies(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    const filters: CompanyFilters = {
      page: this.currentPage(),
      pageSize: this.pageSize(),
      search: this.searchTerm().trim() || undefined,
      industry: this.selectedIndustry() || undefined,
      size: this.selectedSize() || undefined,
    };

    this.companyService.getCompanies(filters).subscribe({
      next: (res) => {
        this.companies.set(res.content);
        this.totalPages.set(res.totalPages);
        this.totalElements.set(res.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Impossible de charger les entreprises. Veuillez réessayer.');
        this.loading.set(false);
      },
    });
  }

  onSearch(): void {
    this.currentPage.set(0);
    this.loadCompanies();
  }

  onFilterChange(): void {
    this.currentPage.set(0);
    this.loadCompanies();
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage.set(page);
      this.loadCompanies();
    }
  }

  confirmDelete(id: number, event: Event): void {
    event.stopPropagation();
    event.preventDefault();
    this.deletingCompanyId.set(id);
  }

  cancelDelete(): void {
    this.deletingCompanyId.set(null);
  }

  executeDelete(id: number): void {
    this.companyService.deleteCompany(id).subscribe({
      next: () => {
        this.deletingCompanyId.set(null);
        this.loadCompanies();
      },
      error: () => {
        this.errorMessage.set('Erreur lors de la suppression de l\'entreprise.');
        this.deletingCompanyId.set(null);
      },
    });
  }
}
