<template>
  <div>
    <div class="page-head"><div><h2>⚙️ 个人中心</h2><div class="sub">账号资料与安全设置</div></div></div>
    <el-row :gutter="16">
      <el-col :span="12"><el-card><template #header>👤 我的资料</template>
        <el-form :model="form" label-width="88px">
          <el-form-item label="用户名"><el-input :model-value="auth.user && auth.user.username" disabled /></el-form-item>
          <el-form-item label="角色"><el-input :model-value="roleCn" disabled /></el-form-item>
          <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
          <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
          <el-form-item label="区域"><el-input v-model="form.area" /></el-form-item>
          <el-form-item label="头像"><ImageUpload v-model="form.avatarUrl" :size="96" /></el-form-item>
          <el-form-item label="头像emoji"><el-input v-model="form.avatarEmoji" placeholder="无照片时展示" /></el-form-item>
          <el-form-item><el-button type="primary" @click="save">保存资料</el-button></el-form-item>
        </el-form></el-card></el-col>
      <el-col :span="12">
        <el-card v-if="auth.isSitter && profile"><template #header>🧑‍🤝‍🧑 我的宠托师档案</template>
          <el-form :model="profile" label-width="88px">
            <el-form-item label="服务区域"><el-input v-model="profile.area" /></el-form-item>
            <el-form-item label="擅长品种"><el-input v-model="profile.specialties" /></el-form-item>
            <el-form-item label="可照护"><el-input v-model="profile.petTypes" /></el-form-item>
            <el-form-item label="从业年限"><el-input-number v-model="profile.experienceYears" :min="0" /></el-form-item>
            <el-form-item label="接单开关"><el-switch v-model="profile.available" active-text="接单中" inactive-text="休息中" /></el-form-item>
            <el-form-item label="自我介绍"><el-input v-model="profile.intro" type="textarea" /></el-form-item>
            <el-form-item><span style="color:#78716c;font-size:13px">评分 {{ profile.rating }} · 累计接单 {{ profile.orderCount }} 单</span>
              <el-button type="primary" style="margin-left:14px" @click="saveProfile">保存档案</el-button></el-form-item>
          </el-form></el-card>
        <el-card :style="auth.isSitter ? 'margin-top:16px' : ''"><template #header>🔒 修改密码</template>
          <el-form :model="pwd" label-width="88px">
            <el-form-item label="原密码"><el-input v-model="pwd.oldPassword" type="password" show-password /></el-form-item>
            <el-form-item label="新密码"><el-input v-model="pwd.newPassword" type="password" show-password /></el-form-item>
            <el-form-item><el-button type="primary" @click="changePwd">修改密码</el-button></el-form-item>
          </el-form></el-card>
      </el-col>
    </el-row>
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { useAuthStore } from '../store/auth'
import ImageUpload from '../components/ImageUpload.vue'
const auth = useAuthStore()
const form = reactive({ realName: '', phone: '', email: '', area: '', avatarEmoji: '', avatarUrl: '' })
const pwd = reactive({ oldPassword: '', newPassword: '' })
const profile = ref(null)
const roleCn = computed(() => ({ ADMIN: '管理员', OWNER: '宠主', SITTER: '宠托师' }[auth.role] || ''))
const save = async () => { const r = await request.put('/auth/profile', form); auth.user = r.data; localStorage.setItem('pet_user', JSON.stringify(r.data)); ElMessage.success('资料已保存') }
const saveProfile = async () => { await request.put('/sitters/my-profile', profile.value); ElMessage.success('档案已保存') }
const changePwd = async () => { if (!pwd.oldPassword || !pwd.newPassword) return ElMessage.warning('请填写完整'); await request.post('/auth/change-password', pwd); ElMessage.success('密码已修改'); pwd.oldPassword = ''; pwd.newPassword = '' }
onMounted(async () => {
  Object.assign(form, auth.user || {})
  if (auth.isSitter) { try { const r = await request.get('/sitters/' + auth.user.id); profile.value = r.data.profile } catch (e) {} }
})
</script>
