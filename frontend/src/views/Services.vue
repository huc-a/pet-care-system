<template>
  <div>
    <div class="page-head"><div><h2>🎾 服务项目</h2><div class="sub">明码标价 · 节假日（法定假期）按 1.5 系数计价 · 长住与多宠有折扣</div></div>
      <el-button type="primary" v-if="auth.isAdmin" @click="open()">+ 新增服务</el-button></div>
    <el-row :gutter="16">
      <el-col :span="6" v-for="s in list" :key="s.id" style="margin-bottom:16px">
        <el-card class="svc-card">
          <img v-if="s.coverUrl" :src="s.coverUrl" class="svc-cover" :alt="s.name" /><div v-else style="font-size:40px">{{ s.icon || '🐾' }}</div>
          <h3 style="margin:8px 0">{{ s.name }} <el-tag size="small" :type="s.category==='ADDON' ? 'info' : 'warning'">{{ CAT[s.category] || s.category }}</el-tag></h3>
          <div class="price">¥{{ money(s.price) }}<span class="unit">/{{ s.pricingType === 'PER_DAY' ? '天' : '次' }}</span></div>
          <p class="desc">{{ s.description }}</p>
          <div style="color:#a8a29e;font-size:12px">时长约 {{ s.durationMin >= 1440 ? '全天' : s.durationMin + ' 分钟' }} · 已售 {{ s.salesCount || 0 }}</div>
          <div v-if="auth.isAdmin" style="margin-top:10px;display:flex;gap:8px">
            <el-button size="small" @click="open(s)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="del(s)">删除</el-button>
          </div>
          <el-button v-else-if="auth.isOwner && s.category !== 'ADDON'" size="small" type="primary" style="margin-top:10px" @click="$router.push('/booking?serviceId=' + s.id)">立即预约</el-button>
        </el-card>
      </el-col>
    </el-row>
    <el-dialog v-model="dlg" :title="form.id ? '编辑服务' : '新增服务'" width="520px">
      <el-form :model="form" label-width="88px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类"><el-select v-model="form.category" style="width:100%">
          <el-option label="上门服务" value="VISIT" /><el-option label="遛狗" value="WALK" /><el-option label="寄养" value="BOARDING" /><el-option label="洗护" value="GROOMING" /><el-option label="附加服务" value="ADDON" /></el-select></el-form-item>
        <el-form-item label="计价方式"><el-select v-model="form.pricingType" style="width:100%"><el-option label="按次" value="PER_TIME" /><el-option label="按天" value="PER_DAY" /></el-select></el-form-item>
        <el-form-item label="基础价"><el-input-number v-model="form.price" :min="0" :step="1" /></el-form-item>
        <el-form-item label="时长(分)"><el-input-number v-model="form.durationMin" :min="0" /></el-form-item>
        <el-form-item label="封面图"><ImageUpload v-model="form.coverUrl" :size="110" /></el-form-item>
        <el-form-item label="图标emoji"><el-input v-model="form.icon" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dlg=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { useAuthStore } from '../store/auth'
import { money } from '../utils/format'
import ImageUpload from '../components/ImageUpload.vue'
const auth = useAuthStore(); const list = ref([]); const dlg = ref(false)
const CAT = { VISIT: '上门服务', WALK: '遛狗', BOARDING: '寄养', GROOMING: '洗护', ADDON: '附加' }
const form = reactive({ coverUrl: '', salesCount: 0, id: null, name: '', category: 'VISIT', pricingType: 'PER_TIME', price: 50, durationMin: 30, icon: '🐾', description: '' })
const load = async () => { list.value = (await request.get('/services')).data }
const open = s => { Object.assign(form, { coverUrl: '', salesCount: 0, id: null, name: '', category: 'VISIT', pricingType: 'PER_TIME', price: 50, durationMin: 30, icon: '🐾', description: '' }, s || {}); dlg.value = true }
const save = async () => { if (form.id) await request.put('/services/' + form.id, form); else await request.post('/services', form); ElMessage.success('保存成功'); dlg.value = false; load() }
const del = async s => { await ElMessageBox.confirm('确认删除服务「' + s.name + '」？', '提示'); await request.delete('/services/' + s.id); ElMessage.success('已删除'); load() }
onMounted(load)
</script>
<style scoped>
.svc-card { height: 100%; }
.svc-cover { width: 100%; height: 150px; object-fit: cover; border-radius: 12px; display: block; }
.price { font-size: 26px; font-weight: 700; color: #ea580c; }
.price .unit { font-size: 13px; color: #a8a29e; font-weight: 400; }
.desc { color: #78716c; font-size: 13px; line-height: 1.7; min-height: 44px; }
</style>
