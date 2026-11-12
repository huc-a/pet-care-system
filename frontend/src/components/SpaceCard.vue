<template>
  <div class="sp-card">
    <div class="cover">
      <img v-if="s.photoUrl" :src="s.photoUrl" :alt="s.name" />
      <div v-else class="cover-ph">🏠</div>
      <span class="badge" :class="{ full: left <= 0 }">{{ left <= 0 ? '已满房' : '剩余 ' + left + ' 位' }}</span>
    </div>
    <div class="body">
      <div class="name">{{ s.name }} <el-tag size="small" type="warning">{{ s.type }}</el-tag></div>
      <div class="desc">{{ s.description }} · {{ s.sizeDesc }}</div>
      <div class="fac">🛠 {{ s.facilities }}</div>
      <div class="prog-row">
        <el-progress :percentage="pct" :color="left <= 0 ? '#ef4444' : '#f97316'" style="flex:1" />
        <span class="prog-txt">已住 {{ s.occupied }}/{{ s.totalCapacity }}</span>
      </div>
      <div class="foot">
        <div class="price">¥{{ money(s.pricePerDay) }}<span class="unit">/天</span></div>
        <el-button type="primary" size="small" round :disabled="left <= 0" @click="$emit('book', s)">{{ left <= 0 ? '已满房' : '预订' }}</el-button>
      </div>
    </div>
  </div>
</template>
<script setup>
import { computed } from 'vue'
import { money } from '../utils/format'
const props = defineProps({ s: { type: Object, required: true } })
defineEmits(['book'])
const left = computed(() => props.s.totalCapacity - props.s.occupied)
const pct = computed(() => Math.round(props.s.occupied * 100 / props.s.totalCapacity))
</script>
<style scoped>
.sp-card { background: #fff; border-radius: 18px; overflow: hidden; box-shadow: 0 4px 20px rgba(249,115,22,.09); height: 100%; display: flex; flex-direction: column; }
.cover { position: relative; height: 180px; background: #ffedd5; }
.cover img { width: 100%; height: 100%; object-fit: cover; display: block; }
.cover-ph { height: 100%; display: flex; align-items: center; justify-content: center; font-size: 60px; }
.badge { position: absolute; top: 10px; right: 10px; background: rgba(22,163,74,.92); color: #fff; font-size: 12px; padding: 3px 10px; border-radius: 999px; }
.badge.full { background: rgba(220,38,38,.92); }
.body { padding: 14px 16px 16px; flex: 1; display: flex; flex-direction: column; }
.name { font-size: 16.5px; font-weight: 700; }
.desc { color: #57534e; font-size: 13px; margin-top: 6px; }
.fac { color: #a8a29e; font-size: 12.5px; margin-top: 4px; line-height: 1.6; }
.prog-row { display: flex; align-items: center; gap: 10px; margin-top: 10px; }
.prog-txt { font-size: 12.5px; color: #78716c; white-space: nowrap; }
.foot { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; }
.price { font-size: 22px; font-weight: 800; color: #ea580c; }
.price .unit { font-size: 12px; color: #a8a29e; font-weight: 400; }
</style>
