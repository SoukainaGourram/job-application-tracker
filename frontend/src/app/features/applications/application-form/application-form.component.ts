import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ApplicationService } from '../../../core/services/application.service';
import { Application, ApplicationUpdateRequest } from '../../../core/models/application.models';

@Component({
  selector: 'app-application-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './application-form.component.html',
  styleUrl: './application-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private applicationService = inject(ApplicationService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  application = signal<Application | null>(null);
  loading = signal<boolean>(true);
  submitting = signal<boolean>(false);
  errorMessage = signal<string | null>(null);
  appId = signal<number | null>(null);

  form: FormGroup = this.fb.group({
    appliedAt: [''],
    cvVersion: ['', [Validators.maxLength(100)]],
    source: ['', [Validators.maxLength(100)]],
    notes: [''],
    coverLetter: [''],
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = parseInt(idParam, 10);
      if (!isNaN(id)) {
        this.appId.set(id);
        this.loadApplication(id);
      } else {
        this.errorMessage.set('Identifiant invalide.');
        this.loading.set(false);
      }
    }
  }

  loadApplication(id: number): void {
    this.loading.set(true);
    this.applicationService.getApplicationById(id).pipe(
      finalize(() => this.loading.set(false))
    ).subscribe({
      next: (app) => {
        this.application.set(app);
        this.form.patchValue({
          appliedAt: app.appliedAt || '',
          cvVersion: app.cvVersion || '',
          source: app.source || app.offer?.source || '',
          notes: app.notes || '',
          coverLetter: app.coverLetter || '',
        });
      },
      error: () => {
        this.errorMessage.set('Impossible de charger la candidature.');
      },
    });
  }

  onSubmit(): void {
    if (this.form.invalid || this.submitting() || !this.appId()) {
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    const formValues = this.form.getRawValue();
    const payload: ApplicationUpdateRequest = {
      appliedAt: formValues.appliedAt || null,
      cvVersion: formValues.cvVersion || null,
      source: formValues.source || null,
      notes: formValues.notes || null,
      coverLetter: formValues.coverLetter || null,
    };

    this.applicationService.updateApplication(this.appId()!, payload).pipe(
      finalize(() => this.submitting.set(false))
    ).subscribe({
      next: (updated) => {
        this.router.navigate(['/applications', updated.id]);
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message || 'Erreur lors de la mise à jour.');
      },
    });
  }
}
