export type ContractType = 'CDI' | 'CDD' | 'STAGE' | 'ALTERNANCE' | 'FREELANCE';

export type OfferStatus = 'SAVED' | 'TO_APPLY' | 'ARCHIVED';

export interface Company {
  id: number;
  name: string;
  city?: string | null;
  country?: string | null;
  website?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface Offer {
  id: number;
  title: string;
  companyName: string;
  company?: Company | null;
  city?: string | null;
  country?: string | null;
  contractType: ContractType;
  technologies: string[];
  description?: string | null;
  jobUrl?: string | null;
  salary?: string | null;
  source?: string | null;
  applicationDeadline?: string | null;
  status: OfferStatus;
  createdAt: string;
  updatedAt: string;
}

export interface OfferCreateRequest {
  title: string;
  companyName: string;
  companyId?: number | null;
  city?: string | null;
  country?: string | null;
  contractType: ContractType;
  technologies: string[];
  description?: string | null;
  jobUrl?: string | null;
  salary?: string | null;
  source?: string | null;
  applicationDeadline?: string | null;
  status?: OfferStatus;
}

export interface OfferUpdateRequest {
  title: string;
  companyName: string;
  companyId?: number | null;
  city?: string | null;
  country?: string | null;
  contractType: ContractType;
  technologies: string[];
  description?: string | null;
  jobUrl?: string | null;
  salary?: string | null;
  source?: string | null;
  applicationDeadline?: string | null;
  status: OfferStatus;
}

export interface OfferStatusUpdateRequest {
  status: OfferStatus;
}

export interface OfferFilters {
  search?: string;
  status?: OfferStatus | '';
  contractType?: ContractType | '';
  city?: string;
  country?: string;
  technology?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
