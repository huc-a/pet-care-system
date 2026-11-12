<template>
  <div>
    <div class="page-head"><div><h2>📦 {{ title }}</h2><div class="sub">共 {{ filtered.length }} 笔订单</div></div>
      <div style="display:flex;gap:10px">
        <el-input v-model="kw" placeholder="搜索订单号/宠物/服务" clearable style="width:220px" />
        <el-select v-model="status" clearable placeholder="全部状态" style="width:140px">
          <el-option v-for="(v, k) in STATUS" :key="k" :label="v.label" :value="k" /></el-select>
        <el-button type="primary" v-if="auth.isOwner" @click="$router.push('/booking')">+ 新建预约</el-button>
      </div></div>
    <el-card><el-table :data="filtered" stripe @row-click="r => $router.push('/orders/' + r.id)" style="cursor:pointer">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="petNames" label="宠物" width="130" />
      <el-table-column prop="serviceName" label="服务" width="130" />
      <el-table-column label="服务日期" width="200"><template #default="{ row }">{{ row.startDate }} ~ {{ row.endDate }}</template></el-table-column>
      <el-table-column prop="sitterName" label="宠托师" width="100"><template #default="{ row }">{{ row.sitterName || '待接单' }}</template></el-table-column>
      <el-table-column label="金额" width="110"><template #default="{ row }">¥{{ money(row.totalAmount) }}</template></el-table-column>
      <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="STATUS[row.status] ? STATUS[row.status].type : 'info'">{{ statusLabel(row.status) }}</el-tag></template></el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="170" />
      <el-table-column label="操作" width="90"><template #default="{ row }"><el-button size="small" @click.stop="$router.push('/orders/' + row.id)">详情</el-button></template></el-table-column>
    </el-table></el-card>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../utils/request'
import { useAuthStore } from '../store/auth'
import { STATUS, statusLabel, money } from '../utils/format'
const auth = useAuthStore(); const list = ref([]); const kw = ref(''); const status = ref('')
const title = computed(() => auth.isAdmin ? '全部订单' : auth.isSitter ? '我的订单' : '我的订单')
const filtered = computed(() => list.value.filter(o =>
  (!status.value || o.status === status.value) &&
  (!kw.value || o.orderNo.includes(kw.value) || (o.petNames || '').includes(kw.value) || (o.serviceName || '').includes(kw.value))))
onMounted(async () => { list.value = (await request.get('/orders')).data })
</script>
