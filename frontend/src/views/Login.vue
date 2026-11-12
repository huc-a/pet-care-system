<template>
  <div class="wrap"><div class="panel">
    <div class="brand"><div style="font-size:52px">🐾</div>
      <h1>城市宠物寄养<br/>与上门喂养平台</h1>
      <p>寄养、上门喂猫喂狗、遛狗、洗护一站预约；<br/>服务报告、GPS 轨迹、智能匹配宠托师，<br/>让每一次托付都看得见、更放心。</p>
    </div>
    <div class="formbox">
      <h2 style="margin:0 0 6px">欢迎回来 👋</h2>
      <p style="color:#78716c;margin:0 0 20px;font-size:13px">登录后开启宠物照护之旅</p>
      <el-alert v-if="route.query.redirect" type="warning" :closable="false" style="margin-bottom:16px" title="登录后将自动回到你刚才浏览的页面继续预约" />
      <el-form :model="form" @keyup.enter="submit">
        <el-form-item><el-input v-model="form.username" placeholder="用户名" size="large" prefix-icon="User" /></el-form-item>
        <el-form-item><el-input v-model="form.password" type="password" placeholder="密码" size="large" prefix-icon="Lock" show-password /></el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="submit">登 录</el-button>
      </el-form>
      <div style="text-align:center;margin-top:14px;font-size:13px">还没有账号？<a :href="registerHref" style="color:#ea580c">立即注册宠主账号</a></div>
      <div class="demo">宠主演示账号（点击填入）：<br/>
        <a href="javascript:void 0" @click="fill('owner','owner123')">宠主 owner / owner123</a>
      </div>
      <div style="text-align:center;margin-top:14px;font-size:12.5px;color:#a8a29e">管理员 / 宠托师请走 <a href="#/admin/login" style="color:#78716c;font-weight:600">管理端入口 →</a></div>
    </div>
  </div></div>
</template>
<script setup>
import { reactive, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../store/auth'
const form = reactive({ username: '', password: '' }); const loading = ref(false); const router = useRouter(); const route = useRoute(); const auth = useAuthStore()
const registerHref = computed(() => '#/register' + (route.query.redirect ? '?redirect=' + encodeURIComponent(route.query.redirect) : ''))
const fill = (u, p) => { form.username = u; form.password = p }
const submit = async () => {
  if (!form.username || !form.password) return ElMessage.warning('请输入用户名和密码')
  loading.value = true
  try {
    const user = await auth.login(form.username, form.password)
    if (user.role !== 'OWNER') {
      auth.logout()
      return ElMessage.error({ message: '该账号是员工/管理员账号，请使用管理端入口登录', duration: 4000 })
    }
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/home')
  }
  catch (e) {} finally { loading.value = false }
}
</script>

<style scoped>
.wrap { min-height: 100vh; background: url('/login-wallpaper.jpg') center/cover no-repeat, linear-gradient(135deg,#f97316,#fb923c); display: flex; align-items: center; justify-content: center; position: relative; }
.wrap::before { content: ''; position: absolute; inset: 0; background: rgba(67,20,7,.35); }
.panel { position: relative; width: 920px; max-width: 94vw; display: flex; border-radius: 24px; overflow: hidden; box-shadow: 0 24px 60px rgba(0,0,0,.35); }
.brand { flex: 1.1; padding: 44px 36px; color: #fff; background: rgba(154,52,18,.35); backdrop-filter: blur(6px); display: flex; flex-direction: column; justify-content: center; }
.brand h1 { font-size: 30px; margin: 10px 0; }
.brand p { opacity: .92; line-height: 1.8; font-size: 14px; }
.formbox { flex: 1; background: rgba(255,251,235,.96); padding: 40px 36px; }
.demo { background: #fff7ed; border: 1px dashed #fdba74; border-radius: 12px; padding: 10px 12px; font-size: 12.5px; color: #9a3412; line-height: 1.9; margin-top: 14px; }
</style>
