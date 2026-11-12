<template>
  <div>
    <div class="banner">
      <div><h2>🐾 你好，{{ auth.user && auth.user.realName }}！</h2>
      <p>{{ bannerText }}</p></div>
      <div class="banner-emoji">🐶🐱🦮</div>
    </div>
    <el-row :gutter="16">
      <el-col :span="6" v-for="c in cards" :key="c.label"><el-card><div class="stat-card">
        <div class="stat-icon" :style="{ background: c.bg }">{{ c.icon }}</div>
        <div><div class="stat-num">{{ c.value }}</div><div class="stat-label">{{ c.label }}</div></div>
      </div></el-card></el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="16"><el-card><template #header>📈 近30天订单量与营收趋势</template><div ref="trendEl" style="height:320px"></div></el-card></el-col>
      <el-col :span="8"><el-card><template #header>🧡 服务类型分布</template><div ref="svcEl" style="height:320px"></div></el-card></el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="8"><el-card><template #header>🐾 宠物种类分布</template><div ref="petEl" style="height:280px"></div></el-card></el-col>
      <el-col :span="8"><el-card><template #header>🏆 宠托师接单 TOP5</template><div ref="topEl" style="height:280px"></div></el-card></el-col>
      <el-col :span="8"><el-card><template #header>📊 订单状态与入住率</template>
        <div ref="statusEl" style="height:190px"></div>
        <div style="padding:4px 10px"><div class="stat-label">寄养空间入住率（{{ d.occupied || 0 }}/{{ d.totalCapacity || 0 }}）</div>
        <el-progress :percentage="d.occupancyRate || 0" :stroke-width="16" color="#f97316" text-inside style="margin-top:6px" /></div>
      </el-card></el-col>
    </el-row>
  </div>
</template>
<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '../utils/request'
import { useAuthStore } from '../store/auth'
import { money } from '../utils/format'
const auth = useAuthStore(); const d = ref({})
const trendEl = ref(), svcEl = ref(), petEl = ref(), topEl = ref(), statusEl = ref()
const STATUS_CN = { PENDING: '待确认', CONFIRMED: '已确认', ACCEPTED: '已接单', IN_SERVICE: '服务中', COMPLETED: '已完成', CANCELLED: '已取消', REFUNDED: '已退款' }
const bannerText = computed(() => auth.isAdmin ? '平台运营总览：订单、营收、入住率一屏尽览。' : auth.isSitter ? '今日也要元气满满地照顾毛孩子们呀！记得及时填写服务报告。' : '爱宠的疫苗/驱虫提醒、订单进度都在这里，出行托付更安心。')
const cards = computed(() => [
  { icon: '📦', label: auth.isAdmin ? '总订单数' : '我的订单数', value: d.value.totalOrders ?? 0, bg: '#ffedd5' },
  { icon: '💰', label: auth.isAdmin ? '平台总营收（元）' : '累计消费/收入（元）', value: money(d.value.totalRevenue), bg: '#fef3c7' },
  { icon: '🐾', label: '宠物数量', value: d.value.totalPets ?? 0, bg: '#ffe4e6' },
  { icon: '⭐', label: '好评率（%）', value: d.value.goodRate ?? 100, bg: '#dcfce7' },
])
const pie = (el, data) => { if (!el) return; echarts.init(el).setOption({ tooltip: {}, legend: { bottom: 0 }, color: ['#f97316','#fb923c','#fbbf24','#84cc16','#38bdf8','#f472b6','#a78bfa'], series: [{ type: 'pie', radius: ['42%','68%'], center: ['50%','44%'], data, label: { formatter: '{b} {c}' } }] }) }
onMounted(async () => {
  const r = await request.get('/stats/dashboard'); d.value = r.data
  await nextTick()
  const t = d.value.trend || []
  echarts.init(trendEl.value).setOption({
    tooltip: { trigger: 'axis' }, legend: { data: ['订单量','营收(元)'] }, grid: { left: 44, right: 48, top: 36, bottom: 30 },
    xAxis: { type: 'category', data: t.map(x => x.date) },
    yAxis: [{ type: 'value', name: '订单' }, { type: 'value', name: '元' }],
    series: [
      { name: '订单量', type: 'bar', data: t.map(x => x.orders), itemStyle: { color: '#fb923c', borderRadius: [4,4,0,0] } },
      { name: '营收(元)', type: 'line', yAxisIndex: 1, smooth: true, data: t.map(x => x.revenue), itemStyle: { color: '#ea580c' }, areaStyle: { color: 'rgba(249,115,22,.12)' } }
    ]
  })
  pie(svcEl.value, Object.entries(d.value.serviceDist || {}).map(([name, value]) => ({ name, value })))
  pie(petEl.value, Object.entries(d.value.speciesDist || {}).map(([name, value]) => ({ name, value })))
  const top = [...(d.value.topSitters || [])].reverse()
  echarts.init(topEl.value).setOption({ tooltip: {}, grid: { left: 60, right: 20, top: 16, bottom: 24 },
    xAxis: { type: 'value' }, yAxis: { type: 'category', data: top.map(x => x.name) },
    series: [{ type: 'bar', data: top.map(x => x.orders), itemStyle: { color: '#f97316', borderRadius: [0,6,6,0] }, label: { show: true, position: 'right' } }] })
  pie(statusEl.value, Object.entries(d.value.statusDist || {}).map(([k, value]) => ({ name: STATUS_CN[k] || k, value })))
})
</script>
<style scoped>
.banner { background: linear-gradient(120deg,#f97316,#fb923c); color: #fff; border-radius: 18px; padding: 22px 26px; display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; box-shadow: 0 8px 24px rgba(249,115,22,.25); }
.banner h2 { margin: 0 0 6px; }
.banner p { margin: 0; opacity: .92; font-size: 13.5px; }
.banner-emoji { font-size: 44px; }
</style>
