import { ref } from 'vue'
import { defineStore } from 'pinia'

import * as authApi from '@/api/auth'
import type { LoginRequest, RegisterRequest } from '@/types/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<Awaited<ReturnType<typeof authApi.getSession>> | null>(null)
  const sessionChecked = ref(false)

  async function login(payload: LoginRequest) {
    user.value = await authApi.login(payload)
  }

  async function register(payload: RegisterRequest) {
    user.value = await authApi.register(payload)
  }

  async function logout() {
    await authApi.logout()
    user.value = null
  }

  async function checkSession() {
    if (sessionChecked.value) {
      return
    }

    try {
      user.value = await authApi.getSession()
    } catch {
      user.value = null
    } finally {
      sessionChecked.value = true
    }
  }

  return { user, sessionChecked, login, register, logout, checkSession }
})
