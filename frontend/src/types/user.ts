export interface UserProfile {
  id: number
  userName: string
  firstName: string
  lastName: string
  middleInitial: string | null
  gender: 'M' | 'F'
  dateOfBirth: string
  street: string
  city: string
  state: string
  zip: string
  phone: string
  email: string
}

export type UpdateUserProfileRequest = Omit<UserProfile, 'id' | 'userName' | 'middleInitial'> & {
  middleInitial: string
}
