<template>
  <div>
    <div class="page-head"><div><h2>💉 健康档案与提醒</h2><div class="sub">疫苗 / 驱虫 / 体检 / 体重记录 · 30 天内到期自动提醒</div></div></div>
    <el-card v-if="reminders.length" style="margin-bottom:16px;border:1.5px solid #fdba74 !important">
      <template #header>⏰ 到期提醒（{{ reminders.length }} 条）</template>
      <div v-for="(r, i) in reminders" :key="i" class="rem">
        <span>{{ r.overdue ? '🔴' : '🟠' }} <b>{{ r.petName }}</b>（{{ r.breed }}）的「{{ r.record.name }}」{{ r.text }} · 到期日 {{ r.record.nextDueDate }}</span>
        <el-tag :type="r.overdue ? 'danger' : 'warning'" size="small">{{ r.overdue ? '已过期' : '即将到期' }}</el-tag>
      </div>
    </el-card>
    <el-row :gutter="16">
      <el-col :span="7"><el-card><template #header>🐾 选择宠物</template>
        <div v-for="p in pets" :key="p.id" class="petrow" :class="{ on: cur && cur.id === p.id }" @click="select(p)">
          <img v-if="p.photoUrl" :src="p.photoUrl" style="width:46px;height:46px;border-radius:50%;object-fit:cover" /><span v-else style="font-size:26px">{{ p.emoji || '🐾' }}</span><div><b>{{ p.name }}</b><div style="color:#78716c;font-size:12px">{{ p.species }} · {{ p.breed }} · {{ p.weight }}kg</div></div>
        </div></el-card></el-col>
      <el-col :span="17">
        <el-card><template #header><span>📋 {{ cur ? cur.name + ' 的健康记录' : '健康记录' }}</span>
          <el-button type="primary" size="small" style="float:right" v-if="cur" @click="dlg = true">+ 新增记录</el-button></template>
          <el-table :data="records" size="small" stripe>
            <el-table-column prop="type" label="类型" width="80" />
            <el-table-column prop="name" label="项目" />
            <el-table-column prop="recordDate" label="记录日期" width="110" />
            <el-table-column prop="nextDueDate" label="下次到期" width="110" />
            <el-table-column label="体重" width="90"><template #default="{ row }">{{ row.weight ? row.weight + ' kg' : '-' }}</template></el-table-column>
            <el-table-column prop="note" label="备注" />
            <el-table-column label="操作" width="70"><template #default="{ row }"><el-button size="small" type="danger" plain @click="del(row)">删除</el-button></template></el-table-column>
          </el-table>
          <el-empty v-if="cur && !records.length" description="暂无记录" :image-size="60" />
        </el-card>
        <el-card v-if="trend.length" style="margin-top:16px"><template #header>⚖️ 体重趋势</template><div ref="chartEl" style="height:240px"></div></el-card>
      </el-col>
    </el-row>
    <el-dialog v-model="dlg" title="新增健康记录" width="500px">
      <el-form :model="form" label-width="92px">
        <el-form-item label="类型"><el-select v-model="form.type" style="width:100%"><el-option label="疫苗" value="疫苗" /><el-option label="驱虫" value="驱虫" /><el-option label="体检" value="体检" /><el-option label="体重" value="体重" /></el-select></el-form-item>
        <el-form-item label="项目名称"><el-input v-model="form.name" placeholder="如：狂犬疫苗 / 体外驱虫" /></el-form-item>
        <el-form-item label="记录日期"><el-date-picker v-model="form.recordDate" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="下次到期"><el-date-picker v-model="form.nextDueDate" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="体重(kg)" v-if="form.type === '体重'"><el-input-number v-model="form.weight" :step="0.1" :precision="1" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.note" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dlg=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import request from '../utils/request'
const route = useRoute(); const pets = ref([]); const cur = ref(null); const records = ref([]); const reminders = ref([]); const trend = ref([]); const dlg = ref(false); const chartEl = ref()
const form = reactive({ type: '疫苗', name: '', recordDate: '', nextDueDate: '', weight: null, note: '' })
const select = async p => {
  cur.value = p
  records.value = (await request.get('/health/pet/' + p.id)).data
  trend.value = (await request.get('/health/weight-trend/' + p.id)).data
  await nextTick()
  if (chartEl.value && trend.value.length) echarts.init(chartEl.value).setOption({
    tooltip: {}, grid: { left: 44, right: 20, top: 24, bottom: 28 },
    xAxis: { type: 'category', data: trend.value.map(t => t.date) },
    yAxis: { type: 'value', name: 'kg', scale: true },
    series: [{ type: 'line', smooth: true, data: trend.value.map(t => t.weight), itemStyle: { color: '#f97316' }, areaStyle: { color: 'rgba(249,115,22,.15)' }, label: { show: true } }]
  })
}
const save = async () => { await request.post('/health', { ...form, petId: cur.value.id }); ElMessage.success('已保存'); dlg.value = false; select(cur.value); loadReminders() }
const del = async r => { await request.delete('/health/' + r.id); ElMessage.success('已删除'); select(cur.value); loadReminders() }
const loadReminders = async () => { reminders.value = (await request.get('/health/reminders')).data }
onMounted(async () => {
  pets.value = (await request.get('/pets')).data
  await loadReminders()
  const target = pets.value.find(p => p.id === Number(route.query.petId)) || pets.value[0]
  if (target) select(target)
})
</script>
<style scoped>
.petrow { display: flex; gap: 12px; align-items: center; padding: 10px; border-radius: 12px; cursor: pointer; }
.petrow:hover { background: #fff7ed; }
.petrow.on { background: #ffedd5; }
.rem { display: flex; justify-content: space-between; padding: 8px 2px; border-bottom: 1px dashed #fed7aa; font-size: 13.5px; }
</style>
