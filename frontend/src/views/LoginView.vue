<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { ApiError } from '@/api/auth'
import LegacyBankingLayout from '@/components/LegacyBankingLayout.vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const form = reactive({
  userName: '',
  password: '',
})
const errorMessage = ref('')
const submitting = ref(false)

async function submit() {
  errorMessage.value = ''
  submitting.value = true

  try {
    await authStore.login(form)
    await router.push({ name: 'accounts-home' })
  } catch (error) {
    errorMessage.value = error instanceof ApiError ? error.message : 'Unable to sign in'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <LegacyBankingLayout>
    <fieldset class="legacy-fieldset legacy-fieldset--login">
      <legend>Please Sign In</legend>

      <p v-if="errorMessage" class="legacy-alert" role="alert">{{ errorMessage }}</p>

      <form class="legacy-form-grid" @submit.prevent="submit">
        <label class="legacy-form-label" for="login-user-name">
          User Name:<span class="required"> * </span>
        </label>
        <input
          id="login-user-name"
          v-model="form.userName"
          class="legacy-input"
          name="userName"
          autocomplete="username"
          title="Please enter your user name!"
          required
          autofocus
        />
        <span></span>

        <label class="legacy-form-label" for="login-password">
          Password:<span class="required"> * </span>
        </label>
        <input
          id="login-password"
          v-model="form.password"
          class="legacy-input"
          name="password"
          type="password"
          autocomplete="current-password"
          title="Please enter the password!"
          required
        />
        <span></span>

        <div class="legacy-login-actions">
          <button class="legacy-button" type="submit" :disabled="submitting">
            <span aria-hidden="true">▣</span>
            {{ submitting ? 'Signing In…' : 'Sign In' }}
          </button>
          <span class="legacy-auth-switch">
            <strong>New user?</strong>
            <RouterLink :to="{ name: 'register' }"> Please Sign Up</RouterLink>
          </span>
        </div>
      </form>
    </fieldset>
  </LegacyBankingLayout>
</template>
