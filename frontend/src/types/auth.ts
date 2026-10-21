export interface AuthUser {
  id: number
  userName: string
  firstName: string
  lastName: string
}

export interface LoginRequest {
  userName: string
  password: string
}

export interface RegisterRequest {
  userName: string
  password: string
  repeatPassword: string
  firstName: string
  lastName: string
  middleInitial: string
  gender: 'M' | 'F' | ''
  dateOfBirth: string
  street: string
  city: string
  state: string
  zip: string
  phone: string
  email: string
}

export interface UsernameAvailability {
  available: boolean
  message: string
}
