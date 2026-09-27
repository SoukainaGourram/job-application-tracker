import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.development';
import {
  Offer,
  OfferCreateRequest,
  OfferFilters,
  OfferStatus,
  OfferStatusUpdateRequest,
  OfferUpdateRequest,
  PagedResponse,
} from '../models/offer.models';

const API_URL = `${environment.apiUrl}/offers`;

@Injectable({ providedIn: 'root' })
export class OfferService {
  private http = inject(HttpClient);

  getOffers(filters?: OfferFilters): Observable<PagedResponse<Offer>> {
    let params = new HttpParams();

    if (filters) {
      if (filters.search) params = params.set('search', filters.search);
      if (filters.status) params = params.set('status', filters.status);
      if (filters.contractType) params = params.set('contractType', filters.contractType);
      if (filters.city) params = params.set('city', filters.city);
      if (filters.country) params = params.set('country', filters.country);
      if (filters.technology) params = params.set('technology', filters.technology);
      if (filters.page !== undefined) params = params.set('page', filters.page.toString());
      if (filters.size !== undefined) params = params.set('size', filters.size.toString());
      if (filters.sort) params = params.set('sort', filters.sort);
    }

    return this.http.get<PagedResponse<Offer>>(API_URL, { params });
  }

  getOfferById(id: number): Observable<Offer> {
    return this.http.get<Offer>(`${API_URL}/${id}`);
  }

  createOffer(request: OfferCreateRequest): Observable<Offer> {
    return this.http.post<Offer>(API_URL, request);
  }

  updateOffer(id: number, request: OfferUpdateRequest): Observable<Offer> {
    return this.http.put<Offer>(`${API_URL}/${id}`, request);
  }

  updateStatus(id: number, status: OfferStatus): Observable<Offer> {
    const body: OfferStatusUpdateRequest = { status };
    return this.http.patch<Offer>(`${API_URL}/${id}/status`, body);
  }

  deleteOffer(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/${id}`);
  }
}
