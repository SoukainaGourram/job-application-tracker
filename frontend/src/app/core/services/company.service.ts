import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.development';
import {
  Company,
  CompanyContact,
  CompanyContactRequest,
  CompanyCreateRequest,
  CompanyFilters,
  CompanySummary,
  CompanyUpdateRequest,
} from '../models/company.models';
import { PagedResponse } from '../models/offer.models';

const API_URL = `${environment.apiUrl}/companies`;

@Injectable({ providedIn: 'root' })
export class CompanyService {
  private http = inject(HttpClient);

  getCompanies(filters?: CompanyFilters): Observable<PagedResponse<CompanySummary>> {
    let params = new HttpParams();

    if (filters) {
      if (filters.search) params = params.set('search', filters.search);
      if (filters.industry) params = params.set('industry', filters.industry);
      if (filters.location) params = params.set('location', filters.location);
      if (filters.size) params = params.set('size', filters.size);
      if (filters.page !== undefined) params = params.set('page', filters.page.toString());
      if (filters.pageSize !== undefined) params = params.set('size', filters.pageSize.toString());
    }

    return this.http.get<PagedResponse<CompanySummary>>(API_URL, { params });
  }

  getAllCompaniesForSelect(): Observable<CompanySummary[]> {
    return this.http.get<CompanySummary[]>(`${API_URL}/all`);
  }

  getCompanyById(id: number): Observable<Company> {
    return this.http.get<Company>(`${API_URL}/${id}`);
  }

  createCompany(request: CompanyCreateRequest): Observable<Company> {
    return this.http.post<Company>(API_URL, request);
  }

  updateCompany(id: number, request: CompanyUpdateRequest): Observable<Company> {
    return this.http.put<Company>(`${API_URL}/${id}`, request);
  }

  deleteCompany(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/${id}`);
  }

  // ── Contacts ─────────────────────────────────────────────────────────────

  getContacts(companyId: number): Observable<CompanyContact[]> {
    return this.http.get<CompanyContact[]>(`${API_URL}/${companyId}/contacts`);
  }

  getContactById(companyId: number, contactId: number): Observable<CompanyContact> {
    return this.http.get<CompanyContact>(`${API_URL}/${companyId}/contacts/${contactId}`);
  }

  addContact(companyId: number, request: CompanyContactRequest): Observable<CompanyContact> {
    return this.http.post<CompanyContact>(`${API_URL}/${companyId}/contacts`, request);
  }

  updateContact(
    companyId: number,
    contactId: number,
    request: CompanyContactRequest
  ): Observable<CompanyContact> {
    return this.http.put<CompanyContact>(`${API_URL}/${companyId}/contacts/${contactId}`, request);
  }

  deleteContact(companyId: number, contactId: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/${companyId}/contacts/${contactId}`);
  }
}
