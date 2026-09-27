import { ComponentFixture, TestBed } from '@angular/core/testing';
import { OfferFormComponent } from './offer-form.component';
import { OfferService } from '../../../core/services/offer.service';
import { of } from 'rxjs';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { Offer } from '../../../core/models/offer.models';

const MOCK_OFFER: Offer = {
  id: 1,
  title: 'Full Stack Engineer',
  companyName: 'Acme Corp',
  contractType: 'CDI',
  status: 'SAVED',
  technologies: ['Angular', 'Java'],
  city: 'Paris',
  country: 'France',
  createdAt: '2026-09-27T10:00:00Z',
  updatedAt: '2026-09-27T10:00:00Z',
};

describe('OfferFormComponent', () => {
  let component: OfferFormComponent;
  let fixture: ComponentFixture<OfferFormComponent>;
  let offerServiceMock: any;
  let router: Router;

  beforeEach(async () => {
    offerServiceMock = {
      getOfferById: vi.fn().mockReturnValue(of(MOCK_OFFER)),
      createOffer: vi.fn().mockReturnValue(of(MOCK_OFFER)),
      updateOffer: vi.fn().mockReturnValue(of(MOCK_OFFER)),
    };

    await TestBed.configureTestingModule({
      imports: [OfferFormComponent],
      providers: [
        { provide: OfferService, useValue: offerServiceMock },
        provideRouter([
          { path: 'offers', component: class {} },
          { path: 'offers/:id', component: class {} },
        ]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => null, // default create mode
              },
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(OfferFormComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockImplementation(() => Promise.resolve(true));
    fixture.detectChanges();
  });

  it('should create OfferFormComponent in create mode', () => {
    expect(component).toBeTruthy();
    expect(component.isEditMode()).toBe(false);
  });

  it('form should be invalid when required fields are empty', () => {
    component.form.patchValue({ title: '', companyName: '' });
    expect(component.form.invalid).toBe(true);
  });

  it('form should be valid when all required fields are filled', () => {
    component.form.patchValue({
      title: 'Senior Developer',
      companyName: 'TechCorp',
      contractType: 'CDI',
      status: 'SAVED',
    });
    expect(component.form.valid).toBe(true);
  });

  it('onSubmit in create mode should call createOffer', () => {
    component.form.patchValue({
      title: 'DevOps Engineer',
      companyName: 'CloudCo',
      contractType: 'CDI',
      technologiesInput: 'Docker, Kubernetes',
      status: 'SAVED',
    });

    component.onSubmit();
    expect(offerServiceMock.createOffer).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/offers', 1]);
  });
});

describe('OfferFormComponent (Edit Mode)', () => {
  let component: OfferFormComponent;
  let fixture: ComponentFixture<OfferFormComponent>;
  let offerServiceMock: any;

  beforeEach(async () => {
    offerServiceMock = {
      getOfferById: vi.fn().mockReturnValue(of(MOCK_OFFER)),
      updateOffer: vi.fn().mockReturnValue(of(MOCK_OFFER)),
    };

    await TestBed.configureTestingModule({
      imports: [OfferFormComponent],
      providers: [
        { provide: OfferService, useValue: offerServiceMock },
        provideRouter([
          { path: 'offers', component: class {} },
          { path: 'offers/:id', component: class {} },
        ]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => '1', // edit mode for id 1
              },
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(OfferFormComponent);
    component = fixture.componentInstance;
    const router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockImplementation(() => Promise.resolve(true));
    fixture.detectChanges();
  });

  it('should load offer data on edit mode init', () => {
    expect(component.isEditMode()).toBe(true);
    expect(offerServiceMock.getOfferById).toHaveBeenCalledWith(1);
    expect(component.form.get('title')?.value).toBe('Full Stack Engineer');
  });

  it('onSubmit in edit mode should call updateOffer', () => {
    component.onSubmit();
    expect(offerServiceMock.updateOffer).toHaveBeenCalled();
  });
});
