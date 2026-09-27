import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PipelineComponent } from './pipeline.component';
import { ApplicationService } from '../../core/services/application.service';
import { of, throwError } from 'rxjs';
import { provideRouter } from '@angular/router';
import { ApplicationSummary } from '../../core/models/application.models';

const MOCK_KANBAN_APPS: ApplicationSummary[] = [
  {
    id: 1,
    offerId: 10,
    offerTitle: 'Fullstack Dev',
    companyName: 'Airbus',
    contractType: 'CDI',
    status: 'TO_APPLY',
    createdAt: '2026-09-27T10:00:00Z',
  },
  {
    id: 2,
    offerId: 11,
    offerTitle: 'Frontend Dev',
    companyName: 'Thales',
    contractType: 'CDI',
    status: 'INTERVIEW',
    createdAt: '2026-09-27T10:00:00Z',
  },
];

describe('PipelineComponent', () => {
  let component: PipelineComponent;
  let fixture: ComponentFixture<PipelineComponent>;
  let applicationServiceMock: any;

  beforeEach(async () => {
    applicationServiceMock = {
      getKanbanApplications: vi.fn().mockImplementation(() =>
        of(MOCK_KANBAN_APPS.map((item) => ({ ...item })))
      ),
      updateStatus: vi.fn().mockReturnValue(of(undefined)),
    };

    await TestBed.configureTestingModule({
      imports: [PipelineComponent],
      providers: [
        { provide: ApplicationService, useValue: applicationServiceMock },
        provideRouter([
          { path: 'pipeline', component: class {} },
          { path: 'applications/:id', component: class {} },
        ]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PipelineComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create PipelineComponent and distribute cards into columns', () => {
    expect(component).toBeTruthy();
    expect(applicationServiceMock.getKanbanApplications).toHaveBeenCalled();

    const toApplyCol = component.columns().find((c) => c.id === 'TO_APPLY');
    const interviewCol = component.columns().find((c) => c.id === 'INTERVIEW');

    expect(toApplyCol?.items.length).toBe(1);
    expect(interviewCol?.items.length).toBe(1);
  });

  it('should handle drop and update status', () => {
    const toApplyCol = component.columns().find((c) => c.id === 'TO_APPLY')!;
    const appliedCol = component.columns().find((c) => c.id === 'APPLIED')!;

    const event: any = {
      previousContainer: { data: toApplyCol.items },
      container: { data: appliedCol.items },
      previousIndex: 0,
      currentIndex: 0,
    };

    component.onDrop(event, appliedCol);

    expect(applicationServiceMock.updateStatus).toHaveBeenCalledWith(1, 'APPLIED');
    expect(appliedCol.items.length).toBe(1);
    expect(appliedCol.items[0].status).toBe('APPLIED');
  });

  it('should rollback card on status update failure', () => {
    applicationServiceMock.updateStatus.mockReturnValue(throwError(() => new Error('API Error')));
    vi.spyOn(window, 'alert').mockImplementation(() => {});

    const toApplyCol = component.columns().find((c) => c.id === 'TO_APPLY')!;
    const appliedCol = component.columns().find((c) => c.id === 'APPLIED')!;

    const event: any = {
      previousContainer: { data: toApplyCol.items },
      container: { data: appliedCol.items },
      previousIndex: 0,
      currentIndex: 0,
    };

    component.onDrop(event, appliedCol);

    // After failure, card should be rolled back to previous container with previous status
    expect(window.alert).toHaveBeenCalled();
    expect(toApplyCol.items.length).toBe(1);
    expect(toApplyCol.items[0].status).toBe('TO_APPLY');
  });
});
