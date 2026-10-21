<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { ApiError } from '@/api/client'
import { getUserProfile, updateUserProfile } from '@/api/user'
import homeIcon from '@/assets/legacy/home.png'
import logoutIcon from '@/assets/legacy/logout.png'
import succeedIcon from '@/assets/legacy/succeed.png'
import LegacyBankingLayout from '@/components/LegacyBankingLayout.vue'
import { useAuthStore } from '@/stores/auth'
import type { UserProfile } from '@/types/user'

const authStore = useAuthStore()
const router = useRouter()
const profile = ref<UserProfile | null>(null)
const updated = ref(false)
const loading = ref(true)
const submitting = ref(false)
const errorMessage = ref('')
const fieldErrors = ref<Record<string, string>>({})
const form = reactive({
  firstName: '',
  lastName: '',
  middleInitial: '',
  gender: '' as '' | 'M' | 'F',
  dateOfBirth: '',
  street: '',
  city: '',
  state: '',
  zip: '',
  phone: '',
  email: '',
})

onMounted(loadProfile)

async function loadProfile() {
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await getUserProfile()
    profile.value = result
    form.firstName = result.firstName
    form.lastName = result.lastName
    form.middleInitial = result.middleInitial ?? ''
    form.gender = result.gender
    form.dateOfBirth = result.dateOfBirth
    form.street = result.street
    form.city = result.city
    form.state = result.state
    form.zip = result.zip
    form.phone = result.phone
    form.email = result.email
  } catch (error) {
    errorMessage.value = error instanceof ApiError ? error.message : 'Unable to load user profile'
  } finally {
    loading.value = false
  }
}

async function submit() {
  errorMessage.value = ''
  fieldErrors.value = {}
  if (!form.gender) {
    fieldErrors.value.gender = 'Please select your gender!'
    return
  }
  submitting.value = true
  try {
    profile.value = await updateUserProfile({
      ...form,
      gender: form.gender,
      middleInitial: form.middleInitial.trim(),
    })
    updated.value = true
    document.title = 'Online Banking Profile Updated'
    await authStore.checkSession()
  } catch (error) {
    if (error instanceof ApiError) {
      errorMessage.value = error.message
      fieldErrors.value = error.fieldErrors
    } else {
      errorMessage.value = 'Unable to update user profile'
    }
  } finally {
    submitting.value = false
  }
}

function formatFullName() {
  if (!profile.value) return ''
  return [profile.value.firstName, profile.value.middleInitial, profile.value.lastName]
    .filter(Boolean)
    .join(' ')
}

async function logout() {
  await authStore.logout()
  await router.push({ name: 'login' })
}
</script>

<template>
  <LegacyBankingLayout>
    <fieldset v-if="updated && profile" class="legacy-fieldset legacy-fieldset--profile-success">
      <div class="account-created-title">
        <img :src="succeedIcon" alt="" />
        User Profile updated successfully!
      </div>
      <dl class="account-created-detail">
        <dt>Full Name:</dt>
        <dd>{{ formatFullName() }}</dd>
        <dt>Gender:</dt>
        <dd>{{ profile.gender }}</dd>
        <dt>Date of Birth:</dt>
        <dd>{{ profile.dateOfBirth }}</dd>
        <dt>Address:</dt>
        <dd>{{ profile.street }}, {{ profile.city }}, {{ profile.state }}, {{ profile.zip }}</dd>
        <dt>Phone:</dt>
        <dd>{{ profile.phone }}</dd>
        <dt>Email:</dt>
        <dd>{{ profile.email }}</dd>
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

    <fieldset v-else class="legacy-fieldset legacy-fieldset--profile">
      <legend>Please Edit Your Profile</legend>
      <p v-if="errorMessage" class="legacy-alert" role="alert">{{ errorMessage }}</p>
      <p v-if="loading" class="legacy-empty-note">Loading profile…</p>
      <form v-else class="legacy-form-grid profile-form-grid" @submit.prevent="submit">
        <label class="legacy-form-label" for="profile-first-name"
          >First Name:<span class="required"> * </span></label
        >
        <input
          id="profile-first-name"
          v-model="form.firstName"
          class="legacy-input"
          required
          minlength="2"
          maxlength="50"
        />
        <span v-if="fieldErrors.firstName" class="legacy-message">{{ fieldErrors.firstName }}</span
        ><span v-else></span>

        <label class="legacy-form-label" for="profile-last-name"
          >Last Name:<span class="required"> * </span></label
        >
        <input
          id="profile-last-name"
          v-model="form.lastName"
          class="legacy-input"
          required
          minlength="2"
          maxlength="50"
        />
        <span v-if="fieldErrors.lastName" class="legacy-message">{{ fieldErrors.lastName }}</span
        ><span v-else></span>

        <label class="legacy-form-label" for="profile-middle-initial">Middle Initial:</label>
        <input
          id="profile-middle-initial"
          v-model="form.middleInitial"
          class="legacy-input"
          maxlength="1"
        />
        <span v-if="fieldErrors.middleInitial" class="legacy-message">{{
          fieldErrors.middleInitial
        }}</span
        ><span v-else></span>

        <label class="legacy-form-label" for="profile-gender"
          >Gender:<span class="required"> * </span></label
        >
        <select
          id="profile-gender"
          v-model="form.gender"
          class="legacy-input legacy-select"
          required
        >
          <option value="" disabled>Please select</option>
          <option value="M">M</option>
          <option value="F">F</option>
        </select>
        <span v-if="fieldErrors.gender" class="legacy-message">{{ fieldErrors.gender }}</span
        ><span v-else></span>

        <label class="legacy-form-label" for="profile-date-of-birth"
          >Date of Birth:<span class="required"> * </span></label
        >
        <input
          id="profile-date-of-birth"
          v-model="form.dateOfBirth"
          class="legacy-input"
          type="date"
          required
        />
        <span v-if="fieldErrors.dateOfBirth" class="legacy-message">{{
          fieldErrors.dateOfBirth
        }}</span
        ><span v-else></span>

        <label class="legacy-form-label" for="profile-street"
          >Street:<span class="required"> * </span></label
        >
        <input
          id="profile-street"
          v-model="form.street"
          class="legacy-input"
          required
          minlength="2"
          maxlength="100"
        />
        <span v-if="fieldErrors.street" class="legacy-message">{{ fieldErrors.street }}</span
        ><span v-else></span>

        <label class="legacy-form-label" for="profile-city"
          >City:<span class="required"> * </span></label
        >
        <input
          id="profile-city"
          v-model="form.city"
          class="legacy-input"
          required
          minlength="2"
          maxlength="40"
        />
        <span v-if="fieldErrors.city" class="legacy-message">{{ fieldErrors.city }}</span
        ><span v-else></span>

        <label class="legacy-form-label" for="profile-state"
          >State:<span class="required"> * </span></label
        >
        <input
          id="profile-state"
          v-model="form.state"
          class="legacy-input"
          required
          minlength="2"
          maxlength="40"
        />
        <span v-if="fieldErrors.state" class="legacy-message">{{ fieldErrors.state }}</span
        ><span v-else></span>

        <label class="legacy-form-label" for="profile-zip"
          >Zip Code:<span class="required"> * </span></label
        >
        <input
          id="profile-zip"
          v-model="form.zip"
          class="legacy-input"
          required
          pattern="^\d{5}(?:[-\s]\d{4})?$"
        />
        <span v-if="fieldErrors.zip" class="legacy-message">{{ fieldErrors.zip }}</span
        ><span v-else></span>

        <label class="legacy-form-label" for="profile-phone"
          >Phone:<span class="required"> * </span></label
        >
        <input id="profile-phone" v-model="form.phone" class="legacy-input" required />
        <span v-if="fieldErrors.phone" class="legacy-message">{{ fieldErrors.phone }}</span
        ><span v-else></span>

        <label class="legacy-form-label" for="profile-email"
          >Email:<span class="required"> * </span></label
        >
        <input
          id="profile-email"
          v-model="form.email"
          class="legacy-input"
          type="email"
          required
          maxlength="80"
        />
        <span v-if="fieldErrors.email" class="legacy-message">{{ fieldErrors.email }}</span
        ><span v-else></span>

        <div class="legacy-form-actions">
          <RouterLink class="legacy-link-button" :to="{ name: 'accounts-home' }"
            ><span aria-hidden="true">←</span>Cancel</RouterLink
          >
          <button class="legacy-button" type="submit" :disabled="submitting">
            {{ submitting ? 'Updating…' : 'Update User Profile' }}
          </button>
        </div>
      </form>
    </fieldset>
  </LegacyBankingLayout>
</template>
