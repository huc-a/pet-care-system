<template>
  <div>
    <div class="page-head"><div><h2>🏠 寄养空间</h2><div class="sub">实时占用可视化 · 入住 +1 / 退房 −1 · 满房自动拦截下单</div></div>
      <el-button type="primary" v-if="auth.isAdmin" @click="open()">+ 新增空间</el-button></div>
    <el-row :gutter="16">
      <el-col :span="8" v-for="s in list" :key="s.id" style="margin-bottom:16px">
        <el-card>
          <img v-if="s.photoUrl" :src="s.photoUrl" class="sp-photo" :alt="s.name" />
          <div style="display:flex;justify-content:space-between;align-items:center">
            <h3 style="margin:0">{{ s.name }}</h3>
            <el-tag :type="s.occupied >= s.totalCapacity ? 'danger' : 'success'">{{ s.occupied >= s.totalCapacity ? '已满房' : '可预约' }}</el-tag>
          </div>
          <div style="color:#78716c;font-size:13px;margin:6px 0">{{ s.type }} · {{ s.sizeDesc }} · ¥{{ money(s.pricePerDay) }}/天</div>
          <div class="grid">
            <div v-for="i in s.totalCapacity" :key="i" class="cell" :class="{ occ: i <= s.occupied }" :title="i <= s.occupied ? '已入住' : '空闲'">{{ i <= s.occupied ? '🐾' : '' }}</div>
          </div>
          <el-progress :percentage="Math.round(s.occupied * 100 / s.totalCapacity)" :color="s.occupied >= s.totalCapacity ? '#ef4444' : '#f97316'" style="margin:10px 0" />
          <div style="font-size:13px;color:#57534e">已入住 <b>{{ s.occupied }}</b> / 总量 {{ s.totalCapacity }} · 设施：{{ s.facilities }}</div>
          <div style="font-size:13px;color:#78716c;margin-top:4px">{{ s.description }}</div>
          <div style="margin-top:10px;display:flex;gap:8px">
            <el-button size="small" @click="showGuests(s)">在住宠物（{{ guests[s.id] ? guests[s.id].length : 0 }}）</el-button>
            <template v-if="auth.isAdmin"><el-button size="small" @click="open(s)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="del(s)">删除</el-button></template>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-dialog v-model="gDlg" :title="(curSpace && curSpace.name) + ' · 在住宠物'" width="520px">
      <el-table :data="curGuests" size="small">
        <el-table-column prop="petNames" label="宠物" />
        <el-table-column prop="orderNo" label="订单号" width="170" />
        <el-table-column prop="checkinTime" label="入住时间" width="170" />
      </el-table>
      <el-empty v-if="!curGuests.length" description="暂无在住宠物" :image-size="60" />
    </el-dialog>
    <el-dialog v-model="dlg" :title="form.id ? '编辑空间' : '新增空间'" width="520px">
      <el-form :model="form" label-width="88px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类型"><el-input v-model="form.type" placeholder="猫别墅/犬舍/家庭寄养/豪华房" /></el-form-item>
        <el-form-item label="总容量"><el-input-number v-model="form.totalCapacity" :min="1" /></el-form-item>
        <el-form-item label="日单价"><el-input-number v-model="form.pricePerDay" :min="0" /></el-form-item>
        <el-form-item label="面积"><el-input v-model="form.sizeDesc" /></el-form-item>
        <el-form-item label="空间照片"><ImageUpload v-model="form.photoUrl" :size="110" /></el-form-item>
        <el-form-item label="设施"><el-input v-model="form.facilities" type="textarea" /></el-form-item>
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
const auth = useAuthStore(); const list = ref([]); const guests = ref({}); const dlg = ref(false); const gDlg = ref(false); const curSpace = ref(null); const curGuests = ref([])
const form = reactive({ photoUrl: '', id: null, name: '', type: '猫别墅', totalCapacity: 6, pricePerDay: 88, sizeDesc: '', facilities: '', description: '' })
const load = async () => {
  list.value = (await request.get('/spaces')).data
  for (const s of list.value) { try { guests.value[s.id] = (await request.get('/spaces/' + s.id + '/guests')).data } catch (e) {} }
}
const showGuests = s => { curSpace.value = s; curGuests.value = guests.value[s.id] || []; gDlg.value = true }
const open = s => { Object.assign(form, { photoUrl: '', id: null, name: '', type: '猫别墅', totalCapacity: 6, pricePerDay: 88, sizeDesc: '', facilities: '', description: '' }, s || {}); dlg.value = true }
const save = async () => { if (form.id) await request.put('/spaces/' + form.id, form); else await request.post('/spaces', form); ElMessage.success('保存成功'); dlg.value = false; load() }
const del = async s => { await ElMessageBox.confirm('确认删除空间「' + s.name + '」？', '提示'); await request.delete('/spaces/' + s.id); ElMessage.success('已删除'); load() }
onMounted(load)
</script>
<style scoped>
.sp-photo { width: 100%; height: 160px; object-fit: cover; border-radius: 12px; display: block; margin-bottom: 10px; }
.grid { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 8px; }
.cell { width: 34px; height: 34px; border-radius: 9px; background: #ffedd5; border: 1.5px dashed #fdba74; display: flex; align-items: center; justify-content: center; font-size: 16px; }
.cell.occ { background: linear-gradient(135deg,#f97316,#fb923c); border: 1.5px solid #ea580c; }
</style>
