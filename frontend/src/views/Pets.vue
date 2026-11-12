<template>
  <div>
    <div class="page-head"><div><h2>🐾 宠物档案</h2><div class="sub">共 {{ list.length }} 只宠物 · 疫苗齐全才可寄养哦</div></div>
      <el-button type="primary" @click="open()">+ 新增宠物</el-button></div>
    <el-row :gutter="16">
      <el-col :span="6" v-for="p in list" :key="p.id" style="margin-bottom:16px">
        <el-card class="pet-card">
          <img v-if="p.photoUrl" :src="p.photoUrl" class="pet-photo" :alt="p.name" /><div v-else class="pet-emoji">{{ p.emoji || '🐾' }}</div>
          <h3 style="margin:6px 0">{{ p.name }} <el-tag size="small" type="warning">{{ p.species }} · {{ p.breed }}</el-tag></h3>
          <div class="pet-info">
            <div>性别：{{ p.gender }} · {{ Math.floor((p.ageMonths||0)/12) }}岁{{ (p.ageMonths||0)%12 }}个月 · {{ p.weight }}kg</div>
            <div>毛色：{{ p.color }} · {{ p.neutered ? '已绝育' : '未绝育' }}</div>
            <div>性格：{{ p.personality }}</div>
            <div>疫苗：<el-tag size="small" :type="p.vaccineStatus && p.vaccineStatus.includes('齐全') ? 'success' : 'danger'">{{ p.vaccineStatus }}</el-tag></div>
          </div>
          <div style="margin-top:10px;display:flex;gap:8px">
            <el-button size="small" @click="open(p)">编辑</el-button>
            <el-button size="small" @click="$router.push('/health?petId=' + p.id)">健康档案</el-button>
            <el-button size="small" type="danger" plain @click="del(p)">删除</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!list.length" description="还没有宠物档案，快来添加第一只毛孩子吧" />
    <el-dialog v-model="dlg" :title="form.id ? '编辑宠物' : '新增宠物'" width="560px">
      <el-form :model="form" label-width="92px">
        <el-form-item label="名字"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="种类"><el-select v-model="form.species" style="width:100%"><el-option label="猫" value="猫" /><el-option label="狗" value="狗" /><el-option label="其他" value="其他" /></el-select></el-form-item>
        <el-form-item label="品种"><el-input v-model="form.breed" placeholder="如：金毛 / 英短" /></el-form-item>
        <el-form-item label="性别"><el-radio-group v-model="form.gender"><el-radio value="公">公</el-radio><el-radio value="母">母</el-radio></el-radio-group></el-form-item>
        <el-form-item label="年龄(月)"><el-input-number v-model="form.ageMonths" :min="1" /></el-form-item>
        <el-form-item label="体重(kg)"><el-input-number v-model="form.weight" :min="0.1" :step="0.1" :precision="1" /></el-form-item>
        <el-form-item label="已绝育"><el-switch v-model="form.neutered" /></el-form-item>
        <el-form-item label="毛色"><el-input v-model="form.color" /></el-form-item>
        <el-form-item label="宠物照片"><ImageUpload v-model="form.photoUrl" :size="110" /></el-form-item>
        <el-form-item label="头像emoji"><el-input v-model="form.emoji" placeholder="🐶 / 🐱（无照片时展示）" /></el-form-item>
        <el-form-item label="疫苗状态"><el-select v-model="form.vaccineStatus" style="width:100%"><el-option label="已接种齐全" value="已接种齐全" /><el-option label="缺失疫苗" value="缺失疫苗" /><el-option label="未接种" value="未接种" /></el-select></el-form-item>
        <el-form-item label="性格"><el-input v-model="form.personality" /></el-form-item>
        <el-form-item label="过敏史"><el-input v-model="form.allergy" /></el-form-item>
        <el-form-item label="喂养习惯"><el-input v-model="form.feedingHabit" type="textarea" /></el-form-item>
        <el-form-item label="定点医院"><el-input v-model="form.hospital" /></el-form-item>
        <el-form-item label="宠主ID" v-if="auth.isAdmin"><el-input-number v-model="form.ownerId" :min="1" /></el-form-item>
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
import ImageUpload from '../components/ImageUpload.vue'
const auth = useAuthStore(); const list = ref([]); const dlg = ref(false)
const form = reactive({ photoUrl: '', id: null, name: '', species: '狗', breed: '', gender: '公', ageMonths: 12, weight: 5, neutered: false, color: '', emoji: '🐶', vaccineStatus: '已接种齐全', personality: '', allergy: '无', feedingHabit: '', hospital: '', ownerId: null })
const load = async () => { list.value = (await request.get('/pets')).data }
const open = p => { Object.assign(form, { photoUrl: '', id: null, name: '', species: '狗', breed: '', gender: '公', ageMonths: 12, weight: 5, neutered: false, color: '', emoji: '🐶', vaccineStatus: '已接种齐全', personality: '', allergy: '无', feedingHabit: '', hospital: '', ownerId: null }, p || {}); dlg.value = true }
const save = async () => { if (form.id) await request.put('/pets/' + form.id, form); else await request.post('/pets', form); ElMessage.success('保存成功'); dlg.value = false; load() }
const del = async p => { await ElMessageBox.confirm('确认删除宠物「' + p.name + '」？', '提示'); await request.delete('/pets/' + p.id); ElMessage.success('已删除'); load() }
onMounted(load)
</script>
<style scoped>
.pet-card { text-align: left; }
.pet-emoji { font-size: 44px; }
.pet-photo { width: 100%; height: 170px; object-fit: cover; border-radius: 12px; display: block; margin-bottom: 6px; }
.pet-info { font-size: 13px; color: #78716c; line-height: 1.9; }
</style>
