import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ApplicationService } from '../../../core/services/application.service';
import { InterviewService } from '../../../core/services/interview.service';
import { ApplicationStatus } from '../../../core/models/application.models';
import {
  InterviewStatus,
  InterviewWithApplicationContext,
  INTERVIEW_STATUS_LABELS,
  INTERVIEW_TYPE_LABELS,
  InterviewType,
} from '../../../core/models/interview.models';

const RELEVANT_APPLICATION_STATUSES: ApplicationStatus[] = ['SCREENING', 'INTERVIEW', 'OFFER'];

@Component({
  selector: 'app-interviews-page',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './interviews-page.component.html',
  styleUrl: './interviews-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InterviewsPageComponent implements OnInit {
  private applicationService = inject(ApplicationService);
  private interviewService = inject(InterviewService);

  interviews = signal<InterviewWithApplicationContext[]>([]);
  loading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  /** Filtre statut entretien : vide = tous */
  statusFilter = signal<InterviewStatus | ''>('');

  readonly statusFilterOptions: { value: InterviewStatus | ''; label: string }[] = [
    { value: '', label: 'Tous les statuts' },
    { value: 'SCHEDULED', label: INTERVIEW_STATUS_LABELS.SCHEDULED },
    { value: 'COMPLETED', label: INTERVIEW_STATUS_LABELS.COMPLETED },
    { value: 'CANCELLED', label: INTERVIEW_STATUS_LABELS.CANCELLED },
    { value: 'NO_SHOW', label: INTERVIEW_STATUS_LABELS.NO_SHOW },
  ];

  ngOnInit(): void {
    this.loadInterviews();
  }

  loadInterviews(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.applicationService.getKanbanApplications().subscribe({
      next: (applications) => {
        const relevant = applications.filter((app) =>
          RELEVANT_APPLICATION_STATUSES.includes(app.status)
        );

        if (relevant.length === 0) {
          this.interviews.set([]);
          this.loading.set(false);
          return;
        }

        forkJoin(
          relevant.map((app) =>
            this.interviewService.getInterviews(app.id).pipe(
              map((list) =>
                list.map(
                  (interview): InterviewWithApplicationContext => ({
                    ...interview,
                    offerTitle: app.offerTitle,
                    companyName: app.companyName,
                  })
                )
              ),
              catchError(() => of([] as InterviewWithApplicationContext[]))
            )
          )
        ).subscribe({
          next: (groups) => {
            const merged = groups
              .flat()
              .sort(
                (a, b) =>
                  new Date(a.scheduledAt).getTime() - new Date(b.scheduledAt).getTime()
              );
            this.interviews.set(merged);
            this.loading.set(false);
          },
          error: () => {
            this.errorMessage.set('Impossible de charger les entretiens.');
            this.loading.set(false);
          },
        });
      },
      error: () => {
        this.errorMessage.set('Impossible de charger les candidatures.');
        this.loading.set(false);
      },
    });
  }

  filteredInterviews(): InterviewWithApplicationContext[] {
    const filter = this.statusFilter();
    const all = this.interviews();
    if (!filter) return all;
    return all.filter((i) => i.status === filter);
  }

  getTypeLabel(type: InterviewType): string {
    return INTERVIEW_TYPE_LABELS[type] ?? type;
  }

  getStatusLabel(status: InterviewStatus): string {
    return INTERVIEW_STATUS_LABELS[status] ?? status;
  }

  getStatusClass(status: InterviewStatus): string {
    switch (status) {
      case 'SCHEDULED':
        return 'badge-scheduled';
      case 'COMPLETED':
        return 'badge-completed';
      case 'CANCELLED':
        return 'badge-cancelled';
      case 'NO_SHOW':
        return 'badge-no-show';
      default:
        return '';
    }
  }
}
