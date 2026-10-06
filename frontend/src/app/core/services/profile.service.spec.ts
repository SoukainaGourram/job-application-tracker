import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ProfileService } from './profile.service';
import { Profile, UpdateProfileRequest } from '../models/profile.models';

describe('ProfileService', () => {
  let service: ProfileService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ProfileService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(ProfileService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch user profile via GET /api/profile', () => {
    const mockProfile: Profile = {
      id: 1,
      firstName: 'Soukaina',
      lastName: 'Gourram',
      email: 'soukaina@example.com',
    };

    service.getProfile().subscribe((profile) => {
      expect(profile).toEqual(mockProfile);
      expect(profile.firstName).toBe('Soukaina');
    });

    const req = httpMock.expectOne('/api/profile');
    expect(req.request.method).toBe('GET');
    req.flush(mockProfile);
  });

  it('should update user profile via PUT /api/profile', () => {
    const requestData: UpdateProfileRequest = {
      firstName: 'Sarah',
      lastName: 'Benali',
    };

    const updatedProfile: Profile = {
      id: 1,
      firstName: 'Sarah',
      lastName: 'Benali',
      email: 'soukaina@example.com',
    };

    service.updateProfile(requestData).subscribe((profile) => {
      expect(profile).toEqual(updatedProfile);
      expect(profile.firstName).toBe('Sarah');
    });

    const req = httpMock.expectOne('/api/profile');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(requestData);
    req.flush(updatedProfile);
  });
});
