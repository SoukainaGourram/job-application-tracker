import { TestBed } from '@angular/core/testing';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { OfferService } from './offer.service';
import { Offer, OfferCreateRequest, OfferStatus, PagedResponse } from '../models/offer.models';

const MOCK_OFFER: Offer = {
  id: 1,
  title: 'Frontend Developer',
  companyName: 'Acme',
  contractType: 'CDI',
  technologies: ['Angular', 'TypeScript'],
  status: 'SAVED',
  createdAt: '2026-09-27T10:00:00Z',
  updatedAt: '2026-09-27T10:00:00Z',
};

const MOCK_PAGE: PagedResponse<Offer> = {
  content: [MOCK_OFFER],
  totalElements: 1,
  totalPages: 1,
  size: 10,
  number: 0,
  first: true,
  last: true,
  empty: false,
};

describe('OfferService', () => {
  let service: OfferService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        OfferService,
      ],
    });

    service = TestBed.inject(OfferService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getOffers() should perform GET with filter query parameters', () => {
    service.getOffers({ search: 'Angular', status: 'SAVED', page: 0, size: 10 }).subscribe((res) => {
      expect(res.content.length).toBe(1);
      expect(res.content[0].title).toBe('Frontend Developer');
    });

    const req = httpMock.expectOne((r) =>
      r.url === '/api/offers' &&
      r.params.get('search') === 'Angular' &&
      r.params.get('status') === 'SAVED' &&
      r.params.get('page') === '0'
    );
    expect(req.request.method).toBe('GET');
    req.flush(MOCK_PAGE);
  });

  it('getOfferById() should perform GET /api/offers/:id', () => {
    service.getOfferById(1).subscribe((offer) => {
      expect(offer.id).toBe(1);
      expect(offer.title).toBe('Frontend Developer');
    });

    const req = httpMock.expectOne('/api/offers/1');
    expect(req.request.method).toBe('GET');
    req.flush(MOCK_OFFER);
  });

  it('createOffer() should perform POST /api/offers', () => {
    const payload: OfferCreateRequest = {
      title: 'Frontend Developer',
      companyName: 'Acme',
      contractType: 'CDI',
      technologies: ['Angular'],
    };

    service.createOffer(payload).subscribe((offer) => {
      expect(offer.id).toBe(1);
    });

    const req = httpMock.expectOne('/api/offers');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(MOCK_OFFER);
  });

  it('updateOffer() should perform PUT /api/offers/:id', () => {
    service.updateOffer(1, { ...MOCK_OFFER, title: 'Senior Frontend Developer' }).subscribe((offer) => {
      expect(offer.title).toBe('Senior Frontend Developer');
    });

    const req = httpMock.expectOne('/api/offers/1');
    expect(req.request.method).toBe('PUT');
    req.flush({ ...MOCK_OFFER, title: 'Senior Frontend Developer' });
  });

  it('updateStatus() should perform PATCH /api/offers/:id/status', () => {
    service.updateStatus(1, 'TO_APPLY').subscribe((offer) => {
      expect(offer.status).toBe('TO_APPLY');
    });

    const req = httpMock.expectOne('/api/offers/1/status');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ status: 'TO_APPLY' });
    req.flush({ ...MOCK_OFFER, status: 'TO_APPLY' });
  });

  it('deleteOffer() should perform DELETE /api/offers/:id', () => {
    service.deleteOffer(1).subscribe();

    const req = httpMock.expectOne('/api/offers/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
