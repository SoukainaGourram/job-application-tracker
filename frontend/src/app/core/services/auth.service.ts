import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { TokenService } from './token.service';
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  User,
} from '../models/auth.models';
import { environment } from '../../../environments/environment.development';

// In production (ng build), environment.development.ts is replaced with environment.ts
// Both point to the same apiUrl: '/api' — handled by proxy (dev) or Nginx (Docker/prod)
const API_URL = `${environment.apiUrl}/auth`;

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private tokenService = inject(TokenService);
  private router = inject(Router);

  // ── Reactive state with Signals ───────────────────────────────────────────
  private _currentUser = signal<User | null>(this.loadUserFromToken());

  /** Public read-only signal for current user */
  readonly currentUser = this._currentUser.asReadonly();

  /** Derived signal — true when user is authenticated AND token is not expired */
  readonly isAuthenticated = computed(
    () => this._currentUser() !== null && !this.tokenService.isExpired()
  );

  // ── Auth methods ──────────────────────────────────────────────────────────

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_URL}/register`, request).pipe(
      tap((response) => this.handleAuthResponse(response))
    );
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_URL}/login`, request).pipe(
      tap((response) => this.handleAuthResponse(response))
    );
  }

  logout(): void {
    this.tokenService.removeToken();
    this._currentUser.set(null);
    this.router.navigate(['/login']);
  }

  /**
   * Fetches the current user from the backend.
   * Use this to refresh user data after a page reload or after profile update.
   */
  fetchCurrentUser(): Observable<User> {
    return this.http.get<User>(`${API_URL}/me`).pipe(
      tap((user) => this._currentUser.set(user))
    );
  }

  isAuthenticated$(): boolean {
    return this.isAuthenticated();
  }

  // ── Private helpers ───────────────────────────────────────────────────────

  private handleAuthResponse(response: AuthResponse): void {
    this.tokenService.saveToken(response.accessToken);
    this._currentUser.set(response.user);
  }

  /**
   * On service init, try to restore user from existing token.
   * If a valid (non-expired) token is found in localStorage, restore the user
   * from the token payload to avoid a blank state on page reload (F5).
   * Full data can be refreshed lazily with fetchCurrentUser() if needed.
   */
  private loadUserFromToken(): User | null {
    if (!this.tokenService.hasToken() || this.tokenService.isExpired()) {
      this.tokenService.removeToken();
      return null;
    }

    // Decode the token payload (no signature verification — done server-side)
    const payload = this.tokenService.decodePayload<{
      sub: string;     // email
      role: string;    // e.g. "ROLE_USER"
      exp: number;
      iat: number;
    }>();

    if (!payload?.sub) return null;

    // Reconstruct a minimal User from token claims to avoid any API call on init.
    // The 'id', 'firstName', 'lastName', 'createdAt' fields are set to defaults
    // and will be populated as soon as the app calls fetchCurrentUser().
    return {
      id: 0,
      firstName: '',
      lastName: '',
      email: payload.sub,
      role: (payload.role?.replace('ROLE_', '') ?? 'USER') as 'USER' | 'ADMIN',
      createdAt: '',
    };
  }
}
