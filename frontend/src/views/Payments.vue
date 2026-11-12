<template>
  <div>
    <div class="page-head"><div><h2>💳 支付流水</h2><div class="sub">平台担保支付 · 共 {{ list.length }} 条 · 净额 ¥{{ money(net) }}</div></div></div>
    <el-card><el-table :data="list" stripe>
      <el-table-column prop="txnNo" label="流水号" width="210" />
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="userName" label="用户" width="110" />
      <el-table-column label="类型" width="90"><template #default="{ row }"><el-tag :type="row.type === 'PAY' ? 'success' : 'danger'">{{ row.type === 'PAY' ? '支付' : '退款' }}</el-tag></template></el-table-column>
      <el-table-column label="金额" width="120"><template #default="{ row }"><b :style="{ color: row.type === 'PAY' ? '#16a34a' : '#dc2626' }">{{ row.type === 'PAY' ? '+' : '−' }}¥{{ money(row.amount) }}</b></template></el-table-column>
      <el-table-column prop="method" label="方式" width="110" />
      <el-table-column prop="status" label="状态" width="90" />
      <el-table-column prop="createTime" label="时间" />
    </el-table></el-card>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../utils/request'
import { money } from '../utils/format'
const list = ref([])
const net = computed(() => list.value.reduce((s, p) => s + (p.type === 'PAY' ? Number(p.amount) : -Number(p.amount)), 0))
onMounted(async () => { list.value = (await request.get('/payments')).data })
</script>
