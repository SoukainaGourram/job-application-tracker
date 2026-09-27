import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegisterComponent } from './register.component';
import { AuthService } from '../../../core/services/auth.service';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { vi } from 'vitest';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authServiceMock: { register: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    authServiceMock = {
      register: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        provideRouter([{ path: 'dashboard', component: RegisterComponent }]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create RegisterComponent', () => {
    expect(component).toBeTruthy();
  });

  it('should invalidate form when passwords do not match', () => {
    component.form.patchValue({
      firstName: 'Jane',
      lastName: 'Doe',
      email: 'jane@test.com',
      password: 'Password123!',
      confirmPassword: 'MismatchPassword!',
    });

    expect(component.form.errors?.['passwordMismatch']).toBe(true);
    expect(component.form.valid).toBe(false);
  });

  it('should validate password pattern requirement', () => {
    const passwordControl = component.form.get('password');
    passwordControl?.setValue('simple');
    expect(passwordControl?.errors?.['minlength']).toBeDefined();

    passwordControl?.setValue('nouppercase1!');
    expect(passwordControl?.errors?.['pattern']).toBeDefined();

    passwordControl?.setValue('ValidPassword1!');
    expect(passwordControl?.errors).toBeNull();
  });

  it('should call AuthService.register with valid credentials', () => {
    authServiceMock.register.mockReturnValue(of({
      accessToken: 'token',
      tokenType: 'Bearer',
      expiresIn: 86400000,
      user: { id: 1, firstName: 'Jane', lastName: 'Doe', email: 'jane@test.com', role: 'USER', createdAt: '' }
    }));

    component.form.setValue({
      firstName: 'Jane',
      lastName: 'Doe',
      email: 'jane@test.com',
      password: 'Password123!',
      confirmPassword: 'Password123!',
    });

    component.onSubmit();
    expect(authServiceMock.register).toHaveBeenCalledWith({
      firstName: 'Jane',
      lastName: 'Doe',
      email: 'jane@test.com',
      password: 'Password123!',
    });
  });

  it('should handle registration conflict error', () => {
    const errorResponse = new HttpErrorResponse({
      error: { message: 'Email already in use: jane@test.com', status: 409 },
      status: 409,
      statusText: 'Conflict',
    });
    authServiceMock.register.mockReturnValue(throwError(() => errorResponse));

    component.form.setValue({
      firstName: 'Jane',
      lastName: 'Doe',
      email: 'jane@test.com',
      password: 'Password123!',
      confirmPassword: 'Password123!',
    });

    component.onSubmit();
    expect(component.errorMessage()).toBe('Email already in use: jane@test.com');
  });
});
