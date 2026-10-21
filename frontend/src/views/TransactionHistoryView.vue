<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { getAccounts } from '@/api/account'
import { ApiError } from '@/api/client'
import { getTransactionHistory } from '@/api/transaction'
import homeIcon from '@/assets/legacy/home.png'
import logoutIcon from '@/assets/legacy/logout.png'
import LegacyBankingLayout from '@/components/LegacyBankingLayout.vue'
import { useAuthStore } from '@/stores/auth'
import type { BankAccount } from '@/types/account'
import type { TransactionHistoryRecord } from '@/types/transaction'

const authStore = useAuthStore()
const router = useRouter()
const accounts = ref<BankAccount[]>([])
const records = ref<TransactionHistoryRecord[] | null>(null)
const loadingAccounts = ref(true)
const loadingRecords = ref(false)
const errorMessage = ref('')
const fieldErrors = ref<Record<string, string>>({})
const form = ref({
  accountId: '' as number | '',
  startDate: '',
  endDate: '',
})

const canSubmit = computed(
  () =>
    form.value.accountId !== '' &&
    form.value.startDate !== '' &&
    form.value.endDate !== '' &&
    form.value.startDate <= form.value.endDate,
)

onMounted(loadAccounts)

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

async function submit() {
  fieldErrors.value = {}
  errorMessage.value = ''
  records.value = null

  if (form.value.accountId === '') {
    fieldErrors.value.accountId = 'Please choose a valid account'
  }
  if (!form.value.startDate) {
    fieldErrors.value.startDate = 'Start date is required'
  }
  if (!form.value.endDate) {
    fieldErrors.value.endDate = 'End date is required'
  }
  if (form.value.startDate && form.value.endDate && form.value.startDate > form.value.endDate) {
    errorMessage.value = 'Start date must not be after end date'
  }
  if (Object.keys(fieldErrors.value).length > 0 || errorMessage.value) return

  loadingRecords.value = true
  try {
    records.value = await getTransactionHistory(
      form.value.accountId as number,
      form.value.startDate,
      form.value.endDate,
    )
    document.title = 'Online Banking History'
  } catch (error) {
    if (error instanceof ApiError) {
      errorMessage.value = error.message
      fieldErrors.value = error.fieldErrors
    } else {
      errorMessage.value = 'Unable to load transaction history'
    }
  } finally {
    loadingRecords.value = false
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
    <fieldset v-if="records" class="legacy-fieldset legacy-fieldset--history-results">
      <div class="history-results-title">
        Transaction History for Account ID {{ form.accountId }}
      </div>
      <table class="account-table history-table">
        <caption>
          Records from
          {{
            form.startDate
          }}
          to
          {{
            form.endDate
          }}
        </caption>
        <thead>
          <tr>
            <th scope="col">Transaction Id</th>
            <th scope="col">Transaction Time</th>
            <th scope="col">Transaction Amount</th>
            <th scope="col">Transaction Note</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="records.length === 0">
            <td class="account-table-empty" colspan="4">No records found.</td>
          </tr>
          <tr v-for="record in records" v-else :key="record.id">
            <td>{{ record.id }}</td>
            <td>{{ formatDate(record.transactionTime) }}</td>
            <td>{{ formatAmount(record.transactionAmount) }}</td>
            <td>{{ record.transactionNote }}</td>
          </tr>
        </tbody>
      </table>
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

    <fieldset v-else class="legacy-fieldset legacy-fieldset--history">
      <legend>Transaction History</legend>
      <p v-if="errorMessage" class="legacy-alert" role="alert">{{ errorMessage }}</p>
      <form class="legacy-form-grid" @submit.prevent="submit">
        <label class="legacy-form-label" for="history-account">
          Select Account:<span class="required"> * </span>
        </label>
        <select
          id="history-account"
          v-model.number="form.accountId"
          class="legacy-input legacy-select"
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
        <span v-if="fieldErrors.accountId" class="legacy-message">{{ fieldErrors.accountId }}</span>
        <span v-else></span>

        <label class="legacy-form-label" for="history-start-date">
          Start Date:<span class="required"> * </span>
        </label>
        <input
          id="history-start-date"
          v-model="form.startDate"
          class="legacy-input"
          type="date"
          required
        />
        <span v-if="fieldErrors.startDate" class="legacy-message">{{ fieldErrors.startDate }}</span>
        <span v-else></span>

        <label class="legacy-form-label" for="history-end-date">
          End Date:<span class="required"> * </span>
        </label>
        <input
          id="history-end-date"
          v-model="form.endDate"
          class="legacy-input"
          type="date"
          required
        />
        <span v-if="fieldErrors.endDate" class="legacy-message">{{ fieldErrors.endDate }}</span>
        <span v-else></span>

        <div class="legacy-form-actions">
          <RouterLink class="legacy-link-button" :to="{ name: 'accounts-home' }">
            <span aria-hidden="true">←</span>
            Cancel
          </RouterLink>
          <button
            class="legacy-button"
            type="submit"
            :disabled="loadingRecords || loadingAccounts || accounts.length === 0 || !canSubmit"
          >
            <span aria-hidden="true">✓</span>
            {{ loadingRecords ? 'Loading…' : 'View History' }}
          </button>
        </div>
      </form>
      <p v-if="!loadingAccounts && accounts.length === 0" class="legacy-empty-note">
        Please create a bank account before viewing transaction history.
      </p>
    </fieldset>
  </LegacyBankingLayout>
</template>
