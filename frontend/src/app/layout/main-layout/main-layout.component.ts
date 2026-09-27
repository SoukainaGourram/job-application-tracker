import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

interface NavItem {
  label: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MainLayoutComponent {
  authService = inject(AuthService);

  navItems: NavItem[] = [
    { label: 'Dashboard',     icon: 'home',          route: '/dashboard' },
    { label: 'Offres',        icon: 'briefcase',     route: '/offers' },
    { label: 'Candidatures',  icon: 'send',          route: '/applications' },
    { label: 'Pipeline',      icon: 'columns',       route: '/pipeline' },
    { label: 'Entretiens',    icon: 'calendar',      route: '/interviews' },
    { label: 'Entreprises',   icon: 'building',      route: '/companies' },
    { label: 'Notifications', icon: 'bell',          route: '/notifications' },
  ];

  logout(): void {
    this.authService.logout();
  }
}
