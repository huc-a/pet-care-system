<template>
  <div>
    <div class="page-head"><div><h2>🎯 接单大厅</h2><div class="sub">已确认待接单订单 · 先到先得，抢单后进入「我的订单」</div></div>
      <el-button @click="load">刷新</el-button></div>
    <el-row :gutter="16">
      <el-col :span="8" v-for="o in list" :key="o.id" style="margin-bottom:16px">
        <el-card>
          <div style="display:flex;justify-content:space-between"><b>{{ o.serviceName }}</b><el-tag type="primary">已确认</el-tag></div>
          <div style="font-size:13.5px;color:#57534e;line-height:2;margin-top:8px">
            <div>🐾 宠物：{{ o.petNames }}（{{ o.petCount }}只）</div>
            <div>📅 {{ o.startDate }} ~ {{ o.endDate }} · 每日 {{ o.timesPerDay }} 次</div>
            <div>📍 {{ o.area }} · {{ o.address }}</div>
            <div>💰 报酬：<b style="color:#ea580c">¥{{ money(o.totalAmount) }}</b></div>
            <div v-if="o.remark">📝 {{ o.remark }}</div>
          </div>
          <el-button type="primary" style="width:100%;margin-top:10px" @click="accept(o)">立即抢单</el-button>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!list.length" description="大厅暂无待接订单，休息一下吧 ☕" />
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { money } from '../utils/format'
const list = ref([]); const router = useRouter()
const load = async () => { list.value = (await request.get('/orders/hall')).data }
const accept = async o => { await request.put('/orders/' + o.id + '/accept'); ElMessage.success('抢单成功！'); router.push('/orders/' + o.id) }
onMounted(load)
</script>
