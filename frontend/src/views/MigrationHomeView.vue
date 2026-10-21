<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { getAccounts } from '@/api/account'
import { ApiError } from '@/api/client'
import addAccountIcon from '@/assets/legacy/addfile.png'
import depositIcon from '@/assets/legacy/deposit.png'
import historyIcon from '@/assets/legacy/history.png'
import logoutIcon from '@/assets/legacy/logout.png'
import transferIcon from '@/assets/legacy/transfer.png'
import userInfoIcon from '@/assets/legacy/userinfo.png'
import withdrawIcon from '@/assets/legacy/withdraw.png'
import LegacyBankingLayout from '@/components/LegacyBankingLayout.vue'
import { useAuthStore } from '@/stores/auth'
import type { BankAccount } from '@/types/account'

const authStore = useAuthStore()
const router = useRouter()
const accounts = ref<BankAccount[]>([])
const loadingAccounts = ref(true)
const accountError = ref('')
const menuItems = [
  { label: 'New Account', icon: addAccountIcon, routeName: 'new-account' },
  { label: 'Withdraw Money', icon: withdrawIcon, routeName: 'withdraw' },
  { label: 'Deposit Money', icon: depositIcon, routeName: 'deposit' },
  { label: 'Transfer Money', icon: transferIcon, routeName: 'transfer' },
  { label: 'Transaction History', icon: historyIcon, routeName: 'history' },
]

onMounted(loadAccounts)

async function loadAccounts() {
  loadingAccounts.value = true
  accountError.value = ''
  try {
    accounts.value = await getAccounts()
  } catch (error) {
    accountError.value = error instanceof ApiError ? error.message : 'Unable to load bank accounts'
  } finally {
    loadingAccounts.value = false
  }
}

function openMenuItem(routeName?: string) {
  if (routeName) {
    void router.push({ name: routeName })
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
    <fieldset class="legacy-fieldset legacy-fieldset--account">
      <div class="account-north">
        <span class="account-greeting">
          {{ authStore.user?.firstName }}, welcome to your account!
        </span>
        <span class="account-user-actions">
          <button class="account-user-action" type="button" @click="openMenuItem('profile')">
            <img :src="userInfoIcon" alt="" />
            User Profile
          </button>
          <button class="account-user-action" type="button" @click="logout">
            <img :src="logoutIcon" alt="" />
            Logout
          </button>
        </span>
      </div>

      <div class="account-layout">
        <nav class="account-menu" aria-label="Bank account functions">
          <button
            v-for="item in menuItems"
            :key="item.label"
            class="account-menu-button"
            type="button"
            :disabled="!item.routeName"
            :title="
              item.routeName ? undefined : 'This function will be enabled in its migration stage.'
            "
            @click="openMenuItem(item.routeName)"
          >
            <img :src="item.icon" alt="" />
            {{ item.label }}
          </button>
        </nav>

        <section class="account-main">
          <p v-if="accountError" class="legacy-alert account-load-error" role="alert">
            {{ accountError }}
            <button class="account-retry" type="button" @click="loadAccounts">Retry</button>
          </p>

          <table class="account-table">
            <caption>
              Bank Account Summary
            </caption>
            <thead>
              <tr>
                <th scope="col">Account Id</th>
                <th scope="col">Account Type</th>
                <th scope="col">Current Balance</th>
                <th scope="col">Time of Created</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loadingAccounts">
                <td class="account-table-empty" colspan="4">Loading accounts…</td>
              </tr>
              <tr v-else-if="!accountError && accounts.length === 0">
                <td class="account-table-empty" colspan="4">No records found.</td>
              </tr>
              <tr v-for="account in accounts" v-else :key="account.id">
                <td>{{ account.id }}</td>
                <td>{{ account.accountType }}</td>
                <td>{{ formatBalance(account.accountBalance) }}</td>
                <td>{{ formatDate(account.dateOfCreated) }}</td>
              </tr>
            </tbody>
          </table>
        </section>
      </div>
    </fieldset>
  </LegacyBankingLayout>
</template>
