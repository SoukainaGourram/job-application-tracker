import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.development';
import {
  Interview,
  InterviewCreateRequest,
  InterviewStatusUpdateRequest,
  InterviewUpdateRequest,
} from '../models/interview.models';

@Injectable({ providedIn: 'root' })
export class InterviewService {
  private http = inject(HttpClient);

  private baseUrl(applicationId: number): string {
    return `${environment.apiUrl}/applications/${applicationId}/interviews`;
  }

  getInterviews(applicationId: number): Observable<Interview[]> {
    return this.http.get<Interview[]>(this.baseUrl(applicationId));
  }

  getInterview(applicationId: number, interviewId: number): Observable<Interview> {
    return this.http.get<Interview>(`${this.baseUrl(applicationId)}/${interviewId}`);
  }

  createInterview(applicationId: number, request: InterviewCreateRequest): Observable<Interview> {
    return this.http.post<Interview>(this.baseUrl(applicationId), request);
  }

  updateInterview(applicationId: number, interviewId: number, request: InterviewUpdateRequest): Observable<Interview> {
    return this.http.put<Interview>(`${this.baseUrl(applicationId)}/${interviewId}`, request);
  }

  updateStatus(applicationId: number, interviewId: number, request: InterviewStatusUpdateRequest): Observable<Interview> {
    return this.http.patch<Interview>(`${this.baseUrl(applicationId)}/${interviewId}/status`, request);
  }

  deleteInterview(applicationId: number, interviewId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl(applicationId)}/${interviewId}`);
  }
}
