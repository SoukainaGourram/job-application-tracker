import { TestBed } from '@angular/core/testing';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { CompanyService } from './company.service';
import {
  Company,
  CompanyContact,
  CompanyContactRequest,
  CompanyCreateRequest,
  CompanySummary,
  CompanyUpdateRequest,
} from '../models/company.models';
import { PagedResponse } from '../models/offer.models';

const MOCK_CONTACT: CompanyContact = {
  id: 100,
  companyId: 10,
  firstName: 'Claire',
  lastName: 'Martin',
  jobTitle: 'Recruteuse',
  email: 'claire@techcorp.com',
  phone: '+33600000000',
  linkedinUrl: 'https://linkedin.com/in/claire',
  notes: 'Très sympa',
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

describe('CompanyService', () => {
  let service: CompanyService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        CompanyService,
      ],
    });

    service = TestBed.inject(CompanyService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getCompanies() should call GET /api/companies with query params', () => {
    service.getCompanies({ search: 'Tech', industry: 'Tech', page: 0, pageSize: 10 }).subscribe((res) => {
      expect(res.content.length).toBe(1);
      expect(res.content[0].name).toBe('TechCorp');
    });

    const req = httpMock.expectOne((r) =>
      r.url === '/api/companies' &&
      r.params.get('search') === 'Tech' &&
      r.params.get('industry') === 'Tech' &&
      r.params.get('page') === '0' &&
      r.params.get('size') === '10'
    );
    expect(req.request.method).toBe('GET');
    req.flush(MOCK_PAGE);
  });

  it('getAllCompaniesForSelect() should call GET /api/companies/all', () => {
    service.getAllCompaniesForSelect().subscribe((list) => {
      expect(list.length).toBe(1);
      expect(list[0].name).toBe('TechCorp');
    });

    const req = httpMock.expectOne('/api/companies/all');
    expect(req.request.method).toBe('GET');
    req.flush([MOCK_SUMMARY]);
  });

  it('getCompanyById() should call GET /api/companies/:id', () => {
    service.getCompanyById(10).subscribe((comp) => {
      expect(comp.id).toBe(10);
      expect(comp.name).toBe('TechCorp');
      expect(comp.contacts.length).toBe(1);
    });

    const req = httpMock.expectOne('/api/companies/10');
    expect(req.request.method).toBe('GET');
    req.flush(MOCK_COMPANY);
  });

  it('createCompany() should call POST /api/companies', () => {
    const payload: CompanyCreateRequest = { name: 'TechCorp', industry: 'Tech' };

    service.createCompany(payload).subscribe((comp) => {
      expect(comp.id).toBe(10);
    });

    const req = httpMock.expectOne('/api/companies');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(MOCK_COMPANY);
  });

  it('updateCompany() should call PUT /api/companies/:id', () => {
    const payload: CompanyUpdateRequest = { name: 'TechCorp Updated' };

    service.updateCompany(10, payload).subscribe((comp) => {
      expect(comp.name).toBe('TechCorp Updated');
    });

    const req = httpMock.expectOne('/api/companies/10');
    expect(req.request.method).toBe('PUT');
    req.flush({ ...MOCK_COMPANY, name: 'TechCorp Updated' });
  });

  it('deleteCompany() should call DELETE /api/companies/:id', () => {
    service.deleteCompany(10).subscribe();

    const req = httpMock.expectOne('/api/companies/10');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('getContacts() should call GET /api/companies/:companyId/contacts', () => {
    service.getContacts(10).subscribe((contacts) => {
      expect(contacts.length).toBe(1);
      expect(contacts[0].firstName).toBe('Claire');
    });

    const req = httpMock.expectOne('/api/companies/10/contacts');
    expect(req.request.method).toBe('GET');
    req.flush([MOCK_CONTACT]);
  });

  it('addContact() should call POST /api/companies/:companyId/contacts', () => {
    const reqBody: CompanyContactRequest = {
      firstName: 'Claire',
      lastName: 'Martin',
      email: 'claire@techcorp.com',
    };

    service.addContact(10, reqBody).subscribe((contact) => {
      expect(contact.id).toBe(100);
      expect(contact.firstName).toBe('Claire');
    });

    const req = httpMock.expectOne('/api/companies/10/contacts');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(reqBody);
    req.flush(MOCK_CONTACT);
  });

  it('updateContact() should call PUT /api/companies/:companyId/contacts/:contactId', () => {
    const reqBody: CompanyContactRequest = {
      firstName: 'Claire-Marie',
      lastName: 'Martin',
    };

    service.updateContact(10, 100, reqBody).subscribe((contact) => {
      expect(contact.firstName).toBe('Claire-Marie');
    });

    const req = httpMock.expectOne('/api/companies/10/contacts/100');
    expect(req.request.method).toBe('PUT');
    req.flush({ ...MOCK_CONTACT, firstName: 'Claire-Marie' });
  });

  it('deleteContact() should call DELETE /api/companies/:companyId/contacts/:contactId', () => {
    service.deleteContact(10, 100).subscribe();

    const req = httpMock.expectOne('/api/companies/10/contacts/100');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
