import {
  Component,
  OnInit,
  inject,
  signal,
  computed,
  ChangeDetectionStrategy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { DashboardService } from '../../core/services/dashboard.service';
import { DashboardStats } from '../../core/models/dashboard.models';
import { ApplicationStatus } from '../../core/models/application.models';
import {
  InterviewType,
  INTERVIEW_TYPE_LABELS,
} from '../../core/models/interview.models';

interface StatusBarItem {
  status: ApplicationStatus;
  label: string;
  count: number;
  percentage: number;
  color: string;
}

const STATUS_METADATA: Record<ApplicationStatus, { label: string; color: string }> = {
  TO_APPLY:   { label: 'À postuler',    color: '#f59e0b' },
  APPLIED:    { label: 'Postulé',       color: '#3b82f6' },
  SCREENING:  { label: 'Screening RH',  color: '#8b5cf6' },
  INTERVIEW:  { label: 'Entretien',     color: '#06b6d4' },
  OFFER:      { label: 'Offre reçue',   color: '#10b981' },
  ACCEPTED:   { label: 'Acceptée',      color: '#22c55e' },
  REJECTED:   { label: 'Refusée',       color: '#ef4444' },
  WITHDRAWN:  { label: 'Retirée',       color: '#6b7280' },
};

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private dashboardService = inject(DashboardService);

  readonly stats = signal<DashboardStats | null>(null);
  readonly loading = signal<boolean>(true);
  readonly errorMessage = signal<string | null>(null);

  /** User's first name for greeting */
  readonly userFirstName = computed(() => {
    return this.authService.currentUser()?.firstName || 'Candidat';
  });

  /** Computed distribution list with percentage & colors */
  readonly statusDistributionBars = computed<StatusBarItem[]>(() => {
    const s = this.stats();
    if (!s) return [];

    const total = s.totalApplications || 0;
    const statuses: ApplicationStatus[] = [
      'TO_APPLY',
      'APPLIED',
      'SCREENING',
      'INTERVIEW',
      'OFFER',
      'ACCEPTED',
      'REJECTED',
      'WITHDRAWN',
    ];

    return statuses.map((status) => {
      const count = s.applicationsByStatus?.[status] ?? 0;
      const percentage = total > 0 ? (count / total) * 100 : 0;
      const meta = STATUS_METADATA[status];

      return {
        status,
        label: meta.label,
        count,
        percentage,
        color: meta.color,
      };
    });
  });

  ngOnInit(): void {
    this.loadDashboard();
    // Refresh user profile if first name isn't set yet
    if (!this.authService.currentUser()?.firstName) {
      this.authService.fetchCurrentUser().subscribe();
    }
  }

  loadDashboard(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.dashboardService.getStats().subscribe({
      next: (data) => {
        this.stats.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set(
          'Impossible de récupérer les statistiques du tableau de bord. Veuillez vérifier votre connexion.'
        );
        this.loading.set(false);
      },
    });
  }

  // ── Helpers ─────────────────────────────────────────────────────────────────

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
    return STATUS_METADATA[status]?.label ?? status;
  }

  getTypeLabel(type: InterviewType): string {
    return INTERVIEW_TYPE_LABELS[type] ?? type;
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    try {
      const date = new Date(dateStr);
      return date.toLocaleDateString('fr-FR', {
        day: '2-digit',
        month: 'short',
      });
    } catch {
      return dateStr;
    }
  }

  formatTime(dateStr: string): string {
    if (!dateStr) return '';
    try {
      const date = new Date(dateStr);
      return date.toLocaleTimeString('fr-FR', {
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return '';
    }
  }

  getMonthAbbr(dateStr: string): string {
    if (!dateStr) return '';
    try {
      const date = new Date(dateStr);
      return date.toLocaleDateString('fr-FR', { month: 'short' });
    } catch {
      return '';
    }
  }

  getDayNumber(dateStr: string): string {
    if (!dateStr) return '';
    try {
      const date = new Date(dateStr);
      return String(date.getDate());
    } catch {
      return '';
    }
  }
}
