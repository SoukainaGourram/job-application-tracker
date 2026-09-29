import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CompaniesListComponent } from './companies-list.component';
import { CompanyService } from '../../../core/services/company.service';
import { of, throwError } from 'rxjs';
import { provideRouter } from '@angular/router';
import { CompanySummary } from '../../../core/models/company.models';
import { PagedResponse } from '../../../core/models/offer.models';

const MOCK_SUMMARY: CompanySummary = {
  id: 10,
  name: 'TechCorp',
  industry: 'Tech',
  location: 'Paris',
  size: 'Grand Groupe',
  offersCount: 2,
  contactsCount: 1,
  createdAt: '2026-09-29T10:00:00Z',
};

const MOCK_PAGE: PagedResponse<CompanySummary> = {
  content: [MOCK_SUMMARY],
  totalElements: 1,
  totalPages: 1,
  size: 10,
  number: 0,
  first: true,
  last: true,
  empty: false,
};

describe('CompaniesListComponent', () => {
  let component: CompaniesListComponent;
  let fixture: ComponentFixture<CompaniesListComponent>;
  let companyServiceMock: any;

  beforeEach(async () => {
    companyServiceMock = {
      getCompanies: vi.fn().mockReturnValue(of(MOCK_PAGE)),
      deleteCompany: vi.fn().mockReturnValue(of(undefined)),
    };

    await TestBed.configureTestingModule({
      imports: [CompaniesListComponent],
      providers: [
        { provide: CompanyService, useValue: companyServiceMock },
        provideRouter([
          { path: 'companies', component: class {} },
          { path: 'companies/new', component: class {} },
          { path: 'companies/:id', component: class {} },
        ]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CompaniesListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create CompaniesListComponent', () => {
    expect(component).toBeTruthy();
  });

  it('should load companies on init', () => {
    expect(companyServiceMock.getCompanies).toHaveBeenCalled();
    expect(component.companies().length).toBe(1);
    expect(component.companies()[0].name).toBe('TechCorp');
  });

  it('should trigger search and reload companies', () => {
    component.searchTerm.set('TechCorp');
    component.onSearch();

    expect(companyServiceMock.getCompanies).toHaveBeenCalledWith(
      expect.objectContaining({ search: 'TechCorp', page: 0 })
    );
  });

  it('should handle load error gracefully', () => {
    companyServiceMock.getCompanies.mockReturnValue(throwError(() => new Error('API Error')));
    component.loadCompanies();

    expect(component.errorMessage()).toBe('Impossible de charger les entreprises. Veuillez réessayer.');
    expect(component.loading()).toBe(false);
  });

  it('should confirm and execute deletion', () => {
    const mockEvent = { stopPropagation: vi.fn(), preventDefault: vi.fn() } as any;
    component.confirmDelete(10, mockEvent);
    expect(component.deletingCompanyId()).toBe(10);

    component.executeDelete(10);
    expect(companyServiceMock.deleteCompany).toHaveBeenCalledWith(10);
    expect(component.deletingCompanyId()).toBeNull();
  });

  it('should cancel deletion', () => {
    component.deletingCompanyId.set(10);
    component.cancelDelete();
    expect(component.deletingCompanyId()).toBeNull();
  });
});
