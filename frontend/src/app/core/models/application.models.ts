import { ContractType, Offer } from './offer.models';

export type ApplicationStatus =
  | 'TO_APPLY'
  | 'APPLIED'
  | 'SCREENING'
  | 'INTERVIEW'
  | 'OFFER'
  | 'ACCEPTED'
  | 'REJECTED'
  | 'WITHDRAWN';

export interface ApplicationHistory {
  id: number;
  applicationId: number;
  oldStatus?: ApplicationStatus | null;
  newStatus: ApplicationStatus;
  changedAt: string;
  note?: string | null;
}

export interface Application {
  id: number;
  offer: Offer;
  status: ApplicationStatus;
  appliedAt?: string | null;
  notes?: string | null;
  coverLetter?: string | null;
  cvVersion?: string | null;
  source?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ApplicationSummary {
  id: number;
  offerId: number;
  offerTitle: string;
  companyName: string;
  city?: string | null;
  country?: string | null;
  contractType: ContractType;
  status: ApplicationStatus;
  appliedAt?: string | null;
  technologies?: string[] | null;
  createdAt: string;
}

export interface ApplicationCreateRequest {
  offerId: number;
  status?: ApplicationStatus;
  appliedAt?: string | null;
  notes?: string | null;
  coverLetter?: string | null;
  cvVersion?: string | null;
  source?: string | null;
}

export interface ApplicationUpdateRequest {
  appliedAt?: string | null;
  notes?: string | null;
  coverLetter?: string | null;
  cvVersion?: string | null;
  source?: string | null;
}

export interface ApplicationStatusUpdateRequest {
  status: ApplicationStatus;
  note?: string | null;
}

export interface ApplicationFilters {
  search?: string;
  status?: ApplicationStatus | '';
  company?: string;
  page?: number;
  size?: number;
  sort?: string;
}
