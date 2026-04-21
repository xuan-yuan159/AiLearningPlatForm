import { computed, ref } from 'vue'

const TOKEN_KEY = 'teacher_token'
const PROFILE_KEY = 'teacher_profile'
const DEFAULT_AVATAR = 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=120&q=80'

function createDefaultProfile() {
  return {
    userId: '',
    username: '',
    nickname: '',
    role: '',
    avatar: DEFAULT_AVATAR,
  }
}

const token = ref(localStorage.getItem(TOKEN_KEY) || '')
const profile = ref(createDefaultProfile())

const persistedProfile = localStorage.getItem(PROFILE_KEY)
if (persistedProfile) {
  try {
    profile.value = {
      ...createDefaultProfile(),
      ...JSON.parse(persistedProfile),
    }
  } catch {
    profile.value = createDefaultProfile()
  }
}

const isAuthenticated = computed(() => Boolean(token.value))

function syncProfile() {
  localStorage.setItem(PROFILE_KEY, JSON.stringify(profile.value))
}

export function getToken() {
  return token.value
}

export function login(payload) {
  token.value = payload.token
  profile.value = {
    ...createDefaultProfile(),
    ...profile.value,
    ...payload,
    avatar: payload.avatar || profile.value.avatar || DEFAULT_AVATAR,
  }

  localStorage.setItem(TOKEN_KEY, token.value)
  syncProfile()
}

export function logout() {
  token.value = ''
  profile.value = createDefaultProfile()
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(PROFILE_KEY)
}

export function updateProfile(payload) {
  profile.value = { ...profile.value, ...payload }
  syncProfile()
}

export function useAuth() {
  return {
    token,
    profile,
    isAuthenticated,
    login,
    logout,
    updateProfile,
    getToken,
  }
}

