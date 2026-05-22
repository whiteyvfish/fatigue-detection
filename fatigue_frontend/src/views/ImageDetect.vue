<template>
  <div class="detect-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="main-title">
          <el-icon class="title-icon"><PictureFilled /></el-icon>
          疲劳检测
        </h1>
        <p class="subtitle">上传驾驶员照片，AI 智能分析疲劳状态</p>
      </div>
      <el-tag
        v-if="aiStatus"
        :type="aiStatus === 'UP' ? 'success' : 'danger'"
        effect="dark"
        size="large"
        class="status-tag"
      >
        <el-icon v-if="aiStatus === 'UP'"><CircleCheck /></el-icon>
        <el-icon v-else><CircleClose /></el-icon>
        AI服务{{ aiStatus === 'UP' ? '正常' : '异常' }}
      </el-tag>
    </div>

    <el-row :gutter="24">
      <!-- 左侧：上传区域 -->
      <el-col :xs="24" :lg="12">
        <div class="panel upload-panel">
          <div class="panel-header">
            <h3 class="panel-title">
              <el-icon><Upload /></el-icon>
              上传图片
            </h3>
          </div>

          <div class="upload-area">
            <el-upload
              ref="uploadRef"
              class="uploader"
              drag
              action="#"
              :auto-upload="false"
              :on-change="handleFileChange"
              accept="image/*"
              :class="{ 'has-file': selectedFile }"
            >
              <div class="upload-content" v-if="!selectedFile">
                <div class="upload-icon-wrapper">
                  <el-icon class="upload-icon"><UploadFilled /></el-icon>
                  <div class="upload-ring"></div>
                </div>
                <div class="upload-text">
                  <p class="main-text">拖拽图片到此处或 <em>点击上传</em></p>
                  <p class="sub-text">支持 JPG、PNG 格式，建议尺寸 224x224 以上</p>
                </div>
              </div>

              <div class="upload-preview" v-else>
                <img :src="previewUrl" class="preview-img" />
                <div class="preview-overlay">
                  <el-icon><Refresh /></el-icon>
                  <span>更换图片</span>
                </div>
              </div>
            </el-upload>
          </div>

          <!-- 输出选项 - 复选框 -->
          <div class="output-options">
            <el-checkbox v-model="options.showAnnotated" size="default" class="option-checkbox">
              <span class="option-label">显示标注结果图</span>
              <el-tooltip content="在检测结果中显示人脸关键点标注框" placement="top">
                <el-icon class="option-tip"><QuestionFilled /></el-icon>
              </el-tooltip>
            </el-checkbox>
            <el-checkbox v-model="options.showDetails" size="default" class="option-checkbox">
              <span class="option-label">显示检测详情</span>
              <el-tooltip content="显示每个检测目标的标签、置信度和边界框信息" placement="top">
                <el-icon class="option-tip"><QuestionFilled /></el-icon>
              </el-tooltip>
            </el-checkbox>
          </div>

          <div class="upload-actions">
            <el-button
              type="primary"
              @click="handleDetect"
              :loading="loading"
              :disabled="!selectedFile"
              size="large"
              class="detect-btn"
            >
              <el-icon v-if="!loading"><Aim /></el-icon>
              {{ loading ? 'AI 分析中...' : '开始检测' }}
            </el-button>

            <el-button
              v-if="selectedFile"
              @click="clearFile"
              size="large"
              class="clear-btn"
            >
              <el-icon><Delete /></el-icon>
              清除
            </el-button>
          </div>
        </div>

        <!-- 使用提示 -->
        <div class="panel tips-panel">
          <div class="panel-header">
            <h3 class="panel-title">
              <el-icon><InfoFilled /></el-icon>
              使用提示
            </h3>
          </div>
          <ul class="tips-list">
            <li><el-icon><Check /></el-icon> 请确保图片清晰可见面部特征</li>
            <li><el-icon><Check /></el-icon> 建议在光线充足的环境下拍摄</li>
            <li><el-icon><Check /></el-icon> 支持单次上传一张图片进行检测</li>
            <li><el-icon><Check /></el-icon> 检测过程将调用 AI 模型进行推理分析</li>
          </ul>
        </div>
      </el-col>

      <!-- 右侧：检测结果 -->
      <el-col :xs="24" :lg="12">
        <div class="panel result-panel" v-if="result">
          <div class="panel-header">
            <h3 class="panel-title">
              <el-icon><DocumentChecked /></el-icon>
              检测结果
            </h3>
            <el-tag
              :type="result.resultClass === 'drowsy' ? 'danger' : 'success'"
              effect="dark"
              size="large"
              class="result-tag"
            >
              {{ result.resultClassCn }}
            </el-tag>
          </div>

          <div class="result-content">
            <!-- 结果概览卡片 -->
            <div class="result-overview">
              <div class="overview-item" :class="result.resultClass">
                <div class="overview-icon">
                  <el-icon v-if="result.resultClass === 'drowsy'"><WarningFilled /></el-icon>
                  <el-icon v-else><CircleCheckFilled /></el-icon>
                </div>
                <div class="overview-info">
                  <span class="overview-label">检测结论</span>
                  <span class="overview-value">{{ result.resultClassCn }}</span>
                </div>
              </div>
            </div>

            <!-- 详细数据 -->
            <div class="result-details">
              <div class="detail-item">
                <span class="detail-label">记录ID</span>
                <span class="detail-value mono">{{ result.recordId }}</span>
              </div>

              <div class="detail-item">
                <span class="detail-label">置信度</span>
                <div class="detail-progress">
                  <el-progress
                    :percentage="Math.round((result.confidence || 0) * 100)"
                    :color="result.resultClass === 'drowsy' ? '#ef4444' : '#10b981'"
                    :stroke-width="10"
                  />
                  <span class="progress-text">{{ ((result.confidence || 0) * 100).toFixed(1) }}%</span>
                </div>
              </div>

              <div class="detail-item">
                <span class="detail-label">处理耗时</span>
                <span class="detail-value">
                  <el-icon><Timer /></el-icon>
                  {{ result.totalTime || result.processingTime }}ms
                </span>
              </div>

              <div class="detail-item">
                <span class="detail-label">检测目标数</span>
                <span class="detail-value">
                  <el-icon><Aim /></el-icon>
                  {{ result.detections ? result.detections.length : 0 }} 个
                </span>
              </div>
            </div>

            <!-- 检测目标详情（仅当选项开启且存在detections时显示） -->
            <div class="detections-section" v-if="options.showDetails && result.detections && result.detections.length > 0">
              <h4 class="section-title">
                <el-icon><List /></el-icon>
                检测目标详情
              </h4>
              <div class="detections-list">
                <div
                  v-for="(det, idx) in result.detections"
                  :key="idx"
                  class="detection-item"
                  :class="{ 'fatigue-sign': isFatigueLabel(det.label) }"
                >
                  <el-tag
                    :type="isFatigueLabel(det.label) ? 'danger' : ''"
                    size="small"
                    effect="light"
                  >
                    {{ det.label }}
                  </el-tag>
                  <span class="det-score">{{ (det.score * 100).toFixed(1) }}%</span>
                  <span class="det-bbox" v-if="det.bbox">
                    [{{ det.bbox.join(', ') }}]
                  </span>
                </div>
              </div>
            </div>

            <!-- 结果图片（仅当选项开启时显示） -->
            <div class="result-image-section" v-if="options.showAnnotated">
              <h4 class="section-title">
                <el-icon><View /></el-icon>
                标注结果图
              </h4>
              <div v-if="result.resultImageBase64" class="image-wrapper">
                <img :src="result.resultImageBase64" alt="检测结果" />
                <div class="image-overlay" @click="showImagePreview">
                  <el-icon><ZoomIn /></el-icon>
                  <span>点击查看大图</span>
                </div>
              </div>
              <el-alert
                v-else
                title="未获取到 AI 标注图"
                type="warning"
                :closable="false"
                description="AI 端未返回可视化结果，可能原因：AI 服务异常、未开启可视化参数或模型未检测到目标。请检查后端日志确认 AI 服务是否正常运行。"
                show-icon
              />
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div class="panel empty-panel" v-else>
          <div class="empty-content">
            <div class="empty-icon-wrapper">
              <el-icon class="empty-icon"><Aim /></el-icon>
              <div class="empty-ring"></div>
            </div>
            <h3>等待检测</h3>
            <p>上传图片并点击"开始检测"，AI 将自动分析</p>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图片预览对话框 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="检测结果预览"
      width="70%"
      class="preview-dialog"
      center
    >
      <img v-if="result?.resultImageBase64" :src="result.resultImageBase64" class="preview-dialog-img" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { detectImage, healthCheck } from '@/api'
import {
  PictureFilled, CircleCheck, CircleClose, Upload, UploadFilled,
  Aim, Delete, Refresh, InfoFilled, Check, DocumentChecked,
  WarningFilled, CircleCheckFilled, Timer, View, ZoomIn,
  QuestionFilled, List
} from '@element-plus/icons-vue'

const selectedFile = ref(null)
const previewUrl = ref('')
const loading = ref(false)
const result = ref(null)
const aiStatus = ref('')
const previewDialogVisible = ref(false)
const uploadRef = ref(null)

// 输出选项
const options = reactive({
  showAnnotated: true,   // 显示标注结果图
  showDetails: true       // 显示检测详情列表
})

// 检查AI服务状态
onMounted(async () => {
  try {
    const res = await healthCheck()
    aiStatus.value = res.data?.data?.aiService || ''
    if (!aiStatus.value) {
      // fallback: 检查 backend 状态
      aiStatus.value = res.data?.data?.backend === 'UP' ? 'UP' : 'DOWN'
    }
  } catch (error) {
    aiStatus.value = 'DOWN'
  }
})

// 疲劳标签判断
const isFatigueLabel = (label) => {
  if (!label) return false
  const lower = label.toLowerCase()
  return lower.includes('closed') || lower.includes('yawn') || lower.includes('drowsy')
}

const handleFileChange = (uploadFile, uploadFiles) => {
  // 即使一次意外多选，也只取最后选中的那张
  const file = uploadFiles && uploadFiles.length > 0
      ? uploadFiles[uploadFiles.length - 1]
      : uploadFile

  // 【关键】清空 el-upload 内部的文件列表，解除浏览器 input 的 value 锁定
  // 这样下次点击"更换图片"时，新文件一定会触发 on-change
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }

  selectedFile.value = file.raw
  previewUrl.value = URL.createObjectURL(file.raw)
  result.value = null
}

const clearFile = () => {
  selectedFile.value = null
  previewUrl.value = ''
  result.value = null
  // 同时清空 el-upload 内部状态，避免残留
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

const handleDetect = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择图片')
    return
  }

  loading.value = true
  try {
    // 调用后端 /detection/image，传递 visualize 参数
    // 后端会调用 AI 服务器 /analyze_image 进行 YOLOv8 推理
    const res = await detectImage(
      selectedFile.value,
      'user001',
      options.showAnnotated
    )

    if (res.data.code === 200) {
      result.value = res.data.data
      ElMessage.success('AI 检测完成')
    } else {
      ElMessage.error(res.data.message || '检测失败')
    }
  } catch (error) {
    console.error('检测失败:', error)
    ElMessage.error('检测失败：' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

const showImagePreview = () => {
  if (result.value?.resultImageBase64) {
    previewDialogVisible.value = true
  }
}
</script>

<style scoped>
/* ========== 基础布局 ========== */
.detect-page {
  padding: 24px;
  color: #334155;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  min-height: 100%;
}

/* ========== 页面头部 ========== */
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

.status-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  font-weight: 500;
}

/* ========== 面板样式 ========== */
.panel {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(148, 163, 184, 0.15);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.panel:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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

/* ========== 上传区域 ========== */
.upload-area {
  margin-bottom: 16px;
}

:deep(.uploader) {
  width: 100%;
}

:deep(.uploader .el-upload) {
  width: 100%;
}

:deep(.uploader .el-upload-dragger) {
  width: 100%;
  height: 260px;
  background: rgba(248, 250, 252, 0.8);
  border: 2px dashed rgba(148, 163, 184, 0.25);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

:deep(.uploader .el-upload-dragger:hover) {
  border-color: #0ea5e9;
  background: rgba(14, 165, 233, 0.04);
}

:deep(.uploader.has-file .el-upload-dragger) {
  padding: 0;
  border-style: solid;
  border-color: rgba(14, 165, 233, 0.3);
}

.upload-content {
  text-align: center;
}

.upload-icon-wrapper {
  position: relative;
  width: 72px;
  height: 72px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-icon {
  font-size: 44px;
  color: #0ea5e9;
  z-index: 1;
}

.upload-ring {
  position: absolute;
  inset: 0;
  border: 2px solid rgba(14, 165, 233, 0.2);
  border-radius: 50%;
  animation: pulse-ring 2s infinite;
}

@keyframes pulse-ring {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.1); opacity: 0.5; }
}

.upload-text .main-text {
  font-size: 16px;
  color: #475569;
  margin: 0 0 8px 0;
}

.upload-text .main-text em {
  color: #0ea5e9;
  font-style: normal;
  font-weight: 600;
}

.upload-text .sub-text {
  font-size: 13px;
  color: #94a3b8;
  margin: 0;
}

/* 预览区域 */
.upload-preview {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  border-radius: 10px;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-overlay {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.75);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s ease;
  gap: 8px;
  cursor: pointer;
}

.preview-overlay .el-icon {
  font-size: 32px;
}

.upload-preview:hover .preview-overlay {
  opacity: 1;
}

/* ========== 输出选项 - 复选框 ========== */
.output-options {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  padding: 12px 16px;
  margin-bottom: 16px;
  background: rgba(248, 250, 252, 0.8);
  border-radius: 10px;
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.option-checkbox {
  display: flex;
  align-items: center;
  height: auto;
  margin-right: 0;
}

:deep(.option-checkbox .el-checkbox__label) {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #475569;
  padding-left: 8px;
}

:deep(.option-checkbox .el-checkbox__inner) {
  border-color: #94a3b8;
}

:deep(.option-checkbox .el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #0ea5e9;
  border-color: #0ea5e9;
}

.option-label {
  color: #475569;
  font-weight: 500;
}

.option-tip {
  color: #94a3b8;
  font-size: 14px;
  cursor: help;
}

.option-tip:hover {
  color: #0ea5e9;
}

/* ========== 操作按钮 ========== */
.upload-actions {
  display: flex;
  gap: 12px;
}

.detect-btn {
  flex: 1;
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%) !important;
  border: none !important;
  height: 44px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.detect-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(14, 165, 233, 0.4);
}

.detect-btn:disabled {
  background: rgba(148, 163, 184, 0.3) !important;
}

.clear-btn {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.25);
  color: #64748b;
  height: 44px;
}

.clear-btn:hover {
  border-color: rgba(14, 165, 233, 0.3);
  color: #0ea5e9;
  background: rgba(14, 165, 233, 0.05);
}

/* ========== 提示面板 ========== */
.tips-panel {
  margin-bottom: 0;
}

.tips-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.tips-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  color: #64748b;
  font-size: 13px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.1);
}

.tips-list li:last-child {
  border-bottom: none;
}

.tips-list .el-icon {
  color: #10b981;
  font-size: 16px;
  flex-shrink: 0;
}

/* ========== 结果面板 ========== */
.result-panel {
  margin-bottom: 0;
}

.result-tag {
  padding: 6px 16px;
  font-size: 14px;
  border-radius: 6px;
  font-weight: 600;
}

.result-overview {
  margin-bottom: 24px;
}

.overview-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid;
  background: rgba(248, 250, 252, 0.8);
}

.overview-item.alert,
.overview-item.normal {
  border-color: rgba(16, 185, 129, 0.25);
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.06) 0%, rgba(16, 185, 129, 0.02) 100%);
}

.overview-item.drowsy {
  border-color: rgba(239, 68, 68, 0.25);
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.06) 0%, rgba(239, 68, 68, 0.02) 100%);
}

.overview-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
}

.overview-item.alert .overview-icon,
.overview-item.normal .overview-icon {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
}

.overview-item.drowsy .overview-icon {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}

.overview-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.overview-label {
  font-size: 13px;
  color: #64748b;
}

.overview-value {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

.overview-item.drowsy .overview-value {
  color: #dc2626;
}

.overview-item.alert .overview-value,
.overview-item.normal .overview-value {
  color: #059669;
}

/* ========== 详细数据 ========== */
.result-details {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid rgba(148, 163, 184, 0.1);
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  width: 100px;
  color: #64748b;
  font-size: 13px;
  font-weight: 500;
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  color: #475569;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}

.detail-value .el-icon {
  color: #0ea5e9;
}

.detail-value.mono {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  color: #64748b;
}

.detail-progress {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

:deep(.detail-progress .el-progress-bar__outer) {
  background: rgba(148, 163, 184, 0.2);
  border-radius: 5px;
}

:deep(.detail-progress .el-progress-bar__inner) {
  border-radius: 5px;
}

.progress-text {
  min-width: 48px;
  text-align: right;
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
}

/* ========== 检测目标详情 ========== */
.detections-section {
  margin-bottom: 20px;
  padding-top: 16px;
  border-top: 1px solid rgba(148, 163, 184, 0.1);
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.detections-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detection-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  background: rgba(248, 250, 252, 0.8);
  border-radius: 8px;
  border: 1px solid rgba(148, 163, 184, 0.1);
  transition: all 0.2s ease;
}

.detection-item:hover {
  border-color: rgba(14, 165, 233, 0.2);
  background: rgba(14, 165, 233, 0.03);
}

.detection-item.fatigue-sign {
  border-left: 3px solid #ef4444;
  background: rgba(254, 242, 242, 0.6);
}

.det-score {
  font-weight: 600;
  color: #475569;
  font-size: 13px;
  min-width: 50px;
}

.det-bbox {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 11px;
  color: #94a3b8;
  margin-left: auto;
}

/* ========== 结果图片 ========== */
.result-image-section {
  padding-top: 16px;
  border-top: 1px solid rgba(148, 163, 184, 0.1);
}

.image-wrapper {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(148, 163, 184, 0.2);
}

.image-wrapper img {
  width: 100%;
  display: block;
}

.image-overlay {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s ease;
  cursor: pointer;
  gap: 8px;
}

.image-overlay .el-icon {
  font-size: 32px;
}

.image-wrapper:hover .image-overlay {
  opacity: 1;
}

/* ========== 空状态 ========== */
.empty-panel {
  min-height: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-content {
  text-align: center;
}

.empty-icon-wrapper {
  position: relative;
  width: 90px;
  height: 90px;
  margin: 0 auto 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-icon {
  font-size: 52px;
  color: #94a3b8;
  z-index: 1;
}

.empty-ring {
  position: absolute;
  inset: 0;
  border: 2px solid rgba(148, 163, 184, 0.2);
  border-radius: 50%;
  animation: pulse-ring 3s infinite;
}

.empty-content h3 {
  font-size: 18px;
  color: #475569;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.empty-content p {
  font-size: 14px;
  color: #94a3b8;
  margin: 0;
}

/* ========== 预览对话框 ========== */
:deep(.preview-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.preview-dialog .el-dialog__header) {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-bottom: 1px solid rgba(148, 163, 184, 0.15);
  margin-right: 0;
  padding: 20px 24px;
  color: #1e293b;
  font-weight: 600;
}

:deep(.preview-dialog .el-dialog__body) {
  padding: 24px;
  background: #f8fafc;
}

.preview-dialog-img {
  width: 100%;
  border-radius: 8px;
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .detect-page {
    padding: 12px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .main-title {
    font-size: 20px;
  }

  .panel {
    padding: 16px;
  }

  :deep(.uploader .el-upload-dragger) {
    height: 200px;
  }

  .output-options {
    flex-direction: column;
    gap: 12px;
  }

  .overview-item {
    flex-direction: column;
    text-align: center;
  }
}
</style>
