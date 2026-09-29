export interface CompanyContact {
  id: number;
  companyId: number;
  firstName: string;
  lastName: string;
  jobTitle?: string | null;
  email?: string | null;
  phone?: string | null;
  linkedinUrl?: string | null;
  notes?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CompanyContactRequest {
  firstName: string;
  lastName: string;
  jobTitle?: string | null;
  email?: string | null;
  phone?: string | null;
  linkedinUrl?: string | null;
  notes?: string | null;
}

export interface Company {
  id: number;
  name: string;
  website?: string | null;
  industry?: string | null;
  location?: string | null;
  size?: string | null;
  description?: string | null;
  notes?: string | null;
  city?: string | null;
  country?: string | null;
  contacts: CompanyContact[];
  offersCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CompanySummary {
  id: number;
  name: string;
  website?: string | null;
  industry?: string | null;
  location?: string | null;
  size?: string | null;
  offersCount: number;
  contactsCount: number;
  createdAt: string;
}

export interface CompanyCreateRequest {
  name: string;
  website?: string | null;
  industry?: string | null;
  location?: string | null;
  size?: string | null;
  description?: string | null;
  notes?: string | null;
}

export interface CompanyUpdateRequest {
  name: string;
  website?: string | null;
  industry?: string | null;
  location?: string | null;
  size?: string | null;
  description?: string | null;
  notes?: string | null;
}

export interface CompanyFilters {
  search?: string;
  industry?: string;
  location?: string;
  size?: string;
  page?: number;
  pageSize?: number;
}
