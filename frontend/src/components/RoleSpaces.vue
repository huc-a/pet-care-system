<template>
  <component :is="isStaff ? Spaces : FrontSpaces" />
</template>
<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../store/auth'
import Spaces from '../views/Spaces.vue'
import FrontSpaces from '../views/front/FrontSpaces.vue'
const auth = useAuthStore()
// 游客与宠主看前台卡片版；管理员/宠托师看后台管理版
const isStaff = computed(() => !!(auth.isAdmin || auth.isSitter))
</script>
