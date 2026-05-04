<template>
  <div class="detector-container">
    <h2>疲劳驾驶检测系统</h2>

    <!-- 图片上传检测 -->
    <div class="upload-section">
      <input
          type="file"
          accept="image/*"
          @change="handleImageUpload"
          ref="fileInput"
      />
      <button @click="triggerUpload">上传图片检测</button>
    </div>

    <!-- 实时视频检测 -->
    <div class="video-section">
      <video
          ref="videoRef"
          autoplay
          playsinline
          muted
      ></video>
      <canvas ref="canvasRef" style="display: none;"></canvas>

      <div class="controls">
        <button @click="startRealtime" :disabled="isRealtime">
          开始实时检测
        </button>
        <button @click="stopRealtime" :disabled="!isRealtime">
          停止检测
        </button>
      </div>
    </div>

    <!-- 结果显示 -->
    <div v-if="result" class="result-section">
      <h3>检测结果</h3>
      <p :class="['status', result.resultClass]">
        状态: {{ result.resultClass === 'drowsy' ? '疲劳' : '清醒' }}
      </p>
      <p>置信度: {{ (result.confidence * 100).toFixed(2) }}%</p>
      <p>处理耗时: {{ result.processingTime }}ms</p>
    </div>

    <!-- 实时统计 -->
    <div v-if="realtimeStats" class="stats-section">
      <h3>实时统计</h3>
      <p>总帧数: {{ realtimeStats.totalFrames }}</p>
      <p>疲劳帧数: {{ realtimeStats.drowsyFrames }}</p>
      <p>疲劳率: {{ realtimeStats.drowsyRate }}</p>
    </div>
  </div>
</template>

<script>
import { detectImage, RealtimeDetection } from '@/api/detection'

export default {
  name: 'FatigueDetector',
  data() {
    return {
      result: null,
      isRealtime: false,
      realtimeStats: null,
      rtDetection: null,
      stream: null
    }
  },
  methods: {
    triggerUpload() {
      this.$refs.fileInput.click()
    },

    async handleImageUpload(event) {
      const file = event.target.files[0]
      if (!file) return

      try {
        const response = await detectImage(file, 'user001', 'device001')
        if (response.data.code === 200) {
          this.result = response.data.data
          this.$message.success('检测完成')
        } else {
          this.$message.error(response.data.message)
        }
      } catch (error) {
        this.$message.error('检测失败: ' + error.message)
      }
    },

    async startRealtime() {
      try {
        // 获取摄像头权限
        this.stream = await navigator.mediaDevices.getUserMedia({
          video: { width: 640, height: 480 }
        })
        this.$refs.videoRef.srcObject = this.stream

        // 建立WebSocket连接
        this.rtDetection = new RealtimeDetection(
            (data) => this.handleRealtimeMessage(data),
            (err) => console.error('实时检测错误:', err)
        )
        this.rtDetection.connect()
        this.isRealtime = true

        // 开始捕获帧（每秒10帧）
        this.captureInterval = setInterval(() => {
          this.captureAndSend()
        }, 100)

      } catch (error) {
        this.$message.error('无法启动摄像头: ' + error.message)
      }
    },

    captureAndSend() {
      if (!this.isRealtime) return

      const video = this.$refs.videoRef
      const canvas = this.$refs.canvasRef
      const ctx = canvas.getContext('2d')

      canvas.width = 224  // 模型输入尺寸
      canvas.height = 224
      ctx.drawImage(video, 0, 0, 224, 224)

      // 转为Base64并发送
      const base64 = canvas.toDataURL('image/jpeg', 0.8).split(',')[1]
      this.rtDetection.sendFrame(base64)
    },

    handleRealtimeMessage(data) {
      if (data.type === 'detection_result') {
        this.result = data.result
        this.realtimeStats = data.sessionStats
      } else if (data.type === 'warning') {
        this.$message.warning(data.message)
      }
    },

    stopRealtime() {
      this.isRealtime = false

      if (this.captureInterval) {
        clearInterval(this.captureInterval)
      }

      if (this.rtDetection) {
        this.rtDetection.disconnect()
      }

      if (this.stream) {
        this.stream.getTracks().forEach(track => track.stop())
      }
    }
  },
  beforeDestroy() {
    this.stopRealtime()
  }
}
</script>

<style scoped>
.detector-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.upload-section, .video-section {
  margin: 20px 0;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
}

video {
  width: 100%;
  max-width: 640px;
  border-radius: 4px;
}

.status {
  font-size: 24px;
  font-weight: bold;
}

.status.alert {
  color: #67C23A;
}

.status.drowsy {
  color: #F56C6C;
}

.result-section, .stats-section {
  margin-top: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}
</style>