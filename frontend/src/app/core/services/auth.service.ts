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

const API_URL = 'http://localhost:8080/api/auth';

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
   * Use this to refresh user data (e.g., after profile update).
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
   * Returns null if token is missing or expired.
   */
  private loadUserFromToken(): User | null {
    if (!this.tokenService.hasToken() || this.tokenService.isExpired()) {
      this.tokenService.removeToken();
      return null;
    }
    // Decode user info from token payload (non-sensitive fields)
    const payload = this.tokenService.decodePayload<{
      sub: string;
      role: string;
    }>();
    if (!payload) return null;

    // Return minimal user from token — full data fetched lazily from /me
    return null; // Will be populated by fetchCurrentUser() after navigation
  }
}
