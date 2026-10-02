import { TestBed } from '@angular/core/testing';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { InterviewService } from './interview.service';
import { Interview, InterviewCreateRequest } from '../models/interview.models';

const MOCK_INTERVIEW: Interview = {
  id: 1,
  applicationId: 10,
  type: 'TECHNICAL',
  status: 'SCHEDULED',
  scheduledAt: '2026-10-15T14:00:00Z',
  location: 'Visio',
  createdAt: '2026-10-01T10:00:00Z',
  updatedAt: '2026-10-01T10:00:00Z',
};

describe('InterviewService', () => {
  let service: InterviewService;
  let httpMock: HttpTestingController;
  const base = '/api/applications/10/interviews';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        InterviewService,
      ],
    });

    service = TestBed.inject(InterviewService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getInterviews() should GET list', () => {
    service.getInterviews(10).subscribe((list) => {
      expect(list.length).toBe(1);
      expect(list[0].type).toBe('TECHNICAL');
    });

    const req = httpMock.expectOne(base);
    expect(req.request.method).toBe('GET');
    req.flush([MOCK_INTERVIEW]);
  });

  it('getInterview() should GET by id', () => {
    service.getInterview(10, 1).subscribe((interview) => {
      expect(interview.id).toBe(1);
    });

    const req = httpMock.expectOne(`${base}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(MOCK_INTERVIEW);
  });

  it('createInterview() should POST body', () => {
    const body: InterviewCreateRequest = {
      type: 'HR',
      scheduledAt: '2026-10-20T09:00:00Z',
    };

    service.createInterview(10, body).subscribe((created) => {
      expect(created.status).toBe('SCHEDULED');
    });

    const req = httpMock.expectOne(base);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({ ...MOCK_INTERVIEW, type: 'HR' });
  });

  it('updateInterview() should PUT', () => {
    service.updateInterview(10, 1, { location: 'Paris' }).subscribe((updated) => {
      expect(updated.location).toBe('Paris');
    });

    const req = httpMock.expectOne(`${base}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ location: 'Paris' });
    req.flush({ ...MOCK_INTERVIEW, location: 'Paris' });
  });

  it('updateStatus() should PATCH /status', () => {
    service.updateStatus(10, 1, { status: 'COMPLETED' }).subscribe((updated) => {
      expect(updated.status).toBe('COMPLETED');
    });

    const req = httpMock.expectOne(`${base}/1/status`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ status: 'COMPLETED' });
    req.flush({ ...MOCK_INTERVIEW, status: 'COMPLETED' });
  });

  it('deleteInterview() should DELETE', () => {
    service.deleteInterview(10, 1).subscribe();

    const req = httpMock.expectOne(`${base}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
