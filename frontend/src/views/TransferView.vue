<script setup lang="ts">
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { checkAccountExistence, getAccounts } from '@/api/account'
import { ApiError } from '@/api/client'
import { createTransfer } from '@/api/transaction'
import homeIcon from '@/assets/legacy/home.png'
import logoutIcon from '@/assets/legacy/logout.png'
import succeedIcon from '@/assets/legacy/succeed.png'
import LegacyBankingLayout from '@/components/LegacyBankingLayout.vue'
import { useAuthStore } from '@/stores/auth'
import type { BankAccount } from '@/types/account'
import type { MoneyTransaction } from '@/types/transaction'

const authStore = useAuthStore()
const router = useRouter()
const accounts = ref<BankAccount[]>([])
const completedTransaction = ref<MoneyTransaction | null>(null)
const fieldErrors = ref<Record<string, string>>({})
const errorMessage = ref('')
const destinationMessage = ref('')
const destinationExists = ref<boolean | null>(null)
const loadingAccounts = ref(true)
const submitting = ref(false)
const form = reactive({
  accountFromId: '' as number | '',
  accountToId: '',
  amount: '',
  securityPin: '',
})
let destinationTimer: ReturnType<typeof setTimeout> | undefined
let destinationRequest = 0

onMounted(loadAccounts)
onBeforeUnmount(() => clearTimeout(destinationTimer))

async function loadAccounts() {
  loadingAccounts.value = true
  errorMessage.value = ''
  try {
    accounts.value = await getAccounts()
  } catch (error) {
    errorMessage.value = error instanceof ApiError ? error.message : 'Unable to load bank accounts'
  } finally {
    loadingAccounts.value = false
  }
}

function scheduleDestinationCheck() {
  clearTimeout(destinationTimer)
  const requestId = ++destinationRequest
  destinationMessage.value = ''
  destinationExists.value = null
  if (!/^\d+$/.test(form.accountToId)) {
    return
  }
  destinationTimer = setTimeout(() => void checkDestination(requestId), 250)
}

async function checkDestination(requestId: number) {
  const accountId = Number(form.accountToId)
  try {
    const result = await checkAccountExistence(accountId)
    if (requestId === destinationRequest) {
      destinationMessage.value = result.message
      destinationExists.value = result.exists
    }
  } catch {
    if (requestId === destinationRequest) {
      destinationMessage.value = ''
      destinationExists.value = null
    }
  }
}

async function submit() {
  fieldErrors.value = {}
  errorMessage.value = ''

  if (form.accountFromId === '') {
    fieldErrors.value.accountFromId = 'Please choose a valid account'
    return
  }
  if (!/^\d+$/.test(form.accountToId)) {
    fieldErrors.value.accountToId = 'To Account must be a valid account id'
    return
  }

  submitting.value = true
  try {
    completedTransaction.value = await createTransfer({
      accountFromId: form.accountFromId,
      accountToId: Number(form.accountToId),
      amount: form.amount,
      securityPin: form.securityPin,
    })
    document.title = 'Online Banking Transaction'
  } catch (error) {
    if (error instanceof ApiError) {
      errorMessage.value = error.message
      fieldErrors.value = error.fieldErrors
    } else {
      errorMessage.value = 'Unable to complete transfer'
    }
  } finally {
    submitting.value = false
  }
}

function formatAmount(amount: number) {
  return Number(amount).toFixed(2)
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
    <fieldset v-if="completedTransaction" class="legacy-fieldset">
      <div class="account-created-title">
        <img :src="succeedIcon" alt="" />
        Money Transaction succeed!
      </div>

      <dl class="account-created-detail">
        <dt>Transaction Id:</dt>
        <dd>{{ completedTransaction.id }}</dd>
        <dt>Transaction Type:</dt>
        <dd>{{ completedTransaction.transactionType }}</dd>
        <dt>Amount of Money:</dt>
        <dd>{{ formatAmount(completedTransaction.transactionAmount) }}</dd>
        <dt>From Account Id:</dt>
        <dd>{{ completedTransaction.accountFromId }}</dd>
        <dt>To Account Id:</dt>
        <dd>{{ completedTransaction.accountToId }}</dd>
        <dt>Transaction Time:</dt>
        <dd>{{ formatDate(completedTransaction.transactionTime) }}</dd>
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
      <legend>Transfer Money</legend>

      <p v-if="errorMessage" class="legacy-alert" role="alert">{{ errorMessage }}</p>

      <form class="legacy-form-grid" @submit.prevent="submit">
        <label class="legacy-form-label" for="transfer-account-from">
          From Account:<span class="required"> * </span>
        </label>
        <select
          id="transfer-account-from"
          v-model.number="form.accountFromId"
          class="legacy-input legacy-select"
          name="accountFromId"
          required
          :disabled="loadingAccounts || accounts.length === 0"
          autofocus
        >
          <option value="" disabled>
            {{ loadingAccounts ? 'Loading accounts…' : 'Please select' }}
          </option>
          <option v-for="account in accounts" :key="account.id" :value="account.id">
            {{ account.id }}
          </option>
        </select>
        <span v-if="fieldErrors.accountFromId" class="legacy-message">
          {{ fieldErrors.accountFromId }}
        </span>
        <span v-else></span>

        <label class="legacy-form-label" for="transfer-account-to">
          To Account:<span class="required"> * </span>
        </label>
        <input
          id="transfer-account-to"
          v-model="form.accountToId"
          class="legacy-input"
          name="accountToId"
          type="text"
          inputmode="numeric"
          pattern="\d+"
          title="Please enter account Id to transfer!"
          required
          @keyup="scheduleDestinationCheck"
        />
        <span
          v-if="fieldErrors.accountToId || destinationMessage"
          class="legacy-message"
          :class="{ 'legacy-message--success': destinationExists && !fieldErrors.accountToId }"
        >
          {{ fieldErrors.accountToId || destinationMessage }}
        </span>
        <span v-else></span>

        <label class="legacy-form-label" for="transfer-amount">
          Amount to Transfer:<span class="required"> * </span>
        </label>
        <input
          id="transfer-amount"
          v-model="form.amount"
          class="legacy-input"
          name="amount"
          type="number"
          inputmode="decimal"
          min="0.01"
          max="10000.00"
          step="0.01"
          title="Plese enter amount of money to transfer!"
          required
        />
        <span v-if="fieldErrors.amount" class="legacy-message">{{ fieldErrors.amount }}</span>
        <span v-else></span>

        <label class="legacy-form-label" for="transfer-pin">
          Security Pin:<span class="required"> * </span>
        </label>
        <input
          id="transfer-pin"
          v-model="form.securityPin"
          class="legacy-input"
          name="securityPin"
          type="password"
          inputmode="numeric"
          autocomplete="current-password"
          title="Please enter Security Pin!"
          required
        />
        <span v-if="fieldErrors.securityPin" class="legacy-message">
          {{ fieldErrors.securityPin }}
        </span>
        <span v-else></span>

        <div class="legacy-form-actions">
          <RouterLink class="legacy-link-button" :to="{ name: 'accounts-home' }">
            <span aria-hidden="true">←</span>
            Cancel
          </RouterLink>
          <button
            class="legacy-button"
            type="submit"
            :disabled="submitting || loadingAccounts || accounts.length === 0"
          >
            <span aria-hidden="true">✓</span>
            {{ submitting ? 'Processing…' : 'Confirm Transfer' }}
          </button>
        </div>
      </form>

      <p v-if="!loadingAccounts && accounts.length === 0" class="legacy-empty-note">
        Please create a bank account before making a transaction.
      </p>
    </fieldset>
  </LegacyBankingLayout>
</template>
