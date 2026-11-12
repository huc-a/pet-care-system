<template>
  <div>
    <div class="head"><div><h2>🧑‍🤝‍🧑 找宠托师</h2><p>平台实名认证 · 每次服务图文报告 · 遛狗轨迹可查</p></div></div>

    <el-card class="match-card">
      <template #header>🤖 智能匹配 · 告诉我们你的需求，系统打分推荐 Top3</template>
      <div class="match-form">
        <el-select v-model="m.area" placeholder="服务区域" style="width:180px">
          <el-option v-for="a in areas" :key="a" :label="a" :value="a" />
        </el-select>
        <el-select v-model="m.species" placeholder="宠物种类" style="width:150px">
          <el-option label="猫" value="猫" /><el-option label="狗" value="狗" />
        </el-select>
        <el-input v-model="m.breed" placeholder="品种，如：金毛 / 英短" style="width:210px" />
        <el-button type="primary" :loading="matching" @click="doMatch">开始匹配</el-button>
      </div>
      <div v-if="matches.length" class="match-list">
        <div v-for="(mm, i) in matches" :key="mm.sitterId" class="match-row">
          <img v-if="mm.avatarUrl" :src="mm.avatarUrl" class="m-ava" />
          <div class="rank">{{ ['🥇','🥈','🥉'][i] }}</div>
          <div style="flex:1"><b>{{ mm.name }}</b> · {{ mm.area }} · ⭐{{ mm.rating }} · 接单 {{ mm.orderCount }} 单 · 约 {{ mm.distanceKm }} km
            <div class="reasons">{{ (mm.reasons || []).join('；') }}</div></div>
          <div class="score">{{ mm.score }} 分</div>
          <el-button size="small" type="primary" @click="$router.push('/booking?sitterId=' + mm.sitterId)">选 TA 预约</el-button>
        </div>
      </div>
    </el-card>

    <div class="bar">
      <div class="tabs">
        <span class="tab" :class="{ on: area === 'ALL' }" @click="area = 'ALL'">全部区域</span>
        <span v-for="a in areas" :key="a" class="tab" :class="{ on: area === a }" @click="area = a">{{ a }}</span>
      </div>
      <el-input v-model="kw" placeholder="搜名字/擅长品种" clearable style="width:220px"><template #prefix>🔍</template></el-input>
      <el-select v-model="sort" style="width:160px">
        <el-option label="评分优先" value="rating" />
        <el-option label="接单量优先" value="orders" />
        <el-option label="距离优先" value="dist" />
      </el-select>
    </div>
    <el-row :gutter="18">
      <el-col :span="8" v-for="it in shown" :key="it.profile.id" style="margin-bottom:18px">
        <SitterCard :it="it" @book="x => $router.push('/booking?sitterId=' + x.user.id)" />
      </el-col>
    </el-row>
    <el-empty v-if="!shown.length" description="没有符合条件的宠托师" />
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
import { isLoggedIn, ensureLogin } from '../../utils/guard'
import SitterCard from '../../components/SitterCard.vue'
const router = useRouter()
const list = ref([]); const area = ref('ALL'); const kw = ref(''); const sort = ref('rating')
const m = reactive({ area: '朝阳区', species: '狗', breed: '' })
const matches = ref([]); const matching = ref(false)
const areas = computed(() => [...new Set(list.value.map(i => i.profile.area))])
const shown = computed(() => {
  let arr = list.value.filter(i => (area.value === 'ALL' || i.profile.area === area.value) && (!kw.value || (i.user.realName + i.profile.specialties).includes(kw.value)))
  if (sort.value === 'rating') arr = [...arr].sort((a, b) => Number(b.profile.rating) - Number(a.profile.rating))
  if (sort.value === 'orders') arr = [...arr].sort((a, b) => b.profile.orderCount - a.profile.orderCount)
  if (sort.value === 'dist') arr = [...arr].sort((a, b) => Number(a.profile.distanceKm) - Number(b.profile.distanceKm))
  return arr
})
const doMatch = async () => {
  if (!ensureLogin(router, '/sitters')) return
  matching.value = true
  try { matches.value = (await request.post('/sitters/match', m)).data; if (!matches.value.length) ElMessage.info('暂无匹配结果，可直接从下方挑选') }
  finally { matching.value = false }
}
onMounted(async () => { list.value = (await request.get(isLoggedIn() ? '/sitters' : '/public/sitters')).data })
</script>
<style scoped>
.head h2 { margin: 0; font-size: 22px; } .head p { margin: 5px 0 14px; color: #9a3412; opacity: .7; font-size: 13px; }
.match-card { margin-bottom: 20px; }
.match-form { display: flex; gap: 12px; flex-wrap: wrap; }
.match-list { margin-top: 16px; }
.match-row { display: flex; align-items: center; gap: 12px; padding: 12px 6px; border-bottom: 1px dashed #fed7aa; }
.match-row:last-child { border-bottom: none; }
.m-ava { width: 46px; height: 46px; border-radius: 50%; object-fit: cover; }
.rank { font-size: 24px; }
.reasons { color: #78716c; font-size: 13px; margin-top: 2px; }
.score { font-size: 19px; font-weight: 800; color: #ea580c; }
.bar { display: flex; align-items: center; gap: 14px; margin-bottom: 18px; flex-wrap: wrap; }
.tabs { display: flex; gap: 8px; flex: 1; flex-wrap: wrap; }
.tab { padding: 8px 16px; background: #fff; border: 1px solid #fed7aa; border-radius: 999px; cursor: pointer; font-size: 14px; font-weight: 600; color: #57534e; }
.tab.on { background: linear-gradient(135deg,#f97316,#fb923c); color: #fff; border-color: transparent; }
</style>
