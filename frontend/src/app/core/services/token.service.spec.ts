import { TestBed } from '@angular/core/testing';
import { TokenService } from './token.service';

describe('TokenService', () => {
  let service: TokenService;

  const MOCK_TOKEN =
    'eyJhbGciOiJIUzI1NiJ9.' +
    btoa(JSON.stringify({ sub: 'jane@example.com', role: 'ROLE_USER', exp: Math.floor(Date.now() / 1000) + 86400 }))
      .replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_') +
    '.signature';

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TokenService);
    localStorage.clear();
  });

  afterEach(() => localStorage.clear());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('saveToken() should persist token in localStorage', () => {
    service.saveToken('my-token');
    expect(localStorage.getItem('jt_access_token')).toBe('my-token');
  });

  it('getToken() should return saved token', () => {
    localStorage.setItem('jt_access_token', 'my-token');
    expect(service.getToken()).toBe('my-token');
  });

  it('removeToken() should clear the token', () => {
    service.saveToken('my-token');
    service.removeToken();
    expect(service.getToken()).toBeNull();
  });

  it('hasToken() should return true when token exists', () => {
    service.saveToken('my-token');
    expect(service.hasToken()).toBe(true);
  });

  it('hasToken() should return false when no token', () => {
    expect(service.hasToken()).toBe(false);
  });

  it('isExpired() should return true when no token', () => {
    expect(service.isExpired()).toBe(true);
  });

  it('isExpired() should return false for a valid non-expired token', () => {
    service.saveToken(MOCK_TOKEN);
    expect(service.isExpired()).toBe(false);
  });
});
