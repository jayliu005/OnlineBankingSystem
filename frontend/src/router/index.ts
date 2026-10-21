import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import MigrationHomeView from '@/views/MigrationHomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'accounts-home',
      component: MigrationHomeView,
      meta: { requiresAuth: true, title: 'Online Banking Account Home' },
    },
    {
      path: '/accounts/new',
      name: 'new-account',
      component: () => import('@/views/NewAccountView.vue'),
      meta: { requiresAuth: true, title: 'Online Banking Add New Account' },
    },
    {
      path: '/accounts/deposit',
      name: 'deposit',
      component: () => import('@/views/MoneyTransactionView.vue'),
      props: { transactionType: 'Deposit' },
      meta: { requiresAuth: true, title: 'Online Banking Deposit' },
    },
    {
      path: '/accounts/withdraw',
      name: 'withdraw',
      component: () => import('@/views/MoneyTransactionView.vue'),
      props: { transactionType: 'Withdraw' },
      meta: { requiresAuth: true, title: 'Online Banking Withdraw' },
    },
    {
      path: '/accounts/transfer',
      name: 'transfer',
      component: () => import('@/views/TransferView.vue'),
      meta: { requiresAuth: true, title: 'Online Banking Transfer' },
    },
    {
      path: '/accounts/history',
      name: 'history',
      component: () => import('@/views/TransactionHistoryView.vue'),
      meta: { requiresAuth: true, title: 'Online Banking History' },
    },
    {
      path: '/user/profile',
      name: 'profile',
      component: () => import('@/views/UserProfileView.vue'),
      meta: { requiresAuth: true, title: 'Online Banking Edit Profile' },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { guestOnly: true, title: 'Online Banking Sign In' },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { guestOnly: true, title: 'Online Banking Sign Up' },
    },
  ],
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()
  await authStore.checkSession()

  if (to.meta.requiresAuth && !authStore.user) {
    return { name: 'login' }
  }
  if (to.meta.guestOnly && authStore.user) {
    return { name: 'accounts-home' }
  }
})

router.afterEach((to) => {
  document.title = typeof to.meta.title === 'string' ? to.meta.title : 'OnlineBankingSystem'
})

export default router
