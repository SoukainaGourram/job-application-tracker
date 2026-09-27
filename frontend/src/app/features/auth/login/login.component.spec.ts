import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { AuthService } from '../../../core/services/auth.service';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { vi } from 'vitest';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authServiceMock: { login: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    authServiceMock = {
      login: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        provideRouter([{ path: 'dashboard', component: LoginComponent }]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create LoginComponent', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with empty fields and invalid state', () => {
    expect(component.form.valid).toBe(false);
    expect(component.form.get('email')?.value).toBe('');
    expect(component.form.get('password')?.value).toBe('');
  });

  it('should validate email format', () => {
    const emailControl = component.form.get('email');
    emailControl?.setValue('invalid-email');
    expect(emailControl?.hasError('email')).toBe(true);

    emailControl?.setValue('valid@example.com');
    expect(emailControl?.hasError('email')).toBe(false);
  });

  it('should toggle password visibility', () => {
    expect(component.showPassword()).toBe(false);
    component.togglePassword();
    expect(component.showPassword()).toBe(true);
    component.togglePassword();
    expect(component.showPassword()).toBe(false);
  });

  it('should call AuthService.login on valid form submit', () => {
    authServiceMock.login.mockReturnValue(of({
      accessToken: 'token',
      tokenType: 'Bearer',
      expiresIn: 86400000,
      user: { id: 1, firstName: 'Jane', lastName: 'Doe', email: 'jane@test.com', role: 'USER', createdAt: '' }
    }));

    component.form.setValue({
      email: 'jane@test.com',
      password: 'Password123!',
    });

    component.onSubmit();
    expect(authServiceMock.login).toHaveBeenCalledWith({
      email: 'jane@test.com',
      password: 'Password123!',
    });
  });

  it('should display error message on login failure', () => {
    const errorResponse = new HttpErrorResponse({
      error: { message: 'Invalid email or password', status: 401 },
      status: 401,
      statusText: 'Unauthorized',
    });
    authServiceMock.login.mockReturnValue(throwError(() => errorResponse));

    component.form.setValue({
      email: 'jane@test.com',
      password: 'WrongPassword!',
    });

    component.onSubmit();
    expect(component.errorMessage()).toBe('Invalid email or password');
  });
});
