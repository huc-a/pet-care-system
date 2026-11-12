<template>
  <div class="wrap"><div class="panel">
    <div class="brand"><div style="font-size:52px">🐶</div>
      <h1>加入宠寄养平台</h1>
      <p>注册宠主账号，即可为爱宠建立健康档案、<br/>预约寄养与上门喂养、领取新人优惠券。</p>
    </div>
    <div class="formbox">
      <h2 style="margin:0 0 20px">注册宠主账号 🐾</h2>
      <el-form :model="form" label-width="76px">
        <el-form-item label="用户名"><el-input v-model="form.username" placeholder="登录用户名" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.realName" placeholder="您的姓名/昵称" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password placeholder="设置密码" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" placeholder="手机号" /></el-form-item>
        <el-form-item label="所在区域"><el-input v-model="form.area" placeholder="如：朝阳区" /></el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="submit">注 册</el-button>
      </el-form>
      <div style="text-align:center;margin-top:14px;font-size:13px">已有账号？<a :href="loginHref" style="color:#ea580c">去登录</a></div>
    </div>
  </div></div>
</template>
<script setup>
import { reactive, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
const form = reactive({ username: '', realName: '', password: '', phone: '', area: '' })
const loading = ref(false); const router = useRouter(); const route = useRoute()
const loginHref = computed(() => '#/login' + (route.query.redirect ? '?redirect=' + encodeURIComponent(route.query.redirect) : ''))
const submit = async () => {
  if (!form.username || !form.password || !form.realName) return ElMessage.warning('请填写用户名、昵称和密码')
  loading.value = true
  try {
    await request.post('/auth/register', form); ElMessage.success('注册成功，请登录')
    router.push(route.query.redirect ? { path: '/login', query: { redirect: route.query.redirect } } : '/login')
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
