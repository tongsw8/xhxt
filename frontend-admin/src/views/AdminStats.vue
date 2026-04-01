<template>
  <div class="stats-page">
    <div class="top-bar">
      <el-radio-group v-model="days" @change="loadData" size="large">
        <el-radio-button :value="7">近7天</el-radio-button>
        <el-radio-button :value="30">近30天</el-radio-button>
      </el-radio-group>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :sm="12">
        <el-card class="panel" shadow="never">
          <template #header>
            <div class="hd"><span>销售额变化（已支付）</span><span class="sum">合计 ¥{{ totalSales.toFixed(2) }}</span></div>
          </template>
          <div class="chart-wrap" @mousemove="onHover($event, salesPlot, salesTrend, 'sales')" @mouseleave="hover.sales = null">
            <svg viewBox="0 0 700 180" preserveAspectRatio="none" class="line-svg">
              <polyline class="grid" points="0,150 700,150" />
              <polyline class="line sales animated" :style="lineStyle" :points="salesPoints" />
              <g v-for="(p, i) in salesPlot" :key="'s'+i">
                <circle class="dot sales" :cx="p.x" :cy="p.y" r="4" :style="dotStyle(i)" />
              </g>
            </svg>
            <div v-if="hover.sales" class="tooltip" :style="{ left: hover.sales.left + 'px', top: hover.sales.top + 'px' }">
              <div>{{ hover.sales.date }}</div>
              <div>¥{{ Number(hover.sales.value || 0).toFixed(2) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12">
        <el-card class="panel" shadow="never">
          <template #header>
            <div class="hd"><span>新增用户变化</span><span class="sum">累计 {{ totalUsers }} 人</span></div>
          </template>
          <div class="chart-wrap" @mousemove="onHover($event, userPlot, userTrend, 'users')" @mouseleave="hover.users = null">
            <svg viewBox="0 0 700 180" preserveAspectRatio="none" class="line-svg">
              <polyline class="grid" points="0,150 700,150" />
              <polyline class="line users animated" :style="lineStyle" :points="userPoints" />
              <g v-for="(p, i) in userPlot" :key="'u'+i">
                <circle class="dot users" :cx="p.x" :cy="p.y" r="4" :style="dotStyle(i)" />
              </g>
            </svg>
            <div v-if="hover.users" class="tooltip" :style="{ left: hover.users.left + 'px', top: hover.users.top + 'px' }">
              <div>{{ hover.users.date }}</div>
              <div>{{ hover.users.value }} 人</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="panel" shadow="never">
      <template #header>
        <div class="hd"><span>热销商品（按销量）</span></div>
      </template>
      <el-table :data="hotProducts" border>
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="productName" label="商品" min-width="220" />
        <el-table-column prop="quantity" label="销量" width="120" />
        <el-table-column label="销售额" width="140">
          <template #default="{ row }">¥{{ Number(row.amount || 0).toFixed(2) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import http from '../api/http'

const days = ref(7)
const salesTrend = ref([])
const userTrend = ref([])
const hotProducts = ref([])
const hover = reactive({ sales: null, users: null })

const totalSales = computed(() => salesTrend.value.reduce((s, x) => s + Number(x.value || 0), 0))
const totalUsers = computed(() => userTrend.value.reduce((s, x) => s + Number(x.value || 0), 0))

function toPlot(list) {
  if (!list.length) return []
  const values = list.map(x => Number(x.value || 0))
  const max = Math.max(...values, 1)
  const min = Math.min(...values, 0)
  const range = max - min || 1
  return values.map((v, i) => {
    const x = 30 + (i * (640 / Math.max(list.length - 1, 1)))
    const y = 150 - ((v - min) / range) * 120
    return { x, y }
  })
}

const salesPlot = computed(() => toPlot(salesTrend.value))
const userPlot = computed(() => toPlot(userTrend.value))
const salesPoints = computed(() => salesPlot.value.map(p => `${p.x},${p.y}`).join(' '))
const userPoints = computed(() => userPlot.value.map(p => `${p.x},${p.y}`).join(' '))

const lineStyle = computed(() => ({ '--duration': '1100ms' }))
const dotStyle = (i) => ({ animationDelay: `${120 + i * 60}ms` })

function onHover(evt, points, list, type) {
  if (!points.length) return
  const rect = evt.currentTarget.getBoundingClientRect()
  const x = evt.clientX - rect.left
  const ratio = Math.max(0, Math.min(1, x / rect.width))
  const idx = Math.min(list.length - 1, Math.round(ratio * (list.length - 1)))
  const row = list[idx]
  hover[type] = {
    date: row?.date || '',
    value: row?.value || 0,
    left: Math.max(8, Math.min(rect.width - 120, x - 52)),
    top: 8,
  }
}

async function loadData() {
  const res = await http.get('/admin/dashboard/stats', { params: { days: days.value } })
  const data = res.data?.data || {}
  salesTrend.value = data.salesTrend || []
  userTrend.value = data.userTrend || []
  hotProducts.value = data.hotProducts || []
  hover.sales = null
  hover.users = null
}

onMounted(loadData)
</script>

<style scoped>
.stats-page { display: grid; gap: 16px; }
.top-bar { display: flex; justify-content: flex-end; }
.panel { border-radius: 12px; }
.hd { font-weight: 700; color: #334155; display: flex; justify-content: space-between; }
.sum { color: #0f766e; font-size: 13px; }
.chart-wrap { position: relative; padding: 6px 4px 0; }
.line-svg { width: 100%; height: 180px; background: linear-gradient(180deg, #f8fafc 0%, #ffffff 100%); border-radius: 10px; border: 1px solid #e2e8f0; }
.grid { fill: none; stroke: #e2e8f0; stroke-width: 1; }
.line { fill: none; stroke-width: 3; stroke-linecap: round; stroke-linejoin: round; }
.line.sales { stroke: #2563eb; }
.line.users { stroke: #059669; }
.line.animated { stroke-dasharray: 1500; stroke-dashoffset: 1500; animation: drawLine var(--duration) ease-out forwards; }
.dot { stroke: #fff; stroke-width: 2; opacity: 0; animation: fadeDot 300ms ease forwards; }
.dot.sales { fill: #2563eb; }
.dot.users { fill: #059669; }
.tooltip { position: absolute; min-width: 104px; padding: 8px 10px; background: rgba(15,23,42,.92); color: #fff; border-radius: 8px; font-size: 12px; pointer-events: none; }
@keyframes drawLine { to { stroke-dashoffset: 0; } }
@keyframes fadeDot { to { opacity: 1; } }
</style>