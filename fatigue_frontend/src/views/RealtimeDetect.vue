<!--<template>-->
<!--  <div class="realtime-container">-->
<!--    <el-row :gutter="20">-->
<!--      <el-col :span="16">-->
<!--        <el-card>-->
<!--          <template #header>-->
<!--            <div class="card-header">-->
<!--              <span>实时视频监测</span>-->
<!--              <div>-->
<!--                <el-tag :type="isDetecting ? 'success' : 'info'" style="margin-right: 10px;">-->
<!--                  {{ isDetecting ? '监测中' : '已停止' }}-->
<!--                </el-tag>-->
<!--                <el-button-->
<!--                    :type="isDetecting ? 'danger' : 'primary'"-->
<!--                    @click="toggleDetection"-->
<!--                >-->
<!--                  {{ isDetecting ? '停止监测' : '开始监测' }}-->
<!--                </el-button>-->
<!--              </div>-->
<!--            </div>-->
<!--          </template>-->

<!--          <div class="video-container">-->
<!--            <video-->
<!--                ref="videoRef"-->
<!--                autoplay-->
<!--                playsinline-->
<!--                muted-->
<!--                class="video-element"-->
<!--            ></video>-->

<!--            <div v-if="currentResult" class="overlay-info" :class="currentResult.resultClass">-->
<!--              <div class="status-badge">-->
<!--                {{ currentResult.resultClass === 'drowsy' ? '⚠️ 疲劳 detected' : '✓ Normal' }}-->
<!--              </div>-->
<!--              <div class="confidence">-->
<!--                Confidence: {{ (currentResult.confidence * 100).toFixed(1) }}%-->
<!--              </div>-->
<!--            </div>-->

<!--            <canvas ref="canvasRef" style="display: none;"></canvas>-->
<!--          </div>-->

<!--          <div v-if="wsStatus" class="ws-status" :class="wsStatus">-->
<!--            WebSocket: {{ wsStatus }}-->
<!--          </div>-->
<!--        </el-card>-->
<!--      </el-col>-->

<!--      <el-col :span="8">-->
<!--        <el-card>-->
<!--          <template #header>-->
<!--            <span>实时统计</span>-->
<!--          </template>-->

<!--          <div class="stats-panel">-->
<!--            <div class="stat-item">-->
<!--              <div class="stat-value">{{ stats.totalFrames }}</div>-->
<!--              <div class="stat-label">总帧数</div>-->
<!--            </div>-->

<!--            <div class="stat-item">-->
<!--              <div class="stat-value" :class="{ 'danger': stats.drowsyFrames > 0 }">-->
<!--                {{ stats.drowsyFrames }}-->
<!--              </div>-->
<!--              <div class="stat-label">疲劳帧数</div>-->
<!--            </div>-->

<!--            <div class="stat-item">-->
<!--              <div class="stat-value">{{ drowsyRate }}</div>-->
<!--              <div class="stat-label">疲劳占比</div>-->
<!--            </div>-->
<!--          </div>-->

<!--          <el-alert-->
<!--              v-if="showWarning"-->
<!--              title="持续检测到疲劳状态！"-->
<!--              type="error"-->
<!--              description="请立即停车休息，避免发生危险！"-->
<!--              show-icon-->
<!--              :closable="false"-->
<!--              style="margin-top: 20px;"-->
<!--          />-->
<!--        </el-card>-->

<!--        <el-card style="margin-top: 20px;">-->
<!--          <template #header>-->
<!--            <span>系统日志</span>-->
<!--          </template>-->
<!--          <div class="log-panel">-->
<!--            <div v-for="(log, index) in logs" :key="index" class="log-item" :class="log.type">-->
<!--              [{{ log.time }}] {{ log.message }}-->
<!--            </div>-->
<!--          </div>-->
<!--        </el-card>-->
<!--      </el-col>-->
<!--    </el-row>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup>-->
<!--import { ref, computed, onUnmounted } from 'vue'-->
<!--import { ElMessage } from 'element-plus'-->
<!--import { DetectionWebSocket } from '@/api'-->

<!--const videoRef = ref(null)-->
<!--const canvasRef = ref(null)-->
<!--const isDetecting = ref(false)-->
<!--const wsStatus = ref('disconnected')-->
<!--const currentResult = ref(null)-->
<!--const stats = ref({ totalFrames: 0, drowsyFrames: 0 })-->
<!--const logs = ref([])-->
<!--const showWarning = ref(false)-->

<!--let ws = null-->
<!--let stream = null-->
<!--let captureInterval = null-->

<!--const drowsyRate = computed(() => {-->
<!--  if (stats.value.totalFrames === 0) return '0%'-->
<!--  return ((stats.value.drowsyFrames / stats.value.totalFrames) * 100).toFixed(1) + '%'-->
<!--})-->

<!--const addLog = (message, type = 'info') => {-->
<!--  const time = new Date().toLocaleTimeString()-->
<!--  logs.value.unshift({ time, message, type })-->
<!--  if (logs.value.length > 20) logs.value.pop()-->
<!--}-->

<!--const toggleDetection = async () => {-->
<!--  if (isDetecting.value) {-->
<!--    stopDetection()-->
<!--  } else {-->
<!--    await startDetection()-->
<!--  }-->
<!--}-->

<!--const startDetection = async () => {-->
<!--  try {-->
<!--    // 获取摄像头权限-->
<!--    stream = await navigator.mediaDevices.getUserMedia({-->
<!--      video: { width: 640, height: 480, frameRate: { ideal: 10, max: 15 } },-->
<!--      audio: false-->
<!--    })-->
<!--    videoRef.value.srcObject = stream-->

<!--    // 建立WebSocket连接-->
<!--    ws = new DetectionWebSocket(-->
<!--        (data) => handleWsMessage(data),-->
<!--        (error) => {-->
<!--          console.error('WebSocket错误:', error)-->
<!--          wsStatus.value = 'error'-->
<!--        }-->
<!--    )-->
<!--    ws.connect()-->

<!--    isDetecting.value = true-->
<!--    addLog('开始实时监测', 'success')-->

<!--    // 开始捕获帧（每100ms一帧，即10fps）-->
<!--    captureInterval = setInterval(captureFrame, 100)-->

<!--  } catch (error) {-->
<!--    ElMessage.error('启动失败：' + error.message)-->
<!--    addLog('启动失败: ' + error.message, 'error')-->
<!--  }-->
<!--}-->

<!--const stopDetection = () => {-->
<!--  isDetecting.value = false-->

<!--  if (captureInterval) {-->
<!--    clearInterval(captureInterval)-->
<!--    captureInterval = null-->
<!--  }-->

<!--  if (ws) {-->
<!--    ws.disconnect()-->
<!--    ws = null-->
<!--  }-->

<!--  if (stream) {-->
<!--    stream.getTracks().forEach(track => track.stop())-->
<!--    stream = null-->
<!--  }-->

<!--  wsStatus.value = 'disconnected'-->
<!--  addLog('停止监测', 'info')-->
<!--}-->

<!--// const captureFrame = () => {-->
<!--//   if (!isDetecting.value || !videoRef.value || !canvasRef.value) return-->
<!--//-->
<!--//   const video = videoRef.value-->
<!--//   const canvas = canvasRef.value-->
<!--//   const ctx = canvas.getContext('2d')-->
<!--//-->
<!--//   // 设置canvas尺寸为模型输入尺寸-->
<!--//   canvas.width = 224-->
<!--//   canvas.height = 224-->
<!--//-->
<!--//   // 绘制视频帧（居中裁剪）-->
<!--//   const scale = Math.max(canvas.width / video.videoWidth, canvas.height / video.videoHeight)-->
<!--//   const x = (canvas.width - video.videoWidth * scale) / 2-->
<!--//   const y = (canvas.height - video.videoHeight * scale) / 2-->
<!--//-->
<!--//   ctx.drawImage(video, x, y, video.videoWidth * scale, video.videoHeight * scale)-->
<!--//-->
<!--//   // 转为Base64并发送-->
<!--//   const base64 = canvas.toDataURL('image/jpeg', 0.8).split(',')[1]-->
<!--//   if (ws) ws.sendFrame(base64)-->
<!--// }-->
<!--const captureFrame = () => {-->
<!--  if (!isDetecting.value || !videoRef.value || !canvasRef.value) return-->

<!--  const video = videoRef.value-->
<!--  const canvas = canvasRef.value-->
<!--  const ctx = canvas.getContext('2d')-->

<!--  // 设置canvas尺寸为模型输入尺寸-->
<!--  canvas.width = 224-->
<!--  canvas.height = 224-->

<!--  // 绘制视频帧（居中裁剪）-->
<!--  const scale = Math.max(canvas.width / video.videoWidth, canvas.height / video.videoHeight)-->
<!--  const x = (canvas.width - video.videoWidth * scale) / 2-->
<!--  const y = (canvas.height - video.videoHeight * scale) / 2-->

<!--  ctx.drawImage(video, x, y, video.videoWidth * scale, video.videoHeight * scale)-->

<!--  // 转为Base64（去掉 data:image/jpeg;base64, 前缀）-->
<!--  const base64 = canvas.toDataURL('image/jpeg', 0.8).split(',')[1]-->

<!--  // 修改：发送 JSON 对象，包含 imageBase64 字段-->
<!--  const frameData = {-->
<!--    imageBase64: base64,-->
<!--    frameIndex: stats.value.totalFrames  // 添加帧序号-->
<!--  }-->

<!--  //if (ws) ws.sendFrame(JSON.stringify(frameData))  // 发送 JSON 字符串-->
<!--  ws.sendFrame({-->
<!--    imageBase64: base64,-->
<!--    frameIndex: stats.value.totalFrames-->
<!--  })-->
<!--}-->
<!--const handleWsMessage = (data) => {-->
<!--  switch (data.type) {-->
<!--    case 'connected':-->
<!--      wsStatus.value = 'connected'-->
<!--      addLog('WebSocket已连接', 'success')-->
<!--      break-->
<!--    case 'detection_result':-->
<!--      currentResult.value = data.result-->
<!--      stats.value = data.sessionStats || { totalFrames: 0, drowsyFrames: 0 }-->

<!--      // 检测持续疲劳-->
<!--      if (stats.value.drowsyFrames > 10 &&-->
<!--          stats.value.drowsyFrames > stats.value.totalFrames * 0.5) {-->
<!--        showWarning.value = true-->
<!--      } else {-->
<!--        showWarning.value = false-->
<!--      }-->
<!--      break-->
<!--    case 'warning':-->
<!--      addLog(data.message, 'warning')-->
<!--      showWarning.value = true-->
<!--      break-->
<!--    case 'error':-->
<!--      addLog(data.message, 'error')-->
<!--      break-->
<!--  }-->
<!--}-->

<!--onUnmounted(() => {-->
<!--  stopDetection()-->
<!--})-->
<!--</script>-->

<!--<style scoped>-->
<!--.realtime-container {-->
<!--  max-width: 1400px;-->
<!--  margin: 0 auto;-->
<!--}-->

<!--.card-header {-->
<!--  display: flex;-->
<!--  justify-content: space-between;-->
<!--  align-items: center;-->
<!--  font-weight: bold;-->
<!--}-->

<!--.video-container {-->
<!--  position: relative;-->
<!--  background: #000;-->
<!--  border-radius: 8px;-->
<!--  overflow: hidden;-->
<!--  aspect-ratio: 4/3;-->
<!--}-->

<!--.video-element {-->
<!--  width: 100%;-->
<!--  height: 100%;-->
<!--  object-fit: cover;-->
<!--}-->

<!--.overlay-info {-->
<!--  position: absolute;-->
<!--  top: 20px;-->
<!--  left: 20px;-->
<!--  padding: 15px 20px;-->
<!--  border-radius: 8px;-->
<!--  color: white;-->
<!--  font-weight: bold;-->
<!--  backdrop-filter: blur(10px);-->
<!--}-->

<!--.overlay-info.alert {-->
<!--  background: rgba(103, 194, 58, 0.8);-->
<!--}-->

<!--.overlay-info.drowsy {-->
<!--  background: rgba(245, 108, 108, 0.9);-->
<!--  animation: pulse 2s infinite;-->
<!--}-->

<!--@keyframes pulse {-->
<!--  0%, 100% { opacity: 1; }-->
<!--  50% { opacity: 0.7; }-->
<!--}-->

<!--.status-badge {-->
<!--  font-size: 18px;-->
<!--  margin-bottom: 5px;-->
<!--}-->

<!--.ws-status {-->
<!--  margin-top: 10px;-->
<!--  padding: 5px 10px;-->
<!--  border-radius: 4px;-->
<!--  font-size: 12px;-->
<!--  display: inline-block;-->
<!--}-->

<!--.ws-status.connected {-->
<!--  background: #f0f9ff;-->
<!--  color: #409eff;-->
<!--}-->

<!--.ws-status.disconnected {-->
<!--  background: #f4f4f5;-->
<!--  color: #909399;-->
<!--}-->

<!--.ws-status.error {-->
<!--  background: #fef0f0;-->
<!--  color: #f56c6c;-->
<!--}-->

<!--.stats-panel {-->
<!--  display: grid;-->
<!--  grid-template-columns: repeat(3, 1fr);-->
<!--  gap: 15px;-->
<!--  text-align: center;-->
<!--}-->

<!--.stat-item {-->
<!--  padding: 15px;-->
<!--  background: #f5f7fa;-->
<!--  border-radius: 8px;-->
<!--}-->

<!--.stat-value {-->
<!--  font-size: 28px;-->
<!--  font-weight: bold;-->
<!--  color: #409eff;-->
<!--  margin-bottom: 5px;-->
<!--}-->

<!--.stat-value.danger {-->
<!--  color: #f56c6c;-->
<!--}-->

<!--.stat-label {-->
<!--  font-size: 14px;-->
<!--  color: #909399;-->
<!--}-->

<!--.log-panel {-->
<!--  height: 200px;-->
<!--  overflow-y: auto;-->
<!--  background: #f5f7fa;-->
<!--  padding: 10px;-->
<!--  border-radius: 4px;-->
<!--  font-family: monospace;-->
<!--  font-size: 12px;-->
<!--}-->

<!--.log-item {-->
<!--  margin-bottom: 5px;-->
<!--  padding: 3px 0;-->
<!--  border-bottom: 1px solid #ebeef5;-->
<!--}-->

<!--.log-item.error {-->
<!--  color: #f56c6c;-->
<!--}-->

<!--.log-item.success {-->
<!--  color: #67c23a;-->
<!--}-->

<!--.log-item.warning {-->
<!--  color: #e6a23c;-->
<!--}-->
<!--</style>-->
<template>
  <div class="realtime-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="main-title">

        </h1>
        <p class="subtitle">摄像头实时监测驾驶员状态，智能预警</p>
      </div>
      <div class="header-actions">
        <el-tag
            :type="isDetecting ? 'success' : 'info'"
            effect="dark"
            size="large"
            class="status-tag"
        >
          <span class="status-dot" :class="{ active: isDetecting }"></span>
          {{ isDetecting ? '监测中' : '已停止' }}
        </el-tag>
        <el-button
            :type="isDetecting ? 'danger' : 'primary'"
            size="large"
            @click="toggleDetection"
            class="control-btn"
            :class="{ stop: isDetecting }"
        >
          <el-icon v-if="!isDetecting"><VideoPlay /></el-icon>
          <el-icon v-else><VideoPause /></el-icon>
          {{ isDetecting ? '停止监测' : '开始监测' }}
        </el-button>
      </div>
    </div>

    <el-row :gutter="24">
      <!-- 左侧：视频区域 -->
      <el-col :xs="24" :lg="16">
        <div class="video-panel glass-effect">
          <div class="video-container">
            <video
                ref="videoRef"
                autoplay
                playsinline
                muted
                class="video-element"
            ></video>

            <!-- 检测状态覆盖层 -->
            <div v-if="currentResult" class="detection-overlay" :class="currentResult.resultClass">
              <div class="status-badge-large">
                <div class="status-icon-wrapper">
                  <el-icon v-if="currentResult.resultClass === 'drowsy'" class="warning-icon"><WarningFilled /></el-icon>
                  <el-icon v-else class="success-icon"><CircleCheckFilled /></el-icon>
                </div>
                <div class="status-text">
                  <span class="status-label">{{ currentResult.resultClass === 'drowsy' ? '疲劳 detected' : '状态正常' }}</span>
                  <span class="confidence">置信度 {{ (currentResult.confidence * 100).toFixed(1) }}%</span>
                </div>
              </div>

              <!-- 新增：详细指标 -->
              <div class="detail-metrics" v-if="currentResult.perclos !== undefined">
                <div class="metric">
                  <span class="metric-label">PERCLOS</span>
                  <span class="metric-value">{{ currentResult.perclos?.toFixed(3) }}</span>
                </div>
                <div class="metric">
                  <span class="metric-label">眨眼频率</span>
                  <span class="metric-value">{{ currentResult.blinkFreq?.toFixed(3) || '-' }}</span>
                </div>
                <div class="metric">
                  <span class="metric-label">哈欠频率</span>
                  <span class="metric-value">{{ currentResult.yawnFreq?.toFixed(4) || '-' }}</span>
                </div>
              </div>
            </div>


            <!-- 启动提示 -->
            <div v-if="!isDetecting" class="start-hint">
              <div class="hint-content">
                <el-icon class="hint-icon"><VideoCamera /></el-icon>
                <p>点击右上角"开始监测"启动实时检测</p>
              </div>
            </div>

            <!-- WebSocket状态指示器 -->
            <div class="connection-status" :class="wsStatus">
              <el-icon v-if="wsStatus === 'connected'"><CircleCheck /></el-icon>
              <el-icon v-else-if="wsStatus === 'error'"><CircleClose /></el-icon>
              <el-icon v-else><Loading /></el-icon>
              <span>{{ wsStatusText }}</span>
            </div>

            <canvas ref="canvasRef" style="display: none;"></canvas>
          </div>

          <!-- 实时数据条 -->
          <div class="video-stats-bar" v-if="isDetecting">
            <div class="stat-item">
              <el-icon><Odometer /></el-icon>
              <span class="stat-label">帧率</span>
              <span class="stat-value">{{ fps }} FPS</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <el-icon><Timer /></el-icon>
              <span class="stat-label">延迟</span>
              <span class="stat-value">{{ latency }}ms</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <el-icon><DataLine /></el-icon>
              <span class="stat-label">总帧数</span>
              <span class="stat-value">{{ stats.totalFrames }}</span>
            </div>
          </div>
        </div>

        <!-- 疲劳警告 -->
        <transition name="warning-slide">
          <div v-if="showWarning" class="warning-panel glass-effect">
            <div class="warning-content">
              <div class="warning-icon-wrapper">
                <el-icon><WarningFilled /></el-icon>
                <div class="warning-pulse"></div>
              </div>
              <div class="warning-text">
                <h3>⚠️ 持续检测到疲劳状态！</h3>
                <p>请立即停车休息，避免发生危险！已连续检测到 {{ stats.drowsyFrames }} 帧疲劳状态</p>
              </div>
              <el-button type="danger" @click="showWarning = false" plain>
                我知道了
              </el-button>
            </div>
          </div>
        </transition>
      </el-col>

      <!-- 右侧：统计与日志 -->
      <el-col :xs="24" :lg="8">
        <!-- 实时统计 -->
        <div class="stats-panel glass-effect">
          <div class="panel-header">
            <h3 class="panel-title">
              <el-icon><TrendCharts /></el-icon>
              实时统计
            </h3>
            <el-button text type="primary" @click="resetStats" :disabled="!isDetecting">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </div>

          <div class="stats-grid">
            <div class="stat-card-large">
              <div class="stat-icon-bg blue">
                <el-icon><VideoCamera /></el-icon>
              </div>
              <div class="stat-info">
                <span class="stat-value">{{ stats.totalFrames }}</span>
                <span class="stat-label">总帧数</span>
              </div>
            </div>

            <div class="stat-card-large">
              <div class="stat-icon-bg" :class="stats.drowsyFrames > 0 ? 'red' : 'green'">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stat-info">
                <span class="stat-value" :class="{ danger: stats.drowsyFrames > 0 }">
                  {{ stats.drowsyFrames }}
                </span>
                <span class="stat-label">疲劳帧数</span>
              </div>
            </div>

            <div class="stat-card-large full-width">
              <div class="stat-icon-bg purple">
                <el-icon><PieChart /></el-icon>
              </div>
              <div class="stat-info">
                <span class="stat-value">{{ drowsyRate }}</span>
                <span class="stat-label">疲劳占比</span>
              </div>
              <div class="rate-bar-container">
                <div class="rate-bar" :style="{ width: drowsyRate }"></div>
              </div>
            </div>
          </div>

          <!-- 状态分布图 -->
          <div class="mini-chart-section">
            <h4 class="mini-chart-title">状态分布</h4>
            <div ref="miniChartRef" class="mini-chart"></div>
          </div>
        </div>

        <!-- 系统日志 -->
        <div class="log-panel glass-effect">
          <div class="panel-header">
            <h3 class="panel-title">
              <el-icon><List /></el-icon>
              系统日志
            </h3>
            <el-tag size="small" type="info">{{ logs.length }} 条</el-tag>
          </div>

          <div class="log-container" ref="logContainerRef">
            <transition-group name="log-slide">
              <div
                  v-for="(log, index) in logs"
                  :key="index"
                  class="log-item"
                  :class="log.type"
              >
                <div class="log-dot"></div>
                <div class="log-content">
                  <span class="log-time">{{ log.time }}</span>
                  <span class="log-message">{{ log.message }}</span>
                </div>
              </div>
            </transition-group>
            <div v-if="logs.length === 0" class="empty-logs">
              <el-icon><InfoFilled /></el-icon>
              <span>暂无日志记录</span>
            </div>
          </div>

          <div class="log-actions">
            <el-button link type="primary" @click="clearLogs" :disabled="logs.length === 0">
              <el-icon><Delete /></el-icon>
              清空日志
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { DetectionWebSocket } from '@/api'
import * as echarts from 'echarts'
import {
  VideoCamera, VideoPlay, VideoPause, WarningFilled, CircleCheckFilled,
  CircleCheck, CircleClose, Loading, Odometer, Timer, DataLine,
  TrendCharts, Refresh, Warning, PieChart, List, InfoFilled,
  Delete
} from '@element-plus/icons-vue'

const videoRef = ref(null)
const canvasRef = ref(null)
const miniChartRef = ref(null)
const logContainerRef = ref(null)

const isDetecting = ref(false)
const wsStatus = ref('disconnected')
const currentResult = ref(null)
const stats = ref({ totalFrames: 0, drowsyFrames: 0 })
const logs = ref([])
const showWarning = ref(false)
const fps = ref(0)
const latency = ref(0)

let ws = null
let stream = null
let captureInterval = null
let miniChart = null
let frameCount = 0
let lastTime = Date.now()

const drowsyRate = computed(() => {
  if (stats.value.totalFrames === 0) return '0%'
  return ((stats.value.drowsyFrames / stats.value.totalFrames) * 100).toFixed(1) + '%'
})

const wsStatusText = computed(() => {
  const map = {
    connected: '已连接',
    disconnected: '未连接',
    error: '连接错误'
  }
  return map[wsStatus.value] || '未知'
})

const addLog = (message, type = 'info') => {
  const time = new Date().toLocaleTimeString('zh-CN', { hour12: false })
  logs.value.unshift({ time, message, type })
  if (logs.value.length > 50) logs.value.pop()

  // 自动滚动到顶部
  nextTick(() => {
    if (logContainerRef.value) {
      logContainerRef.value.scrollTop = 0
    }
  })
}

const toggleDetection = async () => {
  if (isDetecting.value) {
    stopDetection()
  } else {
    await startDetection()
  }
}

const startDetection = async () => {
  try {
    stream = await navigator.mediaDevices.getUserMedia({
      video: { width: 640, height: 480, frameRate: { ideal: 10, max: 15 } },
      audio: false
    })
    videoRef.value.srcObject = stream

    ws = new DetectionWebSocket(
        (data) => handleWsMessage(data),
        (error) => {
          console.error('WebSocket错误:', error)
          wsStatus.value = 'error'
          addLog('WebSocket连接错误', 'error')
        }
    )
    ws.connect()

    isDetecting.value = true
    frameCount = 0
    lastTime = Date.now()
    addLog('开始实时监测', 'success')

    captureInterval = setInterval(captureFrame, 100)

  } catch (error) {
    ElMessage.error('启动失败：' + error.message)
    addLog('启动失败: ' + error.message, 'error')
  }
}

const stopDetection = () => {
  isDetecting.value = false

  if (captureInterval) {
    clearInterval(captureInterval)
    captureInterval = null
  }

  if (ws) {
    ws.disconnect()
    ws = null
  }

  if (stream) {
    stream.getTracks().forEach(track => track.stop())
    stream = null
  }

  wsStatus.value = 'disconnected'
  currentResult.value = null
  showWarning.value = false
  fps.value = 0
  latency.value = 0
  addLog('停止监测', 'info')
}

const captureFrame = () => {
  if (!isDetecting.value || !videoRef.value || !canvasRef.value) return

  const video = videoRef.value
  const canvas = canvasRef.value
  const ctx = canvas.getContext('2d')

  canvas.width = 169
  canvas.height = 160

  const scale = Math.max(canvas.width / video.videoWidth, canvas.height / video.videoHeight)
  const x = (canvas.width - video.videoWidth * scale) / 2
  const y = (canvas.height - video.videoHeight * scale) / 2

  ctx.drawImage(video, x, y, video.videoWidth * scale, video.videoHeight * scale)

  const base64 = canvas.toDataURL('image/jpeg', 0.8).split(',')[1]

  const startTime = Date.now()
  ws.sendFrame({
    imageBase64: base64,
    frameIndex: stats.value.totalFrames
  })

  // 计算FPS
  frameCount++
  const now = Date.now()
  if (now - lastTime >= 1000) {
    fps.value = frameCount
    frameCount = 0
    lastTime = now
  }

  // 估算延迟（这里简化处理）
  latency.value = Date.now() - startTime + 50
}

const handleWsMessage = (data) => {
  switch (data.type) {
    case 'connected':
      wsStatus.value = 'connected'
      addLog('WebSocket已连接', 'success')
      break
    case 'detection_result':
      currentResult.value = data.result
      stats.value = data.sessionStats || { totalFrames: 0, drowsyFrames: 0 }

      // 更新迷你图表
      updateMiniChart()

      // 检测持续疲劳
      if (stats.value.drowsyFrames > 10 &&
          stats.value.drowsyFrames > stats.value.totalFrames * 0.5) {
        showWarning.value = true
      } else {
        showWarning.value = false
      }
      break
    case 'warning':
      addLog(data.message, 'warning')
      showWarning.value = true
      break
    case 'error':
      addLog(data.message, 'error')
      break
  }
}

const resetStats = () => {
  stats.value = { totalFrames: 0, drowsyFrames: 0 }
  addLog('统计数据已重置', 'info')
}

const clearLogs = () => {
  logs.value = []
}

const updateMiniChart = () => {
  if (!miniChart) return

  const alertCount = stats.value.totalFrames - stats.value.drowsyFrames
  const drowsyCount = stats.value.drowsyFrames

  const option = {
    backgroundColor: 'transparent',
    series: [
      {
        type: 'pie',
        radius: ['50%', '70%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 4,
          borderColor: 'rgba(15, 23, 42, 0.8)',
          borderWidth: 2
        },
        label: { show: false },
        data: [
          { value: alertCount, name: '清醒', itemStyle: { color: '#10b981' } },
          { value: drowsyCount, name: '疲劳', itemStyle: { color: '#ef4444' } }
        ]
      }
    ]
  }

  miniChart.setOption(option)
}

// 初始化迷你图表
nextTick(() => {
  if (miniChartRef.value) {
    miniChart = echarts.init(miniChartRef.value)
    updateMiniChart()
  }
})

watch(() => stats.value, () => {
  updateMiniChart()
}, { deep: true })

onUnmounted(() => {
  stopDetection()
  if (miniChart) {
    miniChart.dispose()
  }
})
</script>

<style scoped>
.realtime-page {
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
  align-items: center;
  gap: 16px;
}

.status-tag {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(94, 234, 212, 0.2);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #64748b;
  transition: all 0.3s ease;
}

.status-dot.active {
  background: #10b981;
  box-shadow: 0 0 8px #10b981;
  animation: blink 1.5s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.control-btn {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  padding: 0 24px;
  height: 44px;
}

.control-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(16, 185, 129, 0.4);
}

.control-btn.stop {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

.control-btn.stop:hover {
  box-shadow: 0 8px 20px rgba(239, 68, 68, 0.4);
}

/* 玻璃效果 */
.glass-effect {
  background: rgba(30, 41, 59, 0.5);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(94, 234, 212, 0.1);
  border-radius: 12px;
}

/* 视频面板 */
.video-panel {
  padding: 20px;
  margin-bottom: 20px;
}

.video-container {
  position: relative;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  border-radius: 12px;
  overflow: hidden;
  aspect-ratio: 4/3;
  border: 1px solid rgba(94, 234, 212, 0.2);
}

.video-element {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform: scaleX(-1); /* 镜像翻转 */
}

/* 检测覆盖层 */
.detection-overlay {
  position: absolute;
  top: 20px;
  left: 20px;
  padding: 16px 20px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
  border: 1px solid;
  animation: slide-in 0.3s ease;
}

@keyframes slide-in {
  from { transform: translateX(-20px); opacity: 0; }
  to { transform: translateX(0); opacity: 1; }
}

.detection-overlay.alert {
  background: rgba(16, 185, 129, 0.2);
  border-color: rgba(16, 185, 129, 0.4);
}

.detection-overlay.drowsy {
  background: rgba(239, 68, 68, 0.2);
  border-color: rgba(239, 68, 68, 0.4);
  animation: pulse-danger 2s infinite;
}

@keyframes pulse-danger {
  0%, 100% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.4); }
  50% { box-shadow: 0 0 20px 10px rgba(239, 68, 68, 0.2); }
}

.status-badge-large {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-icon-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.detection-overlay.alert .status-icon-wrapper {
  background: rgba(16, 185, 129, 0.3);
}

.detection-overlay.drowsy .status-icon-wrapper {
  background: rgba(239, 68, 68, 0.3);
}

.success-icon {
  color: #34d399;
}

.warning-icon {
  color: #f87171;
  animation: shake 0.5s ease-in-out infinite;
}

@keyframes shake {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(-10deg); }
  75% { transform: rotate(10deg); }
}

.status-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.status-label {
  font-size: 14px;
  font-weight: 600;
  color: #f8fafc;
}

.confidence {
  font-size: 12px;
  color: #94a3b8;
}

/* 启动提示 */
.start-hint {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.9);
}

.hint-content {
  text-align: center;
  color: #64748b;
}

.hint-icon {
  font-size: 48px;
  margin-bottom: 12px;
  color: #5eead4;
}

.hint-content p {
  margin: 0;
  font-size: 14px;
}

/* 连接状态 */
.connection-status {
  position: absolute;
  bottom: 16px;
  left: 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 12px;
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid;
}

.connection-status.connected {
  color: #34d399;
  border-color: rgba(16, 185, 129, 0.4);
}

.connection-status.disconnected {
  color: #94a3b8;
  border-color: rgba(148, 163, 184, 0.3);
}

.connection-status.error {
  color: #f87171;
  border-color: rgba(239, 68, 68, 0.4);
}

.connection-status .el-icon {
  font-size: 14px;
}

/* 视频统计条 */
.video-stats-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 16px;
  padding: 12px 16px;
  background: rgba(15, 23, 42, 0.5);
  border-radius: 8px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.stat-item .el-icon {
  color: #5eead4;
  font-size: 16px;
}

.stat-label {
  color: #64748b;
}

.stat-value {
  color: #f8fafc;
  font-weight: 600;
}

.stat-divider {
  width: 1px;
  height: 20px;
  background: rgba(148, 163, 184, 0.2);
}

/* 警告面板 */
.warning-panel {
  padding: 16px;
  border: 1px solid rgba(239, 68, 68, 0.3);
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.1) 0%, rgba(239, 68, 68, 0.05) 100%);
}

.warning-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.warning-icon-wrapper {
  position: relative;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.warning-icon-wrapper .el-icon {
  font-size: 32px;
  color: #f87171;
  z-index: 1;
}

.warning-pulse {
  position: absolute;
  inset: 0;
  border: 2px solid rgba(239, 68, 68, 0.4);
  border-radius: 50%;
  animation: warning-pulse 2s infinite;
}

@keyframes warning-pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0; }
}

.warning-text {
  flex: 1;
}

.warning-text h3 {
  font-size: 16px;
  color: #f87171;
  margin: 0 0 4px 0;
}

.warning-text p {
  font-size: 13px;
  color: #94a3b8;
  margin: 0;
}

/* 警告动画 */
.warning-slide-enter-active,
.warning-slide-leave-active {
  transition: all 0.3s ease;
}

.warning-slide-enter-from {
  opacity: 0;
  transform: translateY(-20px);
}

.warning-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* 面板头部 */
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #f8fafc;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 统计面板 */
.stats-panel {
  padding: 20px;
  margin-bottom: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card-large {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: rgba(15, 23, 42, 0.5);
  border-radius: 10px;
  border: 1px solid rgba(94, 234, 212, 0.1);
}

.stat-card-large.full-width {
  grid-column: 1 / -1;
  flex-wrap: wrap;
}

.stat-icon-bg {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 22px;
}

.stat-icon-bg.blue {
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
}

.stat-icon-bg.green {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.stat-icon-bg.red {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

.stat-icon-bg.purple {
  background: linear-gradient(135deg, #8b5cf6 0%, #6d28d9 100%);
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-card-large.full-width .stat-info {
  flex: 1;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #f8fafc;
}

.stat-value.danger {
  color: #f87171;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
}

.rate-bar-container {
  width: 100%;
  height: 6px;
  background: rgba(148, 163, 184, 0.2);
  border-radius: 3px;
  overflow: hidden;
  margin-top: 8px;
}

.rate-bar {
  height: 100%;
  background: linear-gradient(90deg, #ef4444, #f87171);
  border-radius: 3px;
  transition: width 0.3s ease;
}

/* 迷你图表 */
.mini-chart-section {
  padding-top: 16px;
  border-top: 1px solid rgba(148, 163, 184, 0.1);
}

.mini-chart-title {
  font-size: 13px;
  color: #64748b;
  margin: 0 0 12px 0;
  font-weight: 500;
}

.mini-chart {
  height: 120px;
}

/* 日志面板 */
.log-panel {
  padding: 20px;
  display: flex;
  flex-direction: column;
  max-height: 400px;
}

.log-container {
  flex: 1;
  overflow-y: auto;
  margin: 0 -20px;
  padding: 0 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.log-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid rgba(148, 163, 184, 0.05);
  animation: log-in 0.3s ease;
}

@keyframes log-in {
  from { opacity: 0; transform: translateX(-10px); }
  to { opacity: 1; transform: translateX(0); }
}

.log-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
}

.log-item.info .log-dot {
  background: #0ea5e9;
}

.log-item.success .log-dot {
  background: #10b981;
}

.log-item.warning .log-dot {
  background: #f59e0b;
}

.log-item.error .log-dot {
  background: #ef4444;
}

.log-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
}

.log-time {
  font-size: 11px;
  color: #64748b;
  font-family: monospace;
}

.log-message {
  font-size: 13px;
  color: #e2e8f0;
  margin: 0;
}

.empty-logs {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px 0;
  color: #64748b;
  font-size: 13px;
}

.log-actions {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(148, 163, 184, 0.1);
  text-align: center;
}

/* 日志动画 */
.log-slide-enter-active,
.log-slide-leave-active {
  transition: all 0.3s ease;
}

.log-slide-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.log-slide-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

/* 响应式 */
@media (max-width: 768px) {
  .realtime-page {
    padding: 12px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .main-title {
    font-size: 20px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .stat-card-large.full-width {
    grid-column: 1;
  }

  .warning-content {
    flex-direction: column;
    text-align: center;
  }
}
</style>