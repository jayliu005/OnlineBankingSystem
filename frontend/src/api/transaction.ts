import { request } from '@/api/client'
import type {
  CashTransactionType,
  MoneyTransaction,
  MoneyTransactionRequest,
  TransactionHistoryRecord,
  TransferRequest,
} from '@/types/transaction'

export function createMoneyTransaction(
  transactionType: CashTransactionType,
  payload: MoneyTransactionRequest,
) {
  const path = transactionType === 'Deposit' ? 'deposits' : 'withdrawals'
  return request<MoneyTransaction>(`/api/transactions/${path}`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function createTransfer(payload: TransferRequest) {
  return request<MoneyTransaction>('/api/transactions/transfers', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function getTransactionHistory(accountId: number, startDate: string, endDate: string) {
  const params = new URLSearchParams({
    accountId: String(accountId),
    startDate,
    endDate,
  })
  return request<TransactionHistoryRecord[]>(`/api/transactions/history?${params.toString()}`)
}
