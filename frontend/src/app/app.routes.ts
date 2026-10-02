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
      // ── Applications Module (Phase 4) ─────────────────────────────────────
      {
        path: 'applications',
        loadComponent: () =>
          import('./features/applications/applications-list/applications-list.component').then(m => m.ApplicationsListComponent),
        title: 'Candidatures — JobTrack',
      },
      {
        path: 'applications/:id',
        loadComponent: () =>
          import('./features/applications/application-detail/application-detail.component').then(m => m.ApplicationDetailComponent),
        title: 'Détail de la candidature — JobTrack',
      },
      {
        path: 'applications/:id/edit',
        loadComponent: () =>
          import('./features/applications/application-form/application-form.component').then(m => m.ApplicationFormComponent),
        title: 'Modifier la candidature — JobTrack',
      },
      {
        path: 'pipeline',
        loadComponent: () =>
          import('./features/pipeline/pipeline.component').then(m => m.PipelineComponent),
        title: 'Pipeline Kanban — JobTrack',
      },
      {
        path: 'interviews',
        loadComponent: () =>
          import('./features/interviews/interviews-page/interviews-page.component').then(m => m.InterviewsPageComponent),
        title: 'Entretiens — JobTrack',
      },
      // ── Companies Module (Phase 5) ──────────────────────────────────────────
      {
        path: 'companies',
        loadComponent: () =>
          import('./features/companies/companies-list/companies-list.component').then(m => m.CompaniesListComponent),
        title: 'Entreprises — JobTrack',
      },
      {
        path: 'companies/new',
        loadComponent: () =>
          import('./features/companies/company-form/company-form.component').then(m => m.CompanyFormComponent),
        title: 'Ajouter une entreprise — JobTrack',
      },
      {
        path: 'companies/:id',
        loadComponent: () =>
          import('./features/companies/company-detail/company-detail.component').then(m => m.CompanyDetailComponent),
        title: 'Détail de l\'entreprise — JobTrack',
      },
      {
        path: 'companies/:id/edit',
        loadComponent: () =>
          import('./features/companies/company-form/company-form.component').then(m => m.CompanyFormComponent),
        title: 'Modifier l\'entreprise — JobTrack',
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
