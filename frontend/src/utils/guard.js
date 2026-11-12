import { ElMessage } from 'element-plus'
// 是否已登录
export const isLoggedIn = () => !!localStorage.getItem('pet_token')
// 需要身份的动作统一入口：未登录时跳客人登录页并带 redirect，登录后自动回跳
export function ensureLogin(router, target) {
  if (isLoggedIn()) return true
  ElMessage.info('登录后即可继续，正在为你跳转登录页')
  router.push({ path: '/login', query: { redirect: target || router.currentRoute.value.fullPath } })
  return false
}
