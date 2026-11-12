<template>
  <div>
    <div class="shop-head">
      <div><h2>🛍️ 服务商城</h2><p>明码标价 · 节假日 ×1.5 · 长住与多宠折扣 · 可叠加优惠券</p></div>
      <el-input v-model="kw" placeholder="搜索服务名称" clearable style="width:280px" size="large">
        <template #prefix>🔍</template>
      </el-input>
    </div>
    <div class="bar">
      <div class="tabs">
        <span v-for="t in tabs" :key="t.key" class="tab" :class="{ on: cat === t.key }" @click="cat = t.key">{{ t.label }}</span>
      </div>
      <el-select v-model="sort" style="width:170px">
        <el-option label="默认排序" value="def" />
        <el-option label="销量从高到低" value="sales" />
        <el-option label="价格从低到高" value="priceAsc" />
        <el-option label="价格从高到低" value="priceDesc" />
      </el-select>
      <el-button type="primary" plain @click="$router.push('/booking')">🤖 智能匹配 + 价格试算</el-button>
    </div>
    <el-row :gutter="18">
      <el-col :span="6" v-for="s in shown" :key="s.id" style="margin-bottom:18px">
        <ServiceCard :s="s" @book="bookService" @detail="showDetail" />
      </el-col>
    </el-row>
    <el-empty v-if="!shown.length" description="没有找到符合条件的服务" />
    <el-dialog v-model="dlg" width="640px">
      <template v-if="cur">
        <img v-if="cur.coverUrl" :src="cur.coverUrl" class="dlg-cover" />
        <h3 style="margin:12px 0 4px">{{ cur.icon }} {{ cur.name }}</h3>
        <div style="color:#ea580c;font-size:22px;font-weight:800">¥{{ money(cur.price) }}<span style="font-size:13px;color:#a8a29e;font-weight:400">/{{ cur.pricingType === 'PER_DAY' ? '天' : '次' }}起 · 已售 {{ cur.salesCount || 0 }} · 时长 {{ cur.durationMin >= 1440 ? '全天' : cur.durationMin + ' 分钟' }}</span></div>
        <p style="color:#57534e;line-height:1.8">{{ cur.description }}</p>
        <p style="color:#a8a29e;font-size:13px">下单页可实时试算价格：节假日系数、长住折扣、多宠折扣与优惠券抵扣逐项列明。</p>
      </template>
      <template #footer><el-button @click="dlg = false">再看看</el-button><el-button type="primary" @click="bookService(cur)">立即预约</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import { money } from '../../utils/format'
import { isLoggedIn } from '../../utils/guard'
import ServiceCard from '../../components/ServiceCard.vue'
const route = useRoute(); const router = useRouter()
const list = ref([]); const cat = ref('ALL'); const sort = ref('def'); const kw = ref('')
const dlg = ref(false); const cur = ref(null)
const tabs = [
  { key: 'ALL', label: '全部' }, { key: 'VISIT', label: '🍚 上门喂养' }, { key: 'WALK', label: '🦮 遛狗' },
  { key: 'BOARDING', label: '🏠 寄养' }, { key: 'GROOMING', label: '🛁 洗护' }, { key: 'ADDON', label: '🎁 附加服务' }
]
const shown = computed(() => {
  let arr = list.value.filter(s => (cat.value === 'ALL' || s.category === cat.value) && (!kw.value || (s.name + s.description).includes(kw.value)))
  if (sort.value === 'sales') arr = [...arr].sort((a, b) => (b.salesCount || 0) - (a.salesCount || 0))
  if (sort.value === 'priceAsc') arr = [...arr].sort((a, b) => Number(a.price) - Number(b.price))
  if (sort.value === 'priceDesc') arr = [...arr].sort((a, b) => Number(b.price) - Number(a.price))
  return arr
})
const bookService = s => { if (s) router.push('/booking?serviceId=' + s.id) }
const showDetail = s => { cur.value = s; dlg.value = true }
onMounted(async () => {
  list.value = (await request.get(isLoggedIn() ? '/services' : '/public/services')).data
  if (route.query.cat) cat.value = route.query.cat
  if (route.query.q) kw.value = route.query.q
})
</script>
<style scoped>
.shop-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.shop-head h2 { margin: 0; font-size: 22px; }
.shop-head p { margin: 5px 0 0; color: #9a3412; opacity: .7; font-size: 13px; }
.bar { display: flex; align-items: center; gap: 14px; margin-bottom: 18px; flex-wrap: wrap; }
.tabs { display: flex; gap: 8px; flex: 1; flex-wrap: wrap; }
.tab { padding: 8px 16px; background: #fff; border: 1px solid #fed7aa; border-radius: 999px; cursor: pointer; font-size: 14px; font-weight: 600; color: #57534e; }
.tab:hover { color: #ea580c; }
.tab.on { background: linear-gradient(135deg,#f97316,#fb923c); color: #fff; border-color: transparent; }
.dlg-cover { width: 100%; height: 260px; object-fit: cover; border-radius: 14px; display: block; }
</style>
