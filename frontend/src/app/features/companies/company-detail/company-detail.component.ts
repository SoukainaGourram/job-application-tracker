import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CompanyService } from '../../../core/services/company.service';
import { Company, CompanyContact, CompanyContactRequest } from '../../../core/models/company.models';

@Component({
  selector: 'app-company-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './company-detail.component.html',
  styleUrl: './company-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CompanyDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private companyService = inject(CompanyService);
  private fb = inject(FormBuilder);

  companyId = signal<number>(0);
  company = signal<Company | null>(null);
  loading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  // Deletion confirmation for company
  showDeleteModal = signal<boolean>(false);

  // Contact Modal State
  showContactModal = signal<boolean>(false);
  editingContactId = signal<number | null>(null);
  contactFormSubmitting = signal<boolean>(false);
  contactErrorMessage = signal<string | null>(null);

  contactForm: FormGroup = this.fb.group({
    firstName: ['', [Validators.required, Validators.maxLength(100)]],
    lastName: ['', [Validators.required, Validators.maxLength(100)]],
    jobTitle: ['', [Validators.maxLength(150)]],
    email: ['', [Validators.email, Validators.maxLength(255)]],
    phone: ['', [Validators.maxLength(50)]],
    linkedinUrl: ['', [Validators.maxLength(500)]],
    notes: [''],
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id || isNaN(id)) {
      this.router.navigate(['/companies']);
      return;
    }

    this.companyId.set(id);
    this.loadCompany();
  }

  loadCompany(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.companyService.getCompanyById(this.companyId()).subscribe({
      next: (data) => {
        this.company.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Impossible de charger les détails de l\'entreprise.');
        this.loading.set(false);
      },
    });
  }

  openAddContactModal(): void {
    this.editingContactId.set(null);
    this.contactErrorMessage.set(null);
    this.contactForm.reset();
    this.showContactModal.set(true);
  }

  openEditContactModal(contact: CompanyContact): void {
    this.editingContactId.set(contact.id);
    this.contactErrorMessage.set(null);
    this.contactForm.patchValue({
      firstName: contact.firstName,
      lastName: contact.lastName,
      jobTitle: contact.jobTitle || '',
      email: contact.email || '',
      phone: contact.phone || '',
      linkedinUrl: contact.linkedinUrl || '',
      notes: contact.notes || '',
    });
    this.showContactModal.set(true);
  }

  closeContactModal(): void {
    this.showContactModal.set(false);
    this.editingContactId.set(null);
    this.contactForm.reset();
  }

  saveContact(): void {
    if (this.contactForm.invalid || this.contactFormSubmitting()) {
      this.contactForm.markAllAsTouched();
      return;
    }

    this.contactFormSubmitting.set(true);
    this.contactErrorMessage.set(null);

    const formVal = this.contactForm.value;
    const request: CompanyContactRequest = {
      firstName: formVal.firstName.trim(),
      lastName: formVal.lastName.trim(),
      jobTitle: formVal.jobTitle?.trim() || null,
      email: formVal.email?.trim() || null,
      phone: formVal.phone?.trim() || null,
      linkedinUrl: formVal.linkedinUrl?.trim() || null,
      notes: formVal.notes?.trim() || null,
    };

    const editId = this.editingContactId();

    if (editId) {
      this.companyService.updateContact(this.companyId(), editId, request).subscribe({
        next: () => {
          this.contactFormSubmitting.set(false);
          this.closeContactModal();
          this.loadCompany();
        },
        error: () => {
          this.contactErrorMessage.set('Erreur lors de la modification du contact.');
          this.contactFormSubmitting.set(false);
        },
      });
    } else {
      this.companyService.addContact(this.companyId(), request).subscribe({
        next: () => {
          this.contactFormSubmitting.set(false);
          this.closeContactModal();
          this.loadCompany();
        },
        error: () => {
          this.contactErrorMessage.set('Erreur lors de l\'ajout du contact.');
          this.contactFormSubmitting.set(false);
        },
      });
    }
  }

  deleteContact(contactId: number): void {
    if (!confirm('Voulez-vous vraiment supprimer ce contact ?')) {
      return;
    }

    this.companyService.deleteContact(this.companyId(), contactId).subscribe({
      next: () => {
        this.loadCompany();
      },
      error: () => {
        alert('Impossible de supprimer ce contact.');
      },
    });
  }

  deleteCompany(): void {
    this.companyService.deleteCompany(this.companyId()).subscribe({
      next: () => {
        this.router.navigate(['/companies']);
      },
      error: () => {
        this.errorMessage.set('Erreur lors de la suppression de l\'entreprise.');
        this.showDeleteModal.set(false);
      },
    });
  }
}
