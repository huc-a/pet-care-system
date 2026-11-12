<template>
  <div>
    <div class="page-head"><div><h2>📢 公告与消息</h2><div class="sub">平台公告与我的站内消息</div></div>
      <el-button type="primary" v-if="auth.isAdmin" @click="dlg = true">+ 发布公告</el-button></div>
    <el-row :gutter="16">
      <el-col :span="14">
        <el-card v-for="n in notices" :key="n.id" style="margin-bottom:14px">
          <div style="display:flex;justify-content:space-between"><h3 style="margin:0">{{ n.title }}</h3><el-tag size="small" type="warning">{{ n.type }}</el-tag></div>
          <p style="color:#57534e;line-height:1.8;font-size:14px">{{ n.content }}</p>
          <div style="color:#a8a29e;font-size:12.5px">{{ n.publisher }} · {{ n.createTime }}
            <el-button v-if="auth.isAdmin" size="small" type="danger" plain style="margin-left:10px" @click="del(n)">删除</el-button></div>
        </el-card>
        <el-empty v-if="!notices.length" description="暂无公告" />
      </el-col>
      <el-col :span="10">
        <el-card><template #header>🔔 我的消息</template>
          <div v-for="m in msgs" :key="m.id" class="msg" @click="read(m)">
            <div style="display:flex;justify-content:space-between"><b>{{ m.title }}</b><el-badge is-dot :hidden="m.readFlag" /></div>
            <div style="color:#57534e;font-size:13px;margin:4px 0">{{ m.content }}</div>
            <div style="color:#a8a29e;font-size:12px">{{ m.createTime }}</div>
          </div>
          <el-empty v-if="!msgs.length" description="暂无消息" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
    <el-dialog v-model="dlg" title="发布公告" width="520px">
      <el-form :model="form" label-width="70px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="form.type" style="width:100%"><el-option label="平台公告" value="平台公告" /><el-option label="功能更新" value="功能更新" /><el-option label="服务通知" value="服务通知" /></el-select></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dlg=false">取消</el-button><el-button type="primary" @click="create">发布</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { useAuthStore } from '../store/auth'
const auth = useAuthStore(); const notices = ref([]); const msgs = ref([]); const dlg = ref(false)
const form = reactive({ title: '', type: '平台公告', content: '' })
const load = async () => { notices.value = (await request.get('/notices')).data; msgs.value = (await request.get('/notices/messages/mine')).data }
const create = async () => { await request.post('/notices', form); ElMessage.success('发布成功'); dlg.value = false; load() }
const del = async n => { await ElMessageBox.confirm('确认删除公告？', '提示'); await request.delete('/notices/' + n.id); ElMessage.success('已删除'); load() }
const read = async m => { if (!m.readFlag) { await request.put('/notices/messages/' + m.id + '/read'); m.readFlag = true } }
onMounted(load)
</script>
<style scoped>
.msg { padding: 10px 4px; border-bottom: 1px dashed #fed7aa; cursor: pointer; }
</style>
