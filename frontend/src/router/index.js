import { createRouter, createWebHashHistory } from 'vue-router'
const routes = [
  // 客人入口（前台风格）：宠主登录/注册
  { path: '/login', component: () => import('../views/Login.vue'), meta: { public: true, guestEntry: true } },
  { path: '/register', component: () => import('../views/Register.vue'), meta: { public: true, guestEntry: true } },
  // 管理端入口（后台风格）：管理员/宠托师登录，无注册
  { path: '/admin/login', component: () => import('../views/AdminLogin.vue'), meta: { public: true, staffEntry: true } },
  { path: '/', component: () => import('../layout/RootShell.vue'), children: [
    { path: '', redirect: () => defaultHome() },
    // 宠主商城前台（游客可浏览：guest: true）
    { path: 'home', component: () => import('../views/front/Home.vue'), meta: { title: '首页', front: true, guest: true, ownerFront: true } },
    { path: 'shop', component: () => import('../views/front/Shop.vue'), meta: { title: '服务商城', front: true, guest: true, ownerFront: true } },
    { path: 'sitters', component: () => import('../components/RoleSitters.vue'), meta: { title: '宠托师', front: true, guest: true } },
    { path: 'spaces', component: () => import('../components/RoleSpaces.vue'), meta: { title: '寄养空间', front: true, guest: true } },
    // 后台/共用页面（需登录）
    { path: 'dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '数据仪表盘', staffOnly: true } },
    { path: 'pets', component: () => import('../views/Pets.vue'), meta: { title: '宠物档案' } },
    { path: 'services', component: () => import('../views/Services.vue'), meta: { title: '服务项目', staffOnly: true } },
    { path: 'booking', component: () => import('../views/Booking.vue'), meta: { title: '预约下单' } },
    { path: 'orders', component: () => import('../views/Orders.vue'), meta: { title: '订单管理' } },
    { path: 'orders/:id', component: () => import('../views/OrderDetail.vue'), meta: { title: '订单详情' } },
    { path: 'hall', component: () => import('../views/Hall.vue'), meta: { title: '接单大厅', staffOnly: true } },
    { path: 'calendar', component: () => import('../views/Calendar.vue'), meta: { title: '预约日历' } },
    { path: 'health', component: () => import('../views/Health.vue'), meta: { title: '健康档案与提醒' } },
    { path: 'reviews', component: () => import('../views/Reviews.vue'), meta: { title: '评价管理' } },
    { path: 'coupons', component: () => import('../views/Coupons.vue'), meta: { title: '优惠券' } },
    { path: 'payments', component: () => import('../views/Payments.vue'), meta: { title: '支付流水' } },
    { path: 'notices', component: () => import('../views/Notices.vue'), meta: { title: '公告消息' } },
    { path: 'users', component: () => import('../views/Users.vue'), meta: { title: '用户管理', staffOnly: true } },
    { path: 'profile', component: () => import('../views/Profile.vue'), meta: { title: '个人中心' } },
  ] },
  { path: '/:pathMatch(.*)*', redirect: () => defaultHome() }
]
const router = createRouter({ history: createWebHashHistory(), routes })
const currentRole = () => { try { return (JSON.parse(localStorage.getItem('pet_user') || 'null') || {}).role || '' } catch (e) { return '' } }
const hasToken = () => !!localStorage.getItem('pet_token')
function defaultHome() {
  if (!hasToken()) return '/home'
  return currentRole() === 'OWNER' ? '/home' : '/dashboard'
}
// 后台专属路由：未登录时走管理端入口
const STAFF_PATHS = ['/dashboard', '/users', '/hall', '/services']
router.beforeEach((to, from, next) => {
  const token = hasToken()
  const role = currentRole()
  // 公开页（登录/注册）
  if (to.meta.public) {
    if (!token) return next()
    // 已登录用户回到各自的家
    if (to.meta.guestEntry && role !== 'OWNER') return next('/dashboard')
    if (to.meta.staffEntry && role === 'OWNER') return next(to.query.redirect || '/home')
    if (to.meta.guestEntry && role === 'OWNER') return next(to.query.redirect || '/home')
    return next()
  }
  // 未登录
  if (!token) {
    if (to.meta.guest) return next() // 游客可浏览前台公开页
    if (to.path === '/' || to.path === '') return next('/home')
    if (STAFF_PATHS.includes(to.path)) return next({ path: '/admin/login' })
    // 前台受保护页 → 客人登录并带回跳
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }
  // 已登录
  if (to.path === '/') return next(defaultHome())
  if (role === 'OWNER') {
    if (to.meta.staffOnly) {
      if (to.path === '/services') return next('/shop')
      return next('/home')
    }
  } else {
    // 管理员/宠托师不进宠主商城专属前台（首页/商城）；宠托师、寄养空间为共用页，走后台布局
    if (to.meta.ownerFront) return next('/dashboard')
  }
  next()
})
export default router
