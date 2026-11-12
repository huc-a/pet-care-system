<template>
  <div>
    <div class="page-head"><div><h2>🎟️ 优惠券</h2><div class="sub">{{ auth.isOwner ? '领券后下单自动抵扣' : '优惠券模板与发放管理' }}</div></div>
      <el-button type="primary" v-if="auth.isAdmin" @click="dlg = true">+ 发放优惠券</el-button></div>
    <h3 style="margin:6px 0 12px">🎁 领券中心</h3>
    <el-row :gutter="16">
      <el-col :span="8" v-for="c in list" :key="c.id" style="margin-bottom:16px">
        <div class="coupon">
          <div class="c-left"><div class="c-amt">{{ c.type === 'DISCOUNT' ? (c.discountRate * 10) + '折' : '¥' + c.offAmount }}</div>
            <div class="c-cond">{{ c.type === 'DISCOUNT' ? '折扣券' : '满 ¥' + c.threshold + ' 可用' }}</div></div>
          <div class="c-right"><b>{{ c.name }}</b>
            <div style="font-size:12.5px;color:#78716c;margin:4px 0">有效期 {{ c.validFrom }} ~ {{ c.validTo }} · 已领 {{ c.claimedCount }}/{{ c.totalCount || '不限' }}</div>
            <el-button size="small" type="primary" v-if="auth.isOwner" @click="claim(c)">立即领取</el-button>
            <el-button size="small" type="danger" plain v-if="auth.isAdmin" @click="del(c)">删除</el-button>
          </div>
        </div>
      </el-col>
    </el-row>
    <template v-if="auth.isOwner">
      <h3 style="margin:10px 0 12px">👛 我的优惠券</h3>
      <el-row :gutter="16">
        <el-col :span="8" v-for="m in mine" :key="m.userCoupon.id" style="margin-bottom:16px">
          <div class="coupon" :class="{ used: m.effectiveStatus !== 'UNUSED' }">
            <div class="c-left"><div class="c-amt">{{ m.coupon.type === 'DISCOUNT' ? (m.coupon.discountRate * 10) + '折' : '¥' + m.coupon.offAmount }}</div>
              <div class="c-cond">{{ { UNUSED: '未使用', USED: '已使用', EXPIRED: '已过期' }[m.effectiveStatus] }}</div></div>
            <div class="c-right"><b>{{ m.coupon.name }}</b>
              <div style="font-size:12.5px;color:#78716c;margin-top:4px">有效期至 {{ m.coupon.validTo }}</div>
              <el-button size="small" type="primary" v-if="m.effectiveStatus === 'UNUSED'" style="margin-top:6px" @click="$router.push('/booking')">去使用</el-button>
            </div>
          </div>
        </el-col>
      </el-row>
      <el-empty v-if="!mine.length" description="还没有领取优惠券" :image-size="60" />
    </template>
    <el-dialog v-model="dlg" title="发放优惠券" width="500px">
      <el-form :model="form" label-width="92px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="form.type" style="width:100%"><el-option label="满减券" value="FULL_REDUCE" /><el-option label="折扣券" value="DISCOUNT" /></el-select></el-form-item>
        <el-form-item label="满减门槛" v-if="form.type === 'FULL_REDUCE'"><el-input-number v-model="form.threshold" :min="0" /></el-form-item>
        <el-form-item label="减免金额" v-if="form.type === 'FULL_REDUCE'"><el-input-number v-model="form.offAmount" :min="1" /></el-form-item>
        <el-form-item label="折扣率" v-if="form.type === 'DISCOUNT'"><el-input-number v-model="form.discountRate" :min="0.1" :max="0.99" :step="0.05" :precision="2" /><span style="color:#a8a29e;font-size:12px;margin-left:8px">0.9 表示9折</span></el-form-item>
        <el-form-item label="发放总量"><el-input-number v-model="form.totalCount" :min="1" /></el-form-item>
        <el-form-item label="有效期"><el-date-picker v-model="vrange" type="daterange" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dlg=false">取消</el-button><el-button type="primary" @click="create">发放</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { useAuthStore } from '../store/auth'
const auth = useAuthStore(); const list = ref([]); const mine = ref([]); const dlg = ref(false); const vrange = ref([])
const form = reactive({ name: '', type: 'FULL_REDUCE', threshold: 100, offAmount: 20, discountRate: 0.9, totalCount: 100 })
const load = async () => { list.value = (await request.get('/coupons')).data; if (auth.isOwner) mine.value = (await request.get('/coupons/mine')).data }
const claim = async c => { await request.post('/coupons/' + c.id + '/claim'); ElMessage.success('领取成功！'); load() }
const del = async c => { await ElMessageBox.confirm('确认删除优惠券「' + c.name + '」？', '提示'); await request.delete('/coupons/' + c.id); ElMessage.success('已删除'); load() }
const create = async () => { if (!vrange.value || !vrange.value[0]) return ElMessage.warning('请选择有效期'); await request.post('/coupons', { ...form, validFrom: vrange.value[0], validTo: vrange.value[1] }); ElMessage.success('发放成功'); dlg.value = false; load() }
onMounted(load)
</script>
<style scoped>
.coupon { display: flex; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 18px rgba(249,115,22,.12); background: #fff; }
.coupon.used { opacity: .55; filter: grayscale(.5); }
.c-left { background: linear-gradient(135deg,#f97316,#fb923c); color: #fff; width: 118px; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 16px 6px; flex-shrink: 0; }
.c-amt { font-size: 26px; font-weight: 800; }
.c-cond { font-size: 12px; opacity: .9; }
.c-right { padding: 14px 16px; flex: 1; }
</style>
