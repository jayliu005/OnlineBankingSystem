import { request } from '@/api/client'
import type { AccountExistence, BankAccount, CreateAccountRequest } from '@/types/account'

export function getAccounts() {
  return request<BankAccount[]>('/api/accounts')
}

export function createAccount(payload: CreateAccountRequest) {
  return request<BankAccount>('/api/accounts', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function checkAccountExistence(accountId: number) {
  return request<AccountExistence>(`/api/accounts/${accountId}/existence`)
}
