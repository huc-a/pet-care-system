<template>
  <component :is="isStaff ? Sitters : FrontSitters" />
</template>
<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../store/auth'
import Sitters from '../views/Sitters.vue'
import FrontSitters from '../views/front/FrontSitters.vue'
const auth = useAuthStore()
// 游客与宠主看前台卡片版；管理员/宠托师看后台管理版
const isStaff = computed(() => !!(auth.isAdmin || auth.isSitter))
</script>
