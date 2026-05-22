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
                  <el-icon v-else-if="currentResult.resultClass === 'unknown'" class="unknown-icon"><QuestionFilled /></el-icon>
                  <el-icon v-else class="success-icon"><CircleCheckFilled /></el-icon>
                </div>
                <div class="status-text">
                  <span class="status-label">
                    {{ currentResult.resultClass === 'drowsy' ? '疲劳' :
                       currentResult.resultClass === 'unknown' ? '未检测到人脸' : '状态正常' }}
                  </span>
                  <span class="confidence" v-if="currentResult.resultClass !== 'unknown'">
                    置信度 {{ ((currentResult.confidence || 0) * 100).toFixed(1) }}%
                  </span>
                  <span class="reason-text" v-if="currentResult.reason">
                    原因: {{ currentResult.reason }}
                  </span>
                </div>
              </div>

              <!-- 详细生理指标 -->
              <div class="detail-metrics" v-if="currentResult.resultClass !== 'unknown'">
                <div class="metric" :class="{ danger: currentResult.perclos > 0.3 }">
                  <span class="metric-label">PERCLOS</span>
                  <span class="metric-value">{{ (currentResult.perclos || 0).toFixed(3) }}</span>
                </div>
                <div class="metric">
                  <span class="metric-label">眨眼频率</span>
                  <span class="metric-value">{{ (currentResult.blinkFreq || 0).toFixed(3) }}</span>
                </div>
                <div class="metric">
                  <span class="metric-label">哈欠频率</span>
                  <span class="metric-value">{{ (currentResult.yawnFreq || 0).toFixed(4) }}</span>
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
            <canvas ref="overlayCanvasRef" class="overlay-canvas"></canvas>
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
  Delete, QuestionFilled
} from '@element-plus/icons-vue'

const videoRef = ref(null)
const canvasRef = ref(null)
const overlayCanvasRef = ref(null)
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

const onVideoLoaded = () => {
  videoLoaded.value = true
  if (videoRef.value) {
    videoDuration.value = videoRef.value.duration
    // 【新增】同步 overlay canvas 尺寸
    const rect = videoRef.value.getBoundingClientRect()
    if (overlayCanvasRef.value) {
      overlayCanvasRef.value.width = rect.width
      overlayCanvasRef.value.height = rect.height
    }
  }
}

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
let reconnectTimer = null
let isManualStop = false   // 【新增】区分手动停止还是异常断开

const startDetection = async () => {
  isManualStop = false
  try {
    stream = await navigator.mediaDevices.getUserMedia({
      video: { width: 640, height: 480, frameRate: { ideal: 10, max: 15 } },
      audio: false
    })
    videoRef.value.srcObject = stream

    connectWs()  // 【抽出来】

    isDetecting.value = true
    frameCount = 0
    lastTime = Date.now()
    addLog('开始实时监测', 'success')

    captureInterval = setInterval(captureFrame, 150)  // ~7fps，匹配AI处理能力

  } catch (error) {
    ElMessage.error('启动失败：' + error.message)
    addLog('启动失败: ' + error.message, 'error')
  }
}

// 【新增】抽离连接逻辑，支持重连
const connectWs = () => {
  if (ws) {
    try { ws.disconnect() } catch (e) {}
  }

  ws = new DetectionWebSocket(
      (data) => handleWsMessage(data),
      (error) => {
        console.error('WebSocket错误:', error)
        wsStatus.value = 'error'
        addLog('WebSocket连接错误，3秒后尝试重连...', 'error')
        // 自动重连（只要不是手动停止）
        if (!isManualStop && isDetecting.value) {
          clearTimeout(reconnectTimer)
          reconnectTimer = setTimeout(() => {
            if (isDetecting.value) connectWs()
          }, 3000)
        }
      }
  )
  ws.connect()
}

const stopDetection = () => {
  isManualStop = true   // 【新增】标记手动停止
  clearTimeout(reconnectTimer)

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

  // 【修复】清除 overlay canvas 上的检测框残留，避免停止后最后一帧的框仍显示在页面上
  const canvas = overlayCanvasRef.value
  if (canvas) {
    const ctx = canvas.getContext('2d')
    ctx.clearRect(0, 0, canvas.width, canvas.height)
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

  canvas.width = 480
  canvas.height = 360

  // contain 缩放：完整保留画面，不裁切，留黑边填充
  const scale = Math.min(canvas.width / video.videoWidth, canvas.height / video.videoHeight)
  const x = (canvas.width - video.videoWidth * scale) / 2
  const y = (canvas.height - video.videoHeight * scale) / 2

  // 黑底
  ctx.fillStyle = '#000000'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  ctx.drawImage(video, x, y, video.videoWidth * scale, video.videoHeight * scale)

  const base64 = canvas.toDataURL('image/jpeg', 0.92).split(',')[1]

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
      // 归一化类名（后端去重已防抖，无需额外滑动窗口）
      const cls = (data.result.class || '').toLowerCase()
      const mapped = {
        resultClass: cls === 'awake' ? 'alert' : (cls === 'fatigue' ? 'drowsy' : cls),
        confidence: data.result.confidence || 0,
        isDrowsy: data.result.isDrowsy || false,
        perclos: data.result.perclos,
        blinkFreq: data.result.blinkFreq,
        yawnFreq: data.result.yawnFreq,
        fatigueStatus: data.result.fatigueStatus,
        reason: data.result.reason,
        detections: data.result.detections || []
      }

      currentResult.value = mapped
      stats.value = data.sessionStats || { totalFrames: 0, drowsyFrames: 0 }
      drawOverlay(mapped)

      updateMiniChart()

      if (stats.value.drowsyFrames > 10 &&
          stats.value.drowsyFrames > stats.value.totalFrames * 0.5) {
        showWarning.value = true
      } else {
        showWarning.value = false
      }
      break
    case 'detection_boxes':
      // 轻量消息：仅刷新检测框坐标，不更改疲劳状态判断
      if (currentResult.value && data.result) {
        currentResult.value.detections = data.result.detections || []
      }
      drawOverlay(data.result)
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

const drawOverlay = (result) => {
  const canvas = overlayCanvasRef.value
  const video = videoRef.value
  if (!canvas || !video || !result) return

  // 保证 canvas 像素尺寸和显示尺寸一致
  const rect = video.getBoundingClientRect()
  if (canvas.width !== rect.width || canvas.height !== rect.height) {
    canvas.width = rect.width
    canvas.height = rect.height
  }

  const ctx = canvas.getContext('2d')
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  // AI 端收到的图是 480×360，把检测框坐标映射到视频显示尺寸
  const aiWidth = 480
  const aiHeight = 360
  const scaleX = canvas.width / aiWidth
  const scaleY = canvas.height / aiHeight

  const detections = result.detections || []
  detections.forEach(det => {
    const [x1, y1, x2, y2] = det.bbox || [0, 0, 0, 0]
    const label = det.label || 'unknown'
    const score = det.score || 0

    const rx1 = x1 * scaleX
    const ry1 = y1 * scaleY
    const rw = (x2 - x1) * scaleX
    const rh = (y2 - y1) * scaleY

    // 颜色：闭眼/哈欠/低头 → 红色，其他 → 绿色
    const isFatigue = ['close_eyes', 'yawn', 'head_low'].some(k => label.toLowerCase().includes(k))
    ctx.strokeStyle = isFatigue ? '#ef4444' : '#10b981'
    ctx.lineWidth = 2

    // 画框
    ctx.strokeRect(rx1, ry1, rw, rh)

    // 文字标签
    const text = `${label} ${(score * 100).toFixed(0)}%`
    ctx.font = 'bold 12px sans-serif'
    const tw = ctx.measureText(text).width

    // 文字背景
    ctx.fillStyle = ctx.strokeStyle
    ctx.fillRect(rx1, Math.max(0, ry1 - 18), tw + 8, 18)

    // 文字
    ctx.fillStyle = '#ffffff'
    ctx.fillText(text, rx1 + 4, Math.max(14, ry1 - 4))
  })
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
  color: #334155;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  min-height: 100%;
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
  color: #1e293b;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  color: #0ea5e9;
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
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.2);
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
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(148, 163, 184, 0.15);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

/* 视频面板 */
.video-panel {
  padding: 20px;
  margin-bottom: 20px;
}

.video-container {
  position: relative;
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  border-radius: 12px;
  overflow: hidden;
  aspect-ratio: 4/3;
  border: 1px solid rgba(148, 163, 184, 0.2);
}

.video-element {
  width: 100%;
  height: 100%;
  object-fit: cover;
  /* transform: scaleX(-1);  ← 注释掉或删掉，否则画框坐标会左右反 */
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

.detection-overlay.unknown {
  background: rgba(245, 158, 11, 0.2);
  border-color: rgba(245, 158, 11, 0.4);
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

.unknown-icon {
  color: #f59e0b;
}

@keyframes shake {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(-10deg); }
  75% { transform: rotate(10deg); }
}

.reason-text {
  font-size: 11px;
  color: #94a3b8;
  display: block;
  margin-top: 2px;
}

.detection-overlay.unknown .status-icon-wrapper {
  background: rgba(245, 158, 11, 0.3);
}

/* 生理指标条 */
.detail-metrics {
  display: flex;
  gap: 16px;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid rgba(255,255,255,0.15);
}

.metric {
  text-align: center;
  min-width: 70px;
}

.metric-label {
  font-size: 10px;
  color: rgba(255,255,255,0.6);
  display: block;
  margin-bottom: 2px;
}

.metric-value {
  font-size: 16px;
  font-weight: 700;
  color: #f8fafc;
}

.metric.danger .metric-value {
  color: #f87171;
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
  color: rgba(255, 255, 255, 0.7);
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
  color: #94a3b8;
}

.hint-icon {
  font-size: 48px;
  margin-bottom: 12px;
  color: #0ea5e9;
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
  color: #94a3b8;
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
  background: rgba(248, 250, 252, 0.8);
  border-radius: 8px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.stat-item .el-icon {
  color: #0ea5e9;
  font-size: 16px;
}

.stat-item .stat-label {
  color: #64748b;
}

.stat-item .stat-value {
  color: #1e293b;
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
  color: #1e293b;
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
  background: rgba(248, 250, 252, 0.8);
  border-radius: 10px;
  border: 1px solid rgba(148, 163, 184, 0.1);
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
  color: #1e293b;
}

.stat-value.danger {
  color: #ef4444;
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
  color: #475569;
  margin: 0;
}

.empty-logs {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px 0;
  color: #94a3b8;
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
.overlay-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;  /* 让鼠标事件穿透到 video */
  z-index: 10;
}
</style>