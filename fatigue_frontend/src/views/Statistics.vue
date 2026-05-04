<!--&lt;!&ndash;<template>&ndash;&gt;-->
<!--&lt;!&ndash;  <div class="statistics-container">&ndash;&gt;-->
<!--&lt;!&ndash;    <el-row :gutter="20">&ndash;&gt;-->
<!--&lt;!&ndash;      <el-col :span="8">&ndash;&gt;-->
<!--&lt;!&ndash;        <el-card class="stat-card">&ndash;&gt;-->
<!--&lt;!&ndash;          <div class="stat-icon" style="background: #67c23a;">&ndash;&gt;-->
<!--&lt;!&ndash;            <el-icon size="40"><CircleCheck /></el-icon>&ndash;&gt;-->
<!--&lt;!&ndash;          </div>&ndash;&gt;-->
<!--&lt;!&ndash;          <div class="stat-info">&ndash;&gt;-->
<!--&lt;!&ndash;            <div class="stat-title">清醒次数</div>&ndash;&gt;-->
<!--&lt;!&ndash;            <div class="stat-number">{{ stats.alertCount || 0 }}</div>&ndash;&gt;-->
<!--&lt;!&ndash;          </div>&ndash;&gt;-->
<!--&lt;!&ndash;        </el-card>&ndash;&gt;-->
<!--&lt;!&ndash;      </el-col>&ndash;&gt;-->

<!--&lt;!&ndash;      <el-col :span="8">&ndash;&gt;-->
<!--&lt;!&ndash;        <el-card class="stat-card">&ndash;&gt;-->
<!--&lt;!&ndash;          <div class="stat-icon" style="background: #f56c6c;">&ndash;&gt;-->
<!--&lt;!&ndash;            <el-icon size="40"><Warning /></el-icon>&ndash;&gt;-->
<!--&lt;!&ndash;          </div>&ndash;&gt;-->
<!--&lt;!&ndash;          <div class="stat-info">&ndash;&gt;-->
<!--&lt;!&ndash;            <div class="stat-title">疲劳次数</div>&ndash;&gt;-->
<!--&lt;!&ndash;            <div class="stat-number">{{ stats.drowsyCount || 0 }}</div>&ndash;&gt;-->
<!--&lt;!&ndash;          </div>&ndash;&gt;-->
<!--&lt;!&ndash;        </el-card>&ndash;&gt;-->
<!--&lt;!&ndash;      </el-col>&ndash;&gt;-->

<!--&lt;!&ndash;      <el-col :span="8">&ndash;&gt;-->
<!--&lt;!&ndash;        <el-card class="stat-card">&ndash;&gt;-->
<!--&lt;!&ndash;          <div class="stat-icon" style="background: #409eff;">&ndash;&gt;-->
<!--&lt;!&ndash;            <el-icon size="40"><TrendCharts /></el-icon>&ndash;&gt;-->
<!--&lt;!&ndash;          </div>&ndash;&gt;-->
<!--&lt;!&ndash;          <div class="stat-info">&ndash;&gt;-->
<!--&lt;!&ndash;            <div class="stat-title">疲劳率</div>&ndash;&gt;-->
<!--&lt;!&ndash;            <div class="stat-number">{{ stats.drowsyRate || '0%' }}</div>&ndash;&gt;-->
<!--&lt;!&ndash;          </div>&ndash;&gt;-->
<!--&lt;!&ndash;        </el-card>&ndash;&gt;-->
<!--&lt;!&ndash;      </el-col>&ndash;&gt;-->
<!--&lt;!&ndash;    </el-row>&ndash;&gt;-->

<!--&lt;!&ndash;    <el-card style="margin-top: 20px;">&ndash;&gt;-->
<!--&lt;!&ndash;      <template #header>&ndash;&gt;-->
<!--&lt;!&ndash;        <div class="card-header">&ndash;&gt;-->
<!--&lt;!&ndash;          <span>检测趋势（最近7天）</span>&ndash;&gt;-->
<!--&lt;!&ndash;          <el-radio-group v-model="days" @change="loadStats">&ndash;&gt;-->
<!--&lt;!&ndash;            <el-radio-button :label="7">7天</el-radio-button>&ndash;&gt;-->
<!--&lt;!&ndash;            <el-radio-button :label="30">30天</el-radio-button>&ndash;&gt;-->
<!--&lt;!&ndash;          </el-radio-group>&ndash;&gt;-->
<!--&lt;!&ndash;        </div>&ndash;&gt;-->
<!--&lt;!&ndash;      </template>&ndash;&gt;-->

<!--&lt;!&ndash;      <div ref="chartRef" class="chart-container"></div>&ndash;&gt;-->
<!--&lt;!&ndash;    </el-card>&ndash;&gt;-->
<!--&lt;!&ndash;  </div>&ndash;&gt;-->
<!--&lt;!&ndash;</template>&ndash;&gt;-->

<!--&lt;!&ndash;<script setup>&ndash;&gt;-->
<!--&lt;!&ndash;import { ref, onMounted, nextTick } from 'vue'&ndash;&gt;-->
<!--&lt;!&ndash;import * as echarts from 'echarts'&ndash;&gt;-->
<!--&lt;!&ndash;import { CircleCheck, Warning, TrendCharts } from '@element-plus/icons-vue'&ndash;&gt;-->
<!--&lt;!&ndash;import { getStatistics } from '@/api'&ndash;&gt;-->

<!--&lt;!&ndash;const stats = ref({})&ndash;&gt;-->
<!--&lt;!&ndash;const days = ref(7)&ndash;&gt;-->
<!--&lt;!&ndash;const chartRef = ref(null)&ndash;&gt;-->
<!--&lt;!&ndash;let chart = null&ndash;&gt;-->

<!--&lt;!&ndash;const loadStats = async () => {&ndash;&gt;-->
<!--&lt;!&ndash;  try {&ndash;&gt;-->
<!--&lt;!&ndash;    const res = await getStatistics('user001', days.value)&ndash;&gt;-->
<!--&lt;!&ndash;    if (res.data.code === 200) {&ndash;&gt;-->
<!--&lt;!&ndash;      stats.value = res.data.data&ndash;&gt;-->
<!--&lt;!&ndash;      updateChart()&ndash;&gt;-->
<!--&lt;!&ndash;    }&ndash;&gt;-->
<!--&lt;!&ndash;  } catch (error) {&ndash;&gt;-->
<!--&lt;!&ndash;    console.error('加载统计失败:', error)&ndash;&gt;-->
<!--&lt;!&ndash;  }&ndash;&gt;-->
<!--&lt;!&ndash;}&ndash;&gt;-->

<!--&lt;!&ndash;const updateChart = () => {&ndash;&gt;-->
<!--&lt;!&ndash;  if (!chart) return&ndash;&gt;-->

<!--&lt;!&ndash;  const option = {&ndash;&gt;-->
<!--&lt;!&ndash;    tooltip: {&ndash;&gt;-->
<!--&lt;!&ndash;      trigger: 'axis'&ndash;&gt;-->
<!--&lt;!&ndash;    },&ndash;&gt;-->
<!--&lt;!&ndash;    legend: {&ndash;&gt;-->
<!--&lt;!&ndash;      data: ['清醒', '疲劳']&ndash;&gt;-->
<!--&lt;!&ndash;    },&ndash;&gt;-->
<!--&lt;!&ndash;    xAxis: {&ndash;&gt;-->
<!--&lt;!&ndash;      type: 'category',&ndash;&gt;-->
<!--&lt;!&ndash;      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']&ndash;&gt;-->
<!--&lt;!&ndash;    },&ndash;&gt;-->
<!--&lt;!&ndash;    yAxis: {&ndash;&gt;-->
<!--&lt;!&ndash;      type: 'value'&ndash;&gt;-->
<!--&lt;!&ndash;    },&ndash;&gt;-->
<!--&lt;!&ndash;    series: [&ndash;&gt;-->
<!--&lt;!&ndash;      {&ndash;&gt;-->
<!--&lt;!&ndash;        name: '清醒',&ndash;&gt;-->
<!--&lt;!&ndash;        type: 'bar',&ndash;&gt;-->
<!--&lt;!&ndash;        data: [120, 132, 101, 134, 90, 230, 210],&ndash;&gt;-->
<!--&lt;!&ndash;        itemStyle: { color: '#67c23a' }&ndash;&gt;-->
<!--&lt;!&ndash;      },&ndash;&gt;-->
<!--&lt;!&ndash;      {&ndash;&gt;-->
<!--&lt;!&ndash;        name: '疲劳',&ndash;&gt;-->
<!--&lt;!&ndash;        type: 'bar',&ndash;&gt;-->
<!--&lt;!&ndash;        data: [20, 12, 21, 14, 30, 40, 20],&ndash;&gt;-->
<!--&lt;!&ndash;        itemStyle: { color: '#f56c6c' }&ndash;&gt;-->
<!--&lt;!&ndash;      }&ndash;&gt;-->
<!--&lt;!&ndash;    ]&ndash;&gt;-->
<!--&lt;!&ndash;  }&ndash;&gt;-->

<!--&lt;!&ndash;  chart.setOption(option)&ndash;&gt;-->
<!--&lt;!&ndash;}&ndash;&gt;-->

<!--&lt;!&ndash;onMounted(async () => {&ndash;&gt;-->
<!--&lt;!&ndash;  await loadStats()&ndash;&gt;-->
<!--&lt;!&ndash;  await nextTick()&ndash;&gt;-->
<!--&lt;!&ndash;  if (chartRef.value) {&ndash;&gt;-->
<!--&lt;!&ndash;    chart = echarts.init(chartRef.value)&ndash;&gt;-->
<!--&lt;!&ndash;    updateChart()&ndash;&gt;-->
<!--&lt;!&ndash;    window.addEventListener('resize', () => chart.resize())&ndash;&gt;-->
<!--&lt;!&ndash;  }&ndash;&gt;-->
<!--&lt;!&ndash;})&ndash;&gt;-->
<!--&lt;!&ndash;</script>&ndash;&gt;-->

<!--&lt;!&ndash;<style scoped>&ndash;&gt;-->
<!--&lt;!&ndash;.statistics-container {&ndash;&gt;-->
<!--&lt;!&ndash;  max-width: 1200px;&ndash;&gt;-->
<!--&lt;!&ndash;  margin: 0 auto;&ndash;&gt;-->
<!--&lt;!&ndash;}&ndash;&gt;-->

<!--&lt;!&ndash;.stat-card {&ndash;&gt;-->
<!--&lt;!&ndash;  display: flex;&ndash;&gt;-->
<!--&lt;!&ndash;  align-items: center;&ndash;&gt;-->
<!--&lt;!&ndash;  padding: 20px;&ndash;&gt;-->
<!--&lt;!&ndash;}&ndash;&gt;-->

<!--&lt;!&ndash;.stat-icon {&ndash;&gt;-->
<!--&lt;!&ndash;  width: 80px;&ndash;&gt;-->
<!--&lt;!&ndash;  height: 80px;&ndash;&gt;-->
<!--&lt;!&ndash;  border-radius: 50%;&ndash;&gt;-->
<!--&lt;!&ndash;  display: flex;&ndash;&gt;-->
<!--&lt;!&ndash;  align-items: center;&ndash;&gt;-->
<!--&lt;!&ndash;  justify-content: center;&ndash;&gt;-->
<!--&lt;!&ndash;  color: white;&ndash;&gt;-->
<!--&lt;!&ndash;  margin-right: 20px;&ndash;&gt;-->
<!--&lt;!&ndash;}&ndash;&gt;-->

<!--&lt;!&ndash;.stat-info {&ndash;&gt;-->
<!--&lt;!&ndash;  flex: 1;&ndash;&gt;-->
<!--&lt;!&ndash;}&ndash;&gt;-->

<!--&lt;!&ndash;.stat-title {&ndash;&gt;-->
<!--&lt;!&ndash;  font-size: 16px;&ndash;&gt;-->
<!--&lt;!&ndash;  color: #909399;&ndash;&gt;-->
<!--&lt;!&ndash;  margin-bottom: 10px;&ndash;&gt;-->
<!--&lt;!&ndash;}&ndash;&gt;-->

<!--&lt;!&ndash;.stat-number {&ndash;&gt;-->
<!--&lt;!&ndash;  font-size: 32px;&ndash;&gt;-->
<!--&lt;!&ndash;  font-weight: bold;&ndash;&gt;-->
<!--&lt;!&ndash;  color: #303133;&ndash;&gt;-->
<!--&lt;!&ndash;}&ndash;&gt;-->

<!--&lt;!&ndash;.card-header {&ndash;&gt;-->
<!--&lt;!&ndash;  display: flex;&ndash;&gt;-->
<!--&lt;!&ndash;  justify-content: space-between;&ndash;&gt;-->
<!--&lt;!&ndash;  align-items: center;&ndash;&gt;-->
<!--&lt;!&ndash;}&ndash;&gt;-->

<!--&lt;!&ndash;.chart-container {&ndash;&gt;-->
<!--&lt;!&ndash;  height: 400px;&ndash;&gt;-->
<!--&lt;!&ndash;}&ndash;&gt;-->
<!--&lt;!&ndash;</style>&ndash;&gt;-->
<!--<template>-->
<!--  <div class="statistics-container">-->
<!--    <el-row :gutter="20">-->
<!--      <el-col :span="8">-->
<!--        <el-card class="stat-card" shadow="hover">-->
<!--          <transition name="fade">-->
<!--            <div class="stat-icon stat-icon-circle-check">-->
<!--              <el-icon size="40"><CircleCheck /></el-icon>-->
<!--            </div>-->
<!--          </transition>-->
<!--          <div class="stat-info">-->
<!--            <div class="stat-title">清醒次数</div>-->
<!--            <div class="stat-number">{{ stats.alertCount || 0 }}</div>-->
<!--          </div>-->
<!--        </el-card>-->
<!--      </el-col>-->
<!--      <el-col :span="8">-->
<!--        <el-card class="stat-card" shadow="hover">-->
<!--          <transition name="fade">-->
<!--            <div class="stat-icon stat-icon-warning">-->
<!--              <el-icon size="40"><Warning /></el-icon>-->
<!--            </div>-->
<!--          </transition>-->
<!--          <div class="stat-info">-->
<!--            <div class="stat-title">疲劳次数</div>-->
<!--            <div class="stat-number">{{ stats.drowsyCount || 0 }}</div>-->
<!--          </div>-->
<!--        </el-card>-->
<!--      </el-col>-->
<!--      <el-col :span="8">-->
<!--        <el-card class="stat-card" shadow="hover">-->
<!--          <transition name="fade">-->
<!--            <div class="stat-icon stat-icon-trend-charts">-->
<!--              <el-icon size="40"><TrendCharts /></el-icon>-->
<!--            </div>-->
<!--          </transition>-->
<!--          <div class="stat-info">-->
<!--            <div class="stat-title">疲劳率</div>-->
<!--            <div class="stat-number">{{ stats.drowsyRate || '0%' }}</div>-->
<!--          </div>-->
<!--        </el-card>-->
<!--      </el-col>-->
<!--    </el-row>-->
<!--    <el-card style="margin-top: 20px;" shadow="hover">-->
<!--      <template #header>-->
<!--        <div class="card-header">-->
<!--          <span>检测趋势（最近{{ days }}天）</span>-->
<!--          <el-radio-group v-model="days" @change="loadStats">-->
<!--            <el-radio-button :label="7">7天</el-radio-button>-->
<!--            <el-radio-button :label="30">30天</el-radio-button>-->
<!--          </el-radio-group>-->
<!--        </div>-->
<!--      </template>-->
<!--      <div ref="chartRef" class="chart-container"></div>-->
<!--    </el-card>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup>-->
<!--import { ref, onMounted, nextTick } from 'vue'-->
<!--import * as echarts from 'echarts'-->
<!--import { CircleCheck, Warning, TrendCharts } from '@element-plus/icons-vue'-->
<!--import { getStatistics } from '@/api'-->

<!--const stats = ref({})-->
<!--const days = ref(7)-->
<!--const chartRef = ref(null)-->
<!--let chart = null-->

<!--const loadStats = async () => {-->
<!--  try {-->
<!--    const res = await getStatistics('user001', days.value)-->
<!--    if (res.data.code === 200) {-->
<!--      stats.value = res.data.data-->
<!--      updateChart()-->
<!--    }-->
<!--  } catch (error) {-->
<!--    console.error('加载统计失败:', error)-->
<!--  }-->
<!--}-->

<!--const updateChart = () => {-->
<!--  if (!chart) return-->

<!--  const option = {-->
<!--    tooltip: {-->
<!--      trigger: 'axis',-->
<!--      axisPointer: {-->
<!--        type: 'shadow'-->
<!--      }-->
<!--    },-->
<!--    legend: {-->
<!--      data: ['清醒', '疲劳'],-->
<!--      top: '10%'-->
<!--    },-->
<!--    xAxis: {-->
<!--      type: 'category',-->
<!--      data: stats.value.days || ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],-->
<!--      axisLabel: {-->
<!--        interval: 0,-->
<!--        rotate: 45-->
<!--      }-->
<!--    },-->
<!--    yAxis: {-->
<!--      type: 'value',-->
<!--      name: '次数',-->
<!--      nameTextStyle: {-->
<!--        color: '#303133'-->
<!--      }-->
<!--    },-->
<!--    series: [-->
<!--      {-->
<!--        name: '清醒',-->
<!--        type: 'bar',-->
<!--        stack: '总量',-->
<!--        itemStyle: { color: '#67c23a' },-->
<!--        data: stats.value.alertCountPerDay || [120, 132, 101, 134, 90, 230, 210],-->
<!--        label: {-->
<!--          show: true,-->
<!--          position: 'top',-->
<!--          color: '#303133'-->
<!--        }-->
<!--      },-->
<!--      {-->
<!--        name: '疲劳',-->
<!--        type: 'bar',-->
<!--        stack: '总量',-->
<!--        itemStyle: { color: '#f56c6c' },-->
<!--        data: stats.value.drowsyCountPerDay || [20, 12, 21, 14, 30, 40, 20],-->
<!--        label: {-->
<!--          show: true,-->
<!--          position: 'top',-->
<!--          color: '#303133'-->
<!--        }-->
<!--      }-->
<!--    ]-->
<!--  }-->

<!--  chart.setOption(option)-->
<!--}-->

<!--onMounted(async () => {-->
<!--  await loadStats()-->
<!--  await nextTick()-->
<!--  if (chartRef.value) {-->
<!--    chart = echarts.init(chartRef.value)-->
<!--    updateChart()-->
<!--    window.addEventListener('resize', () => chart.resize())-->
<!--  }-->
<!--})-->
<!--</script>-->

<!--<style scoped>-->
<!--@import '../assets/main.css';-->

<!--.statistics-container {-->
<!--  background-color: #ffffff;-->
<!--  border-radius: 10px;-->
<!--  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);-->
<!--}-->

<!--.stat-card {-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  padding: 20px;-->
<!--  transition: transform 0.3s, box-shadow 0.3s;-->
<!--  border-radius: 10px;-->
<!--  background-color: #f8f9fa;-->
<!--}-->

<!--.stat-card:hover {-->
<!--  transform: translateY(-5px);-->
<!--  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.2);-->
<!--}-->

<!--.stat-icon {-->
<!--  width: 80px;-->
<!--  height: 80px;-->
<!--  border-radius: 50%;-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  justify-content: center;-->
<!--  color: white;-->
<!--  margin-right: 20px;-->
<!--}-->

<!--.stat-info {-->
<!--  flex: 1;-->
<!--}-->

<!--.stat-title {-->
<!--  font-size: 18px;-->
<!--  color: #909399;-->
<!--  margin-bottom: 10px;-->
<!--}-->

<!--.stat-number {-->
<!--  font-size: 36px;-->
<!--  font-weight: bold;-->
<!--  color: #303133;-->
<!--}-->

<!--.card-header {-->
<!--  display: flex;-->
<!--  justify-content: space-between;-->
<!--  align-items: center;-->
<!--}-->

<!--.chart-container {-->
<!--  height: 400px;-->
<!--}-->

<!--.fade-enter-active, .fade-leave-active {-->
<!--  transition: opacity 0.5s;-->
<!--}-->

<!--.fade-enter, .fade-leave-to {-->
<!--  opacity: 0;-->
<!--}-->
<!--</style>-->

<template>
  <div class="statistics-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="main-title">

        </h1>
        <p class="subtitle">实时监控驾驶员状态，智能分析疲劳趋势</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" class="refresh-btn" @click="loadStats" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-button class="export-btn" @click="exportData">
          <el-icon><Download /></el-icon>
          导出报表
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="8">
        <div class="stat-card glass-effect" :class="{ 'pulse': stats.alertCount > 0 }">
          <div class="card-glow alert-glow"></div>
          <div class="stat-content">
            <div class="stat-icon-wrapper alert-bg">
              <el-icon size="32"><CircleCheck /></el-icon>
              <div class="icon-ring"></div>
            </div>
            <div class="stat-details">
              <span class="stat-label">清醒检测次数</span>
              <div class="stat-value-wrapper">
                <span class="stat-value" :class="{ 'count-up': isAnimating }">{{ animatedAlertCount }}</span>
                <span class="stat-unit">次</span>
              </div>
              <div class="stat-trend positive">
                <el-icon><ArrowUp /></el-icon>
                <span>占比 {{ alertRate }}%</span>
              </div>
            </div>
          </div>
          <div class="progress-bar">
            <div class="progress-fill alert-progress" :style="{ width: alertRate + '%' }"></div>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="8">
        <div class="stat-card glass-effect warning" :class="{ 'pulse-warning': stats.drowsyCount > 0 }">
          <div class="card-glow warning-glow"></div>
          <div class="stat-content">
            <div class="stat-icon-wrapper warning-bg">
              <el-icon size="32"><Warning /></el-icon>
              <div class="icon-ring warning-ring"></div>
            </div>
            <div class="stat-details">
              <span class="stat-label">疲劳预警次数</span>
              <div class="stat-value-wrapper">
                <span class="stat-value warning-text" :class="{ 'count-up': isAnimating }">{{ animatedDrowsyCount }}</span>
                <span class="stat-unit">次</span>
              </div>
              <div class="stat-trend" :class="drowsyRateNum > 20 ? 'negative' : 'neutral'">
                <el-icon><WarningFilled /></el-icon>
                <span>占比 {{ drowsyRateNum }}%</span>
              </div>
            </div>
          </div>
          <div class="progress-bar">
            <div class="progress-fill warning-progress" :style="{ width: drowsyRateNum + '%' }"></div>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="8">
        <div class="stat-card glass-effect primary">
          <div class="card-glow primary-glow"></div>
          <div class="stat-content">
            <div class="stat-icon-wrapper primary-bg">
              <el-icon size="32"><PieChart /></el-icon>
              <div class="icon-ring primary-ring"></div>
            </div>
            <div class="stat-details">
              <span class="stat-label">总检测次数</span>
              <div class="stat-value-wrapper">
                <span class="stat-value primary-text">{{ totalCount }}</span>
                <span class="stat-unit">次</span>
              </div>
              <div class="stat-trend neutral">
                <el-icon><Timer /></el-icon>
                <span>近{{ days }}天统计</span>
              </div>
            </div>
          </div>
          <div class="circular-progress">
            <el-progress
                type="dashboard"
                :percentage="drowsyRateNum"
                :color="progressColors"
                :stroke-width="8"
                :width="80"
            />
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :span="24">
          <div class="chart-card glass-effect">
            <div class="chart-header">
              <div class="chart-title-group">
                <h3 class="chart-title">
                  <el-icon><DataLine /></el-icon>
                  检测趋势分析
                </h3>
                <p class="chart-desc">最近{{ days }}天的清醒与疲劳检测数据对比</p>
              </div>
              <div class="chart-controls">
                <el-radio-group v-model="days" @change="handleDaysChange" size="default">
                  <el-radio-button :label="7">近7天</el-radio-button>
                  <el-radio-button :label="30">近30天</el-radio-button>
                </el-radio-group>
              </div>
            </div>
            <div ref="chartRef" class="main-chart"></div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :xs="24" :lg="12">
          <div class="chart-card glass-effect">
            <div class="chart-header compact">
              <h3 class="chart-title">
                <el-icon><PieChart /></el-icon>
                状态分布占比
              </h3>
            </div>
            <div ref="pieChartRef" class="sub-chart"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="12">
          <div class="chart-card glass-effect">
            <div class="chart-header compact">
              <h3 class="chart-title">
                <el-icon><Odometer /></el-icon>
                疲劳率趋势
              </h3>
            </div>
            <div ref="lineChartRef" class="sub-chart"></div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 数据表格 -->
    <div class="table-section glass-effect">
      <div class="table-header">
        <h3 class="table-title">
          <el-icon><List /></el-icon>
          详细数据记录
        </h3>
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="tableData.length"
            layout="total, sizes, prev, pager, next"
            class="custom-pagination"
            background
        />
      </div>
      <el-table
          :data="paginatedData"
          style="width: 100%"
          :header-cell-style="headerStyle"
          :cell-style="cellStyle"
          class="custom-table"
          stripe
      >
        <el-table-column prop="date" label="日期" min-width="120">
          <template #default="{ row }">
            <div class="date-cell">
              <el-icon><Calendar /></el-icon>
              <span>{{ row.date }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="alertCount" label="清醒次数" min-width="100">
          <template #default="{ row }">
            <el-tag type="success" effect="dark" class="count-tag">
              {{ row.alertCount }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="drowsyCount" label="疲劳次数" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.drowsyCount > 10 ? 'danger' : 'warning'" effect="dark" class="count-tag">
              {{ row.drowsyCount }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="total" label="总检测次数" min-width="100">
          <template #default="{ row }">
            <span class="total-count">{{ row.total }}</span>
          </template>
        </el-table-column>
        <el-table-column label="疲劳率" min-width="150">
          <template #default="{ row }">
            <div class="rate-cell">
              <el-progress
                  :percentage="row.rate"
                  :color="progressColors"
                  :stroke-width="6"
                  class="rate-progress"
              />
              <span class="rate-text">{{ row.rate }}%</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <div class="status-cell">
              <span class="status-dot" :class="getStatusClass(row.rate)"></span>
              <span class="status-text">{{ getStatusText(row.rate) }}</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed, watch } from 'vue'
import * as echarts from 'echarts'
import { getStatistics } from '@/api'
import { ElMessage } from 'element-plus'
import {
  TrendCharts, Refresh, Download, CircleCheck, Warning, PieChart,
  ArrowUp, WarningFilled, Timer, DataLine, Calendar, List,
  Odometer
} from '@element-plus/icons-vue'

// 数据状态
const stats = ref({
  alertCount: 0,
  drowsyCount: 0,
  drowsyRate: '0%',
  days: [],
  alertCountPerDay: [],
  drowsyCountPerDay: []
})
const days = ref(7)
const loading = ref(false)
const isAnimating = ref(false)

// 动画数值
const animatedAlertCount = ref(0)
const animatedDrowsyCount = ref(0)

// 图表引用
const chartRef = ref(null)
const pieChartRef = ref(null)
const lineChartRef = ref(null)
let mainChart = null
let pieChart = null
let lineChart = null

// 表格分页
const currentPage = ref(1)
const pageSize = ref(10)
const tableData = ref([])

// 计算属性
const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return tableData.value.slice(start, end)
})

const totalCount = computed(() => {
  return (stats.value.alertCount || 0) + (stats.value.drowsyCount || 0)
})

const alertRate = computed(() => {
  const total = totalCount.value
  return total > 0 ? ((stats.value.alertCount / total) * 100).toFixed(1) : 0
})

const drowsyRateNum = computed(() => {
  const rate = stats.value.drowsyRate || '0%'
  return parseFloat(rate.replace('%', ''))
})

const progressColors = [
  { color: '#10b981', percentage: 20 },
  { color: '#f59e0b', percentage: 40 },
  { color: '#ef4444', percentage: 60 }
]

// 动画函数
const animateValue = (start, end, duration, callback) => {
  const startTime = performance.now()
  const animate = (currentTime) => {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    const easeOutQuart = 1 - Math.pow(1 - progress, 4)
    callback(Math.floor(start + (end - start) * easeOutQuart))
    if (progress < 1) {
      requestAnimationFrame(animate)
    }
  }
  requestAnimationFrame(animate)
}

// 加载统计数据
const loadStats = async () => {
  loading.value = true
  try {
    const res = await getStatistics('user001', days.value)
    if (res.data.code === 200) {
      const newStats = res.data.data

      // 数字动画
      isAnimating.value = true
      animateValue(animatedAlertCount.value, newStats.alertCount || 0, 800, (val) => {
        animatedAlertCount.value = val
      })
      animateValue(animatedDrowsyCount.value, newStats.drowsyCount || 0, 800, (val) => {
        animatedDrowsyCount.value = val
      })

      setTimeout(() => {
        isAnimating.value = false
      }, 800)

      stats.value = newStats
      generateTableData()

      nextTick(() => {
        updateMainChart()
        updatePieChart()
        updateLineChart()
      })
    }
  } catch (error) {
    console.error('加载统计失败:', error)
    ElMessage.error('数据加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 生成表格数据
const generateTableData = () => {
  const data = []
  const daysList = stats.value.days || []
  const alertList = stats.value.alertCountPerDay || []
  const drowsyList = stats.value.drowsyCountPerDay || []

  for (let i = 0; i < daysList.length; i++) {
    const alert = alertList[i] || 0
    const drowsy = drowsyList[i] || 0
    const total = alert + drowsy
    data.push({
      date: daysList[i],
      alertCount: alert,
      drowsyCount: drowsy,
      total: total,
      rate: total > 0 ? Math.round((drowsy / total) * 100) : 0
    })
  }

  tableData.value = data.reverse()
}

// 获取状态
const getStatusClass = (rate) => {
  if (rate < 10) return 'status-excellent'
  if (rate < 20) return 'status-good'
  if (rate < 30) return 'status-warning'
  return 'status-danger'
}

const getStatusText = (rate) => {
  if (rate < 10) return '优秀'
  if (rate < 20) return '良好'
  if (rate < 30) return '一般'
  return '需关注'
}

// 表格样式
const headerStyle = () => ({
  background: 'rgba(30, 41, 59, 0.8)',
  color: '#94a3b8',
  fontWeight: 600,
  fontSize: '13px',
  borderBottom: '1px solid rgba(148, 163, 184, 0.1)'
})

const cellStyle = () => ({
  background: 'transparent',
  color: '#e2e8f0',
  borderBottom: '1px solid rgba(148, 163, 184, 0.05)'
})

// 更新主图表
const updateMainChart = () => {
  if (!mainChart) return

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(15, 23, 42, 0.9)',
      borderColor: 'rgba(94, 234, 212, 0.2)',
      textStyle: { color: '#e2e8f0' }
    },
    legend: {
      data: ['清醒', '疲劳'],
      top: 0,
      right: 20,
      textStyle: { color: '#94a3b8', fontSize: 13 },
      itemWidth: 16,
      itemHeight: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: stats.value.days || [],
      axisLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.2)' } },
      axisLabel: {
        color: '#94a3b8',
        fontSize: 12,
        interval: 0,
        rotate: days.value > 7 ? 45 : 0
      },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      name: '检测次数',
      nameTextStyle: { color: '#64748b', fontSize: 12 },
      axisLine: { show: false },
      axisLabel: { color: '#64748b' },
      splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.1)', type: 'dashed' } }
    },
    series: [
      {
        name: '清醒',
        type: 'bar',
        stack: '总量',
        barWidth: '50%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#34d399' },
            { offset: 1, color: '#10b981' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        data: stats.value.alertCountPerDay || []
      },
      {
        name: '疲劳',
        type: 'bar',
        stack: '总量',
        barWidth: '50%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#fbbf24' },
            { offset: 1, color: '#f59e0b' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        data: stats.value.drowsyCountPerDay || []
      }
    ]
  }

  mainChart.setOption(option, true)
}

// 更新饼图
const updatePieChart = () => {
  if (!pieChart) return

  const totalAlert = stats.value.alertCount || 0
  const totalDrowsy = stats.value.drowsyCount || 0

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(15, 23, 42, 0.9)',
      borderColor: 'rgba(94, 234, 212, 0.2)',
      textStyle: { color: '#e2e8f0' },
      formatter: '{b}: {c}次 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: { color: '#94a3b8' }
    },
    series: [
      {
        name: '状态分布',
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: 'rgba(15, 23, 42, 0.8)',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 18,
            fontWeight: 'bold',
            color: '#e2e8f0'
          }
        },
        labelLine: { show: false },
        data: [
          { value: totalAlert, name: '清醒', itemStyle: { color: '#10b981' } },
          { value: totalDrowsy, name: '疲劳', itemStyle: { color: '#f59e0b' } }
        ]
      }
    ]
  }

  pieChart.setOption(option)
}

// 更新折线图
const updateLineChart = () => {
  if (!lineChart) return

  const daysList = stats.value.days || []
  const alertList = stats.value.alertCountPerDay || []
  const drowsyList = stats.value.drowsyCountPerDay || []

  // 计算每日疲劳率
  const rateData = daysList.map((_, i) => {
    const total = (alertList[i] || 0) + (drowsyList[i] || 0)
    return total > 0 ? ((drowsyList[i] || 0) / total * 100).toFixed(1) : 0
  })

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(15, 23, 42, 0.9)',
      borderColor: 'rgba(94, 234, 212, 0.2)',
      textStyle: { color: '#e2e8f0' },
      formatter: '{b}<br/>疲劳率: {c}%'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: daysList,
      axisLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.2)' } },
      axisLabel: { color: '#94a3b8', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      name: '疲劳率(%)',
      max: 100,
      axisLine: { show: false },
      axisLabel: { color: '#64748b', formatter: '{value}%' },
      splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.1)', type: 'dashed' } }
    },
    series: [
      {
        name: '疲劳率',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#f59e0b' },
            { offset: 1, color: '#ef4444' }
          ]),
          width: 3
        },
        itemStyle: { color: '#f59e0b' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245, 158, 11, 0.3)' },
            { offset: 1, color: 'rgba(245, 158, 11, 0.05)' }
          ])
        },
        data: rateData
      }
    ]
  }

  lineChart.setOption(option)
}

// 处理天数切换
const handleDaysChange = () => {
  loadStats()
}

// 导出数据
const exportData = () => {
  ElMessage.success('数据导出功能开发中...')
}

// 初始化
onMounted(async () => {
  await loadStats()

  await nextTick()

  if (chartRef.value) {
    mainChart = echarts.init(chartRef.value)
    updateMainChart()
  }

  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
    updatePieChart()
  }

  if (lineChartRef.value) {
    lineChart = echarts.init(lineChartRef.value)
    updateLineChart()
  }

  const handleResize = () => {
    mainChart?.resize()
    pieChart?.resize()
    lineChart?.resize()
  }

  window.addEventListener('resize', handleResize)

  watch(() => stats.value, () => {
    updateMainChart()
    updatePieChart()
    updateLineChart()
  }, { deep: true })
})
</script>

<style scoped>
.statistics-page {
  padding: 20px;
  color: #e2e8f0;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.header-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.main-title {
  font-size: 26px;
  font-weight: 700;
  color: #f8fafc;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  color: #5eead4;
  font-size: 28px;
}

.subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.refresh-btn {
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
}

.refresh-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(14, 165, 233, 0.4);
}

.export-btn {
  border-color: rgba(94, 234, 212, 0.4);
  color: #5eead4;

}

.export-btn:hover {
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(94, 234, 212, 0.2);
  color: #94a3b8;
  padding: 10px 20px;
  border-radius: 8px;
}

/* 玻璃效果 */
.glass-effect {
  background: rgba(148, 174, 217, 0.5);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(118, 236, 217, 0.88);
  border-radius: 12px;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  padding: 20px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(118, 236, 217, 0.88);
  border-color: rgba(83, 231, 207, 0.2);
}

.card-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.alert-glow {
  background: radial-gradient(circle, rgba(118, 236, 217, 0.88) 0%, transparent 70%);
}

.warning-glow {
  background: radial-gradient(circle, rgba(245, 158, 11, 0.15) 0%, transparent 70%);
}

.primary-glow {
  background: radial-gradient(circle, rgba(14, 165, 233, 0.15) 0%, transparent 70%);
}

.stat-card:hover .card-glow {
  opacity: 1;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  z-index: 1;
}

.stat-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  position: relative;
  flex-shrink: 0;
}

.alert-bg {
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
  box-shadow: 0 8px 16px rgba(16, 185, 129, 0.3);
}

.warning-bg {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  box-shadow: 0 8px 16px rgba(245, 158, 11, 0.3);
}

.primary-bg {
  background: linear-gradient(135deg, #38bdf8 0%, #0ea5e9 100%);
  box-shadow: 0 8px 16px rgba(14, 165, 233, 0.3);
}

.icon-ring {
  position: absolute;
  inset: -4px;
  border: 2px solid rgba(16, 185, 129, 0.3);
  border-radius: 16px;
  animation: pulse-ring 2s infinite;
}

.warning-ring {
  border-color: rgba(245, 158, 11, 0.3);
}

.primary-ring {
  border-color: rgba(14, 165, 233, 0.3);
}

@keyframes pulse-ring {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.05); opacity: 0.5; }
}

.stat-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 13px;
  color: #94a3b8;
  font-weight: 500;
}

.stat-value-wrapper {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #f8fafc;
  line-height: 1;
}

.warning-text {
  color: #fbbf24;
}

.primary-text {
  color: #38bdf8;
}

.stat-unit {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 500;
  margin-top: 2px;
}

.positive {
  color: #34d399;
}

.negative {
  color: #f87171;
}

.neutral {
  color: #94a3b8;
}

/* 进度条 */
.progress-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: rgba(148, 163, 184, 0.1);
  border-radius: 0 0 12px 12px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  transition: width 1s ease;
}

.alert-progress {
  background: linear-gradient(90deg, #10b981, #34d399);
}

.warning-progress {
  background: linear-gradient(90deg, #f59e0b, #fbbf24);
}

/* 环形进度 */
.circular-progress {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
}

:deep(.circular-progress .el-progress__text) {
  color: #f8fafc !important;
  font-weight: 600;
  font-size: 14px !important;
}

/* 脉冲动画 */
.pulse {
  animation: card-pulse 2s infinite;
}

.pulse-warning {
  animation: card-pulse-warning 2s infinite;
}

@keyframes card-pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.2); }
  50% { box-shadow: 0 0 0 8px rgba(16, 185, 129, 0); }
}

@keyframes card-pulse-warning {
  0%, 100% { box-shadow: 0 0 0 0 rgba(245, 158, 11, 0.2); }
  50% { box-shadow: 0 0 0 8px rgba(245, 158, 11, 0); }
}

/* 图表区域 */
.charts-section {
  margin-bottom: 20px;
}

.chart-card {
  padding: 20px;
  height: 100%;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.chart-header.compact {
  margin-bottom: 12px;
}

.chart-title-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #f8fafc;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.chart-desc {
  font-size: 13px;
  color: #64748b;
  margin: 0;
}

.chart-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.main-chart {
  height: 350px;
  width: 100%;
}

.sub-chart {
  height: 280px;
  width: 100%;
}

/* 表格区域 */
.table-section {
  padding: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #f8fafc;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 表格样式 */
:deep(.custom-table) {
  background: transparent;
}

:deep(.custom-table .el-table__body) {
  background: transparent;
}

:deep(.custom-table tr:hover > td) {
  background: rgba(94, 234, 212, 0.05) !important;
}

.date-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #e2e8f0;
  font-size: 13px;
}

.count-tag {
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
}

.total-count {
  font-weight: 600;
  color: #38bdf8;
  font-size: 14px;
}

.rate-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rate-progress {
  flex: 1;
  max-width: 100px;
}

.rate-text {
  font-weight: 600;
  color: #94a3b8;
  min-width: 36px;
  text-align: right;
  font-size: 13px;
}

.status-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-excellent {
  background: #10b981;
  box-shadow: 0 0 8px #10b981;
}

.status-good {
  background: #38bdf8;
  box-shadow: 0 0 8px #38bdf8;
}

.status-warning {
  background: #f59e0b;
  box-shadow: 0 0 8px #f59e0b;
}

.status-danger {
  background: #ef4444;
  box-shadow: 0 0 8px #ef4444;
}

.status-text {
  font-size: 12px;
  font-weight: 500;
  color: #e2e8f0;
}

/* 分页器 */
:deep(.custom-pagination) {
  --el-pagination-button-color: #94a3b8;
  --el-pagination-hover-color: #5eead4;
}

:deep(.custom-pagination .el-pagination__total) {
  color: #64748b;
}

:deep(.custom-pagination .el-pager li) {
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(94, 234, 212, 0.1);
}

:deep(.custom-pagination .el-pager li.active) {
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  border-color: #0ea5e9;
}

/* 数字动画 */
.count-up {
  animation: count-up-anim 0.5s ease;
}

@keyframes count-up-anim {
  0% { transform: scale(1.2); }
  100% { transform: scale(1); }
}

/* 响应式 */
@media (max-width: 768px) {
  .statistics-page {
    padding: 12px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .main-title {
    font-size: 20px;
  }

  .stat-card {
    margin-bottom: 12px;
  }

  .circular-progress {
    display: none;
  }

  .main-chart {
    height: 280px;
  }

  .sub-chart {
    height: 220px;
  }
}
</style>

