<template>
  <div>
    <div class="page-head"><div><h2>⭐ 评价管理</h2><div class="sub">共 {{ list.length }} 条评价 · 评价将计入宠托师综合评分</div></div></div>
    <el-row :gutter="16">
      <el-col :span="8" v-for="r in list" :key="r.id" style="margin-bottom:16px">
        <el-card>
          <div style="display:flex;justify-content:space-between;align-items:center">
            <el-rate :model-value="r.rating" disabled show-score />
            <el-tag v-if="r.hidden" type="danger" size="small">已隐藏</el-tag>
          </div>
          <p style="font-size:14px;line-height:1.7;min-height:48px">{{ r.content }}</p>
          <div style="display:flex;gap:6px;flex-wrap:wrap"><el-tag v-for="t in (r.tags || '').split(',').filter(Boolean)" :key="t" size="small" type="warning">{{ t }}</el-tag></div>
          <div style="color:#a8a29e;font-size:12.5px;margin-top:10px">{{ r.ownerName }} 评价宠托师 {{ r.sitterName }} · {{ r.createTime }}<br/>订单 {{ r.orderNo }}</div>
          <div v-if="auth.isAdmin" style="margin-top:10px;display:flex;gap:8px">
            <el-button size="small" @click="hide(r)">{{ r.hidden ? '恢复显示' : '隐藏评价' }}</el-button>
            <el-button size="small" type="danger" plain @click="del(r)">删除</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!list.length" description="暂无评价" />
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { useAuthStore } from '../store/auth'
const auth = useAuthStore(); const list = ref([])
const load = async () => { list.value = (await request.get('/reviews')).data }
const hide = async r => { await request.put('/reviews/' + r.id + '/hide'); ElMessage.success('已更新'); load() }
const del = async r => { await ElMessageBox.confirm('确认删除该评价？', '提示'); await request.delete('/reviews/' + r.id); ElMessage.success('已删除'); load() }
onMounted(load)
</script>
