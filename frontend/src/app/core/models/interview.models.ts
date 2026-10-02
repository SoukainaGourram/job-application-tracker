export type InterviewType = 'HR' | 'TECHNICAL' | 'MANAGERIAL' | 'BEHAVIORAL' | 'FINAL' | 'OTHER';
export type InterviewStatus = 'SCHEDULED' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW';

export interface Interview {
  id: number;
  applicationId: number;
  type: InterviewType;
  status: InterviewStatus;
  scheduledAt: string; // ISO 8601 Instant
  endedAt?: string | null;
  location?: string | null;
  interviewerName?: string | null;
  notes?: string | null;
  feedback?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface InterviewCreateRequest {
  type: InterviewType;
  scheduledAt: string; // ISO 8601 Instant
  endedAt?: string | null;
  location?: string | null;
  interviewerName?: string | null;
  notes?: string | null;
}

export interface InterviewUpdateRequest {
  type?: InterviewType;
  scheduledAt?: string;
  endedAt?: string | null;
  location?: string | null;
  interviewerName?: string | null;
  notes?: string | null;
  feedback?: string | null;
}

export interface InterviewStatusUpdateRequest {
  status: InterviewStatus;
  notes?: string | null;
}

/** Entretien enrichi du contexte candidature (page globale /interviews). */
export interface InterviewWithApplicationContext extends Interview {
  offerTitle: string;
  companyName: string;
}

export const INTERVIEW_TYPE_LABELS: Record<InterviewType, string> = {
  HR: 'RH / Screening',
  TECHNICAL: 'Technique',
  MANAGERIAL: 'Manager',
  BEHAVIORAL: 'Comportemental',
  FINAL: 'Final',
  OTHER: 'Autre',
};

export const INTERVIEW_STATUS_LABELS: Record<InterviewStatus, string> = {
  SCHEDULED: 'Planifié',
  COMPLETED: 'Réalisé',
  CANCELLED: 'Annulé',
  NO_SHOW: 'Absent',
};
