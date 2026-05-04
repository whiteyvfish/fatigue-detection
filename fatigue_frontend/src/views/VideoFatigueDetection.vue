<!-- VideoFatigueDetection.vue -->
<template>
  <div class="fatigue-detection-page">
    <!-- 顶部标题 -->
    <div class="page-header">
      <h1>🚗 驾驶疲劳检测系统</h1>
      <p class="subtitle">上传驾驶视频，AI 自动分析疲劳状态</p>
    </div>

    <!-- 上传区域 -->
    <div class="upload-section" v-if="!currentTask">
      <div
          class="upload-zone"
          :class="{ 'drag-over': isDragOver, 'uploading': isUploading }"
          @dragenter.prevent="isDragOver = true"
          @dragleave.prevent="isDragOver = false"
          @dragover.prevent
          @drop.prevent="handleDrop"
          @click="triggerFileInput"
      >
        <input
            ref="fileInput"
            type="file"
            accept="video/mp4"
            style="display: none"
            @change="handleFileSelect"
        >

        <div v-if="!isUploading" class="upload-placeholder">
          <div class="upload-icon">📹</div>
          <p class="upload-text">点击或拖拽上传 MP4 视频</p>
          <p class="upload-hint">支持格式：MP4 | 建议时长：30秒-10分钟</p>
        </div>

        <div v-else class="upload-progress">
          <div class="progress-ring">
            <svg viewBox="0 0 100 100">
              <circle class="progress-bg" cx="50" cy="50" r="45"/>
              <circle
                  class="progress-bar"
                  cx="50"
                  cy="50"
                  r="45"
                  :stroke-dasharray="`${uploadProgress * 2.83} 283`"
              />
            </svg>
            <span class="progress-text">{{ uploadProgress }}%</span>
          </div>
          <p class="upload-status">正在上传视频...</p>
        </div>
      </div>
    </div>

    <!-- 分析中状态 -->
    <div class="processing-section" v-else-if="currentTask && currentTask.status === 'processing'">
      <div class="processing-card">
        <div class="processing-animation">
          <div class="spinner"></div>
          <div class="pulse-ring"></div>
        </div>
        <h2>AI 正在分析视频中...</h2>
        <p class="processing-info">{{ currentTask.resultSummary }}</p>

        <div class="processing-stats">
          <div class="stat-item">
            <span class="stat-label">任务ID</span>
            <span class="stat-value mono">{{ currentTask.videoId }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">已耗时</span>
            <span class="stat-value">{{ elapsedTime }}s</span>
          </div>
        </div>

        <button class="btn-secondary" @click="resetUpload">取消并重新上传</button>
      </div>
    </div>

    <!-- 分析完成结果 -->
    <div class="result-section" v-else-if="currentTask && currentTask.status === 'completed'">
      <!-- 状态大标签 -->
      <div class="status-banner" :class="isFatigue ? 'fatigue' : 'normal'">
        <div class="status-icon">{{ isFatigue ? '⚠️' : '✅' }}</div>
        <div class="status-text">
          <h2>{{ isFatigue ? '检测到疲劳状态' : '状态清醒' }}</h2>
          <p>{{ taskDetail?.resultSummary || currentTask.resultSummary }}</p>
        </div>
      </div>

      <!-- 视频播放区 -->
      <div class="video-section">
        <div class="video-container">
          <video
              ref="videoPlayer"
              controls
              :src="videoUrl"
              crossorigin="anonymous"
              @loadedmetadata="onVideoLoaded"
          ></video>
          <div v-if="!videoLoaded" class="video-loading">
            <div class="spinner small"></div>
            <span>加载视频中...</span>
          </div>
        </div>

        <!-- 时间轴标记 -->
        <div class="timeline-markers" v-if="fatigueSegments.length > 0">
          <p class="timeline-title">⚠️ 疲劳时间段标记</p>
          <div class="timeline-bar">
            <div
                v-for="(seg, idx) in fatigueSegments"
                :key="idx"
                class="timeline-segment"
                :style="{
                left: `${(seg.startSec / videoDuration) * 100}%`,
                width: `${((seg.endSec - seg.startSec) / videoDuration) * 100}%`
              }"
                @click="seekTo(seg.startSec)"
                :title="`疲劳时段: ${formatTime(seg.startSec)} - ${formatTime(seg.endSec)}`"
            ></div>
          </div>
          <div class="timeline-labels">
            <span>00:00</span>
            <span>{{ formatTime(videoDuration) }}</span>
          </div>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">🎞️</div>
          <div class="stat-number">{{ taskDetail?.sampledFrames || 0 }}</div>
          <div class="stat-label">采样帧数</div>
        </div>
        <div class="stat-card fatigue">
          <div class="stat-icon">😴</div>
          <div class="stat-number">{{ taskDetail?.fatigueFrames || 0 }}</div>
          <div class="stat-label">疲劳帧数</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">📊</div>
          <div class="stat-number">{{ fatigueRatio }}%</div>
          <div class="stat-label">疲劳占比</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">⏱️</div>
          <div class="stat-number">{{ formatTime(taskDetail?.durationSec || 0) }}</div>
          <div class="stat-label">视频时长</div>
        </div>
      </div>

      <!-- 疲劳时间段详情 -->
      <div class="segments-section" v-if="fatigueSegments.length > 0">
        <h3>⚠️ 疲劳时间段详情</h3>
        <div class="segments-list">
          <div
              v-for="(seg, idx) in fatigueSegments"
              :key="idx"
              class="segment-item"
              @click="seekTo(seg.startSec)"
          >
            <span class="segment-num">#{{ idx + 1 }}</span>
            <span class="segment-time">{{ formatTime(seg.startSec) }} - {{ formatTime(seg.endSec) }}</span>
            <span class="segment-duration">持续 {{ formatDuration(seg.endSec - seg.startSec) }}</span>
            <button class="btn-play">▶ 跳转播放</button>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-bar">
        <button class="btn-primary" @click="resetUpload">
          <span>📤</span> 上传新视频
        </button>
        <button class="btn-secondary" @click="downloadReport">
          <span>📥</span> 下载报告
        </button>
      </div>
    </div>

    <!-- 分析失败 -->
    <div class="error-section" v-else-if="currentTask && currentTask.status === 'failed'">
      <div class="error-card">
        <div class="error-icon">❌</div>
        <h2>分析失败</h2>
        <p class="error-message">{{ currentTask.resultSummary }}</p>
        <button class="btn-primary" @click="resetUpload">重新上传</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'VideoFatigueDetection',

  data() {
    return {
      // 上传状态
      isDragOver: false,
      isUploading: false,
      uploadProgress: 0,

      // 任务状态
      currentTask: null,
      taskDetail: null,
      pollTimer: null,
      elapsedTimer: null,
      elapsedTime: 0,

      // 视频播放
      videoUrl: '',
      videoLoaded: false,
      videoDuration: 0,

      // API 配置
      apiBase: '/api', // 根据你的 context-path 调整，如 /api 或 /
    }
  },

  computed: {
    // 是否疲劳（疲劳帧超过10%或后端明确标记）
    isFatigue() {
      if (!this.taskDetail) return false
      const ratio = this.fatigueRatio
      return ratio > 10 || (this.taskDetail.fatigueFrames > 0 && ratio > 5)
    },

    // 疲劳占比
    fatigueRatio() {
      if (!this.taskDetail) return 0
      const sampled = this.taskDetail.sampledFrames || 0
      const fatigue = this.taskDetail.fatigueFrames || 0
      if (sampled === 0) return 0
      return ((fatigue / sampled) * 100).toFixed(1)
    },

    // 解析疲劳时间段
    fatigueSegments() {
      if (!this.taskDetail || !this.taskDetail.fatigueTimestamps) return []
      try {
        const parsed = JSON.parse(this.taskDetail.fatigueTimestamps)
        return Array.isArray(parsed) ? parsed : []
      } catch (e) {
        return []
      }
    }
  },

  beforeDestroy() {
    this.clearTimers()
  },

  methods: {
    // ========== 上传相关 ==========
    triggerFileInput() {
      if (!this.isUploading) {
        this.$refs.fileInput.click()
      }
    },

    handleFileSelect(e) {
      const file = e.target.files[0]
      if (file) this.uploadFile(file)
    },

    handleDrop(e) {
      this.isDragOver = false
      const files = e.dataTransfer.files
      if (files.length > 0) {
        const file = files[0]
        if (file.type === 'video/mp4' || file.name.endsWith('.mp4')) {
          this.uploadFile(file)
        } else {
          alert('请上传 MP4 格式视频')
        }
      }
    },

    async uploadFile(file) {
      // 校验
      if (!file.name.endsWith('.mp4')) {
        alert('仅支持 MP4 格式')
        return
      }

      this.isUploading = true
      this.uploadProgress = 0

      const formData = new FormData()
      formData.append('file', file)
      formData.append('userId', 'test-user') // 可替换为实际用户ID

      try {
        // 模拟进度（因为 axios 上传进度有时不准确）
        const progressInterval = setInterval(() => {
          if (this.uploadProgress < 90) {
            this.uploadProgress += Math.random() * 15
            if (this.uploadProgress > 90) this.uploadProgress = 90
          }
        }, 300)

        const response = await fetch(`${this.apiBase}/video/upload`, {
          method: 'POST',
          body: formData
        })

        clearInterval(progressInterval)
        this.uploadProgress = 100

        const result = await response.json()

        if (result.code === 200 && result.data) {
          this.currentTask = result.data
          this.startPolling()
        } else {
          alert(result.message || '上传失败')
          this.resetUpload()
        }
      } catch (error) {
        console.error('上传错误:', error)
        alert('上传失败，请检查网络')
        this.resetUpload()
      }
    },

    // ========== 轮询相关 ==========
    startPolling() {
      this.elapsedTime = 0
      this.elapsedTimer = setInterval(() => {
        this.elapsedTime++
      }, 1000)

      this.pollTask()
      this.pollTimer = setInterval(() => {
        this.pollTask()
      }, 2000) // 每2秒轮询一次
    },

    async pollTask() {
      if (!this.currentTask) return

      try {
        const res = await fetch(`${this.apiBase}/video/${this.currentTask.videoId}`)
        const result = await res.json()

        if (result.code === 200 && result.data) {
          this.currentTask = result.data

          // 如果完成或失败，获取详情并停止轮询
          if (['completed', 'failed'].includes(result.data.status)) {
            this.clearTimers()
            if (result.data.status === 'completed') {
              await this.loadDetail()
            }
          }
        }
      } catch (error) {
        console.error('轮询错误:', error)
      }
    },

    async loadDetail() {
      if (!this.currentTask) return
      try {
        const res = await fetch(`${this.apiBase}/video/${this.currentTask.videoId}/detail`)
        const result = await res.json()

        if (result.code === 200 && result.data) {
          this.taskDetail = result.data

          let path = this.taskDetail.annotatedVideoPath || this.taskDetail.videoPath

          if (path) {
            path = path.replace(/\\/g, '/')
            // 提取 uploads/ 后面的相对路径
            const match = path.match(/uploads\/(.+)/)
            if (match) {
              // 必须是 /uploads/... 这样 Vite 代理才能拦截
              this.videoUrl = '/uploads/' + match[1]
            }
          }

          console.log('视频URL:', this.videoUrl)  // 应该是 /uploads/videos/xxx/xxx.mp4
        }
      } catch (error) {
        console.error('获取详情失败:', error)
      }
    },

    clearTimers() {
      if (this.pollTimer) {
        clearInterval(this.pollTimer)
        this.pollTimer = null
      }
      if (this.elapsedTimer) {
        clearInterval(this.elapsedTimer)
        this.elapsedTimer = null
      }
    },

    resetUpload() {
      this.clearTimers()
      this.currentTask = null
      this.taskDetail = null
      this.isUploading = false
      this.uploadProgress = 0
      this.videoUrl = ''
      this.videoLoaded = false
      this.videoDuration = 0
      this.$refs.fileInput && (this.$refs.fileInput.value = '')
    },

    // ========== 视频播放相关 ==========
    onVideoLoaded() {
      this.videoLoaded = true
      if (this.$refs.videoPlayer) {
        this.videoDuration = this.$refs.videoPlayer.duration
      }
    },

    seekTo(seconds) {
      if (this.$refs.videoPlayer) {
        this.$refs.videoPlayer.currentTime = seconds
        this.$refs.videoPlayer.play()
      }
    },

    // ========== 工具方法 ==========
    formatTime(seconds) {
      if (!seconds || isNaN(seconds)) return '00:00'
      const mins = Math.floor(seconds / 60)
      const secs = Math.floor(seconds % 60)
      return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
    },

    formatDuration(seconds) {
      if (seconds < 60) return `${Math.round(seconds)}秒`
      const mins = Math.floor(seconds / 60)
      const secs = Math.round(seconds % 60)
      return `${mins}分${secs}秒`
    },

    downloadReport() {
      if (!this.taskDetail) return
      const report = {
        videoId: this.taskDetail.videoId,
        filename: this.taskDetail.originalFilename,
        status: this.taskDetail.status,
        duration: this.formatTime(this.taskDetail.durationSec),
        sampledFrames: this.taskDetail.sampledFrames,
        fatigueFrames: this.taskDetail.fatigueFrames,
        fatigueRatio: this.fatigueRatio + '%',
        fatigueSegments: this.fatigueSegments,
        conclusion: this.isFatigue ? '疲劳' : '清醒',
        generateTime: new Date().toLocaleString()
      }

      const blob = new Blob([JSON.stringify(report, null, 2)], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `fatigue-report-${this.taskDetail.videoId}.json`
      a.click()
      URL.revokeObjectURL(url)
    }
  }
}
</script>

<style scoped>
/* ========== 基础布局 ========== */
.fatigue-detection-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.subtitle {
  color: #666;
  font-size: 14px;
}

/* ========== 上传区域 ========== */
.upload-section {
  margin-bottom: 30px;
}

.upload-zone {
  background: white;
  border: 2px dashed #d0d7de;
  border-radius: 16px;
  padding: 60px 40px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.upload-zone:hover, .upload-zone.drag-over {
  border-color: #409eff;
  background: #f0f7ff;
}

.upload-zone.uploading {
  cursor: not-allowed;
  border-color: #409eff;
}

.upload-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.upload-text {
  font-size: 18px;
  color: #333;
  margin-bottom: 8px;
}

.upload-hint {
  font-size: 13px;
  color: #999;
}

/* 上传进度 */
.progress-ring {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto 20px;
}

.progress-ring svg {
  transform: rotate(-90deg);
  width: 120px;
  height: 120px;
}

.progress-bg {
  fill: none;
  stroke: #e6e6e6;
  stroke-width: 8;
}

.progress-bar {
  fill: none;
  stroke: #409eff;
  stroke-width: 8;
  stroke-linecap: round;
  transition: stroke-dasharray 0.3s ease;
}

.progress-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.upload-status {
  color: #666;
  font-size: 14px;
}

/* ========== 分析中 ========== */
.processing-section {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.processing-card {
  background: white;
  border-radius: 16px;
  padding: 50px 60px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  max-width: 500px;
  width: 100%;
}

.processing-animation {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
}

.spinner {
  width: 60px;
  height: 60px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  position: absolute;
  top: 10px;
  left: 10px;
}

.spinner.small {
  width: 24px;
  height: 24px;
  border-width: 2px;
  position: static;
  display: inline-block;
  vertical-align: middle;
  margin-right: 8px;
}

.pulse-ring {
  position: absolute;
  top: 0;
  left: 0;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(64, 158, 255, 0.2);
  animation: pulse 2s ease-out infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes pulse {
  0% { transform: scale(0.8); opacity: 1; }
  100% { transform: scale(1.3); opacity: 0; }
}

.processing-card h2 {
  font-size: 20px;
  color: #333;
  margin-bottom: 12px;
}

.processing-info {
  color: #666;
  font-size: 14px;
  margin-bottom: 24px;
}

.processing-stats {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-bottom: 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-item .stat-label {
  font-size: 12px;
  color: #999;
}

.stat-item .stat-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.stat-value.mono {
  font-family: 'Courier New', monospace;
  color: #666;
}

/* ========== 结果区域 ========== */
.result-section {
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 状态横幅 */
.status-banner {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px 30px;
  border-radius: 12px;
  margin-bottom: 24px;
  color: white;
}

.status-banner.fatigue {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
}

.status-banner.normal {
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
}

.status-icon {
  font-size: 40px;
}

.status-text h2 {
  font-size: 22px;
  margin-bottom: 4px;
}

.status-text p {
  font-size: 14px;
  opacity: 0.9;
}

/* 视频区域 */
.video-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.video-container {
  position: relative;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  aspect-ratio: 16/9;
}

.video-container video {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.video-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  gap: 8px;
}

/* 时间轴标记 */
.timeline-markers {
  margin-top: 16px;
}

.timeline-title {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.timeline-bar {
  position: relative;
  height: 24px;
  background: #f0f0f0;
  border-radius: 12px;
  overflow: hidden;
}

.timeline-segment {
  position: absolute;
  top: 0;
  height: 100%;
  background: #ff6b6b;
  border-radius: 12px;
  cursor: pointer;
  opacity: 0.8;
  transition: opacity 0.2s;
}

.timeline-segment:hover {
  opacity: 1;
}

.timeline-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-card.fatigue {
  background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
  border: 1px solid #ffccc7;
}

.stat-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #1a1a2e;
  margin-bottom: 4px;
}

.stat-card.fatigue .stat-number {
  color: #cf1322;
}

.stat-label {
  font-size: 13px;
  color: #666;
}

/* 时间段详情 */
.segments-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.segments-section h3 {
  font-size: 16px;
  color: #333;
  margin-bottom: 16px;
}

.segments-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.segment-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 16px;
  background: #fff2f0;
  border-radius: 8px;
  border-left: 4px solid #ff4d4f;
  cursor: pointer;
  transition: all 0.2s;
}

.segment-item:hover {
  background: #ffccc7;
  transform: translateX(4px);
}

.segment-num {
  font-weight: bold;
  color: #cf1322;
  min-width: 30px;
}

.segment-time {
  flex: 1;
  font-family: 'Courier New', monospace;
  color: #333;
  font-size: 14px;
}

.segment-duration {
  color: #666;
  font-size: 13px;
}

.btn-play {
  background: #ff4d4f;
  color: white;
  border: none;
  padding: 6px 14px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-play:hover {
  background: #cf1322;
}

/* 操作按钮 */
.action-bar {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.btn-primary, .btn-secondary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  border-radius: 8px;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-primary {
  background: #409eff;
  color: white;
}

.btn-primary:hover {
  background: #66b1ff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.btn-secondary {
  background: white;
  color: #666;
  border: 1px solid #d0d7de;
}

.btn-secondary:hover {
  border-color: #409eff;
  color: #409eff;
}

/* 错误区域 */
.error-section {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.error-card {
  background: white;
  border-radius: 16px;
  padding: 50px 60px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}

.error-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.error-message {
  color: #666;
  margin-bottom: 24px;
  max-width: 400px;
}

/* 响应式 */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .segment-item {
    flex-wrap: wrap;
  }

  .processing-stats {
    flex-direction: column;
    gap: 12px;
  }
}
</style>