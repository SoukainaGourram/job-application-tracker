export interface Profile {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
}

export interface UpdateProfileRequest {
  firstName: string;
  lastName: string;
}
