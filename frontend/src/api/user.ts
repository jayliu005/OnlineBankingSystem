import { request } from '@/api/client'
import type { UpdateUserProfileRequest, UserProfile } from '@/types/user'

export function getUserProfile() {
  return request<UserProfile>('/api/user/profile')
}

export function updateUserProfile(payload: UpdateUserProfileRequest) {
  return request<UserProfile>('/api/user/profile', {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}
