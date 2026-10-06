import {
  Component,
  OnInit,
  inject,
  signal,
  computed,
  ChangeDetectionStrategy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProfileService } from '../../core/services/profile.service';
import { AuthService } from '../../core/services/auth.service';
import { Profile } from '../../core/models/profile.models';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProfileComponent implements OnInit {
  private profileService = inject(ProfileService);
  private authService = inject(AuthService);

  readonly loading = signal<boolean>(true);
  readonly saving = signal<boolean>(false);
  readonly initialLoadError = signal<string | null>(null);

  readonly firstName = signal<string>('');
  readonly lastName = signal<string>('');
  readonly email = signal<string>('');

  readonly successMessage = signal<string | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly validationErrors = signal<Record<string, string>>({});

  readonly avatarInitials = computed(() => {
    const f = this.firstName().trim().charAt(0).toUpperCase();
    const l = this.lastName().trim().charAt(0).toUpperCase();
    return (f || '') + (l || '');
  });

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.loading.set(true);
    this.initialLoadError.set(null);
    this.successMessage.set(null);
    this.errorMessage.set(null);

    this.profileService.getProfile().subscribe({
      next: (profile: Profile) => {
        this.firstName.set(profile.firstName);
        this.lastName.set(profile.lastName);
        this.email.set(profile.email);
        this.loading.set(false);
      },
      error: () => {
        this.initialLoadError.set(
          'Impossible de charger vos informations personnelles. Veuillez réessayer.'
        );
        this.loading.set(false);
      },
    });
  }

  onSubmit(): void {
    // Reset previous feedback
    this.successMessage.set(null);
    this.errorMessage.set(null);
    this.validationErrors.set({});

    const fName = this.firstName().trim();
    const lName = this.lastName().trim();

    // Client-side validations
    const errors: Record<string, string> = {};
    if (!fName) {
      errors['firstName'] = 'Le prénom est obligatoire.';
    }
    if (!lName) {
      errors['lastName'] = 'Le nom est obligatoire.';
    }

    if (Object.keys(errors).length > 0) {
      this.validationErrors.set(errors);
      return;
    }

    this.saving.set(true);

    this.profileService.updateProfile({ firstName: fName, lastName: lName }).subscribe({
      next: (updated: Profile) => {
        this.firstName.set(updated.firstName);
        this.lastName.set(updated.lastName);
        this.saving.set(false);
        this.successMessage.set('Votre profil a été mis à jour avec succès.');

        // Refresh global current user in AuthService so header/sidebar updates automatically
        this.authService.fetchCurrentUser().subscribe();
      },
      error: (err) => {
        this.saving.set(false);
        if (err?.error?.validationErrors) {
          this.validationErrors.set(err.error.validationErrors);
        } else {
          this.errorMessage.set(
            err?.error?.message ||
              'Une erreur est survenue lors de la mise à jour de votre profil.'
          );
        }
      },
    });
  }
}
