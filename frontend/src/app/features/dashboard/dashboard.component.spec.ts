import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { DashboardComponent } from './dashboard.component';
import { DashboardService } from '../../core/services/dashboard.service';
import { AuthService } from '../../core/services/auth.service';
import { DashboardStats } from '../../core/models/dashboard.models';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let dashboardServiceMock: any;
  let authServiceMock: any;

  const mockStats: DashboardStats = {
    totalApplications: 12,
    totalOffers: 8,
    totalCompanies: 5,
    totalInterviews: 2,
    applicationsByStatus: {
      TO_APPLY: 2,
      APPLIED: 4,
      SCREENING: 2,
      INTERVIEW: 2,
      OFFER: 1,
      ACCEPTED: 0,
      REJECTED: 1,
      WITHDRAWN: 0,
    },
    recentApplications: [
      {
        id: 1,
        offerId: 10,
        offerTitle: 'Développeur Fullstack',
        companyName: 'Tech Innov',
        contractType: 'CDI',
        status: 'APPLIED',
        createdAt: '2026-10-01T10:00:00Z',
      },
    ],
    upcomingInterviews: [
      {
        id: 1,
        applicationId: 1,
        offerTitle: 'Développeur Fullstack',
        companyName: 'Tech Innov',
        type: 'TECHNICAL',
        status: 'SCHEDULED',
        scheduledAt: '2026-10-15T14:00:00Z',
        location: 'Paris',
      },
    ],
  };

  beforeEach(async () => {
    dashboardServiceMock = {
      getStats: vi.fn().mockReturnValue(of(mockStats)),
    };

    authServiceMock = {
      currentUser: vi.fn().mockReturnValue({
        id: 1,
        firstName: 'Thomas',
        lastName: 'Dubois',
        email: 'thomas@jobtrack.dev',
        role: 'USER',
        createdAt: '',
      }),
      fetchCurrentUser: vi.fn().mockReturnValue(of({})),
    };

    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: DashboardService, useValue: dashboardServiceMock },
        { provide: AuthService, useValue: authServiceMock },
      ],
    }).compileComponents();
  });

  it('should create the component', () => {
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component).toBeTruthy();
  });

  it('should load dashboard stats successfully and compute distribution', () => {
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.loading()).toBe(false);
    expect(component.stats()).toEqual(mockStats);
    expect(component.errorMessage()).toBeNull();

    // Verification of computed distribution
    const bars = component.statusDistributionBars();
    expect(bars.length).toBe(8);

    const appliedBar = bars.find((b) => b.status === 'APPLIED');
    expect(appliedBar?.count).toBe(4);
    // 4 / 12 * 100 = 33.33%
    expect(appliedBar?.percentage).toBeCloseTo(33.33, 1);
  });

  it('should handle error state when fetching dashboard stats fails', () => {
    dashboardServiceMock.getStats.mockReturnValue(
      throwError(() => new Error('Network error'))
    );
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.loading()).toBe(false);
    expect(component.stats()).toBeNull();
    expect(component.errorMessage()).toContain('Impossible de récupérer les statistiques');
  });

  it('should display greeting with user first name', () => {
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.userFirstName()).toBe('Thomas');
  });
});
