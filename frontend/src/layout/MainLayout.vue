<template>
  <el-container style="height:100vh">
    <el-aside width="228px" class="side">
      <div class="brand"><div class="brand-logo">🐾</div>
        <div><div class="brand-name">宠寄养平台</div><div class="brand-sub">Pet Care Platform</div></div>
      </div>
      <el-menu :default-active="$route.path" router class="menu" background-color="transparent" text-color="#ffedd5" active-text-color="#ffffff">
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
          <el-icon><component :is="m.icon" /></el-icon><span>{{ m.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <div class="crumb">🏠 首页 / {{ $route.meta.title || '仪表盘' }}</div>
        <div class="userbox">
          <el-badge :value="unread" :hidden="!unread" class="msg-badge"><el-button text @click="$router.push('/notices')">🔔 消息</el-button></el-badge>
          <img v-if="auth.user && auth.user.avatarUrl" :src="auth.user.avatarUrl" class="avatar-img" /><div v-else class="avatar">{{ (auth.user && auth.user.avatarEmoji) || '🐾' }}</div>
          <span>{{ auth.user && auth.user.realName }}（{{ roleName }}）</span>
          <el-button text @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main"><router-view :key="$route.fullPath" /></el-main>
    </el-container>
  </el-container>
</template>
<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import request from '../utils/request'
const auth = useAuthStore(); const router = useRouter(); const unread = ref(0)
const ALL = [
  { path: '/dashboard', title: '数据仪表盘', icon: 'DataBoard', roles: ['ADMIN','OWNER','SITTER'] },
  { path: '/pets', title: '宠物档案', icon: 'Star', roles: ['ADMIN','OWNER'] },
  { path: ' /services'.trim(), title: '服务项目', icon: 'PriceTag', roles: ['ADMIN','OWNER','SITTER'] },
  { path: '/sitters', title: '宠托师', icon: 'UserFilled', roles: ['ADMIN','OWNER'] },
  { path: '/spaces', title: '寄养空间', icon: 'House', roles: ['ADMIN','OWNER','SITTER'] },
  { path: '/booking', title: '预约下单', icon: 'Calendar', roles: ['OWNER','ADMIN'] },
  { path: '/hall', title: '接单大厅', icon: 'Bell', roles: ['SITTER'] },
  { path: '/orders', title: '订单管理', icon: 'Document', roles: ['ADMIN','OWNER','SITTER'] },
  { path: '/calendar', title: '预约日历', icon: 'Calendar', roles: ['ADMIN','SITTER','OWNER'] },
  { path: '/health', title: '健康提醒', icon: 'FirstAidKit', roles: ['ADMIN','OWNER'] },
  { path: '/reviews', title: '评价管理', icon: 'ChatDotRound', roles: ['ADMIN','OWNER','SITTER'] },
  { path: '/coupons', title: '优惠券', icon: 'Ticket', roles: ['ADMIN','OWNER'] },
  { path: '/payments', title: '支付流水', icon: 'Wallet', roles: ['ADMIN','OWNER'] },
  { path: '/notices', title: '公告消息', icon: 'Notification', roles: ['ADMIN','OWNER','SITTER'] },
  { path: '/users', title: '用户管理', icon: 'User', roles: ['ADMIN'] },
  { path: '/profile', title: '个人中心', icon: 'Setting', roles: ['ADMIN','OWNER','SITTER'] },
]
const menus = computed(() => ALL.filter(m => m.roles.includes(auth.role)))
const roleName = computed(() => ({ ADMIN: '管理员', OWNER: '宠主', SITTER: '宠托师' }[auth.role] || ''))
onMounted(async () => { try { const r = await request.get('/notices/messages/mine'); unread.value = r.data.filter(m => !m.readFlag).length } catch (e) {} })
const logout = () => { auth.logout(); router.push('/admin/login') }
</script>
<style scoped>
.side { background: linear-gradient(180deg, #ea580c, #f97316 45%, #fb923c); padding: 18px 12px; overflow-y: auto; }
.brand { display: flex; gap: 10px; align-items: center; padding: 6px 10px 18px; color: #fff; }
.brand-logo { font-size: 34px; background: rgba(255,255,255,.22); border-radius: 12px; width: 52px; height: 52px; display: flex; align-items: center; justify-content: center; }
.brand-name { font-size: 18px; font-weight: 700; }
.brand-sub { font-size: 11px; opacity: .8; }
.menu { border: none; }
.menu :deep(.el-menu-item) { border-radius: 999px; margin: 3px 0; height: 42px; }
.menu :deep(.el-menu-item.is-active) { background: rgba(255,255,255,.28); font-weight: 700; }
.menu :deep(.el-menu-item:hover) { background: rgba(255,255,255,.16); }
.topbar { background: #fff; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 2px 12px rgba(249,115,22,.08); }
.crumb { font-weight: 600; color: #9a3412; }
.userbox { display: flex; align-items: center; gap: 10px; }
.avatar-img { width: 36px; height: 36px; border-radius: 50%; object-fit: cover; }
.avatar { width: 36px; height: 36px; border-radius: 50%; background: linear-gradient(135deg,#f97316,#fb923c); display: flex; align-items: center; justify-content: center; font-size: 18px; }
.main { padding: 20px; overflow-y: auto; }
</style>
