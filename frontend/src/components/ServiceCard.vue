<template>
  <div class="svc-card" @click="$emit('detail', s)">
    <div class="cover">
      <img v-if="s.coverUrl" :src="s.coverUrl" :alt="s.name" />
      <div v-else class="cover-ph">{{ s.icon || '🐾' }}</div>
      <span class="cat-tag">{{ CAT[s.category] || s.category }}</span>
      <span class="hot" v-if="(s.salesCount || 0) >= 300">🔥 热门</span>
    </div>
    <div class="body">
      <div class="name">{{ s.icon }} {{ s.name }}</div>
      <div class="desc">{{ s.description }}</div>
      <div class="meta">
        <span>🕐 {{ s.durationMin >= 1440 ? '全天' : s.durationMin + ' 分钟' }}</span>
        <span>已售 {{ s.salesCount || 0 }}</span>
      </div>
      <div class="foot">
        <div class="price">¥{{ money(s.price) }}<span class="unit">起/{{ s.pricingType === 'PER_DAY' ? '天' : '次' }}</span></div>
        <el-button type="primary" size="small" round @click.stop="$emit('book', s)">立即预约</el-button>
      </div>
    </div>
  </div>
</template>
<script setup>
import { money } from '../utils/format'
defineProps({ s: { type: Object, required: true } })
defineEmits(['book', 'detail'])
const CAT = { VISIT: '上门服务', WALK: '遛狗', BOARDING: '寄养', GROOMING: '洗护', ADDON: '附加服务' }
</script>
<style scoped>
.svc-card { background: #fff; border-radius: 18px; overflow: hidden; box-shadow: 0 4px 20px rgba(249,115,22,.09); cursor: pointer; transition: .2s; height: 100%; display: flex; flex-direction: column; }
.svc-card:hover { transform: translateY(-4px); box-shadow: 0 12px 28px rgba(249,115,22,.18); }
.cover { position: relative; height: 168px; background: #ffedd5; }
.cover img { width: 100%; height: 100%; object-fit: cover; display: block; }
.cover-ph { height: 100%; display: flex; align-items: center; justify-content: center; font-size: 64px; }
.cat-tag { position: absolute; top: 10px; left: 10px; background: rgba(234,88,12,.92); color: #fff; font-size: 12px; padding: 3px 10px; border-radius: 999px; }
.hot { position: absolute; top: 10px; right: 10px; background: rgba(255,255,255,.92); color: #dc2626; font-size: 12px; padding: 3px 10px; border-radius: 999px; font-weight: 700; }
.body { padding: 14px 16px 16px; flex: 1; display: flex; flex-direction: column; }
.name { font-size: 16.5px; font-weight: 700; color: #431407; }
.desc { color: #78716c; font-size: 13px; line-height: 1.65; margin: 7px 0; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; min-height: 42px; }
.meta { display: flex; justify-content: space-between; color: #a8a29e; font-size: 12.5px; }
.foot { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; }
.price { font-size: 22px; font-weight: 800; color: #ea580c; }
.price .unit { font-size: 12px; color: #a8a29e; font-weight: 400; }
</style>
