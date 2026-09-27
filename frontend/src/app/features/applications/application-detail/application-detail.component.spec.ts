import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ApplicationDetailComponent } from './application-detail.component';
import { ApplicationService } from '../../../core/services/application.service';
import { of } from 'rxjs';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { Application, ApplicationHistory } from '../../../core/models/application.models';

const MOCK_APP: Application = {
  id: 10,
  offer: {
    id: 1,
    title: 'Senior Engineer',
    companyName: 'Airbus',
    contractType: 'CDI',
    status: 'SAVED',
    technologies: ['Java', 'Angular'],
    createdAt: '2026-09-27T10:00:00Z',
    updatedAt: '2026-09-27T10:00:00Z',
  },
  status: 'TO_APPLY',
  createdAt: '2026-09-27T10:00:00Z',
  updatedAt: '2026-09-27T10:00:00Z',
};

const MOCK_HISTORY: ApplicationHistory[] = [
  {
    id: 1,
    applicationId: 10,
    oldStatus: null,
    newStatus: 'TO_APPLY',
    changedAt: '2026-09-27T10:00:00Z',
    note: 'Initial',
  },
];

describe('ApplicationDetailComponent', () => {
  let component: ApplicationDetailComponent;
  let fixture: ComponentFixture<ApplicationDetailComponent>;
  let applicationServiceMock: any;
  let router: Router;

  beforeEach(async () => {
    applicationServiceMock = {
      getApplicationById: vi.fn().mockReturnValue(of(MOCK_APP)),
      getApplicationHistory: vi.fn().mockReturnValue(of(MOCK_HISTORY)),
      updateStatus: vi.fn().mockReturnValue(of({ ...MOCK_APP, status: 'APPLIED' })),
      deleteApplication: vi.fn().mockReturnValue(of(undefined)),
    };

    await TestBed.configureTestingModule({
      imports: [ApplicationDetailComponent],
      providers: [
        { provide: ApplicationService, useValue: applicationServiceMock },
        provideRouter([
          { path: 'applications', component: class {} },
          { path: 'applications/:id', component: class {} },
        ]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => '10',
              },
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationDetailComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockImplementation(() => Promise.resolve(true));
    fixture.detectChanges();
  });

  it('should create ApplicationDetailComponent and load details and history', () => {
    expect(component).toBeTruthy();
    expect(applicationServiceMock.getApplicationById).toHaveBeenCalledWith(10);
    expect(applicationServiceMock.getApplicationHistory).toHaveBeenCalledWith(10);
    expect(component.application()?.offer.companyName).toBe('Airbus');
    expect(component.history().length).toBe(1);
  });

  it('should update status on status change', () => {
    component.onStatusChange('APPLIED');
    expect(applicationServiceMock.updateStatus).toHaveBeenCalledWith(10, 'APPLIED');
  });

  it('should handle deletion and navigation', () => {
    component.promptDelete();
    expect(component.showDeleteConfirm()).toBe(true);

    component.executeDelete();
    expect(applicationServiceMock.deleteApplication).toHaveBeenCalledWith(10);
    expect(router.navigate).toHaveBeenCalledWith(['/applications']);
  });
});
