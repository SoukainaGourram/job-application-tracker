import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.development';
import {
  Application,
  ApplicationCreateRequest,
  ApplicationFilters,
  ApplicationHistory,
  ApplicationStatus,
  ApplicationStatusUpdateRequest,
  ApplicationSummary,
  ApplicationUpdateRequest,
} from '../models/application.models';
import { PagedResponse } from '../models/offer.models';

const API_URL = `${environment.apiUrl}/applications`;
const OFFERS_API_URL = `${environment.apiUrl}/offers`;

@Injectable({ providedIn: 'root' })
export class ApplicationService {
  private http = inject(HttpClient);

  getApplications(filters?: ApplicationFilters): Observable<PagedResponse<ApplicationSummary>> {
    let params = new HttpParams();

    if (filters) {
      if (filters.search) params = params.set('search', filters.search);
      if (filters.status) params = params.set('status', filters.status);
      if (filters.company) params = params.set('company', filters.company);
      if (filters.page !== undefined) params = params.set('page', filters.page.toString());
      if (filters.size !== undefined) params = params.set('size', filters.size.toString());
      if (filters.sort) params = params.set('sort', filters.sort);
    }

    return this.http.get<PagedResponse<ApplicationSummary>>(API_URL, { params });
  }

  getKanbanApplications(): Observable<ApplicationSummary[]> {
    return this.http.get<ApplicationSummary[]>(`${API_URL}/kanban`);
  }

  getApplicationById(id: number): Observable<Application> {
    return this.http.get<Application>(`${API_URL}/${id}`);
  }

  createApplication(request: ApplicationCreateRequest): Observable<Application> {
    return this.http.post<Application>(API_URL, request);
  }

  createApplicationFromOffer(offerId: number): Observable<Application> {
    return this.http.post<Application>(`${OFFERS_API_URL}/${offerId}/applications`, {});
  }

  updateApplication(id: number, request: ApplicationUpdateRequest): Observable<Application> {
    return this.http.put<Application>(`${API_URL}/${id}`, request);
  }

  updateStatus(id: number, status: ApplicationStatus, note?: string): Observable<Application> {
    const body: ApplicationStatusUpdateRequest = { status, note: note || null };
    return this.http.patch<Application>(`${API_URL}/${id}/status`, body);
  }

  deleteApplication(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/${id}`);
  }

  getApplicationHistory(id: number): Observable<ApplicationHistory[]> {
    return this.http.get<ApplicationHistory[]>(`${API_URL}/${id}/history`);
  }
}
