import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { OfferService } from '../../../core/services/offer.service';
import { ContractType, OfferCreateRequest, OfferStatus, OfferUpdateRequest } from '../../../core/models/offer.models';

@Component({
  selector: 'app-offer-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './offer-form.component.html',
  styleUrl: './offer-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OfferFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private offerService = inject(OfferService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  isEditMode = signal<boolean>(false);
  offerId = signal<number | null>(null);
  loading = signal<boolean>(false);
  submitting = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

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

  form: FormGroup = this.fb.group({
    title: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(200)]],
    companyName: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(150)]],
    city: ['', [Validators.maxLength(100)]],
    country: ['', [Validators.maxLength(100)]],
    contractType: ['CDI', [Validators.required]],
    technologiesInput: [''], // Comma-separated in UI, mapped to array
    description: [''],
    jobUrl: ['', [Validators.pattern('^(https?:\\/\\/.+)?$'), Validators.maxLength(500)]],
    salary: ['', [Validators.maxLength(100)]],
    source: ['', [Validators.maxLength(100)]],
    applicationDeadline: [''],
    status: ['SAVED', [Validators.required]],
  });

  // Getters for form controls
  get title() { return this.form.get('title')!; }
  get companyName() { return this.form.get('companyName')!; }
  get contractType() { return this.form.get('contractType')!; }
  get jobUrl() { return this.form.get('jobUrl')!; }
  get status() { return this.form.get('status')!; }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = parseInt(idParam, 10);
      if (!isNaN(id)) {
        this.isEditMode.set(true);
        this.offerId.set(id);
        this.loadOfferForEdit(id);
      }
    }
  }

  loadOfferForEdit(id: number): void {
    this.loading.set(true);
    this.offerService.getOfferById(id).pipe(
      finalize(() => this.loading.set(false))
    ).subscribe({
      next: (offer) => {
        this.form.patchValue({
          title: offer.title,
          companyName: offer.companyName,
          city: offer.city || '',
          country: offer.country || '',
          contractType: offer.contractType,
          technologiesInput: (offer.technologies || []).join(', '),
          description: offer.description || '',
          jobUrl: offer.jobUrl || '',
          salary: offer.salary || '',
          source: offer.source || '',
          applicationDeadline: offer.applicationDeadline || '',
          status: offer.status,
        });
      },
      error: () => {
        this.errorMessage.set('Impossible de charger cette offre.');
      },
    });
  }

  onSubmit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    const formValues = this.form.getRawValue();

    // Parse technologies from comma-separated input
    const technologies = (formValues.technologiesInput as string)
      .split(',')
      .map((t) => t.trim())
      .filter((t) => t.length > 0);

    if (this.isEditMode() && this.offerId()) {
      const updatePayload: OfferUpdateRequest = {
        title: formValues.title,
        companyName: formValues.companyName,
        city: formValues.city || undefined,
        country: formValues.country || undefined,
        contractType: formValues.contractType,
        technologies,
        description: formValues.description || undefined,
        jobUrl: formValues.jobUrl || undefined,
        salary: formValues.salary || undefined,
        source: formValues.source || undefined,
        applicationDeadline: formValues.applicationDeadline || null,
        status: formValues.status,
      };

      this.offerService.updateOffer(this.offerId()!, updatePayload).pipe(
        finalize(() => this.submitting.set(false))
      ).subscribe({
        next: (updated) => {
          this.router.navigate(['/offers', updated.id]);
        },
        error: (err) => {
          this.errorMessage.set(err?.error?.message || 'Erreur lors de la modification de l\'offre.');
        },
      });
    } else {
      const createPayload: OfferCreateRequest = {
        title: formValues.title,
        companyName: formValues.companyName,
        city: formValues.city || undefined,
        country: formValues.country || undefined,
        contractType: formValues.contractType,
        technologies,
        description: formValues.description || undefined,
        jobUrl: formValues.jobUrl || undefined,
        salary: formValues.salary || undefined,
        source: formValues.source || undefined,
        applicationDeadline: formValues.applicationDeadline || null,
        status: formValues.status,
      };

      this.offerService.createOffer(createPayload).pipe(
        finalize(() => this.submitting.set(false))
      ).subscribe({
        next: (created) => {
          this.router.navigate(['/offers', created.id]);
        },
        error: (err) => {
          this.errorMessage.set(err?.error?.message || 'Erreur lors de la création de l\'offre.');
        },
      });
    }
  }
}
