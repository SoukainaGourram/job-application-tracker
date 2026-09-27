import { Injectable } from '@angular/core';

const TOKEN_KEY = 'jt_access_token';

@Injectable({ providedIn: 'root' })
export class TokenService {

  saveToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  removeToken(): void {
    localStorage.removeItem(TOKEN_KEY);
  }

  hasToken(): boolean {
    return this.getToken() !== null;
  }

  /**
   * Decode JWT payload (no signature verification — done server-side).
   * Returns null if token is missing or malformed.
   */
  decodePayload<T = Record<string, unknown>>(): T | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payloadBase64 = token.split('.')[1];
      const decoded = atob(payloadBase64.replace(/-/g, '+').replace(/_/g, '/'));
      return JSON.parse(decoded) as T;
    } catch {
      return null;
    }
  }

  isExpired(): boolean {
    const payload = this.decodePayload<{ exp: number }>();
    if (!payload?.exp) return true;
    // exp is in seconds, Date.now() in ms
    return Date.now() >= payload.exp * 1000;
  }
}
