import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { ProfileComponent } from './profile.component';
import { ProfileService } from '../../core/services/profile.service';
import { AuthService } from '../../core/services/auth.service';
import { Profile } from '../../core/models/profile.models';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  let profileServiceMock: any;
  let authServiceMock: any;

  const mockProfile: Profile = {
    id: 1,
    firstName: 'Soukaina',
    lastName: 'Gourram',
    email: 'soukaina@example.com',
  };

  beforeEach(async () => {
    profileServiceMock = {
      getProfile: vi.fn().mockReturnValue(of(mockProfile)),
      updateProfile: vi.fn().mockReturnValue(
        of({
          id: 1,
          firstName: 'Sarah',
          lastName: 'Benali',
          email: 'soukaina@example.com',
        })
      ),
    };

    authServiceMock = {
      fetchCurrentUser: vi.fn().mockReturnValue(of({})),
    };

    await TestBed.configureTestingModule({
      imports: [ProfileComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ProfileService, useValue: profileServiceMock },
        { provide: AuthService, useValue: authServiceMock },
      ],
    }).compileComponents();
  });

  it('should create the component and load user profile', () => {
    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component).toBeTruthy();
    expect(component.loading()).toBe(false);
    expect(component.firstName()).toBe('Soukaina');
    expect(component.lastName()).toBe('Gourram');
    expect(component.email()).toBe('soukaina@example.com');
    expect(component.avatarInitials()).toBe('SG');
  });

  it('should handle initial load error', () => {
    profileServiceMock.getProfile.mockReturnValue(
      throwError(() => new Error('Server error'))
    );
    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.loading()).toBe(false);
    expect(component.initialLoadError()).toContain(
      'Impossible de charger vos informations'
    );
  });

  it('should validate empty inputs on submit without calling updateProfile', () => {
    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.firstName.set('');
    component.lastName.set('');
    component.onSubmit();

    expect(profileServiceMock.updateProfile).not.toHaveBeenCalled();
    expect(component.validationErrors()['firstName']).toBe(
      'Le prénom est obligatoire.'
    );
    expect(component.validationErrors()['lastName']).toBe(
      'Le nom est obligatoire.'
    );
  });

  it('should update profile successfully and show success message', () => {
    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.firstName.set('Sarah');
    component.lastName.set('Benali');
    component.onSubmit();

    expect(profileServiceMock.updateProfile).toHaveBeenCalledWith({
      firstName: 'Sarah',
      lastName: 'Benali',
    });
    expect(component.saving()).toBe(false);
    expect(component.successMessage()).toBe(
      'Votre profil a été mis à jour avec succès.'
    );
    expect(authServiceMock.fetchCurrentUser).toHaveBeenCalled();
  });

  it('should handle API errors during update', () => {
    profileServiceMock.updateProfile.mockReturnValue(
      throwError(() => ({
        error: { message: 'Erreur lors de la mise à jour' },
      }))
    );
    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.firstName.set('Sarah');
    component.lastName.set('Benali');
    component.onSubmit();

    expect(component.saving()).toBe(false);
    expect(component.errorMessage()).toBe('Erreur lors de la mise à jour');
  });
});
