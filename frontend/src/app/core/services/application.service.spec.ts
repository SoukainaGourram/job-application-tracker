import { TestBed } from '@angular/core/testing';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ApplicationService } from './application.service';
import {
  Application,
  ApplicationCreateRequest,
  ApplicationHistory,
  ApplicationSummary,
} from '../models/application.models';
import { PagedResponse } from '../models/offer.models';

const MOCK_APP: Application = {
  id: 10,
  offer: {
    id: 1,
    title: 'Fullstack Dev',
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

const MOCK_SUMMARY: ApplicationSummary = {
  id: 10,
  offerId: 1,
  offerTitle: 'Fullstack Dev',
  companyName: 'Airbus',
  contractType: 'CDI',
  status: 'TO_APPLY',
  createdAt: '2026-09-27T10:00:00Z',
};

const MOCK_PAGE: PagedResponse<ApplicationSummary> = {
  content: [MOCK_SUMMARY],
  totalElements: 1,
  totalPages: 1,
  size: 10,
  number: 0,
  first: true,
  last: true,
  empty: false,
};

describe('ApplicationService', () => {
  let service: ApplicationService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        ApplicationService,
      ],
    });

    service = TestBed.inject(ApplicationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getApplications() should call GET /api/applications with query params', () => {
    service.getApplications({ search: 'Airbus', status: 'APPLIED', page: 0, size: 10 }).subscribe((res) => {
      expect(res.content.length).toBe(1);
    });

    const req = httpMock.expectOne((r) =>
      r.url === '/api/applications' &&
      r.params.get('search') === 'Airbus' &&
      r.params.get('status') === 'APPLIED'
    );
    expect(req.request.method).toBe('GET');
    req.flush(MOCK_PAGE);
  });

  it('getKanbanApplications() should call GET /api/applications/kanban', () => {
    service.getKanbanApplications().subscribe((list) => {
      expect(list.length).toBe(1);
    });

    const req = httpMock.expectOne('/api/applications/kanban');
    expect(req.request.method).toBe('GET');
    req.flush([MOCK_SUMMARY]);
  });

  it('getApplicationById() should call GET /api/applications/:id', () => {
    service.getApplicationById(10).subscribe((app) => {
      expect(app.id).toBe(10);
    });

    const req = httpMock.expectOne('/api/applications/10');
    expect(req.request.method).toBe('GET');
    req.flush(MOCK_APP);
  });

  it('createApplication() should call POST /api/applications', () => {
    const payload: ApplicationCreateRequest = { offerId: 1, status: 'TO_APPLY' };

    service.createApplication(payload).subscribe((app) => {
      expect(app.id).toBe(10);
    });

    const req = httpMock.expectOne('/api/applications');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(MOCK_APP);
  });

  it('createApplicationFromOffer() should call POST /api/offers/:id/applications', () => {
    service.createApplicationFromOffer(1).subscribe((app) => {
      expect(app.id).toBe(10);
    });

    const req = httpMock.expectOne('/api/offers/1/applications');
    expect(req.request.method).toBe('POST');
    req.flush(MOCK_APP);
  });

  it('updateStatus() should call PATCH /api/applications/:id/status', () => {
    service.updateStatus(10, 'INTERVIEW', 'Passed screening').subscribe((app) => {
      expect(app.status).toBe('INTERVIEW');
    });

    const req = httpMock.expectOne('/api/applications/10/status');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ status: 'INTERVIEW', note: 'Passed screening' });
    req.flush({ ...MOCK_APP, status: 'INTERVIEW' });
  });

  it('deleteApplication() should call DELETE /api/applications/:id', () => {
    service.deleteApplication(10).subscribe();

    const req = httpMock.expectOne('/api/applications/10');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('getApplicationHistory() should call GET /api/applications/:id/history', () => {
    const mockHist: ApplicationHistory[] = [
      { id: 1, applicationId: 10, oldStatus: 'TO_APPLY', newStatus: 'APPLIED', changedAt: '2026-09-27T10:00:00Z', note: 'Sent' }
    ];

    service.getApplicationHistory(10).subscribe((hist) => {
      expect(hist.length).toBe(1);
    });

    const req = httpMock.expectOne('/api/applications/10/history');
    expect(req.request.method).toBe('GET');
    req.flush(mockHist);
  });
});
