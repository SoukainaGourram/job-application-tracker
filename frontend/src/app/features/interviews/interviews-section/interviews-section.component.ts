import {
  Component,
  Input,
  OnInit,
  inject,
  signal,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { InterviewService } from '../../../core/services/interview.service';
import {
  Interview,
  InterviewCreateRequest,
  InterviewStatus,
  InterviewStatusUpdateRequest,
  InterviewType,
  InterviewUpdateRequest,
  INTERVIEW_STATUS_LABELS,
  INTERVIEW_TYPE_LABELS,
} from '../../../core/models/interview.models';

@Component({
  selector: 'app-interviews-section',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './interviews-section.component.html',
  styleUrl: './interviews-section.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InterviewsSectionComponent implements OnInit {
  @Input({ required: true }) applicationId!: number;

  private interviewService = inject(InterviewService);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  interviews = signal<Interview[]>([]);
  loading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  // Form state
  showForm = signal<boolean>(false);
  editingInterviewId = signal<number | null>(null);
  formSubmitting = signal<boolean>(false);
  formError = signal<string | null>(null);

  // Delete confirm
  deletingInterviewId = signal<number | null>(null);
  deleteError = signal<string | null>(null);

  // Status change
  statusChangingId = signal<number | null>(null);
  statusChangeError = signal<string | null>(null);

  readonly typeOptions: { value: InterviewType; label: string }[] = [
    { value: 'HR', label: INTERVIEW_TYPE_LABELS['HR'] },
    { value: 'TECHNICAL', label: INTERVIEW_TYPE_LABELS['TECHNICAL'] },
    { value: 'MANAGERIAL', label: INTERVIEW_TYPE_LABELS['MANAGERIAL'] },
    { value: 'BEHAVIORAL', label: INTERVIEW_TYPE_LABELS['BEHAVIORAL'] },
    { value: 'FINAL', label: INTERVIEW_TYPE_LABELS['FINAL'] },
    { value: 'OTHER', label: INTERVIEW_TYPE_LABELS['OTHER'] },
  ];

  readonly statusOptions: { value: InterviewStatus; label: string }[] = [
    { value: 'COMPLETED', label: INTERVIEW_STATUS_LABELS['COMPLETED'] },
    { value: 'CANCELLED', label: INTERVIEW_STATUS_LABELS['CANCELLED'] },
    { value: 'NO_SHOW', label: INTERVIEW_STATUS_LABELS['NO_SHOW'] },
  ];

  form: FormGroup = this.fb.group({
    type: ['', Validators.required],
    scheduledAt: ['', Validators.required],
    endedAt: [''],
    location: [''],
    interviewerName: [''],
    notes: [''],
    feedback: [''],
  });

  ngOnInit(): void {
    this.loadInterviews();
  }

  loadInterviews(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.interviewService.getInterviews(this.applicationId).subscribe({
      next: (list) => {
        this.interviews.set(list);
        this.loading.set(false);
        this.cdr.markForCheck();
      },
      error: () => {
        this.errorMessage.set('Impossible de charger les entretiens.');
        this.loading.set(false);
        this.cdr.markForCheck();
      },
    });
  }

  openCreateForm(): void {
    this.editingInterviewId.set(null);
    this.form.reset();
    this.formError.set(null);
    this.showForm.set(true);
  }

  openEditForm(interview: Interview): void {
    this.editingInterviewId.set(interview.id);
    this.formError.set(null);
    this.form.patchValue({
      type: interview.type,
      scheduledAt: this.toDatetimeLocal(interview.scheduledAt),
      endedAt: interview.endedAt ? this.toDatetimeLocal(interview.endedAt) : '',
      location: interview.location ?? '',
      interviewerName: interview.interviewerName ?? '',
      notes: interview.notes ?? '',
      feedback: interview.feedback ?? '',
    });
    this.showForm.set(true);
  }

  closeForm(): void {
    this.showForm.set(false);
    this.editingInterviewId.set(null);
    this.form.reset();
    this.formError.set(null);
  }

  onSubmit(): void {
    if (this.form.invalid || this.formSubmitting()) return;

    this.formSubmitting.set(true);
    this.formError.set(null);

    const raw = this.form.getRawValue();
    const editId = this.editingInterviewId();

    if (editId) {
      const request: InterviewUpdateRequest = {
        type: raw.type || undefined,
        scheduledAt: raw.scheduledAt ? new Date(raw.scheduledAt).toISOString() : undefined,
        endedAt: raw.endedAt ? new Date(raw.endedAt).toISOString() : null,
        location: raw.location || null,
        interviewerName: raw.interviewerName || null,
        notes: raw.notes || null,
        feedback: raw.feedback || null,
      };
      this.interviewService.updateInterview(this.applicationId, editId, request).subscribe({
        next: (updated) => {
          this.interviews.update((list) => list.map((i) => (i.id === editId ? updated : i)));
          this.formSubmitting.set(false);
          this.closeForm();
          this.cdr.markForCheck();
        },
        error: () => {
          this.formError.set('Erreur lors de la modification.');
          this.formSubmitting.set(false);
          this.cdr.markForCheck();
        },
      });
    } else {
      const request: InterviewCreateRequest = {
        type: raw.type as InterviewType,
        scheduledAt: new Date(raw.scheduledAt).toISOString(),
        endedAt: raw.endedAt ? new Date(raw.endedAt).toISOString() : null,
        location: raw.location || null,
        interviewerName: raw.interviewerName || null,
        notes: raw.notes || null,
      };
      this.interviewService.createInterview(this.applicationId, request).subscribe({
        next: (created) => {
          this.interviews.update((list) => [...list, created]);
          this.formSubmitting.set(false);
          this.closeForm();
          this.cdr.markForCheck();
        },
        error: () => {
          this.formError.set('Erreur lors de la création.');
          this.formSubmitting.set(false);
          this.cdr.markForCheck();
        },
      });
    }
  }

  promptDelete(id: number): void {
    this.deleteError.set(null);
    this.deletingInterviewId.set(id);
  }

  cancelDelete(): void {
    this.deletingInterviewId.set(null);
    this.deleteError.set(null);
  }

  confirmDelete(id: number): void {
    this.deleteError.set(null);
    this.interviewService.deleteInterview(this.applicationId, id).subscribe({
      next: () => {
        this.interviews.update((list) => list.filter((i) => i.id !== id));
        this.deletingInterviewId.set(null);
        this.cdr.markForCheck();
      },
      error: () => {
        this.deleteError.set('Impossible de supprimer cet entretien.');
        this.deletingInterviewId.set(null);
        this.cdr.markForCheck();
      },
    });
  }

  changeStatus(interview: Interview, newStatus: InterviewStatus): void {
    if (interview.status !== 'SCHEDULED') return;

    this.statusChangeError.set(null);
    this.statusChangingId.set(interview.id);
    const request: InterviewStatusUpdateRequest = { status: newStatus };
    this.interviewService.updateStatus(this.applicationId, interview.id, request).subscribe({
      next: (updated) => {
        this.interviews.update((list) => list.map((i) => (i.id === interview.id ? updated : i)));
        this.statusChangingId.set(null);
        this.cdr.markForCheck();
      },
      error: () => {
        this.statusChangeError.set('Impossible de mettre à jour le statut.');
        this.statusChangingId.set(null);
        this.cdr.markForCheck();
      },
    });
  }

  isEditing(): boolean {
    return this.editingInterviewId() !== null;
  }

  getTypeLabel(type: InterviewType): string {
    return INTERVIEW_TYPE_LABELS[type] ?? type;
  }

  getStatusLabel(status: InterviewStatus): string {
    return INTERVIEW_STATUS_LABELS[status] ?? status;
  }

  getStatusClass(status: InterviewStatus): string {
    switch (status) {
      case 'SCHEDULED': return 'badge-scheduled';
      case 'COMPLETED': return 'badge-completed';
      case 'CANCELLED': return 'badge-cancelled';
      case 'NO_SHOW': return 'badge-no-show';
      default: return '';
    }
  }

  getTypeClass(type: InterviewType): string {
    switch (type) {
      case 'HR': return 'type-hr';
      case 'TECHNICAL': return 'type-technical';
      case 'MANAGERIAL': return 'type-managerial';
      case 'BEHAVIORAL': return 'type-behavioral';
      case 'FINAL': return 'type-final';
      default: return 'type-other';
    }
  }

  /** Convert ISO 8601 Instant string to datetime-local string value */
  private toDatetimeLocal(isoString: string): string {
    const d = new Date(isoString);
    const pad = (n: number) => n.toString().padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }
}
