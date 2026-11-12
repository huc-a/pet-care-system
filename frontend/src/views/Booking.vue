<template>
  <div>
    <div class="page-head"><div><h2>📝 预约下单</h2><div class="sub">自动计价：基础价×天数/次数 + 附加服务，节假日×1.5，长住≥7天9折/≥15天8折，第2只宠物起8折，可叠加优惠券</div></div></div>
    <el-row :gutter="16">
      <el-col :span="14">
        <el-card>
          <el-form :model="form" label-width="104px">
            <el-form-item label="服务项目"><el-select v-model="form.serviceId" style="width:100%" @change="onService">
              <el-option v-for="s in mainServices" :key="s.id" :label="s.icon + ' ' + s.name + '（¥' + s.price + '/' + (s.pricingType==='PER_DAY'?'天':'次') + '）'" :value="s.id" /></el-select></el-form-item>
            <el-form-item label="选择宠物"><el-select v-model="form.petIds" multiple style="width:100%" placeholder="可多选，第2只起8折">
              <el-option v-for="p in pets" :key="p.id" :label="p.emoji + ' ' + p.name + '（' + p.breed + '）'" :value="p.id" /></el-select></el-form-item>
            <el-form-item label="服务日期">
              <el-date-picker v-model="range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始日期" end-placeholder="结束日期" style="width:100%" />
            </el-form-item>
            <el-form-item label="每日次数" v-if="curService && curService.pricingType === 'PER_TIME'"><el-input-number v-model="form.timesPerDay" :min="1" :max="4" /></el-form-item>
            <el-form-item label="寄养空间" v-if="curService && curService.category === 'BOARDING'">
              <el-select v-model="form.spaceId" style="width:100%" placeholder="请选择寄养空间">
                <el-option v-for="s in spaces" :key="s.id" :label="s.name + '（剩余 ' + (s.totalCapacity - s.occupied) + ' 位）'" :value="s.id" :disabled="s.occupied >= s.totalCapacity" /></el-select>
              <div style="color:#ea580c;font-size:12px">寄养需宠物疫苗齐全；满房空间不可选</div></el-form-item>
            <el-form-item label="指定宠托师"><el-select v-model="form.sitterId" clearable style="width:100%" placeholder="不指定则进入接单大厅">
              <el-option v-for="it in sitters" :key="it.user.id" :label="it.user.realName + '（' + it.profile.area + ' · ' + it.profile.rating + '分）'" :value="it.user.id" /></el-select></el-form-item>
            <el-form-item label="附加服务"><el-checkbox-group v-model="form.addonIds">
              <el-checkbox v-for="a in addons" :key="a.id" :value="a.id">{{ a.icon }} {{ a.name }} ¥{{ a.price }}</el-checkbox></el-checkbox-group></el-form-item>
            <el-form-item label="优惠券"><el-select v-model="form.userCouponId" clearable style="width:100%" placeholder="选择我的优惠券">
              <el-option v-for="m in myCoupons" :key="m.userCoupon.id" :label="m.coupon.name" :value="m.userCoupon.id" :disabled="m.effectiveStatus !== 'UNUSED'" /></el-select></el-form-item>
            <el-form-item label="服务区域"><el-input v-model="form.area" placeholder="如：朝阳区" /></el-form-item>
            <el-form-item label="服务地址"><el-input v-model="form.address" placeholder="上门地址/寄养可填门店" /></el-form-item>
            <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" placeholder="宠物习性、注意事项等" /></el-form-item>
            <el-form-item><el-button @click="doMatch" :loading="matching">🤖 智能匹配宠托师</el-button>
              <el-button type="primary" size="large" @click="submit" :loading="submitting">确认下单并支付</el-button></el-form-item>
          </el-form>
        </el-card>
        <el-card v-if="matches.length" style="margin-top:16px">
          <template #header>🤖 智能匹配推荐 Top{{ matches.length }}（按区域+评分+擅长品种+接单量+距离综合打分）</template>
          <div v-for="(m, i) in matches" :key="m.sitterId" class="match-row">
            <div class="rank">{{ ['🥇','🥈','🥉'][i] }}</div>
            <div style="flex:1"><b>{{ m.name }}</b> · {{ m.area }} · ⭐{{ m.rating }} · 接单 {{ m.orderCount }} 单 · 约 {{ m.distanceKm }} km
              <div style="color:#78716c;font-size:13px;margin-top:2px">{{ m.reasons.join('；') }}</div></div>
            <div class="score">{{ m.score }} 分</div>
            <el-button size="small" type="primary" @click="form.sitterId = m.sitterId">选 TA</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card class="quote">
          <template #header>💰 价格试算（实时）</template>
          <template v-if="quote">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="计费单位">{{ quote.quantity }} {{ curService && curService.pricingType === 'PER_DAY' ? '天' : '次' }}（共 {{ quote.days }} 天）</el-descriptions-item>
              <el-descriptions-item label="基础费用">¥{{ money(quote.baseAmount) }}</el-descriptions-item>
              <el-descriptions-item label="附加服务">¥{{ money(quote.addonAmount) }}</el-descriptions-item>
              <el-descriptions-item label="节假日加价">系数 {{ quote.holidayFactor }} · ¥{{ money(quote.holidayAmount) }}</el-descriptions-item>
              <el-descriptions-item label="多宠折扣">−¥{{ money(quote.multiPetDiscount) }}</el-descriptions-item>
              <el-descriptions-item label="长住折扣">−¥{{ money(quote.longStayDiscount) }}</el-descriptions-item>
              <el-descriptions-item label="优惠券抵扣">−¥{{ money(quote.couponDiscount) }}</el-descriptions-item>
            </el-descriptions>
            <div class="total">应付总额：¥{{ money(quote.totalAmount) }}</div>
            <p style="color:#78716c;font-size:12.5px;line-height:1.8">{{ quote.detail }}</p>
          </template>
          <el-empty v-else description="选择服务、宠物与日期后自动试算" :image-size="70" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { useAuthStore } from '../store/auth'
import { money } from '../utils/format'
const auth = useAuthStore(); const route = useRoute(); const router = useRouter()
const services = ref([]); const pets = ref([]); const sitters = ref([]); const spaces = ref([]); const myCoupons = ref([])
const range = ref([]); const quote = ref(null); const matches = ref([]); const matching = ref(false); const submitting = ref(false)
const form = reactive({ serviceId: null, petIds: [], sitterId: null, spaceId: null, timesPerDay: 1, addonIds: [], userCouponId: null, area: '', address: '', remark: '' })
const mainServices = computed(() => services.value.filter(s => s.category !== 'ADDON'))
const addons = computed(() => services.value.filter(s => s.category === 'ADDON'))
const curService = computed(() => services.value.find(s => s.id === form.serviceId))
const onService = () => { if (!curService.value || curService.value.category !== 'BOARDING') form.spaceId = null }
const payload = () => ({ serviceId: form.serviceId, petIds: form.petIds, sitterId: form.sitterId, spaceId: form.spaceId,
  startDate: range.value && range.value[0], endDate: range.value && range.value[1], timesPerDay: form.timesPerDay,
  addonIds: form.addonIds, userCouponId: form.userCouponId, area: form.area, address: form.address, remark: form.remark })
const refreshQuote = async () => {
  if (!form.serviceId || !form.petIds.length || !range.value || !range.value[0]) { quote.value = null; return }
  try { quote.value = (await request.post('/orders/price-preview', payload())).data } catch (e) { quote.value = null }
}
watch([() => form.serviceId, () => form.petIds, range, () => form.timesPerDay, () => form.addonIds, () => form.userCouponId], refreshQuote, { deep: true })
const doMatch = async () => {
  const pet = pets.value.find(p => p.id === form.petIds[0])
  matching.value = true
  try { matches.value = (await request.post('/sitters/match', { area: form.area || (auth.user && auth.user.area) || '', breed: pet ? pet.breed : '', species: pet ? pet.species : '' })).data
    if (!matches.value.length) ElMessage.info('暂无可接单的宠托师') } catch (e) {} finally { matching.value = false }
}
const submit = async () => {
  if (!form.serviceId) return ElMessage.warning('请选择服务项目')
  if (!form.petIds.length) return ElMessage.warning('请选择宠物')
  if (!range.value || !range.value[0]) return ElMessage.warning('请选择服务日期')
  if (curService.value.category === 'BOARDING' && !form.spaceId) return ElMessage.warning('寄养服务请选择寄养空间')
  submitting.value = true
  try { const r = await request.post('/orders', payload()); ElMessage.success('下单成功！订单号 ' + r.data.orderNo + '，应付 ¥' + money(r.data.totalAmount)); router.push('/orders/' + r.data.id) }
  catch (e) {} finally { submitting.value = false }
}
onMounted(async () => {
  services.value = (await request.get('/services')).data
  pets.value = (await request.get('/pets')).data
  sitters.value = (await request.get('/sitters')).data
  spaces.value = (await request.get('/spaces')).data
  try { myCoupons.value = (await request.get('/coupons/mine')).data } catch (e) {}
  form.area = (auth.user && auth.user.area) || ''
  if (route.query.serviceId) form.serviceId = Number(route.query.serviceId)
  if (route.query.sitterId) form.sitterId = Number(route.query.sitterId)
  if (route.query.spaceId) form.spaceId = Number(route.query.spaceId)
  const t = new Date(); const fmt = d => d.toISOString().slice(0, 10)
  range.value = [fmt(t), fmt(t)]
  refreshQuote()
})
</script>
<style scoped>
.quote { position: sticky; top: 0; }
.total { font-size: 22px; font-weight: 700; color: #ea580c; margin-top: 14px; }
.match-row { display: flex; gap: 12px; align-items: center; padding: 12px 4px; border-bottom: 1px dashed #fed7aa; }
.rank { font-size: 26px; }
.score { font-size: 20px; font-weight: 700; color: #ea580c; white-space: nowrap; }
</style>
