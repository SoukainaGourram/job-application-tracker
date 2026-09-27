import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { TokenService } from '../services/token.service';

/**
 * Functional AuthGuard — protects routes requiring authentication.
 * Redirects to /login with the attempted URL as a query param.
 */
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const tokenService = inject(TokenService);
  const router = inject(Router);

  const hasToken = tokenService.hasToken();
  const isExpired = tokenService.isExpired();

  if (hasToken && !isExpired) {
    return true;
  }

  // Token missing or expired — clean up and redirect
  if (hasToken && isExpired) {
    tokenService.removeToken();
  }

  return router.createUrlTree(['/login'], {
    queryParams: { returnUrl: state.url },
  });
};
