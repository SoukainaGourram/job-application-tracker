import { ComponentFixture, TestBed } from '@angular/core/testing';
import { OfferDetailComponent } from './offer-detail.component';
import { OfferService } from '../../../core/services/offer.service';
import { of } from 'rxjs';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { Offer } from '../../../core/models/offer.models';

import { ApplicationService } from '../../../core/services/application.service';

const MOCK_OFFER: Offer = {
  id: 10,
  title: 'Java Cloud Architect',
  companyName: 'CloudCorp',
  contractType: 'CDI',
  status: 'SAVED',
  technologies: ['Java', 'AWS'],
  city: 'Paris',
  country: 'France',
  description: 'Great job opportunity',
  jobUrl: 'https://example.com/job',
  createdAt: '2026-09-27T10:00:00Z',
  updatedAt: '2026-09-27T10:00:00Z',
};

describe('OfferDetailComponent', () => {
  let component: OfferDetailComponent;
  let fixture: ComponentFixture<OfferDetailComponent>;
  let offerServiceMock: any;
  let applicationServiceMock: any;
  let router: Router;

  beforeEach(async () => {
    offerServiceMock = {
      getOfferById: vi.fn().mockReturnValue(of(MOCK_OFFER)),
      updateStatus: vi.fn().mockReturnValue(of({ ...MOCK_OFFER, status: 'TO_APPLY' })),
      deleteOffer: vi.fn().mockReturnValue(of(undefined)),
    };

    applicationServiceMock = {
      createApplicationFromOffer: vi.fn().mockReturnValue(of({ id: 55, status: 'TO_APPLY' })),
    };

    await TestBed.configureTestingModule({
      imports: [OfferDetailComponent],
      providers: [
        { provide: OfferService, useValue: offerServiceMock },
        { provide: ApplicationService, useValue: applicationServiceMock },
        provideRouter([
          { path: 'offers', component: class {} },
          { path: 'offers/:id', component: class {} },
          { path: 'applications/:id', component: class {} },
        ]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => '10',
              },
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(OfferDetailComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockImplementation(() => Promise.resolve(true));
    fixture.detectChanges();
  });

  it('should create OfferDetailComponent and load offer details', () => {
    expect(component).toBeTruthy();
    expect(offerServiceMock.getOfferById).toHaveBeenCalledWith(10);
    expect(component.offer()?.title).toBe('Java Cloud Architect');
    expect(component.loading()).toBe(false);
  });

  it('should handle status update from dropdown', () => {
    component.onStatusChange('TO_APPLY');
    expect(offerServiceMock.updateStatus).toHaveBeenCalledWith(10, 'TO_APPLY');
    expect(component.offer()?.status).toBe('TO_APPLY');
  });

  it('should create application on apply button click and navigate to application detail', () => {
    component.onApplyClick();
    expect(applicationServiceMock.createApplicationFromOffer).toHaveBeenCalledWith(10);
    expect(router.navigate).toHaveBeenCalledWith(['/applications', 55]);
  });

  it('should handle delete confirmation and navigation', () => {
    component.promptDelete();
    expect(component.showDeleteConfirm()).toBe(true);

    component.executeDelete();
    expect(offerServiceMock.deleteOffer).toHaveBeenCalledWith(10);
    expect(router.navigate).toHaveBeenCalledWith(['/offers']);
  });
});
