import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import {
  CdkDragDrop,
  DragDropModule,
  moveItemInArray,
  transferArrayItem,
} from '@angular/cdk/drag-drop';
import { ApplicationService } from '../../core/services/application.service';
import { ApplicationStatus, ApplicationSummary } from '../../core/models/application.models';

export interface KanbanColumn {
  id: ApplicationStatus;
  title: string;
  colorClass: string;
  items: ApplicationSummary[];
}

@Component({
  selector: 'app-pipeline',
  standalone: true,
  imports: [CommonModule, RouterLink, DragDropModule],
  templateUrl: './pipeline.component.html',
  styleUrl: './pipeline.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PipelineComponent implements OnInit {
  private applicationService = inject(ApplicationService);

  loading = signal<boolean>(true);
  errorMessage = signal<string | null>(null);

  columns = signal<KanbanColumn[]>([
    { id: 'TO_APPLY',   title: 'À postuler',    colorClass: 'col-to-apply',   items: [] },
    { id: 'APPLIED',    title: 'Postulé',       colorClass: 'col-applied',    items: [] },
    { id: 'SCREENING',  title: 'Screening RH',  colorClass: 'col-screening',  items: [] },
    { id: 'INTERVIEW',  title: 'Entretien',     colorClass: 'col-interview',  items: [] },
    { id: 'OFFER',      title: 'Offre reçue',   colorClass: 'col-offer',      items: [] },
    { id: 'ACCEPTED',   title: 'Acceptée',      colorClass: 'col-accepted',   items: [] },
    { id: 'REJECTED',   title: 'Refusée',       colorClass: 'col-rejected',   items: [] },
    { id: 'WITHDRAWN',  title: 'Retirée',       colorClass: 'col-withdrawn',  items: [] },
  ]);

  get connectedDropLists(): string[] {
    return this.columns().map((col) => 'drop-list-' + col.id);
  }

  ngOnInit(): void {
    this.loadKanbanData();
  }

  loadKanbanData(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.applicationService.getKanbanApplications().subscribe({
      next: (apps) => {
        this.populateColumns(apps);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Impossible de charger le pipeline.');
        this.loading.set(false);
      },
    });
  }

  populateColumns(apps: ApplicationSummary[]): void {
    this.columns.update((cols) =>
      cols.map((col) => ({
        ...col,
        items: apps.filter((app) => app.status === col.id),
      }))
    );
  }

  onDrop(event: CdkDragDrop<ApplicationSummary[]>, targetColumn: KanbanColumn): void {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      const item = event.previousContainer.data[event.previousIndex];
      if (!item) return;
      const previousStatus = item.status;
      const targetStatus = targetColumn.id;

      // Optimistic transfer
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );
      item.status = targetStatus;

      // Backend sync
      this.applicationService.updateStatus(item.id, targetStatus).subscribe({
        next: () => {
          // Success: status confirmed by server
        },
        error: () => {
          // Rollback on failure!
          alert('Échec de la mise à jour du statut. Rétablissement de la carte...');
          transferArrayItem(
            event.container.data,
            event.previousContainer.data,
            event.currentIndex,
            event.previousIndex
          );
          item.status = previousStatus;
          this.columns.set([...this.columns()]);
        },
      });
    }
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
}
