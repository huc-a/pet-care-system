<template>
  <div>
    <div class="page-head"><div><h2>🧑‍🤝‍🧑 宠托师</h2><div class="sub">平台认证宠托师 · 可按区域查看擅长品种与评分</div></div></div>
    <el-row :gutter="16">
      <el-col :span="8" v-for="it in list" :key="it.profile.id" style="margin-bottom:16px">
        <el-card>
          <div style="display:flex;gap:14px;align-items:center">
            <img v-if="it.user.avatarUrl" :src="it.user.avatarUrl" class="avatar-img" /><div v-else class="avatar">{{ it.user.avatarEmoji || '🧑' }}</div>
            <div><h3 style="margin:0">{{ it.user.realName }} <el-tag size="small" type="success">{{ it.profile.certStatus }}</el-tag></h3>
            <div style="color:#78716c;font-size:13px;margin-top:4px">📍 {{ it.profile.area }} · 从业 {{ it.profile.experienceYears }} 年 · 距您约 {{ it.profile.distanceKm }} km</div></div>
          </div>
          <div style="margin:12px 0"><el-rate :model-value="Number(it.profile.rating)" disabled show-score :score-template="it.profile.rating + ' 分'" />
            <span style="color:#78716c;font-size:13px"> · 累计接单 {{ it.profile.orderCount }} 单</span></div>
          <div style="font-size:13px;color:#57534e;line-height:1.8">
            <div>擅长：{{ it.profile.specialties }}</div><div>可照护：{{ it.profile.petTypes }}</div>
            <div style="color:#78716c">{{ it.profile.intro }}</div>
          </div>
          <div style="margin-top:12px;display:flex;gap:8px">
            <el-button size="small" @click="detail = it; dlg = true">查看详情</el-button>
            <el-button size="small" type="primary" v-if="auth.isOwner" @click="$router.push('/booking?sitterId=' + it.user.id)">找TA下单</el-button>
            <el-button size="small" v-if="auth.isAdmin" @click="toggle(it)">{{ it.profile.available ? '暂停接单' : '恢复接单' }}</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-dialog v-model="dlg" title="宠托师详情" width="560px">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ detail.user.realName }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ detail.user.phone }}</el-descriptions-item>
          <el-descriptions-item label="服务区域">{{ detail.profile.area }}</el-descriptions-item>
          <el-descriptions-item label="认证状态">{{ detail.profile.certStatus }}</el-descriptions-item>
          <el-descriptions-item label="评分">{{ detail.profile.rating }} 分</el-descriptions-item>
          <el-descriptions-item label="累计接单">{{ detail.profile.orderCount }} 单</el-descriptions-item>
          <el-descriptions-item label="擅长品种" :span="2">{{ detail.profile.specialties }}</el-descriptions-item>
          <el-descriptions-item label="自我介绍" :span="2">{{ detail.profile.intro }}</el-descriptions-item>
        </el-descriptions>
        <h4>📝 TA 收到的评价</h4>
        <div v-for="r in sitterReviews" :key="r.id" class="rev">
          <el-rate :model-value="r.rating" disabled size="small" /> <b>{{ r.ownerName }}</b>
          <span style="color:#78716c;font-size:13px">{{ r.content }}</span>
        </div>
        <el-empty v-if="!sitterReviews.length" description="暂无评价" :image-size="60" />
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { useAuthStore } from '../store/auth'
const auth = useAuthStore(); const list = ref([]); const dlg = ref(false); const detail = ref(null); const sitterReviews = ref([])
const load = async () => { list.value = (await request.get('/sitters')).data }
watch(detail, async v => { if (v) sitterReviews.value = (await request.get('/reviews?sitterId=' + v.user.id)).data })
const toggle = async it => { it.profile.available = !it.profile.available; await request.put('/sitters/' + it.profile.id, it.profile); ElMessage.success('已更新接单状态'); load() }
onMounted(load)
</script>
<style scoped>
.avatar-img { width: 56px; height: 56px; border-radius: 50%; object-fit: cover; flex-shrink: 0; }
.avatar { width: 56px; height: 56px; border-radius: 50%; background: linear-gradient(135deg,#f97316,#fb923c); display: flex; align-items: center; justify-content: center; font-size: 28px; flex-shrink: 0; }
.rev { padding: 8px 0; border-bottom: 1px dashed #fed7aa; display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
</style>
