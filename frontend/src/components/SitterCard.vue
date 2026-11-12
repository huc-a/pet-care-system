<template>
  <div class="sit-card">
    <div class="top">
      <img v-if="it.user.avatarUrl" :src="it.user.avatarUrl" class="ava" :alt="it.user.realName" />
      <div v-else class="ava ph">{{ it.user.avatarEmoji || '🧑' }}</div>
      <div class="who">
        <div class="name">{{ it.user.realName }}
          <el-tag size="small" type="success">{{ it.profile.certStatus || '已认证' }}</el-tag>
          <el-tag size="small" :type="it.profile.available ? 'warning' : 'info'">{{ it.profile.available ? '可接单' : '休息中' }}</el-tag>
        </div>
        <div class="sub">📍 {{ it.profile.area }} · 从业 {{ it.profile.experienceYears }} 年 · 距您约 {{ it.profile.distanceKm }} km</div>
        <el-rate :model-value="Number(it.profile.rating)" disabled show-score :score-template="it.profile.rating + ' 分'" />
      </div>
    </div>
    <div class="tags">
      <el-tag v-for="t in specTags" :key="t" size="small" type="warning" effect="plain">{{ t }}</el-tag>
      <span class="cnt">累计接单 {{ it.profile.orderCount }} 单</span>
    </div>
    <p class="intro">{{ it.profile.intro }}</p>
    <el-button type="primary" round style="width:100%" :disabled="!it.profile.available" @click="$emit('book', it)">选 TA 预约</el-button>
  </div>
</template>
<script setup>
import { computed } from 'vue'
const props = defineProps({ it: { type: Object, required: true } })
defineEmits(['book'])
const specTags = computed(() => (props.it.profile.specialties || '').split(/[,，]/).filter(Boolean).slice(0, 4))
</script>
<style scoped>
.sit-card { background: #fff; border-radius: 18px; padding: 18px; box-shadow: 0 4px 20px rgba(249,115,22,.09); height: 100%; display: flex; flex-direction: column; }
.top { display: flex; gap: 14px; }
.ava { width: 72px; height: 72px; border-radius: 50%; object-fit: cover; flex-shrink: 0; border: 3px solid #ffedd5; }
.ava.ph { background: linear-gradient(135deg,#f97316,#fb923c); display: flex; align-items: center; justify-content: center; font-size: 32px; }
.name { font-size: 16.5px; font-weight: 700; display: flex; gap: 6px; align-items: center; flex-wrap: wrap; }
.sub { color: #78716c; font-size: 12.5px; margin: 4px 0 2px; }
.tags { display: flex; gap: 6px; flex-wrap: wrap; align-items: center; margin: 12px 0 4px; }
.cnt { color: #ea580c; font-size: 12.5px; font-weight: 600; margin-left: auto; }
.intro { color: #78716c; font-size: 13px; line-height: 1.7; flex: 1; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
</style>
