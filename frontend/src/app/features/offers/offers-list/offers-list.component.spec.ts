import { ComponentFixture, TestBed } from '@angular/core/testing';
import { OffersListComponent } from './offers-list.component';
import { OfferService } from '../../../core/services/offer.service';
import { of, throwError } from 'rxjs';
import { provideRouter } from '@angular/router';
import { Offer, PagedResponse } from '../../../core/models/offer.models';

const MOCK_OFFER: Offer = {
  id: 1,
  title: 'Java Developer',
  companyName: 'Acme Corp',
  contractType: 'CDI',
  status: 'SAVED',
  technologies: ['Java', 'Spring'],
  city: 'Paris',
  country: 'France',
  createdAt: '2026-09-27T10:00:00Z',
  updatedAt: '2026-09-27T10:00:00Z',
};

const MOCK_RESPONSE: PagedResponse<Offer> = {
  content: [MOCK_OFFER],
  totalElements: 1,
  totalPages: 1,
  size: 10,
  number: 0,
  first: true,
  last: true,
  empty: false,
};

describe('OffersListComponent', () => {
  let component: OffersListComponent;
  let fixture: ComponentFixture<OffersListComponent>;
  let offerServiceMock: any;

  beforeEach(async () => {
    offerServiceMock = {
      getOffers: vi.fn().mockReturnValue(of(MOCK_RESPONSE)),
      updateStatus: vi.fn().mockReturnValue(of({ ...MOCK_OFFER, status: 'TO_APPLY' })),
      deleteOffer: vi.fn().mockReturnValue(of(undefined)),
    };

    await TestBed.configureTestingModule({
      imports: [OffersListComponent],
      providers: [
        { provide: OfferService, useValue: offerServiceMock },
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(OffersListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create OffersListComponent', () => {
    expect(component).toBeTruthy();
  });

  it('should load offers on init', () => {
    expect(offerServiceMock.getOffers).toHaveBeenCalled();
    expect(component.offers().length).toBe(1);
    expect(component.offers()[0].title).toBe('Java Developer');
  });

  it('should handle search filter', () => {
    component.searchTerm.set('Angular');
    component.onSearch();
    expect(offerServiceMock.getOffers).toHaveBeenCalled();
    expect(component.currentPage()).toBe(0);
  });

  it('should handle quick status change', () => {
    component.onQuickStatusChange(MOCK_OFFER, 'TO_APPLY');
    expect(offerServiceMock.updateStatus).toHaveBeenCalledWith(1, 'TO_APPLY');
  });

  it('should show delete confirmation and execute deletion', () => {
    component.confirmDelete(1);
    expect(component.deletingOfferId()).toBe(1);

    component.executeDelete(1);
    expect(offerServiceMock.deleteOffer).toHaveBeenCalledWith(1);
    expect(component.deletingOfferId()).toBeNull();
  });

  it('should handle error when loading offers fails', () => {
    offerServiceMock.getOffers.mockReturnValue(throwError(() => new Error('Network error')));
    component.loadOffers();
    expect(component.errorMessage()).toBeTruthy();
    expect(component.loading()).toBe(false);
  });
});
