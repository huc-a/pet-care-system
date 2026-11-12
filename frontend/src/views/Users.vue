<template>
  <div>
    <div class="page-head"><div><h2>👥 用户管理</h2><div class="sub">共 {{ list.length }} 个账号 · 含管理员 / 宠主 / 宠托师三角色</div></div>
      <el-button type="primary" @click="open()">+ 新增用户</el-button></div>
    <el-card><el-table :data="list" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="头像" width="70"><template #default="{ row }"><img v-if="row.avatarUrl" :src="row.avatarUrl" class="u-ava" /><span v-else>{{ row.avatarEmoji || '🐾' }}</span></template></el-table-column>
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="姓名" width="110" />
      <el-table-column label="角色" width="100"><template #default="{ row }"><el-tag :type="row.role === 'ADMIN' ? 'danger' : row.role === 'SITTER' ? 'success' : 'warning'">{{ { ADMIN: '管理员', OWNER: '宠主', SITTER: '宠托师' }[row.role] }}</el-tag></template></el-table-column>
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column prop="area" label="区域" width="100" />
      <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag></template></el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="170" />
      <el-table-column label="操作"><template #default="{ row }">
        <el-button size="small" @click="open(row)">编辑</el-button>
        <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" plain @click="toggle(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
        <el-button size="small" type="danger" plain @click="del(row)">删除</el-button></template></el-table-column>
    </el-table></el-card>
    <el-dialog v-model="dlg" :title="form.id ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名"><el-input v-model="form.username" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" :placeholder="form.id ? '留空不修改' : '默认 123456'" /></el-form-item>
        <el-form-item label="头像"><ImageUpload v-model="form.avatarUrl" :size="84" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="角色"><el-select v-model="form.role" style="width:100%"><el-option label="管理员" value="ADMIN" /><el-option label="宠主" value="OWNER" /><el-option label="宠托师" value="SITTER" /></el-select></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="区域"><el-input v-model="form.area" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dlg=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import ImageUpload from '../components/ImageUpload.vue'
const list = ref([]); const dlg = ref(false)
const form = reactive({ id: null, username: '', password: '', realName: '', role: 'OWNER', phone: '', area: '', avatarUrl: '' })
const load = async () => { list.value = (await request.get('/users')).data }
const open = u => { Object.assign(form, { id: null, username: '', password: '', realName: '', role: 'OWNER', phone: '', area: '', avatarUrl: '' }, u || {}, { password: '' }); dlg.value = true }
const save = async () => { if (form.id) await request.put('/users/' + form.id, form); else await request.post('/users', form); ElMessage.success('保存成功'); dlg.value = false; load() }
const toggle = async u => { await request.put('/users/' + u.id, { status: u.status === 1 ? 0 : 1 }); ElMessage.success('已更新'); load() }
const del = async u => { await ElMessageBox.confirm('确认删除用户「' + u.username + '」？', '提示'); await request.delete('/users/' + u.id); ElMessage.success('已删除'); load() }
onMounted(load)
</script>

<style scoped>
.u-ava { width: 38px; height: 38px; border-radius: 50%; object-fit: cover; }
</style>
