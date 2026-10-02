import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { InterviewsPageComponent } from './interviews-page.component';
import { ApplicationService } from '../../../core/services/application.service';
import { InterviewService } from '../../../core/services/interview.service';
import { ApplicationSummary } from '../../../core/models/application.models';
import { Interview } from '../../../core/models/interview.models';

const MOCK_APPS: ApplicationSummary[] = [
  {
    id: 10,
    offerId: 1,
    offerTitle: 'Dev Java',
    companyName: 'Airbus',
    contractType: 'CDI',
    status: 'INTERVIEW',
    createdAt: '2026-09-01T10:00:00Z',
  },
  {
    id: 11,
    offerId: 2,
    offerTitle: 'Frontend',
    companyName: 'Capgemini',
    contractType: 'CDI',
    status: 'TO_APPLY',
    createdAt: '2026-09-01T10:00:00Z',
  },
];

const MOCK_INTERVIEWS: Interview[] = [
  {
    id: 1,
    applicationId: 10,
    type: 'TECHNICAL',
    status: 'SCHEDULED',
    scheduledAt: '2026-10-15T14:00:00Z',
    createdAt: '2026-10-01T10:00:00Z',
    updatedAt: '2026-10-01T10:00:00Z',
  },
];

describe('InterviewsPageComponent', () => {
  let fixture: ComponentFixture<InterviewsPageComponent>;
  let component: InterviewsPageComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InterviewsPageComponent],
      providers: [
        provideRouter([]),
        {
          provide: ApplicationService,
          useValue: {
            getKanbanApplications: vi.fn().mockReturnValue(of(MOCK_APPS)),
          },
        },
        {
          provide: InterviewService,
          useValue: {
            getInterviews: vi.fn().mockReturnValue(of(MOCK_INTERVIEWS)),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(InterviewsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should aggregate interviews from relevant applications only', () => {
    expect(component.interviews().length).toBe(1);
    expect(component.interviews()[0].offerTitle).toBe('Dev Java');
    expect(component.loading()).toBe(false);
  });

  it('should filter by interview status', () => {
    component.statusFilter.set('COMPLETED');
    expect(component.filteredInterviews().length).toBe(0);

    component.statusFilter.set('SCHEDULED');
    expect(component.filteredInterviews().length).toBe(1);
  });
});
