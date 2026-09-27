import { ApplicationConfig, provideBrowserGlobalErrorListeners, APP_INITIALIZER } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { AuthService } from './core/services/auth.service';
import { TokenService } from './core/services/token.service';
import { catchError, of } from 'rxjs';

/**
 * APP_INITIALIZER factory — runs once at app startup.
 * If a valid (non-expired) JWT is in localStorage, silently fetches the full
 * user profile from /api/auth/me so that currentUser() is populated immediately
 * (avoids blank state / false logout on page reload F5).
 */
function initializeAuth(authService: AuthService, tokenService: TokenService) {
  return () => {
    if (tokenService.hasToken() && !tokenService.isExpired()) {
      // Fetch full user profile from backend; swallow errors (e.g., 401) gracefully
      return authService.fetchCurrentUser().pipe(
        catchError(() => {
          tokenService.removeToken();
          return of(null);
        })
      );
    }
    return of(null);
  };
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(
      withInterceptors([authInterceptor, errorInterceptor])
    ),
    provideAnimationsAsync(),
    {
      provide: APP_INITIALIZER,
      useFactory: initializeAuth,
      deps: [AuthService, TokenService],
      multi: true,
    },
  ],
};
