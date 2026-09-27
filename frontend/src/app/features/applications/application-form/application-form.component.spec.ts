import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ApplicationFormComponent } from './application-form.component';
import { ApplicationService } from '../../../core/services/application.service';
import { of } from 'rxjs';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { Application } from '../../../core/models/application.models';

const MOCK_APP: Application = {
  id: 10,
  offer: {
    id: 1,
    title: 'Lead Engineer',
    companyName: 'Airbus',
    contractType: 'CDI',
    status: 'SAVED',
    technologies: ['Java', 'Angular'],
    createdAt: '2026-09-27T10:00:00Z',
    updatedAt: '2026-09-27T10:00:00Z',
  },
  status: 'TO_APPLY',
  notes: 'My note',
  createdAt: '2026-09-27T10:00:00Z',
  updatedAt: '2026-09-27T10:00:00Z',
};

describe('ApplicationFormComponent', () => {
  let component: ApplicationFormComponent;
  let fixture: ComponentFixture<ApplicationFormComponent>;
  let applicationServiceMock: any;
  let router: Router;

  beforeEach(async () => {
    applicationServiceMock = {
      getApplicationById: vi.fn().mockReturnValue(of(MOCK_APP)),
      updateApplication: vi.fn().mockReturnValue(of(MOCK_APP)),
    };

    await TestBed.configureTestingModule({
      imports: [ApplicationFormComponent],
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

    fixture = TestBed.createComponent(ApplicationFormComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockImplementation(() => Promise.resolve(true));
    fixture.detectChanges();
  });

  it('should create ApplicationFormComponent and load form values', () => {
    expect(component).toBeTruthy();
    expect(applicationServiceMock.getApplicationById).toHaveBeenCalledWith(10);
    expect(component.form.get('notes')?.value).toBe('My note');
  });

  it('onSubmit should call updateApplication and navigate', () => {
    component.form.patchValue({ notes: 'Updated notes' });
    component.onSubmit();

    expect(applicationServiceMock.updateApplication).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/applications', 10]);
  });
});
