import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { authInterceptor } from './auth.interceptor';
import { TokenService } from '../services/token.service';
import { vi } from 'vitest';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let tokenService: TokenService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        TokenService,
      ],
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    tokenService = TestBed.inject(TokenService);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should add Authorization Bearer header when token exists and is valid', () => {
    vi.spyOn(tokenService, 'getToken').mockReturnValue('valid-test-token');
    vi.spyOn(tokenService, 'isExpired').mockReturnValue(false);

    http.get('/api/offers').subscribe();

    const req = httpMock.expectOne('/api/offers');
    expect(req.request.headers.has('Authorization')).toBe(true);
    expect(req.request.headers.get('Authorization')).toBe('Bearer valid-test-token');
    req.flush([]);
  });

  it('should NOT add Authorization header for /api/auth/login', () => {
    vi.spyOn(tokenService, 'getToken').mockReturnValue('valid-test-token');

    http.post('/api/auth/login', {}).subscribe();

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('should NOT add Authorization header for /api/auth/register', () => {
    vi.spyOn(tokenService, 'getToken').mockReturnValue('valid-test-token');

    http.post('/api/auth/register', {}).subscribe();

    const req = httpMock.expectOne('/api/auth/register');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('should NOT add Authorization header when no token is present', () => {
    vi.spyOn(tokenService, 'getToken').mockReturnValue(null);

    http.get('/api/offers').subscribe();

    const req = httpMock.expectOne('/api/offers');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush([]);
  });
});
