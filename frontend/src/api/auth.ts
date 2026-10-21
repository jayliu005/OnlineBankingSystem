import type { AuthUser, LoginRequest, RegisterRequest, UsernameAvailability } from '@/types/auth'

import { request } from '@/api/client'

export { ApiError } from '@/api/client'

export function login(payload: LoginRequest) {
  return request<AuthUser>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function register(payload: RegisterRequest) {
  return request<AuthUser>('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function logout() {
  return request<void>('/api/auth/logout', { method: 'POST' })
}

export function getSession() {
  return request<AuthUser>('/api/auth/session')
}

export function checkUsernameAvailability(userName: string) {
  const query = new URLSearchParams({ userName })
  return request<UsernameAvailability>(`/api/auth/username-availability?${query}`)
}
