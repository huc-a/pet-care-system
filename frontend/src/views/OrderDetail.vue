<template>
  <div v-if="o">
    <div class="page-head"><div><h2>📋 订单详情 · {{ o.orderNo }}</h2>
      <div class="sub">{{ o.serviceName }} · {{ o.petNames }} · {{ o.startDate }} ~ {{ o.endDate }}</div></div>
      <el-tag size="large" :type="STATUS[o.status] ? STATUS[o.status].type : 'info'">{{ statusLabel(o.status) }}</el-tag></div>
    <el-row :gutter="16">
      <el-col :span="16">
        <el-card><template #header>🚶 订单进度</template>
          <el-steps :active="stepActive" finish-status="success" align-center>
            <el-step title="待确认" /><el-step title="已确认" /><el-step title="已接单" /><el-step title="服务中" /><el-step title="已完成" />
          </el-steps>
          <div style="margin-top:18px;display:flex;gap:10px;flex-wrap:wrap">
            <el-button v-if="canConfirm" type="primary" @click="setStatus('CONFIRMED')">确认订单</el-button>
            <el-button v-if="canStart" type="primary" @click="setStatus('IN_SERVICE')">开始服务</el-button>
            <el-button v-if="canComplete" type="success" @click="setStatus('COMPLETED')">完成服务</el-button>
            <el-button v-if="o.spaceId && ['CONFIRMED','ACCEPTED'].includes(o.status) && !auth.isOwner" type="warning" @click="doCheckin">办理入住</el-button>
            <el-button v-if="o.spaceId && o.status === 'IN_SERVICE' && !auth.isOwner" type="warning" @click="doCheckout">退房结算</el-button>
            <el-button v-if="canReport" type="primary" plain @click="reportDlg = true">📝 填写服务报告</el-button>
            <el-button v-if="canCancel" type="danger" plain @click="setStatus('CANCELLED')">取消订单</el-button>
            <el-button v-if="auth.isAdmin && o.status === 'COMPLETED'" type="danger" plain @click="setStatus('REFUNDED')">退款</el-button>
            <el-button v-if="auth.isOwner && o.status === 'COMPLETED' && !reviewed" type="warning" @click="reviewDlg = true">⭐ 评价本次服务</el-button>
            <el-select v-if="auth.isAdmin && !o.sitterId" v-model="assignSitter" placeholder="指派宠托师" style="width:190px">
              <el-option v-for="it in sitters" :key="it.user.id" :label="it.user.realName" :value="it.user.id" /></el-select>
            <el-button v-if="auth.isAdmin && !o.sitterId" @click="doAssign">指派</el-button>
          </div>
        </el-card>
        <el-card style="margin-top:16px"><template #header>📝 服务报告（{{ reports.length }}）</template>
          <div v-for="r in reports" :key="r.id" class="report">
            <div style="display:flex;justify-content:space-between"><b>{{ r.reportDate }} · {{ r.sitterName }}</b>
              <span>🌡 {{ r.temperature }}℃ · 食欲{{ r.appetite }} · 精神{{ r.spirit }} · 排泄{{ r.excretion }}</span></div>
            <div style="margin:8px 0;display:flex;gap:6px;flex-wrap:wrap">
              <el-tag v-if="r.fed" type="success" size="small">✔ 已喂食</el-tag>
              <el-tag v-if="r.waterChanged" type="success" size="small">✔ 已换水</el-tag>
              <el-tag v-if="r.litterCleaned" type="success" size="small">✔ 已铲屎/清洁</el-tag>
              <el-tag v-if="r.played" type="success" size="small">✔ 已陪玩</el-tag>
              <el-tag v-if="r.walked" type="success" size="small">✔ 已遛弯</el-tag>
              <el-tag v-if="r.bathed" type="success" size="small">✔ 已洗护</el-tag>
            </div>
            <div style="color:#57534e;font-size:13.5px">{{ r.content }}</div>
            <div v-if="r.photoUrl" class="rep-photos"><img v-for="(u, i) in r.photoUrl.split(',').filter(Boolean)" :key="i" :src="u" class="rep-photo" /></div>
          </div>
          <el-empty v-if="!reports.length" description="暂无服务报告" :image-size="60" />
        </el-card>
        <el-card v-if="tracks.length" style="margin-top:16px"><template #header>🦮 遛狗轨迹回放</template>
          <div v-for="t in tracks" :key="t.id" style="margin-bottom:14px">
            <div style="display:flex;gap:16px;align-items:center;margin-bottom:8px">
              <b>{{ t.petName }} · {{ t.walkDate }}</b>
              <span>里程 {{ t.distanceKm }} km</span><span>时长 {{ t.durationMin }} 分钟</span><span>配速 {{ t.paceMinPerKm }} 分/km</span>
              <el-button size="small" type="primary" @click="play(t)">▶ 回放轨迹</el-button>
            </div>
            <svg :viewBox="viewBox(t)" class="track-svg">
              <polyline :points="polyPoints(t)" fill="none" stroke="#fdba74" stroke-width="3" stroke-linejoin="round" />
              <polyline v-if="anim[t.id]" :points="animPoints(t)" fill="none" stroke="#ea580c" stroke-width="3.5" stroke-linejoin="round" />
              <circle :cx="svgPts(t)[0].x" :cy="svgPts(t)[0].y" r="5" fill="#22c55e" />
              <circle :cx="svgPts(t)[svgPts(t).length-1].x" :cy="svgPts(t)[svgPts(t).length-1].y" r="5" fill="#ef4444" />
              <circle v-if="anim[t.id]" :cx="animHead(t).x" :cy="animHead(t).y" r="7" fill="#f97316" stroke="#fff" stroke-width="2" />
            </svg>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card><template #header>💰 价格明细</template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="宠主">{{ o.ownerName }}</el-descriptions-item>
            <el-descriptions-item label="宠托师">{{ o.sitterName || '待接单' }}</el-descriptions-item>
            <el-descriptions-item label="寄养空间" v-if="o.spaceName">{{ o.spaceName }}</el-descriptions-item>
            <el-descriptions-item label="服务地址">{{ o.address || '-' }}</el-descriptions-item>
            <el-descriptions-item label="附加服务">{{ o.addonNames || '无' }}</el-descriptions-item>
            <el-descriptions-item label="基础费用">¥{{ money(o.baseAmount) }}</el-descriptions-item>
            <el-descriptions-item label="附加费用">¥{{ money(o.addonAmount) }}</el-descriptions-item>
            <el-descriptions-item label="节假日加价">×{{ o.holidayFactor }} · ¥{{ money(o.holidayAmount) }}</el-descriptions-item>
            <el-descriptions-item label="多宠折扣">−¥{{ money(o.multiPetDiscount) }}</el-descriptions-item>
            <el-descriptions-item label="长住折扣">−¥{{ money(o.longStayDiscount) }}</el-descriptions-item>
            <el-descriptions-item label="优惠券">−¥{{ money(o.couponDiscount) }}</el-descriptions-item>
            <el-descriptions-item label="实付总额"><b style="color:#ea580c;font-size:17px">¥{{ money(o.totalAmount) }}</b></el-descriptions-item>
          </el-descriptions>
          <p style="color:#78716c;font-size:12.5px;line-height:1.8">{{ o.priceDetail }}</p>
          <p v-if="o.remark" style="font-size:13px">备注：{{ o.remark }}</p>
        </el-card>
        <el-card v-if="orderReviews.length" style="margin-top:16px"><template #header>⭐ 订单评价</template>
          <div v-for="r in orderReviews" :key="r.id"><el-rate :model-value="r.rating" disabled />
            <div style="font-size:13.5px;margin-top:4px">{{ r.content }}</div>
            <div style="color:#a8a29e;font-size:12px">{{ r.ownerName }} · {{ r.createTime }}</div></div>
        </el-card>
      </el-col>
    </el-row>
    <el-dialog v-model="reportDlg" title="填写服务报告" width="560px">
      <el-form :model="rf" label-width="92px">
        <el-form-item label="服务清单">
          <el-checkbox v-model="rf.fed">已喂食</el-checkbox><el-checkbox v-model="rf.waterChanged">已换水</el-checkbox>
          <el-checkbox v-model="rf.litterCleaned">已铲屎/清洁</el-checkbox><el-checkbox v-model="rf.played">已陪玩</el-checkbox>
          <el-checkbox v-model="rf.walked">已遛弯</el-checkbox><el-checkbox v-model="rf.bathed">已洗护</el-checkbox></el-form-item>
        <el-form-item label="食欲"><el-select v-model="rf.appetite"><el-option label="食欲旺盛" value="食欲旺盛" /><el-option label="食欲良好" value="食欲良好" /><el-option label="食欲不振" value="食欲不振" /></el-select></el-form-item>
        <el-form-item label="精神状态"><el-select v-model="rf.spirit"><el-option label="活泼" value="活泼" /><el-option label="放松" value="放松" /><el-option label="安静" value="安静" /><el-option label="萎靡" value="萎靡" /></el-select></el-form-item>
        <el-form-item label="排泄"><el-select v-model="rf.excretion"><el-option label="正常" value="正常" /><el-option label="软便" value="软便" /><el-option label="异常" value="异常" /></el-select></el-form-item>
        <el-form-item label="体温(℃)"><el-input-number v-model="rf.temperature" :step="0.1" :precision="1" :min="35" :max="42" /></el-form-item>
        <el-form-item label="报告内容"><el-input v-model="rf.content" type="textarea" :rows="3" placeholder="今天的服务情况、宠物状态……" /></el-form-item>
        <el-form-item label="服务照片"><ImageUpload v-model="reportPhotos" multiple :max="6" :size="86" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="reportDlg=false">取消</el-button><el-button type="primary" @click="submitReport">提交报告</el-button></template>
    </el-dialog>
    <el-dialog v-model="reviewDlg" title="评价本次服务" width="480px">
      <el-form label-width="70px">
        <el-form-item label="评分"><el-rate v-model="reviewForm.rating" show-score /></el-form-item>
        <el-form-item label="标签"><el-input v-model="reviewForm.tags" placeholder="如：细心,准时（逗号分隔）" /></el-form-item>
        <el-form-item label="评价"><el-input v-model="reviewForm.content" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="reviewDlg=false">取消</el-button><el-button type="primary" @click="submitReview">提交评价</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { useAuthStore } from '../store/auth'
import { STATUS, statusLabel, money } from '../utils/format'
import ImageUpload from '../components/ImageUpload.vue'
const route = useRoute(); const auth = useAuthStore()
const o = ref(null); const reports = ref([]); const tracks = ref([]); const reviewsAll = ref([]); const sitters = ref([])
const reportDlg = ref(false); const reviewDlg = ref(false); const assignSitter = ref(null)
const reportPhotos = ref([])
const rf = reactive({ fed: true, waterChanged: true, litterCleaned: false, played: false, walked: false, bathed: false, appetite: '食欲良好', spirit: '放松', excretion: '正常', temperature: 38.5, content: '' })
const reviewForm = reactive({ rating: 5, tags: '', content: '' })
const anim = ref({}); const animCount = ref({}); let timer = null
const stepActive = computed(() => ({ PENDING: 0, CONFIRMED: 1, ACCEPTED: 2, IN_SERVICE: 3, COMPLETED: 5, CANCELLED: 0, REFUNDED: 5 }[o.value.status] ?? 0))
const canConfirm = computed(() => o.value.status === 'PENDING' && !auth.isOwner)
const canStart = computed(() => o.value.status === 'ACCEPTED' && !auth.isOwner)
const canComplete = computed(() => o.value.status === 'IN_SERVICE' && !o.value.spaceId && !auth.isOwner)
const canReport = computed(() => ['ACCEPTED','IN_SERVICE'].includes(o.value.status) && (auth.isSitter || auth.isAdmin))
const canCancel = computed(() => ['PENDING','CONFIRMED','ACCEPTED'].includes(o.value.status) && !auth.isSitter)
const orderReviews = computed(() => reviewsAll.value.filter(r => r.orderId === o.value.id))
const reviewed = computed(() => orderReviews.value.length > 0)
const load = async () => {
  o.value = (await request.get('/orders/' + route.params.id)).data
  reports.value = (await request.get('/reports/order/' + o.value.id)).data
  tracks.value = (await request.get('/tracks/order/' + o.value.id)).data
  try { reviewsAll.value = (await request.get('/reviews')).data } catch (e) {}
  if (auth.isAdmin) sitters.value = (await request.get('/sitters')).data
}
const setStatus = async s => { await request.put('/orders/' + o.value.id + '/status', { status: s }); ElMessage.success('状态已更新'); load() }
const doCheckin = async () => { await request.post('/checkins/order/' + o.value.id + '/checkin'); ElMessage.success('入住成功，空间占用 +1'); load() }
const doCheckout = async () => { await request.post('/checkins/order/' + o.value.id + '/checkout'); ElMessage.success('退房成功，空间占用 −1，订单已完成'); load() }
const doAssign = async () => { if (!assignSitter.value) return ElMessage.warning('请选择宠托师'); await request.put('/orders/' + o.value.id + '/assign', { sitterId: assignSitter.value }); ElMessage.success('指派成功'); load() }
const submitReport = async () => { await request.post('/reports', { ...rf, photoUrl: reportPhotos.value.join(','), orderId: o.value.id }); ElMessage.success('报告已提交'); reportDlg.value = false; reportPhotos.value = []; load() }
const submitReview = async () => { await request.post('/reviews', { ...reviewForm, orderId: o.value.id }); ElMessage.success('评价成功'); reviewDlg.value = false; load() }
// 轨迹 SVG
const rawPts = t => { try { return JSON.parse(t.points || '[]') } catch (e) { return [] } }
const svgPts = t => {
  const pts = rawPts(t); if (!pts.length) return [{ x: 0, y: 0 }]
  const lngs = pts.map(p => p.lng), lats = pts.map(p => p.lat)
  const minLng = Math.min(...lngs), maxLng = Math.max(...lngs), minLat = Math.min(...lats), maxLat = Math.max(...lats)
  const W = 640, H = 260, pad = 24
  return pts.map(p => ({ x: pad + (maxLng === minLng ? 0.5 : (p.lng - minLng) / (maxLng - minLng)) * (W - 2 * pad), y: H - pad - (maxLat === minLat ? 0.5 : (p.lat - minLat) / (maxLat - minLat)) * (H - 2 * pad) }))
}
const viewBox = () => '0 0 640 260'
const polyPoints = t => svgPts(t).map(p => p.x.toFixed(1) + ',' + p.y.toFixed(1)).join(' ')
const animPoints = t => svgPts(t).slice(0, animCount.value[t.id] || 0).map(p => p.x.toFixed(1) + ',' + p.y.toFixed(1)).join(' ')
const animHead = t => { const pts = svgPts(t); const i = Math.min((animCount.value[t.id] || 1) - 1, pts.length - 1); return pts[Math.max(0, i)] }
const play = t => {
  anim.value[t.id] = true; animCount.value[t.id] = 1
  if (timer) clearInterval(timer)
  const total = svgPts(t).length
  timer = setInterval(() => { animCount.value[t.id]++; if (animCount.value[t.id] > total) { clearInterval(timer); timer = null } }, 160)
}
onUnmounted(() => { if (timer) clearInterval(timer) })
onMounted(load)
</script>
<style scoped>
.report { padding: 12px 4px; border-bottom: 1px dashed #fed7aa; }
.rep-photos { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 8px; }
.rep-photo { width: 108px; height: 108px; object-fit: cover; border-radius: 10px; border: 1px solid #fed7aa; }
.track-svg { width: 100%; height: 260px; background: linear-gradient(180deg,#fff7ed,#ffedd5); border-radius: 12px; border: 1px solid #fed7aa; }
</style>
