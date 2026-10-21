export type MoneyTransactionType = 'Deposit' | 'Withdraw' | 'Transfer'
export type CashTransactionType = Exclude<MoneyTransactionType, 'Transfer'>

export interface MoneyTransactionRequest {
  accountId: number
  amount: string
  securityPin: string
}

export interface MoneyTransaction {
  id: number
  transactionType: MoneyTransactionType
  transactionAmount: number
  accountFromId: number
  accountToId: number | null
  transactionTime: string
}

export interface TransferRequest {
  accountFromId: number
  accountToId: number
  amount: string
  securityPin: string
}

export interface TransactionHistoryRecord {
  id: number
  transactionTime: string
  transactionAmount: number
  transactionNote: string
}
