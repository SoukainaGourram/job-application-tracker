import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  // ── Public routes ───────────────────────────────────────────────────────────
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(m => m.LoginComponent),
    title: 'Connexion — JobTrack',
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register.component').then(m => m.RegisterComponent),
    title: 'Créer un compte — JobTrack',
  },

  // ── Protected routes (inside MainLayout) ────────────────────────────────────
  {
    path: '',
    loadComponent: () =>
      import('./layout/main-layout/main-layout.component').then(m => m.MainLayoutComponent),
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
        title: 'Dashboard — JobTrack',
      },
      // ── Offers Module (Phase 3) ───────────────────────────────────────────
      {
        path: 'offers',
        loadComponent: () =>
          import('./features/offers/offers-list/offers-list.component').then(m => m.OffersListComponent),
        title: 'Mes Offres — JobTrack',
      },
      {
        path: 'offers/new',
        loadComponent: () =>
          import('./features/offers/offer-form/offer-form.component').then(m => m.OfferFormComponent),
        title: 'Ajouter une offre — JobTrack',
      },
      {
        path: 'offers/:id',
        loadComponent: () =>
          import('./features/offers/offer-detail/offer-detail.component').then(m => m.OfferDetailComponent),
        title: 'Détail de l\'offre — JobTrack',
      },
      {
        path: 'offers/:id/edit',
        loadComponent: () =>
          import('./features/offers/offer-form/offer-form.component').then(m => m.OfferFormComponent),
        title: 'Modifier l\'offre — JobTrack',
      },
      {
        path: 'applications',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
        title: 'Candidatures — JobTrack',
      },
      {
        path: 'pipeline',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
        title: 'Pipeline — JobTrack',
      },
      {
        path: 'interviews',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
        title: 'Entretiens — JobTrack',
      },
      {
        path: 'companies',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
        title: 'Entreprises — JobTrack',
      },
      {
        path: 'notifications',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
        title: 'Notifications — JobTrack',
      },
      {
        path: 'profile',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
        title: 'Profil — JobTrack',
      },
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full',
      },
    ],
  },

  // ── Fallback ────────────────────────────────────────────────────────────────
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },
  {
    path: '**',
    redirectTo: 'dashboard',
  },
];
