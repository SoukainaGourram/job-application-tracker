import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ApplicationsListComponent } from './applications-list.component';
import { ApplicationService } from '../../../core/services/application.service';
import { of, throwError } from 'rxjs';
import { provideRouter } from '@angular/router';
import { ApplicationSummary } from '../../../core/models/application.models';
import { PagedResponse } from '../../../core/models/offer.models';

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

describe('ApplicationsListComponent', () => {
  let component: ApplicationsListComponent;
  let fixture: ComponentFixture<ApplicationsListComponent>;
  let applicationServiceMock: any;

  beforeEach(async () => {
    applicationServiceMock = {
      getApplications: vi.fn().mockReturnValue(of(MOCK_PAGE)),
      deleteApplication: vi.fn().mockReturnValue(of(undefined)),
    };

    await TestBed.configureTestingModule({
      imports: [ApplicationsListComponent],
      providers: [
        { provide: ApplicationService, useValue: applicationServiceMock },
        provideRouter([
          { path: 'applications', component: class {} },
          { path: 'applications/:id', component: class {} },
        ]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create ApplicationsListComponent', () => {
    expect(component).toBeTruthy();
  });

  it('should load applications on init', () => {
    expect(applicationServiceMock.getApplications).toHaveBeenCalled();
    expect(component.applications().length).toBe(1);
    expect(component.applications()[0].companyName).toBe('Airbus');
  });

  it('should filter applications', () => {
    component.searchTerm.set('Airbus');
    component.selectedStatus.set('TO_APPLY');
    component.onFilterChange();
    expect(applicationServiceMock.getApplications).toHaveBeenCalled();
    expect(component.currentPage()).toBe(0);
  });

  it('should handle deletion confirmation and execution', () => {
    component.confirmDelete(10);
    expect(component.deletingId()).toBe(10);

    component.executeDelete(10);
    expect(applicationServiceMock.deleteApplication).toHaveBeenCalledWith(10);
    expect(component.deletingId()).toBeNull();
  });

  it('should display error message on loading failure', () => {
    applicationServiceMock.getApplications.mockReturnValue(throwError(() => new Error('Network error')));
    component.loadApplications();
    expect(component.errorMessage()).toBeTruthy();
    expect(component.loading()).toBe(false);
  });
});
