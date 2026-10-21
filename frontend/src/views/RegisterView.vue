<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { ApiError, checkUsernameAvailability } from '@/api/auth'
import LegacyBankingLayout from '@/components/LegacyBankingLayout.vue'
import { useAuthStore } from '@/stores/auth'
import type { RegisterRequest } from '@/types/auth'

const authStore = useAuthStore()
const router = useRouter()
const form = reactive<RegisterRequest>({
  userName: '',
  password: '',
  repeatPassword: '',
  firstName: '',
  lastName: '',
  middleInitial: '',
  gender: '',
  dateOfBirth: '',
  street: '',
  city: '',
  state: '',
  zip: '',
  phone: '',
  email: '',
})
const fieldErrors = ref<Record<string, string>>({})
const errorMessage = ref('')
const availabilityMessage = ref('')
const usernameAvailable = ref<boolean | null>(null)
const submitting = ref(false)

async function checkUserName() {
  availabilityMessage.value = ''
  usernameAvailable.value = null
  if (form.userName.length < 2 || form.userName.length > 30) {
    return
  }

  try {
    const result = await checkUsernameAvailability(form.userName)
    availabilityMessage.value = result.message
    usernameAvailable.value = result.available
  } catch {
    availabilityMessage.value = ''
  }
}

async function submit() {
  errorMessage.value = ''
  fieldErrors.value = {}

  if (form.password !== form.repeatPassword) {
    fieldErrors.value.repeatPassword = 'Passwords do not match'
    return
  }

  submitting.value = true
  try {
    await authStore.register(form)
    await router.push({ name: 'accounts-home' })
  } catch (error) {
    if (error instanceof ApiError) {
      errorMessage.value = error.message
      fieldErrors.value = error.fieldErrors
    } else {
      errorMessage.value = 'Unable to complete registration'
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <LegacyBankingLayout>
    <fieldset class="legacy-fieldset legacy-fieldset--register">
      <legend>Please Sign Up</legend>

      <p v-if="errorMessage" class="legacy-alert" role="alert">{{ errorMessage }}</p>

      <form class="legacy-form-grid" @submit.prevent="submit">
        <label class="legacy-form-label" for="register-user-name">
          User Name:<span class="required"> * </span>
        </label>
        <input
          id="register-user-name"
          v-model="form.userName"
          class="legacy-input"
          name="userName"
          minlength="2"
          maxlength="30"
          autocomplete="username"
          title="Please enter your user name!"
          required
          autofocus
          @keyup="checkUserName"
        />
        <span
          v-if="availabilityMessage || fieldErrors.userName"
          class="legacy-message"
          :class="{ 'legacy-message--success': usernameAvailable && !fieldErrors.userName }"
        >
          {{ fieldErrors.userName || availabilityMessage }}
        </span>
        <span v-else></span>

        <label class="legacy-form-label" for="register-password">
          Password:<span class="required"> * </span>
        </label>
        <input
          id="register-password"
          v-model="form.password"
          class="legacy-input"
          name="password"
          type="password"
          minlength="3"
          autocomplete="new-password"
          title="Please enter a password!"
          required
        />
        <span v-if="fieldErrors.password" class="legacy-message">{{ fieldErrors.password }}</span>
        <span v-else></span>

        <label class="legacy-form-label" for="repeat-password">
          Repeat Password:<span class="required"> * </span>
        </label>
        <input
          id="repeat-password"
          v-model="form.repeatPassword"
          class="legacy-input"
          name="repeatPassword"
          type="password"
          autocomplete="new-password"
          title="Please repeat the password!"
          required
        />
        <span v-if="fieldErrors.repeatPassword" class="legacy-message">
          {{ fieldErrors.repeatPassword }}
        </span>
        <span v-else></span>

        <label class="legacy-form-label" for="first-name">
          First Name:<span class="required"> * </span>
        </label>
        <input
          id="first-name"
          v-model="form.firstName"
          class="legacy-input"
          name="firstName"
          minlength="2"
          maxlength="50"
          title="Please enter your first name!"
          required
        />
        <span v-if="fieldErrors.firstName" class="legacy-message">{{ fieldErrors.firstName }}</span>
        <span v-else></span>

        <label class="legacy-form-label" for="last-name">
          Last Name:<span class="required"> * </span>
        </label>
        <input
          id="last-name"
          v-model="form.lastName"
          class="legacy-input"
          name="lastName"
          minlength="2"
          maxlength="50"
          title="Please enter your last name!"
          required
        />
        <span v-if="fieldErrors.lastName" class="legacy-message">{{ fieldErrors.lastName }}</span>
        <span v-else></span>

        <label class="legacy-form-label" for="middle-initial">Middle Initial:</label>
        <input
          id="middle-initial"
          v-model="form.middleInitial"
          class="legacy-input"
          name="middleInitial"
          maxlength="1"
          title="Please enter your Middle Initial!"
        />
        <span></span>

        <span class="legacy-form-label">Gender:<span class="required"> * </span></span>
        <span class="legacy-radio-group">
          <label
            ><input v-model="form.gender" name="gender" type="radio" value="M" required />
            Male</label
          >
          <label
            ><input v-model="form.gender" name="gender" type="radio" value="F" required />
            Female</label
          >
        </span>
        <span v-if="fieldErrors.gender" class="legacy-message">{{ fieldErrors.gender }}</span>
        <span v-else></span>

        <label class="legacy-form-label" for="date-of-birth">
          Date of Birth:<span class="required"> * </span>
        </label>
        <input
          id="date-of-birth"
          v-model="form.dateOfBirth"
          class="legacy-input"
          name="dateOfBirth"
          type="date"
          title="Please enter your birthday(yyyy-mm-dd)!"
          required
        />
        <span v-if="fieldErrors.dateOfBirth" class="legacy-message">
          {{ fieldErrors.dateOfBirth }}
        </span>
        <span v-else></span>

        <label class="legacy-form-label" for="street">
          Street:<span class="required"> * </span>
        </label>
        <input
          id="street"
          v-model="form.street"
          class="legacy-input"
          name="street"
          minlength="2"
          maxlength="100"
          title="Please enter the street you live in!"
          required
        />
        <span v-if="fieldErrors.street" class="legacy-message">{{ fieldErrors.street }}</span>
        <span v-else></span>

        <label class="legacy-form-label" for="city"> City:<span class="required"> * </span> </label>
        <input
          id="city"
          v-model="form.city"
          class="legacy-input"
          name="city"
          minlength="2"
          maxlength="40"
          title="Plese enter your city!"
          required
        />
        <span v-if="fieldErrors.city" class="legacy-message">{{ fieldErrors.city }}</span>
        <span v-else></span>

        <label class="legacy-form-label" for="state">
          State:<span class="required"> * </span>
        </label>
        <input
          id="state"
          v-model="form.state"
          class="legacy-input"
          name="state"
          minlength="2"
          maxlength="40"
          title="Please enter your state!"
          required
        />
        <span v-if="fieldErrors.state" class="legacy-message">{{ fieldErrors.state }}</span>
        <span v-else></span>

        <label class="legacy-form-label" for="zip">
          Zip Code:<span class="required"> * </span>
        </label>
        <input
          id="zip"
          v-model="form.zip"
          class="legacy-input"
          name="zip"
          maxlength="10"
          pattern="\d{5}(?:[-\s]\d{4})?"
          title="Please enter your zip code!"
          required
        />
        <span v-if="fieldErrors.zip" class="legacy-message">{{ fieldErrors.zip }}</span>
        <span v-else></span>

        <label class="legacy-form-label" for="phone">
          Phone:<span class="required"> * </span>
        </label>
        <input
          id="phone"
          v-model="form.phone"
          class="legacy-input"
          name="phone"
          type="tel"
          maxlength="20"
          pattern="(\+\d{1,2}\s)?\(?\d{3}\)?[-.\s]?\d{3}[-.\s]?\d{4}"
          title="Please enter your phone number!"
          required
        />
        <span v-if="fieldErrors.phone" class="legacy-message">{{ fieldErrors.phone }}</span>
        <span v-else></span>

        <label class="legacy-form-label" for="email">
          Email:<span class="required"> * </span>
        </label>
        <input
          id="email"
          v-model="form.email"
          class="legacy-input"
          name="email"
          type="email"
          maxlength="80"
          autocomplete="email"
          title="Please enter your email!"
          required
        />
        <span v-if="fieldErrors.email" class="legacy-message">{{ fieldErrors.email }}</span>
        <span v-else></span>

        <div class="legacy-form-actions">
          <RouterLink class="legacy-link-button" :to="{ name: 'login' }">
            <span aria-hidden="true">←</span>
            Back to Sign In
          </RouterLink>
          <button
            class="legacy-button"
            type="submit"
            :disabled="submitting || usernameAvailable === false"
          >
            <span aria-hidden="true">✓</span>
            {{ submitting ? 'Creating Account…' : 'Sign Up' }}
          </button>
        </div>
      </form>
    </fieldset>
  </LegacyBankingLayout>
</template>
