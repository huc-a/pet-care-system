<template>
  <div class="wrap">
    <div class="card">
      <div class="head">
        <div class="logo">🐾</div>
        <div>
          <h1>宠寄养平台 · 管理端</h1>
          <p>PetCare Operations Console</p>
        </div>
      </div>
      <div class="tip">此入口仅供平台管理员与宠托师使用</div>
      <el-form :model="form" @keyup.enter="submit">
        <el-form-item><el-input v-model="form.username" placeholder="员工/管理员账号" size="large" prefix-icon="User" /></el-form-item>
        <el-form-item><el-input v-model="form.password" type="password" placeholder="密码" size="large" prefix-icon="Lock" show-password /></el-form-item>
        <el-button type="primary" size="large" class="btn" :loading="loading" @click="submit">进入管理端</el-button>
      </el-form>
      <div class="demo">演示账号（点击填入）：<br />
        <a href="javascript:void 0" @click="fill('admin','admin123')">管理员 admin / admin123</a> ·
        <a href="javascript:void 0" @click="fill('sitter','sitter123')">宠托师 sitter / sitter123</a>
      </div>
      <div class="foot">宠主下单请走 <a href="#/home">前台商城 →</a></div>
    </div>
  </div>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../store/auth'
const form = reactive({ username: '', password: '' }); const loading = ref(false); const router = useRouter(); const auth = useAuthStore()
const fill = (u, p) => { form.username = u; form.password = p }
const submit = async () => {
  if (!form.username || !form.password) return ElMessage.warning('请输入账号和密码')
  loading.value = true
  try {
    const user = await auth.login(form.username, form.password)
    if (user.role === 'OWNER') {
      auth.logout()
      return ElMessage.error({ message: '该账号是宠主账号，请使用前台登录', duration: 4000 })
    }
    ElMessage.success('登录成功')
    router.push('/dashboard')
  }
  catch (e) {} finally { loading.value = false }
}
</script>
<style scoped>
.wrap { min-height: 100vh; background: radial-gradient(1100px 520px at 70% -10%, #334155 0%, transparent 60%), linear-gradient(150deg, #0f172a 0%, #1e293b 55%, #111827 100%); display: flex; align-items: center; justify-content: center; padding: 20px; }
.card { width: 420px; max-width: 94vw; background: #fff; border-radius: 18px; padding: 34px 34px 26px; box-shadow: 0 30px 70px rgba(0,0,0,.5); }
.head { display: flex; gap: 14px; align-items: center; margin-bottom: 8px; }
.logo { width: 54px; height: 54px; border-radius: 14px; background: #1e293b; display: flex; align-items: center; justify-content: center; font-size: 28px; }
.head h1 { font-size: 20px; margin: 0; color: #0f172a; }
.head p { margin: 3px 0 0; font-size: 11.5px; letter-spacing: 1.2px; color: #94a3b8; text-transform: uppercase; }
.tip { background: #f1f5f9; border: 1px solid #e2e8f0; color: #475569; font-size: 12.5px; border-radius: 10px; padding: 9px 12px; margin: 14px 0 20px; }
.btn { width: 100%; background: #1e293b !important; border-color: #1e293b !important; }
.btn:hover { background: #334155 !important; border-color: #334155 !important; }
.demo { background: #f8fafc; border: 1px dashed #cbd5e1; border-radius: 10px; padding: 10px 12px; font-size: 12.5px; color: #475569; line-height: 1.9; margin-top: 16px; }
.demo a { color: #0f172a; font-weight: 600; }
.foot { text-align: center; margin-top: 16px; font-size: 12.5px; color: #94a3b8; }
.foot a { color: #ea580c; font-weight: 600; }
</style>
