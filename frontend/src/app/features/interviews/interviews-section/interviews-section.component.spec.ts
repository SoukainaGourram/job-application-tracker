import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { InterviewsSectionComponent } from './interviews-section.component';
import { Interview } from '../../../core/models/interview.models';

const MOCK_LIST: Interview[] = [
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

describe('InterviewsSectionComponent', () => {
  let fixture: ComponentFixture<InterviewsSectionComponent>;
  let component: InterviewsSectionComponent;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InterviewsSectionComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(InterviewsSectionComponent);
    component = fixture.componentInstance;
    component.applicationId = 10;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();

    const req = httpMock.expectOne('/api/applications/10/interviews');
    req.flush(MOCK_LIST);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should load interviews on init', () => {
    expect(component.interviews().length).toBe(1);
    expect(component.loading()).toBe(false);
  });

  it('should open create form', () => {
    component.openCreateForm();
    expect(component.showForm()).toBe(true);
    expect(component.editingInterviewId()).toBeNull();
  });

  it('should submit create request', () => {
    component.openCreateForm();
    component.form.patchValue({
      type: 'HR',
      scheduledAt: '2026-10-20T11:00',
    });
    component.onSubmit();

    const req = httpMock.expectOne('/api/applications/10/interviews');
    expect(req.request.method).toBe('POST');
    req.flush({
      ...MOCK_LIST[0],
      id: 2,
      type: 'HR',
    });
    fixture.detectChanges();

    expect(component.showForm()).toBe(false);
    expect(component.interviews().length).toBe(2);
  });
});
