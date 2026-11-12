<template>
  <div>
    <div class="head"><div><h2>🏠 寄养空间</h2><p>实时余位 · 入住日报 + 视频看护 · 疫苗齐全才可入住</p></div></div>
    <el-row :gutter="18">
      <el-col :span="8" v-for="s in list" :key="s.id" style="margin-bottom:18px">
        <SpaceCard :s="s" @book="bookSpace" />
      </el-col>
    </el-row>
    <el-empty v-if="!list.length" description="暂无寄养空间" />
    <section class="tips">
      <h3>📋 寄养须知</h3>
      <p>1. 入住需疫苗接种齐全，入住时出示疫苗记录；&nbsp;2. 每日发送在住日报（饮食/精神/照片）；&nbsp;3. 长住优惠：≥7 天 9 折、≥15 天 8 折，下单页自动计算；&nbsp;4. 满房空间不可预订，可先选其他房型或改期。</p>
    </section>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { isLoggedIn } from '../../utils/guard'
import SpaceCard from '../../components/SpaceCard.vue'
const router = useRouter(); const list = ref([]); const services = ref([])
const bookSpace = s => {
  const name = s.type === '猫别墅' ? '门店寄养-猫' : s.type === '家庭寄养' ? '家庭寄养' : '门店寄养-狗'
  const svc = services.value.find(x => x.name === name)
  router.push('/booking?' + (svc ? 'serviceId=' + svc.id + '&' : '') + 'spaceId=' + s.id)
}
onMounted(async () => {
  const base = isLoggedIn() ? '' : '/public'
  list.value = (await request.get(base + '/spaces')).data; services.value = (await request.get(base + '/services')).data
})
</script>
<style scoped>
.head h2 { margin: 0; font-size: 22px; } .head p { margin: 5px 0 16px; color: #9a3412; opacity: .7; font-size: 13px; }
.tips { background: #fff; border-radius: 18px; padding: 18px 22px; box-shadow: 0 4px 20px rgba(249,115,22,.08); }
.tips h3 { margin: 0 0 8px; } .tips p { margin: 0; color: #78716c; font-size: 13.5px; line-height: 1.9; }
</style>
