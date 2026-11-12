import axios from 'axios'
import { ElMessage } from 'element-plus'
const request = axios.create({ baseURL: '/api', timeout: 15000 })
request.interceptors.request.use(cfg => {
  const token = localStorage.getItem('pet_token')
  if (token) cfg.headers.Authorization = 'Bearer ' + token
  return cfg
})
request.interceptors.response.use(
  res => {
    const data = res.data
    if (data && typeof data.code === 'number' && data.code !== 200) {
      ElMessage.error(data.msg || '请求失败')
      return Promise.reject(new Error(data.msg || '请求失败'))
    }
    return data
  },
  err => {
    if (err.response && err.response.status === 403) ElMessage.error('无权访问')
    else if (err.response && err.response.status === 401) {
      // 会话过期：按所在端分流——后台回管理端登录，前台回客人登录（带回跳）；游客公开页不跳转
      let wasStaff = false
      try { const u = JSON.parse(localStorage.getItem('pet_user') || 'null'); wasStaff = !!u && u.role !== 'OWNER' } catch (e) {}
      localStorage.removeItem('pet_token'); localStorage.removeItem('pet_user')
      const path = (location.hash || '').replace(/^#/, '').split('?')[0]
      const guestPaths = ['', '/', '/home', '/shop', '/sitters', '/spaces', '/login', '/register', '/admin/login']
      const staffPaths = ['/dashboard', '/users', '/hall', '/services']
      if (!guestPaths.includes(path)) {
        if (wasStaff || staffPaths.includes(path)) location.hash = '#/admin/login'
        else location.hash = '#/login?redirect=' + encodeURIComponent(path)
      }
    }
    else ElMessage.error(err.message || '网络错误')
    return Promise.reject(err)
  }
)
export default request
