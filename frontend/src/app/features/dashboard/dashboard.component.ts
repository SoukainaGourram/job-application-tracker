import { Component, inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  template: `
    <div class="dashboard-placeholder">
      <div class="welcome-card">
        <div class="icon">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/>
          </svg>
        </div>
        <h1>Bienvenue sur JobTrack</h1>
        <p>
          Bonjour <strong>{{ currentUser()?.firstName }}</strong> ! 
          Votre espace de suivi de candidatures est prêt.
        </p>
        <p class="hint">Le dashboard complet sera disponible en Phase 6.</p>
        <div class="chips">
          <span class="chip">✅ Authentification active</span>
          <span class="chip">🔐 JWT valide</span>
          <span class="chip chip--role">Rôle : {{ currentUser()?.role }}</span>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-placeholder {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: calc(100vh - 4rem);
      padding: 2rem;
    }
    .welcome-card {
      background: rgba(255,255,255,0.04);
      border: 1px solid rgba(255,255,255,0.08);
      border-radius: 1.25rem;
      padding: 3rem 2.5rem;
      text-align: center;
      max-width: 520px;
      width: 100%;
    }
    .icon {
      width: 64px;
      height: 64px;
      background: linear-gradient(135deg, #6366f1, #8b5cf6);
      border-radius: 1rem;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 1.5rem;
      svg { width: 32px; height: 32px; stroke: white; }
    }
    h1 { font-size: 1.75rem; font-weight: 700; color: #fff; margin: 0 0 0.75rem; }
    p { color: rgba(255,255,255,0.6); margin: 0 0 0.5rem; line-height: 1.6; }
    p.hint { font-size: 0.875rem; color: rgba(255,255,255,0.35); margin-top: 0.75rem; }
    strong { color: rgba(255,255,255,0.9); }
    .chips { display: flex; gap: 0.5rem; justify-content: center; flex-wrap: wrap; margin-top: 1.5rem; }
    .chip {
      background: rgba(99,102,241,0.15);
      border: 1px solid rgba(99,102,241,0.3);
      color: #a5b4fc;
      padding: 0.375rem 0.875rem;
      border-radius: 999px;
      font-size: 0.8125rem;
      font-weight: 500;
    }
    .chip--role {
      background: rgba(34,197,94,0.12);
      border-color: rgba(34,197,94,0.3);
      color: #86efac;
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent implements OnInit {
  private authService = inject(AuthService);
  readonly currentUser = this.authService.currentUser;

  ngOnInit(): void {
    // Fetch fresh user data from /api/auth/me
    this.authService.fetchCurrentUser().subscribe();
  }
}
