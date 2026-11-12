<template>
  <div class="front">
    <header class="nav">
      <div class="nav-in">
        <div class="logo" @click="$router.push('/home')">
          <span class="logo-ic">🐾</span>
          <span class="logo-tx">宠寄养<em>PetCare</em></span>
        </div>
        <nav class="links">
          <router-link v-for="l in links" :key="l.path" :to="l.path" class="link" :class="{ on: isOn(l.path) }">{{ l.title }}</router-link>
        </nav>
        <div class="right">
          <template v-if="!loggedIn">
            <el-button round @click="$router.push('/login')">登录</el-button>
            <el-button round type="primary" @click="$router.push('/register')">免费注册</el-button>
          </template>
          <template v-else>
          <el-badge :value="pending" :hidden="!pending" class="bdg">
            <el-button round @click="$router.push('/orders')">🧾 我的订单</el-button>
          </el-badge>
          <el-badge :value="unread" :hidden="!unread" class="bdg">
            <el-button round @click="$router.push('/notices')">🔔 消息</el-button>
          </el-badge>
          <el-dropdown trigger="click" @command="cmd">
            <div class="user-chip">
              <img v-if="auth.user && auth.user.avatarUrl" :src="auth.user.avatarUrl" class="chip-ava" />
              <span v-else class="chip-ava ph">{{ (auth.user && auth.user.avatarEmoji) || '🐾' }}</span>
              <span class="chip-name">{{ auth.user && auth.user.realName }}</span>
              <span class="chip-arrow">▾</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">👤 个人中心</el-dropdown-item>
                <el-dropdown-item command="pets">🐾 我的宠物</el-dropdown-item>
                <el-dropdown-item command="coupons">🎟️ 我的优惠券</el-dropdown-item>
                <el-dropdown-item command="health">💉 健康提醒</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          </template>
        </div>
      </div>
    </header>
    <main class="wrap"><router-view :key="$route.fullPath" /></main>
    <footer class="foot">
      <div class="foot-in">
        <div class="f-col"><div class="f-logo">🐾 宠寄养 PetCare</div>
          <p>城市宠物寄养与上门喂养预约平台<br />让每一次托付都看得见、信得过。</p></div>
        <div class="f-col"><h4>服务保障</h4><p>✅ 宠托师实名认证<br />📝 每次服务图文报告<br />🦮 遛狗 GPS 轨迹可查<br />💉 寄养疫苗校验</p></div>
        <div class="f-col"><h4>快捷入口</h4><p>
          <a @click="$router.push('/shop')">服务商城</a><br />
          <a @click="$router.push('/sitters')">找宠托师</a><br />
          <a @click="$router.push('/spaces')">寄养空间</a><br />
          <a @click="$router.push('/booking')">立即预约</a></p></div>
        <div class="f-col"><h4>联系我们</h4><p>客服热线：400-800-7766<br />服务时间：8:00 - 22:00<br />地址：北京市朝阳区宠物服务中心</p></div>
      </div>
      <div class="f-bottom">© 2026 宠寄养 PetCare Platform · 用心照护每一只毛孩子 · <a class="staff-link" @click="$router.push('/admin/login')">员工/管理入口</a></div>
    </footer>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import request from '../utils/request'
const auth = useAuthStore(); const router = useRouter(); const route = useRoute()
const unread = ref(0); const pending = ref(0)
const loggedIn = computed(() => !!auth.token)
const links = [
  { path: '/home', title: '首页' },
  { path: '/shop', title: '服务商城' },
  { path: '/sitters', title: '找宠托师' },
  { path: '/spaces', title: '寄养空间' },
  { path: '/pets', title: '我的宠物' },
  { path: '/coupons', title: '优惠券' }
]
const isOn = p => route.path === p || (p === '/sitters' && route.path.startsWith('/sitters')) || (p === '/spaces' && route.path.startsWith('/spaces'))
const cmd = c => {
  if (c === 'logout') { auth.logout(); router.push('/home') }
  else if (c === 'profile') router.push('/profile')
  else if (c === 'pets') router.push('/pets')
  else if (c === 'coupons') router.push('/coupons')
  else if (c === 'health') router.push('/health')
}
onMounted(async () => {
  if (!auth.token) return // 游客态不拉取个人数据
  try { const r = await request.get('/notices/messages/mine'); unread.value = r.data.filter(m => !m.readFlag).length } catch (e) {}
  try { const r = await request.get('/orders'); pending.value = r.data.filter(o => ['PENDING', 'CONFIRMED', 'ACCEPTED', 'IN_SERVICE'].includes(o.status)).length } catch (e) {}
})
</script>
<style scoped>
.front { min-height: 100vh; display: flex; flex-direction: column; background: #fff7ed; }
.nav { position: sticky; top: 0; z-index: 50; background: rgba(255,255,255,.96); backdrop-filter: blur(8px); box-shadow: 0 2px 16px rgba(249,115,22,.12); }
.nav-in { max-width: 1240px; margin: 0 auto; padding: 0 20px; height: 66px; display: flex; align-items: center; gap: 26px; }
.logo { display: flex; align-items: center; gap: 9px; cursor: pointer; flex-shrink: 0; }
.logo-ic { width: 42px; height: 42px; border-radius: 13px; background: linear-gradient(135deg,#f97316,#fb923c); display: flex; align-items: center; justify-content: center; font-size: 23px; }
.logo-tx { font-size: 20px; font-weight: 800; color: #9a3412; line-height: 1.05; }
.logo-tx em { display: block; font-style: normal; font-size: 10.5px; font-weight: 600; color: #fdba74; letter-spacing: 1.5px; }
.links { display: flex; gap: 4px; flex: 1; overflow-x: auto; }
.link { padding: 8px 14px; border-radius: 999px; color: #57534e; text-decoration: none; font-size: 15px; font-weight: 600; white-space: nowrap; }
.link:hover { color: #ea580c; background: #fff7ed; }
.link.on { color: #fff; background: linear-gradient(135deg,#f97316,#fb923c); }
.right { display: flex; align-items: center; gap: 10px; flex-shrink: 0; }
.user-chip { display: flex; align-items: center; gap: 8px; cursor: pointer; padding: 4px 10px 4px 4px; border-radius: 999px; background: #fff7ed; border: 1px solid #fed7aa; }
.chip-ava { width: 34px; height: 34px; border-radius: 50%; object-fit: cover; }
.chip-ava.ph { background: linear-gradient(135deg,#f97316,#fb923c); display: flex; align-items: center; justify-content: center; font-size: 17px; }
.chip-name { font-weight: 700; color: #9a3412; font-size: 14px; }
.chip-arrow { color: #fdba74; }
.wrap { flex: 1; width: 100%; max-width: 1240px; margin: 0 auto; padding: 22px 20px 40px; }
.foot { background: #431407; color: #fed7aa; margin-top: auto; }
.foot-in { max-width: 1240px; margin: 0 auto; padding: 34px 20px 10px; display: grid; grid-template-columns: 1.4fr 1fr 1fr 1fr; gap: 24px; }
.f-logo { font-size: 19px; font-weight: 800; color: #fff; margin-bottom: 8px; }
.foot p { font-size: 13px; line-height: 1.9; opacity: .85; margin: 0; }
.foot h4 { color: #fff; margin: 0 0 8px; font-size: 15px; }
.foot a { cursor: pointer; } .foot a:hover { color: #fff; }
.f-bottom { text-align: center; font-size: 12px; opacity: .55; padding: 14px 0 18px; border-top: 1px solid rgba(254,215,170,.15); margin: 16px auto 0; max-width: 1240px; }
.staff-link { cursor: pointer; text-decoration: underline; } .staff-link:hover { color: #fff; }
@media (max-width: 900px) { .foot-in { grid-template-columns: 1fr 1fr; } .chip-name { display: none; } }
</style>
