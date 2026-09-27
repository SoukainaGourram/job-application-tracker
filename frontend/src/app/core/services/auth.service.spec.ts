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

    // URL is now relative /api/auth/login (no hardcoded localhost)
    const req = httpMock.expectOne('/api/auth/login');
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

    // URL is now relative /api/auth/register (no hardcoded localhost)
    const req = httpMock.expectOne('/api/auth/register');
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

  it('loadUserFromToken() should restore user from a valid token on page reload', () => {
    // Create a mock JWT with valid payload (exp: far future)
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const payload = btoa(
      JSON.stringify({
        sub: 'reload@example.com',
        role: 'ROLE_USER',
        exp: Math.floor(Date.now() / 1000) + 86400, // 24h from now
      })
    );
    const mockToken = `${header}.${payload}.signature`;
    localStorage.setItem('jt_access_token', mockToken);

    // Re-create service after token is set (simulates page reload)
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'login', component: class {} }]),
        AuthService,
        TokenService,
      ],
    });

    const freshService = TestBed.inject(AuthService);
    // User should be restored from token payload
    expect(freshService.currentUser()).not.toBeNull();
    expect(freshService.currentUser()?.email).toBe('reload@example.com');
    expect(freshService.isAuthenticated()).toBe(true);
  });
});
