import { ApplicationStatus, ApplicationSummary } from './application.models';
import { InterviewStatus, InterviewType } from './interview.models';

export interface UpcomingInterview {
  id: number;
  applicationId: number;
  offerTitle: string;
  companyName: string;
  type: InterviewType;
  status: InterviewStatus;
  scheduledAt: string;
  location?: string | null;
}

export interface DashboardStats {
  totalApplications: number;
  totalOffers: number;
  totalCompanies: number;
  totalInterviews: number;
  applicationsByStatus: Record<ApplicationStatus, number>;
  recentApplications: ApplicationSummary[];
  upcomingInterviews: UpcomingInterview[];
}
