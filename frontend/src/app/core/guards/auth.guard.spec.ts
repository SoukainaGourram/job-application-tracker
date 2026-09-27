import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { authGuard } from './auth.guard';
import { TokenService } from '../services/token.service';
import { provideRouter } from '@angular/router';
import { vi } from 'vitest';

describe('authGuard', () => {
  let tokenService: TokenService;
  let router: Router;

  const mockRoute = {} as ActivatedRouteSnapshot;
  const mockState = { url: '/dashboard' } as RouterStateSnapshot;

  function runGuard() {
    return TestBed.runInInjectionContext(() =>
      authGuard(mockRoute, mockState)
    );
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        TokenService,
      ],
    });

    tokenService = TestBed.inject(TokenService);
    router = TestBed.inject(Router);
    localStorage.clear();
  });

  afterEach(() => localStorage.clear());

  it('should allow access when a valid non-expired token exists', () => {
    vi.spyOn(tokenService, 'hasToken').mockReturnValue(true);
    vi.spyOn(tokenService, 'isExpired').mockReturnValue(false);

    const result = runGuard();
    expect(result).toBe(true);
  });

  it('should redirect to /login when no token exists', () => {
    vi.spyOn(tokenService, 'hasToken').mockReturnValue(false);
    vi.spyOn(tokenService, 'isExpired').mockReturnValue(true);

    const result = runGuard();
    expect(result).not.toBe(true);
  });

  it('should redirect to /login when token is expired', () => {
    vi.spyOn(tokenService, 'hasToken').mockReturnValue(true);
    vi.spyOn(tokenService, 'isExpired').mockReturnValue(true);
    const removeSpy = vi.spyOn(tokenService, 'removeToken');

    const result = runGuard();
    expect(removeSpy).toHaveBeenCalled();
    expect(result).not.toBe(true);
  });
});
