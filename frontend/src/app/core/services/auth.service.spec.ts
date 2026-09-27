import { TestBed } from '@angular/core/testing';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { TokenService } from './token.service';
import { AuthResponse } from '../models/auth.models';

const MOCK_AUTH_RESPONSE: AuthResponse = {
  accessToken: 'mock.jwt.token',
  tokenType: 'Bearer',
  expiresIn: 86400000,
  user: {
    id: 1,
    firstName: 'Jane',
    lastName: 'Doe',
    email: 'jane@example.com',
    role: 'USER',
    createdAt: new Date().toISOString(),
  },
};

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let tokenService: TokenService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'login', component: class {} }]),
        AuthService,
        TokenService,
      ],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    tokenService = TestBed.inject(TokenService);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('login() should call POST /api/auth/login and store token', () => {
    let result: AuthResponse | undefined;

    service.login({ email: 'jane@example.com', password: 'Password123!' }).subscribe((r) => {
      result = r;
    });

    const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush(MOCK_AUTH_RESPONSE);

    expect(tokenService.getToken()).toBe('mock.jwt.token');
    expect(service.currentUser()?.email).toBe('jane@example.com');
  });

  it('register() should call POST /api/auth/register and store token', () => {
    service
      .register({
        firstName: 'Jane',
        lastName: 'Doe',
        email: 'jane@example.com',
        password: 'Password123!',
      })
      .subscribe();

    const req = httpMock.expectOne('http://localhost:8080/api/auth/register');
    expect(req.request.method).toBe('POST');
    req.flush(MOCK_AUTH_RESPONSE);

    expect(tokenService.getToken()).toBe('mock.jwt.token');
  });

  it('logout() should clear token and set currentUser to null', () => {
    tokenService.saveToken('some-token');
    service.logout();
    expect(tokenService.hasToken()).toBe(false);
    expect(service.currentUser()).toBeNull();
  });

  it('isAuthenticated() should be false when no token', () => {
    expect(service.isAuthenticated()).toBe(false);
  });
});
