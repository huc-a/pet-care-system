<template>
  <div>
    <div class="page-head"><div><h2>📅 预约日历</h2><div class="sub">{{ year }} 年 {{ month }} 月 · 点击日期查看当天订单</div></div>
      <div style="display:flex;gap:8px"><el-button @click="move(-1)">← 上月</el-button><el-button @click="today">本月</el-button><el-button @click="move(1)">下月 →</el-button></div></div>
    <el-row :gutter="16">
      <el-col :span="16"><el-card>
        <div class="cal-head"><span v-for="w in ['日','一','二','三','四','五','六']" :key="w">周{{ w }}</span></div>
        <div class="cal-grid">
          <div v-for="c in cells" :key="c.key" class="cell" :class="{ dim: !c.inMonth, today: c.isToday, sel: sel === c.date }" @click="sel = c.date">
            <div class="dnum">{{ c.day }}</div>
            <div v-if="counts[c.date]" class="cnt">🐾 {{ counts[c.date] }} 单</div>
          </div>
        </div>
      </el-card></el-col>
      <el-col :span="8"><el-card><template #header>📋 {{ sel || '请选择日期' }} 的订单</template>
        <div v-for="oo in dayOrders" :key="oo.id" class="dord" @click="$router.push('/orders/' + oo.id)">
          <b>{{ oo.serviceName }}</b> · {{ oo.petNames }}<br/>
          <span style="color:#78716c;font-size:12.5px">{{ oo.orderNo }} · <el-tag size="small" :type="STATUS[oo.status] ? STATUS[oo.status].type : 'info'">{{ statusLabel(oo.status) }}</el-tag> · ¥{{ money(oo.totalAmount) }}</span>
        </div>
        <el-empty v-if="!dayOrders.length" description="当天暂无订单" :image-size="60" />
      </el-card></el-col>
    </el-row>
  </div>
</template>
<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import request from '../utils/request'
import { STATUS, statusLabel, money } from '../utils/format'
const now = new Date(); const year = ref(now.getFullYear()); const month = ref(now.getMonth() + 1)
const counts = ref({}); const byDay = ref({}); const sel = ref(now.toISOString().slice(0, 10))
const fmt = (y, m, d) => y + '-' + String(m).padStart(2, '0') + '-' + String(d).padStart(2, '0')
const cells = computed(() => {
  const first = new Date(year.value, month.value - 1, 1); const startDay = first.getDay()
  const daysIn = new Date(year.value, month.value, 0).getDate(); const daysPrev = new Date(year.value, month.value - 1, 0).getDate()
  const out = []; const todayStr = new Date().toISOString().slice(0, 10)
  for (let i = 0; i < 42; i++) {
    let d, inMonth = true, y = year.value, m = month.value
    if (i < startDay) { d = daysPrev - startDay + 1 + i; inMonth = false; const dt = new Date(y, m - 2, d); y = dt.getFullYear(); m = dt.getMonth() + 1 }
    else if (i - startDay >= daysIn) { d = i - startDay - daysIn + 1; inMonth = false; const dt = new Date(y, m, d); y = dt.getFullYear(); m = dt.getMonth() + 1 }
    else d = i - startDay + 1
    const date = fmt(y, m, d)
    out.push({ key: date + i, day: d, inMonth, date, isToday: date === todayStr })
  }
  return out
})
const dayOrders = computed(() => byDay.value[sel.value] || [])
const load = async () => { const r = await request.get('/orders/calendar', { params: { year: year.value, month: month.value } }); counts.value = r.data.counts; byDay.value = r.data.orders }
const move = n => { const d = new Date(year.value, month.value - 1 + n, 1); year.value = d.getFullYear(); month.value = d.getMonth() + 1 }
const today = () => { const t = new Date(); year.value = t.getFullYear(); month.value = t.getMonth() + 1; sel.value = t.toISOString().slice(0, 10) }
watch([year, month], load)
onMounted(load)
</script>
<style scoped>
.cal-head { display: grid; grid-template-columns: repeat(7, 1fr); text-align: center; font-weight: 700; color: #9a3412; padding-bottom: 8px; }
.cal-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 6px; }
.cell { min-height: 74px; border: 1.5px solid #ffedd5; border-radius: 12px; padding: 6px 8px; cursor: pointer; background: #fffbeb; }
.cell:hover { border-color: #fb923c; }
.cell.dim { opacity: .38; }
.cell.today { border-color: #ea580c; background: #ffedd5; }
.cell.sel { background: linear-gradient(135deg,#fff7ed,#ffedd5); border-color: #f97316; box-shadow: 0 0 0 2px rgba(249,115,22,.25); }
.dnum { font-weight: 700; }
.cnt { margin-top: 6px; font-size: 12px; background: #f97316; color: #fff; border-radius: 999px; padding: 1px 8px; display: inline-block; }
.dord { padding: 10px 4px; border-bottom: 1px dashed #fed7aa; cursor: pointer; font-size: 13.5px; }
</style>
