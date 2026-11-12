<template>
  <FrontLayout v-if="useFront" />
  <MainLayout v-else />
</template>
<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../store/auth'
import MainLayout from './MainLayout.vue'
import FrontLayout from './FrontLayout.vue'
const route = useRoute(); const auth = useAuthStore()
// 游客与宠主（OWNER）走商城式前台；管理员/宠托师走后台布局
const useFront = computed(() => !(auth.isAdmin || auth.isSitter))
</script>
