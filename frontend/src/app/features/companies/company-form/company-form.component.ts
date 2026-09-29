import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CompanyService } from '../../../core/services/company.service';
import { CompanyCreateRequest, CompanyUpdateRequest } from '../../../core/models/company.models';

@Component({
  selector: 'app-company-form',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './company-form.component.html',
  styleUrl: './company-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CompanyFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private companyService = inject(CompanyService);

  isEditMode = signal<boolean>(false);
  companyId = signal<number | null>(null);
  submitting = signal<boolean>(false);
  loading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  readonly sizeOptions: string[] = [
    'Startup',
    'PME',
    'ETI',
    'Grand Groupe',
  ];

  companyForm: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(150)]],
    website: ['', [Validators.maxLength(255), Validators.pattern(/^(https?:\/\/.+)?$/)]],
    industry: ['', [Validators.maxLength(100)]],
    location: ['', [Validators.maxLength(150)]],
    size: [''],
    description: [''],
    notes: [''],
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      if (!isNaN(id)) {
        this.isEditMode.set(true);
        this.companyId.set(id);
        this.loadCompany(id);
      }
    }
  }

  loadCompany(id: number): void {
    this.loading.set(true);
    this.companyService.getCompanyById(id).subscribe({
      next: (comp) => {
        this.companyForm.patchValue({
          name: comp.name,
          website: comp.website || '',
          industry: comp.industry || '',
          location: comp.location || '',
          size: comp.size || '',
          description: comp.description || '',
          notes: comp.notes || '',
        });
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Impossible de charger les données de l\'entreprise.');
        this.loading.set(false);
      },
    });
  }

  onSubmit(): void {
    if (this.companyForm.invalid || this.submitting()) {
      this.companyForm.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    const val = this.companyForm.value;
    const payload: CompanyCreateRequest = {
      name: val.name.trim(),
      website: val.website?.trim() || null,
      industry: val.industry?.trim() || null,
      location: val.location?.trim() || null,
      size: val.size || null,
      description: val.description?.trim() || null,
      notes: val.notes?.trim() || null,
    };

    if (this.isEditMode() && this.companyId()) {
      const updatePayload: CompanyUpdateRequest = { ...payload };
      this.companyService.updateCompany(this.companyId()!, updatePayload).subscribe({
        next: (res) => {
          this.submitting.set(false);
          this.router.navigate(['/companies', res.id]);
        },
        error: () => {
          this.errorMessage.set('Erreur lors de la mise à jour de l\'entreprise.');
          this.submitting.set(false);
        },
      });
    } else {
      this.companyService.createCompany(payload).subscribe({
        next: (res) => {
          this.submitting.set(false);
          this.router.navigate(['/companies', res.id]);
        },
        error: () => {
          this.errorMessage.set('Erreur lors de la création de l\'entreprise.');
          this.submitting.set(false);
        },
      });
    }
  }
}
