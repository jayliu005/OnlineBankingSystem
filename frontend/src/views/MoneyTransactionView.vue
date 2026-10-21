<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { getAccounts } from '@/api/account'
import { ApiError } from '@/api/client'
import { createMoneyTransaction } from '@/api/transaction'
import homeIcon from '@/assets/legacy/home.png'
import logoutIcon from '@/assets/legacy/logout.png'
import succeedIcon from '@/assets/legacy/succeed.png'
import LegacyBankingLayout from '@/components/LegacyBankingLayout.vue'
import { useAuthStore } from '@/stores/auth'
import type { BankAccount } from '@/types/account'
import type { CashTransactionType, MoneyTransaction } from '@/types/transaction'

const props = defineProps<{
  transactionType: CashTransactionType
}>()

const authStore = useAuthStore()
const router = useRouter()
const accounts = ref<BankAccount[]>([])
const completedTransaction = ref<MoneyTransaction | null>(null)
const fieldErrors = ref<Record<string, string>>({})
const errorMessage = ref('')
const loadingAccounts = ref(true)
const submitting = ref(false)
const form = reactive({
  accountId: '' as number | '',
  amount: '',
  securityPin: '',
})

const isDeposit = computed(() => props.transactionType === 'Deposit')
const amountLabel = computed(() => (isDeposit.value ? 'Amount to Deposit:' : 'Amount to Withdraw:'))
const maximumAmount = computed(() => (isDeposit.value ? '50000.00' : '2000.00'))
const confirmLabel = computed(() => (isDeposit.value ? 'Confirm Deposit' : 'Confirm Withdraw'))

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

  if (form.accountId === '') {
    fieldErrors.value.accountId = 'Please choose a valid account'
    return
  }

  submitting.value = true
  try {
    completedTransaction.value = await createMoneyTransaction(props.transactionType, {
      accountId: form.accountId,
      amount: form.amount,
      securityPin: form.securityPin,
    })
    document.title = 'Online Banking Transaction'
  } catch (error) {
    if (error instanceof ApiError) {
      errorMessage.value = error.message
      fieldErrors.value = error.fieldErrors
    } else {
      errorMessage.value = `Unable to complete ${props.transactionType.toLowerCase()}`
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
        <dd>{{ completedTransaction.accountToId ?? 'N/A' }}</dd>
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
      <legend>{{ transactionType }} Money</legend>

      <p v-if="errorMessage" class="legacy-alert" role="alert">{{ errorMessage }}</p>

      <form class="legacy-form-grid" @submit.prevent="submit">
        <label class="legacy-form-label" for="transaction-account">
          Select Account:<span class="required"> * </span>
        </label>
        <select
          id="transaction-account"
          v-model.number="form.accountId"
          class="legacy-input legacy-select"
          name="accountId"
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
        <span v-if="fieldErrors.accountId" class="legacy-message">
          {{ fieldErrors.accountId }}
        </span>
        <span v-else></span>

        <label class="legacy-form-label" for="transaction-amount">
          {{ amountLabel }}<span class="required"> * </span>
        </label>
        <input
          id="transaction-amount"
          v-model="form.amount"
          class="legacy-input"
          name="amount"
          type="number"
          inputmode="decimal"
          min="0.01"
          :max="maximumAmount"
          step="0.01"
          :title="`Plese enter amount of money to ${transactionType.toLowerCase()}!`"
          required
        />
        <span v-if="fieldErrors.amount" class="legacy-message">{{ fieldErrors.amount }}</span>
        <span v-else></span>

        <label class="legacy-form-label" for="transaction-pin">
          Security Pin:<span class="required"> * </span>
        </label>
        <input
          id="transaction-pin"
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
            {{ submitting ? 'Processing…' : confirmLabel }}
          </button>
        </div>
      </form>

      <p v-if="!loadingAccounts && accounts.length === 0" class="legacy-empty-note">
        Please create a bank account before making a transaction.
      </p>
    </fieldset>
  </LegacyBankingLayout>
</template>
