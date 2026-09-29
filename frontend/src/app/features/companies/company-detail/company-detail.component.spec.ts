import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CompanyDetailComponent } from './company-detail.component';
import { CompanyService } from '../../../core/services/company.service';
import { of, throwError } from 'rxjs';
import { ActivatedRoute, Router, provideRouter } from '@angular/router';
import { Company, CompanyContact } from '../../../core/models/company.models';

const MOCK_CONTACT: CompanyContact = {
  id: 100,
  companyId: 10,
  firstName: 'Claire',
  lastName: 'Martin',
  jobTitle: 'Recruteuse',
  email: 'claire@techcorp.com',
  phone: '+33600000000',
  linkedinUrl: 'https://linkedin.com/in/claire',
  notes: 'Notes',
  createdAt: '2026-09-29T10:00:00Z',
  updatedAt: '2026-09-29T10:00:00Z',
};

const MOCK_COMPANY: Company = {
  id: 10,
  name: 'TechCorp',
  industry: 'Tech',
  location: 'Paris',
  size: 'Grand Groupe',
  website: 'https://techcorp.com',
  description: 'Leader tech',
  notes: 'Notes privées',
  contacts: [MOCK_CONTACT],
  offersCount: 2,
  createdAt: '2026-09-29T10:00:00Z',
  updatedAt: '2026-09-29T10:00:00Z',
};

describe('CompanyDetailComponent', () => {
  let component: CompanyDetailComponent;
  let fixture: ComponentFixture<CompanyDetailComponent>;
  let companyServiceMock: any;
  let router: Router;

  beforeEach(async () => {
    companyServiceMock = {
      getCompanyById: vi.fn().mockReturnValue(of(MOCK_COMPANY)),
      deleteCompany: vi.fn().mockReturnValue(of(undefined)),
      addContact: vi.fn().mockReturnValue(of(MOCK_CONTACT)),
      updateContact: vi.fn().mockReturnValue(of(MOCK_CONTACT)),
      deleteContact: vi.fn().mockReturnValue(of(undefined)),
    };

    await TestBed.configureTestingModule({
      imports: [CompanyDetailComponent],
      providers: [
        { provide: CompanyService, useValue: companyServiceMock },
        provideRouter([
          { path: 'companies', component: class {} },
          { path: 'companies/:id/edit', component: class {} },
        ]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => (key === 'id' ? '10' : null),
              },
            },
          },
        },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockImplementation(() => Promise.resolve(true));

    fixture = TestBed.createComponent(CompanyDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create CompanyDetailComponent and load details', () => {
    expect(component).toBeTruthy();
    expect(companyServiceMock.getCompanyById).toHaveBeenCalledWith(10);
    expect(component.company()?.name).toBe('TechCorp');
    expect(component.company()?.contacts.length).toBe(1);
  });

  it('should open and close contact modal', () => {
    component.openAddContactModal();
    expect(component.showContactModal()).toBe(true);
    expect(component.editingContactId()).toBeNull();

    component.closeContactModal();
    expect(component.showContactModal()).toBe(false);
  });

  it('should open edit contact modal with prefilled values', () => {
    component.openEditContactModal(MOCK_CONTACT);
    expect(component.showContactModal()).toBe(true);
    expect(component.editingContactId()).toBe(100);
    expect(component.contactForm.value.firstName).toBe('Claire');
  });

  it('should save new contact on submit', () => {
    component.openAddContactModal();
    component.contactForm.patchValue({
      firstName: 'Alice',
      lastName: 'Smith',
      email: 'alice@techcorp.com',
    });

    component.saveContact();

    expect(companyServiceMock.addContact).toHaveBeenCalledWith(
      10,
      expect.objectContaining({ firstName: 'Alice', lastName: 'Smith' })
    );
    expect(component.showContactModal()).toBe(false);
  });

  it('should update existing contact on submit', () => {
    component.openEditContactModal(MOCK_CONTACT);
    component.contactForm.patchValue({
      firstName: 'Claire-Marie',
    });

    component.saveContact();

    expect(companyServiceMock.updateContact).toHaveBeenCalledWith(
      10,
      100,
      expect.objectContaining({ firstName: 'Claire-Marie' })
    );
    expect(component.showContactModal()).toBe(false);
  });

  it('should delete contact when confirmed', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    component.deleteContact(100);

    expect(companyServiceMock.deleteContact).toHaveBeenCalledWith(10, 100);
    expect(companyServiceMock.getCompanyById).toHaveBeenCalled();
  });

  it('should delete company and navigate to /companies', () => {
    component.deleteCompany();

    expect(companyServiceMock.deleteCompany).toHaveBeenCalledWith(10);
    expect(router.navigate).toHaveBeenCalledWith(['/companies']);
  });

  it('should handle load error', () => {
    companyServiceMock.getCompanyById.mockReturnValue(throwError(() => new Error('Error')));
    component.loadCompany();

    expect(component.errorMessage()).toBe('Impossible de charger les détails de l\'entreprise.');
    expect(component.loading()).toBe(false);
  });
});
