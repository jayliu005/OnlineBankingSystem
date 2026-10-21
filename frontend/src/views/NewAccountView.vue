<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { createAccount } from '@/api/account'
import { ApiError } from '@/api/client'
import homeIcon from '@/assets/legacy/home.png'
import logoutIcon from '@/assets/legacy/logout.png'
import succeedIcon from '@/assets/legacy/succeed.png'
import LegacyBankingLayout from '@/components/LegacyBankingLayout.vue'
import { useAuthStore } from '@/stores/auth'
import type { BankAccount, CreateAccountRequest } from '@/types/account'

const authStore = useAuthStore()
const router = useRouter()
const form = reactive<CreateAccountRequest>({
  accountType: '',
  securityPin: '',
  repeatSecurityPin: '',
})
const createdAccount = ref<BankAccount | null>(null)
const fieldErrors = ref<Record<string, string>>({})
const errorMessage = ref('')
const submitting = ref(false)

async function submit() {
  fieldErrors.value = {}
  errorMessage.value = ''

  if (form.securityPin !== form.repeatSecurityPin) {
    fieldErrors.value.repeatSecurityPin = 'Security Pins do not match'
    return
  }

  submitting.value = true
  try {
    createdAccount.value = await createAccount(form)
    document.title = 'Online Banking New Account'
  } catch (error) {
    if (error instanceof ApiError) {
      errorMessage.value = error.message
      fieldErrors.value = error.fieldErrors
    } else {
      errorMessage.value = 'Unable to create bank account'
    }
  } finally {
    submitting.value = false
  }
}

function formatBalance(balance: number) {
  return Number(balance).toFixed(2)
}

function formatDate(date: string) {
  return date.replace('T', ' ').slice(0, 19)
}

async function logout() {
  await authStore.logout()
  await router.push({ name: 'login' })
}
</script>

<template>
  <LegacyBankingLayout>
    <fieldset v-if="createdAccount" class="legacy-fieldset">
      <div class="account-created-title">
        <img :src="succeedIcon" alt="" />
        New Bank Account Created successfully!
      </div>

      <dl class="account-created-detail">
        <dt>Account Id:</dt>
        <dd>{{ createdAccount.id }}</dd>
        <dt>Account Type:</dt>
        <dd>{{ createdAccount.accountType }}</dd>
        <dt>Current Balance:</dt>
        <dd>{{ formatBalance(createdAccount.accountBalance) }}</dd>
        <dt>Date of Created:</dt>
        <dd>{{ formatDate(createdAccount.dateOfCreated) }}</dd>
      </dl>

      <div class="account-created-actions">
        <RouterLink class="legacy-link-button" :to="{ name: 'accounts-home' }">
          <img :src="homeIcon" alt="" />
          Back to Home
        </RouterLink>
        <button class="legacy-button" type="button" @click="logout">
          <img :src="logoutIcon" alt="" />
          Logout
        </button>
      </div>
    </fieldset>

    <fieldset v-else class="legacy-fieldset">
      <legend>New Account Detail</legend>

      <p v-if="errorMessage" class="legacy-alert" role="alert">{{ errorMessage }}</p>

      <form class="legacy-form-grid" @submit.prevent="submit">
        <span class="legacy-form-label"> Account Type:<span class="required"> * </span> </span>
        <span class="legacy-radio-group">
          <label>
            <input
              v-model="form.accountType"
              name="accountType"
              type="radio"
              value="Checking"
              required
            />
            Checking
          </label>
          <label>
            <input
              v-model="form.accountType"
              name="accountType"
              type="radio"
              value="Saving"
              required
            />
            Saving
          </label>
        </span>
        <span v-if="fieldErrors.accountType" class="legacy-message">
          {{ fieldErrors.accountType }}
        </span>
        <span v-else></span>

        <label class="legacy-form-label" for="security-pin">
          Security Pin:<span class="required"> * </span>
        </label>
        <input
          id="security-pin"
          v-model="form.securityPin"
          class="legacy-input"
          name="securityPin"
          type="password"
          inputmode="numeric"
          pattern="\d{4}"
          maxlength="4"
          autocomplete="new-password"
          title="Please enter a 4-digit pin!"
          required
          autofocus
        />
        <span v-if="fieldErrors.securityPin" class="legacy-message">
          {{ fieldErrors.securityPin }}
        </span>
        <span v-else></span>

        <label class="legacy-form-label" for="repeat-security-pin">
          Repeat Security Pin:<span class="required"> * </span>
        </label>
        <input
          id="repeat-security-pin"
          v-model="form.repeatSecurityPin"
          class="legacy-input"
          name="repeatSecurityPin"
          type="password"
          inputmode="numeric"
          maxlength="4"
          autocomplete="new-password"
          title="Please repeat the pin!"
          required
        />
        <span v-if="fieldErrors.repeatSecurityPin" class="legacy-message">
          {{ fieldErrors.repeatSecurityPin }}
        </span>
        <span v-else></span>

        <div class="legacy-form-actions">
          <RouterLink class="legacy-link-button" :to="{ name: 'accounts-home' }">
            <span aria-hidden="true">←</span>
            Cancel
          </RouterLink>
          <button class="legacy-button" type="submit" :disabled="submitting">
            <span aria-hidden="true">✓</span>
            {{ submitting ? 'Creating Account…' : 'Create Account' }}
          </button>
        </div>
      </form>
    </fieldset>
  </LegacyBankingLayout>
</template>
