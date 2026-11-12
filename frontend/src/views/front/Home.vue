<template>
  <div>
    <!-- Hero -->
    <section class="hero">
      <img src="/img/hero.jpg" class="hero-bg" alt="宠物寄养" />
      <div class="hero-mask"></div>
      <div class="hero-in">
        <h1>出门在外，毛孩子有人疼 🐾</h1>
        <p>上门喂养 · 遛狗 · 门店寄养 · 家庭寄养 —— 认证宠托师上门服务，每次服务都有图文报告</p>
        <div class="search">
          <el-input v-model="kw" placeholder="搜服务：喂猫 / 遛狗 / 寄养 / 洗护…" size="large" clearable @keyup.enter="goShop">
            <template #prefix>🔍</template>
          </el-input>
          <el-button type="primary" size="large" @click="goShop">搜索服务</el-button>
          <el-button size="large" class="ghost" @click="$router.push('/booking')">立即预约</el-button>
        </div>
        <div class="hero-stats">
          <span>🏠 {{ spaceCount }} 种寄养空间</span>
          <span>🧑‍🤝‍🧑 {{ sitterCount }} 位认证宠托师</span>
          <span>⭐ 平均评分 {{ avgRating }}</span>
          <span>📦 累计服务 {{ totalSales }}+ 单</span>
        </div>
      </div>
    </section>

    <!-- 分类瓷砖 -->
    <section class="cats">
      <div v-for="c in cats" :key="c.key" class="cat" @click="$router.push('/shop?cat=' + c.key)">
        <img v-if="c.img" :src="c.img" :alt="c.title" />
        <span class="cat-t">{{ c.icon }} {{ c.title }}</span>
      </div>
    </section>

    <!-- 领券条 -->
    <section v-if="coupons.length" class="coupon-strip">
      <div class="cs-tx">🎟️ <b>限时领券</b><span>领券下单更划算，先到先得</span></div>
      <div v-for="c in coupons.slice(0, 3)" :key="c.id" class="cs-item">
        <b>{{ c.type === 'DISCOUNT' ? (c.discountRate * 10) + '折券' : '满¥' + c.threshold + '减¥' + c.offAmount }}</b>
        <span>{{ c.name }}</span>
        <el-button size="small" type="primary" round :disabled="claimedIds.includes(c.id)" @click="claim(c)">{{ claimedIds.includes(c.id) ? '已领取' : '领取' }}</el-button>
      </div>
      <el-button text @click="$router.push('/coupons')">全部优惠券 →</el-button>
    </section>

    <!-- 热门服务 -->
    <div class="sec-head"><h2>🔥 热门服务</h2><a @click="$router.push('/shop')">去逛商城 →</a></div>
    <el-row :gutter="18">
      <el-col :span="6" v-for="s in hotServices" :key="s.id" style="margin-bottom:18px">
        <ServiceCard :s="s" @book="bookService" @detail="showDetail" />
      </el-col>
    </el-row>

    <!-- 保障 -->
    <section class="assure">
      <div v-for="a in assures" :key="a.t" class="a-item"><div class="a-ic">{{ a.i }}</div><div><b>{{ a.t }}</b><p>{{ a.d }}</p></div></div>
    </section>

    <!-- 精选宠托师 -->
    <div class="sec-head"><h2>🌟 精选宠托师</h2><a @click="$router.push('/sitters')">找宠托师 →</a></div>
    <el-row :gutter="18">
      <el-col :span="8" v-for="it in topSitters" :key="it.profile.id" style="margin-bottom:18px">
        <SitterCard :it="it" @book="bookSitter" />
      </el-col>
    </el-row>

    <!-- 寄养空间 -->
    <div class="sec-head"><h2>🏠 寄养空间</h2><a @click="$router.push('/spaces')">全部空间 →</a></div>
    <el-row :gutter="18">
      <el-col :span="8" v-for="s in spaces.slice(0, 3)" :key="s.id" style="margin-bottom:18px">
        <SpaceCard :s="s" @book="bookSpace" />
      </el-col>
    </el-row>

    <!-- 评价 + 公告 -->
    <el-row :gutter="18">
      <el-col :span="16">
        <div class="sec-head"><h2>💬 宠主评价</h2></div>
        <el-row :gutter="18">
          <el-col :span="12" v-for="r in reviews.slice(0, 4)" :key="r.id" style="margin-bottom:18px">
            <div class="rev-card">
              <div class="rev-top"><el-rate :model-value="r.rating" disabled size="small" /><span class="rev-who">{{ r.ownerName }} 评价了 {{ r.sitterName }}</span></div>
              <p>{{ r.content }}</p>
              <div class="rev-tags"><el-tag v-for="t in (r.tags || '').split(/[,，]/).filter(Boolean)" :key="t" size="small" type="warning" effect="plain">{{ t }}</el-tag></div>
            </div>
          </el-col>
        </el-row>
      </el-col>
      <el-col :span="8">
        <div class="sec-head"><h2>📢 平台公告</h2><a @click="$router.push('/notices')">更多 →</a></div>
        <div class="notice-card">
          <div v-for="n in notices.slice(0, 4)" :key="n.id" class="n-item">
            <el-tag size="small" type="warning">{{ n.type || '公告' }}</el-tag>
            <div><b>{{ n.title }}</b><p>{{ n.content }}</p></div>
          </div>
          <el-empty v-if="!notices.length" description="暂无公告" :image-size="50" />
        </div>
      </el-col>
    </el-row>

    <!-- 智能匹配 CTA -->
    <section class="cta">
      <div><h3>🤖 不知道选哪位宠托师？让智能匹配帮你挑</h3><p>按区域、评分、擅长品种与距离综合打分，推荐最合适的 Top3</p></div>
      <el-button size="large" class="ghost" @click="$router.push('/sitters')">去智能匹配</el-button>
    </section>

    <!-- 服务详情弹窗 -->
    <el-dialog v-model="dlg" width="640px" class="svc-dlg">
      <template v-if="cur">
        <img v-if="cur.coverUrl" :src="cur.coverUrl" class="dlg-cover" />
        <h3 style="margin:12px 0 4px">{{ cur.icon }} {{ cur.name }}</h3>
        <div style="color:#ea580c;font-size:22px;font-weight:800">¥{{ money(cur.price) }}<span style="font-size:13px;color:#a8a29e;font-weight:400">/{{ cur.pricingType === 'PER_DAY' ? '天' : '次' }}起 · 已售 {{ cur.salesCount || 0 }} · 时长 {{ cur.durationMin >= 1440 ? '全天' : cur.durationMin + ' 分钟' }}</span></div>
        <p style="color:#57534e;line-height:1.8">{{ cur.description }}</p>
        <p style="color:#a8a29e;font-size:13px">计价说明：节假日 ×1.5 系数；寄养长住 ≥7 天 9 折、≥15 天 8 折；第 2 只宠物起 8 折；可叠加优惠券。</p>
      </template>
      <template #footer><el-button @click="dlg = false">再看看</el-button><el-button type="primary" @click="bookService(cur)">立即预约</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
import { money } from '../../utils/format'
import { isLoggedIn, ensureLogin } from '../../utils/guard'
import ServiceCard from '../../components/ServiceCard.vue'
import SitterCard from '../../components/SitterCard.vue'
import SpaceCard from '../../components/SpaceCard.vue'
const router = useRouter()
const guest = !isLoggedIn()
const kw = ref(''); const services = ref([]); const sitters = ref([]); const spaces = ref([]); const coupons = ref([]); const reviews = ref([]); const notices = ref([]); const claimedIds = ref([]); const overview = ref(null)
const dlg = ref(false); const cur = ref(null)
const main = computed(() => services.value.filter(s => s.category !== 'ADDON'))
const hotServices = computed(() => [...main.value].sort((a, b) => (b.salesCount || 0) - (a.salesCount || 0)).slice(0, 8))
const topSitters = computed(() => [...sitters.value].sort((a, b) => Number(b.profile.rating) - Number(a.profile.rating)).slice(0, 3))
const totalSales = computed(() => guest && overview.value ? overview.value.totalSales : services.value.reduce((t, s) => t + (s.salesCount || 0), 0))
const sitterCount = computed(() => guest && overview.value ? overview.value.sitterCount : sitters.value.length)
const spaceCount = computed(() => guest && overview.value ? overview.value.spaceCount : spaces.value.length)
const avgRating = computed(() => guest && overview.value ? overview.value.avgRating : (sitters.value.length ? (sitters.value.reduce((t, i) => t + Number(i.profile.rating), 0) / sitters.value.length).toFixed(1) : '5.0'))
const cats = computed(() => {
  const byCat = k => { const f = main.value.find(s => s.category === k); return f ? f.coverUrl : '' }
  return [
    { key: 'VISIT', title: '上门喂养', icon: '🍚', img: byCat('VISIT') },
    { key: 'WALK', title: '上门遛狗', icon: '🦮', img: byCat('WALK') },
    { key: 'BOARDING', title: '宠物寄养', icon: '🏠', img: byCat('BOARDING') },
    { key: 'GROOMING', title: '洗护美容', icon: '🛁', img: byCat('GROOMING') },
    { key: 'ADDON', title: '附加服务', icon: '🎁', img: (services.value.find(s => s.category === 'ADDON') || {}).coverUrl || '' }
  ]
})
const assures = [
  { i: '🪪', t: '实名认证宠托师', d: '持证上岗、背景审核，服务区域全覆盖' },
  { i: '📝', t: '服务图文报告', d: '喂食换水铲屎逐项打卡，照片实时回传' },
  { i: '🦮', t: '遛狗轨迹可查', d: 'GPS 全程记录里程与时长，不遛假弯' },
  { i: '💉', t: '寄养疫苗校验', d: '疫苗齐全才可入住，健康多重把关' }
]
const goShop = () => router.push('/shop' + (kw.value ? '?q=' + encodeURIComponent(kw.value) : ''))
const bookService = s => { if (s) router.push('/booking?serviceId=' + s.id) }
const bookSitter = it => router.push('/booking?sitterId=' + it.user.id)
const bookSpace = s => {
  const name = s.type === '猫别墅' ? '门店寄养-猫' : s.type === '家庭寄养' ? '家庭寄养' : '门店寄养-狗'
  const svc = services.value.find(x => x.name === name)
  router.push('/booking?' + (svc ? 'serviceId=' + svc.id + '&' : '') + 'spaceId=' + s.id)
}
const showDetail = s => { cur.value = s; dlg.value = true }
const claim = async c => {
  if (!ensureLogin(router, '/coupons')) return
  await request.post('/coupons/' + c.id + '/claim'); claimedIds.value.push(c.id); ElMessage.success('领取成功，下单时可抵扣')
}
onMounted(async () => {
  if (guest) {
    // 游客态：全部走公开只读接口
    services.value = (await request.get('/public/services')).data
    sitters.value = (await request.get('/public/sitters')).data
    spaces.value = (await request.get('/public/spaces')).data
    coupons.value = (await request.get('/public/coupons')).data
    try { reviews.value = (await request.get('/public/reviews')).data } catch (e) {}
    try { notices.value = (await request.get('/public/notices')).data } catch (e) {}
    try { overview.value = (await request.get('/public/overview')).data } catch (e) {}
    return
  }
  services.value = (await request.get('/services')).data
  sitters.value = (await request.get('/sitters')).data
  spaces.value = (await request.get('/spaces')).data
  coupons.value = (await request.get('/coupons')).data
  try { reviews.value = (await request.get('/reviews')).data.filter(r => !r.hidden) } catch (e) {}
  try { notices.value = (await request.get('/notices')).data } catch (e) {}
  try { const mine = (await request.get('/coupons/mine')).data; claimedIds.value = mine.map(m => m.coupon.id) } catch (e) {}
})
</script>
<style scoped>
.hero { position: relative; border-radius: 24px; overflow: hidden; min-height: 340px; display: flex; align-items: center; }
.hero-bg { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; }
.hero-mask { position: absolute; inset: 0; background: linear-gradient(90deg, rgba(67,20,7,.72) 0%, rgba(154,52,18,.45) 55%, rgba(154,52,18,.05) 100%); }
.hero-in { position: relative; padding: 44px 48px; max-width: 720px; color: #fff; }
.hero-in h1 { font-size: 36px; margin: 0 0 10px; }
.hero-in p { font-size: 15.5px; opacity: .92; line-height: 1.7; margin: 0 0 22px; }
.search { display: flex; gap: 10px; max-width: 620px; }
.search .el-input { flex: 1; }
.ghost { background: rgba(255,255,255,.16) !important; color: #fff !important; border: 1px solid rgba(255,255,255,.65) !important; }
.hero-stats { display: flex; gap: 22px; margin-top: 20px; font-size: 13.5px; flex-wrap: wrap; opacity: .95; }
.cats { display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px; margin: 20px 0 4px; }
.cat { position: relative; height: 92px; border-radius: 18px; overflow: hidden; cursor: pointer; box-shadow: 0 4px 16px rgba(249,115,22,.12); }
.cat img { width: 100%; height: 100%; object-fit: cover; display: block; transition: .3s; }
.cat:hover img { transform: scale(1.07); }
.cat-t { position: absolute; left: 12px; bottom: 10px; color: #fff; font-weight: 800; font-size: 15.5px; text-shadow: 0 2px 8px rgba(0,0,0,.55); }
.coupon-strip { margin-top: 20px; background: linear-gradient(90deg,#fff1e6,#ffe4c7); border: 1px dashed #fdba74; border-radius: 18px; padding: 14px 18px; display: flex; align-items: center; gap: 18px; flex-wrap: wrap; }
.cs-tx { font-size: 15px; color: #9a3412; } .cs-tx span { display: block; font-size: 12px; color: #b45309; margin-top: 2px; }
.cs-item { display: flex; align-items: center; gap: 9px; background: #fff; border-radius: 12px; padding: 8px 12px; font-size: 13px; color: #57534e; }
.cs-item b { color: #dc2626; }
.sec-head { display: flex; align-items: baseline; justify-content: space-between; margin: 28px 0 16px; }
.sec-head h2 { margin: 0; font-size: 21px; color: #431407; }
.sec-head a { color: #ea580c; cursor: pointer; font-size: 13.5px; }
.assure { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; background: #fff; border-radius: 18px; padding: 20px; box-shadow: 0 4px 20px rgba(249,115,22,.08); margin-top: 6px; }
.a-item { display: flex; gap: 12px; } .a-ic { font-size: 30px; }
.a-item b { font-size: 15px; } .a-item p { margin: 3px 0 0; color: #78716c; font-size: 12.5px; line-height: 1.6; }
.rev-card { background: #fff; border-radius: 18px; padding: 16px 18px; box-shadow: 0 4px 20px rgba(249,115,22,.08); height: 100%; }
.rev-top { display: flex; gap: 10px; align-items: center; }
.rev-who { font-size: 13px; color: #9a3412; font-weight: 600; }
.rev-card p { color: #57534e; font-size: 13.5px; line-height: 1.75; margin: 8px 0; }
.rev-tags { display: flex; gap: 6px; flex-wrap: wrap; }
.notice-card { background: #fff; border-radius: 18px; padding: 8px 18px; box-shadow: 0 4px 20px rgba(249,115,22,.08); }
.n-item { display: flex; gap: 10px; padding: 12px 0; border-bottom: 1px dashed #fed7aa; }
.n-item:last-child { border-bottom: none; }
.n-item b { font-size: 14px; } .n-item p { margin: 3px 0 0; color: #78716c; font-size: 12.5px; line-height: 1.6; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.cta { margin-top: 26px; background: linear-gradient(90deg,#ea580c,#fb923c); border-radius: 20px; padding: 24px 30px; display: flex; justify-content: space-between; align-items: center; color: #fff; }
.cta h3 { margin: 0 0 6px; font-size: 19px; } .cta p { margin: 0; opacity: .9; font-size: 13.5px; }
.dlg-cover { width: 100%; height: 260px; object-fit: cover; border-radius: 14px; display: block; }
@media (max-width: 1000px) { .cats { grid-template-columns: repeat(3, 1fr); } .assure { grid-template-columns: 1fr 1fr; } }
</style>
