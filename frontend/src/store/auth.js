import { defineStore } from 'pinia'
import request from '../utils/request'
export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('pet_token') || '',
    user: JSON.parse(localStorage.getItem('pet_user') || 'null')
  }),
  getters: {
    role: s => (s.user ? s.user.role : ''),
    isAdmin: s => s.user && s.user.role === 'ADMIN',
    isOwner: s => s.user && s.user.role === 'OWNER',
    isSitter: s => s.user && s.user.role === 'SITTER'
  },
  actions: {
    async login(username, password) {
      const res = await request.post('/auth/login', { username, password })
      this.token = res.data.token; this.user = res.data.user
      localStorage.setItem('pet_token', this.token)
      localStorage.setItem('pet_user', JSON.stringify(this.user))
      return this.user
    },
    logout() {
      this.token = ''; this.user = null
      localStorage.removeItem('pet_token'); localStorage.removeItem('pet_user')
    }
  }
})
