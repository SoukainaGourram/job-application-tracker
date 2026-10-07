import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { DashboardService } from './dashboard.service';
import { DashboardStats } from '../models/dashboard.models';

describe('DashboardService', () => {
  let service: DashboardService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        DashboardService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(DashboardService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch dashboard stats via GET', () => {
    const mockStats: DashboardStats = {
      totalApplications: 5,
      totalOffers: 3,
      totalCompanies: 2,
      totalInterviews: 1,
      applicationsByStatus: {
        TO_APPLY: 1,
        APPLIED: 2,
        SCREENING: 1,
        INTERVIEW: 1,
        OFFER: 0,
        ACCEPTED: 0,
        REJECTED: 0,
        WITHDRAWN: 0,
      },
      recentApplications: [],
      upcomingInterviews: [],
    };

    service.getStats().subscribe((stats) => {
      expect(stats).toEqual(mockStats);
      expect(stats.totalApplications).toBe(5);
    });

    const req = httpMock.expectOne('/api/dashboard/stats');
    expect(req.request.method).toBe('GET');
    req.flush(mockStats);
  });
});
