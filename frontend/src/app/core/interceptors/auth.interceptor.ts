import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TokenService } from '../services/token.service';

/**
 * Functional HTTP interceptor — injects the JWT Bearer token
 * into every outgoing request (except auth endpoints).
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenService = inject(TokenService);
  const token = tokenService.getToken();

  // Skip token injection for public auth endpoints
  if (req.url.includes('/api/auth/register') || req.url.includes('/api/auth/login')) {
    return next(req);
  }

  if (token && !tokenService.isExpired()) {
    const authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
    return next(authReq);
  }

  return next(req);
};
