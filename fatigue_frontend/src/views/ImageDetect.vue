<!--<template>-->
<!--  <div class="detect-container">-->
<!--    <el-row :gutter="20">-->
<!--      <el-col :span="12">-->
<!--        <el-card class="upload-card">-->
<!--          <template #header>-->
<!--            <div class="card-header">-->
<!--              <span>上传检测</span>-->
<!--              <el-tag v-if="aiStatus" :type="aiStatus === 'UP' ? 'success' : 'danger'">-->
<!--                AI服务{{ aiStatus === 'UP' ? '正常' : '异常' }}-->
<!--              </el-tag>-->
<!--            </div>-->
<!--          </template>-->

<!--          <el-upload-->
<!--              class="upload-demo"-->
<!--              drag-->
<!--              action="#"-->
<!--              :auto-upload="false"-->
<!--              :on-change="handleFileChange"-->
<!--              accept="image/*"-->
<!--              :limit="1"-->
<!--          >-->
<!--            <el-icon class="el-icon&#45;&#45;upload"><upload-filled /></el-icon>-->
<!--            <div class="el-upload__text">-->
<!--              拖拽图片到此处或 <em>点击上传</em>-->
<!--            </div>-->
<!--            <template #tip>-->
<!--              <div class="el-upload__tip">-->
<!--                支持 JPG、PNG 格式，建议图片尺寸 224x224 以上-->
<!--              </div>-->
<!--            </template>-->
<!--          </el-upload>-->

<!--          <div v-if="previewUrl" class="preview-section">-->
<!--            <h4>预览</h4>-->
<!--            <img :src="previewUrl" class="preview-image" />-->
<!--          </div>-->

<!--          <el-button-->
<!--              type="primary"-->
<!--              @click="handleDetect"-->
<!--              :loading="loading"-->
<!--              :disabled="!selectedFile"-->
<!--              style="margin-top: 20px; width: 100%"-->
<!--              size="large"-->
<!--          >-->
<!--            开始检测-->
<!--          </el-button>-->
<!--        </el-card>-->
<!--      </el-col>-->

<!--      <el-col :span="12">-->
<!--        <el-card v-if="result" class="result-card">-->
<!--          <template #header>-->
<!--            <div class="card-header">-->
<!--              <span>检测结果</span>-->
<!--              <el-tag :type="result.resultClass === 'drowsy' ? 'danger' : 'success'" size="large">-->
<!--                {{ result.resultClassCn }}-->
<!--              </el-tag>-->
<!--            </div>-->
<!--          </template>-->

<!--          <div class="result-content">-->
<!--            <div class="result-item">-->
<!--              <span class="label">记录ID：</span>-->
<!--              <span class="value">{{ result.recordId }}</span>-->
<!--            </div>-->

<!--            <div class="result-item">-->
<!--              <span class="label">置信度：</span>-->
<!--              <el-progress-->
<!--                  :percentage="Math.round(result.confidence * 100)"-->
<!--                  :color="result.resultClass === 'drowsy' ? '#f56c6c' : '#67c23a'"-->
<!--              />-->
<!--            </div>-->

<!--            <div class="result-item">-->
<!--              <span class="label">清醒概率：</span>-->
<!--              <span class="value">{{ (result.alertProb * 100).toFixed(2) }}%</span>-->
<!--            </div>-->

<!--            <div class="result-item">-->
<!--              <span class="label">疲劳概率：</span>-->
<!--              <span class="value" :class="{ 'danger-text': result.drowsyProb > 0.5 }">-->
<!--                {{ (result.drowsyProb * 100).toFixed(2) }}%-->
<!--              </span>-->
<!--            </div>-->

<!--            <div class="result-item">-->
<!--              <span class="label">处理耗时：</span>-->
<!--              <span class="value">{{ result.processingTime }}ms</span>-->
<!--            </div>-->

<!--            <div v-if="result.resultImageBase64" class="result-image">-->
<!--              <h4>标注结果</h4>-->
<!--              <img :src="result.resultImageBase64" alt="检测结果" />-->
<!--            </div>-->
<!--          </div>-->
<!--        </el-card>-->

<!--        <el-empty v-else description="暂无检测结果" />-->
<!--      </el-col>-->
<!--    </el-row>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup>-->
<!--import { ref, onMounted } from 'vue'-->
<!--import { UploadFilled } from '@element-plus/icons-vue'-->
<!--import { ElMessage } from 'element-plus'-->
<!--import { detectImage, healthCheck } from '@/api'-->

<!--const selectedFile = ref(null)-->
<!--const previewUrl = ref('')-->
<!--const loading = ref(false)-->
<!--const result = ref(null)-->
<!--const aiStatus = ref('')-->

<!--// 检查AI服务状态-->
<!--onMounted(async () => {-->
<!--  try {-->
<!--    const res = await healthCheck()-->
<!--    aiStatus.value = res.data.data.aiService-->
<!--  } catch (error) {-->
<!--    aiStatus.value = 'DOWN'-->
<!--  }-->
<!--})-->

<!--const handleFileChange = (file) => {-->
<!--  selectedFile.value = file.raw-->
<!--  previewUrl.value = URL.createObjectURL(file.raw)-->
<!--  result.value = null-->
<!--}-->

<!--const handleDetect = async () => {-->
<!--  if (!selectedFile.value) {-->
<!--    ElMessage.warning('请选择图片')-->
<!--    return-->
<!--  }-->

<!--  loading.value = true-->
<!--  try {-->
<!--    const res = await detectImage(selectedFile.value, 'user001')-->
<!--    if (res.data.code === 200) {-->
<!--      result.value = res.data.data-->
<!--      ElMessage.success('检测完成')-->
<!--    } else {-->
<!--      ElMessage.error(res.data.message)-->
<!--    }-->
<!--  } catch (error) {-->
<!--    ElMessage.error('检测失败：' + error.message)-->
<!--  } finally {-->
<!--    loading.value = false-->
<!--  }-->
<!--}-->
<!--</script>-->

<!--<style scoped>-->
<!--.detect-container {-->
<!--  max-width: 1200px;-->
<!--  margin: 0 auto;-->
<!--}-->

<!--.card-header {-->
<!--  display: flex;-->
<!--  justify-content: space-between;-->
<!--  align-items: center;-->
<!--  font-weight: bold;-->
<!--}-->

<!--.upload-demo {-->
<!--  text-align: center;-->
<!--}-->

<!--.preview-section {-->
<!--  margin-top: 20px;-->
<!--  text-align: center;-->
<!--}-->

<!--.preview-image {-->
<!--  max-width: 100%;-->
<!--  max-height: 300px;-->
<!--  border-radius: 8px;-->
<!--  border: 1px solid #dcdfe6;-->
<!--}-->

<!--.result-content {-->
<!--  padding: 10px;-->
<!--}-->

<!--.result-item {-->
<!--  margin-bottom: 20px;-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--}-->

<!--.label {-->
<!--  width: 100px;-->
<!--  color: #606266;-->
<!--  font-weight: 500;-->
<!--}-->

<!--.value {-->
<!--  flex: 1;-->
<!--  color: #303133;-->
<!--  font-size: 16px;-->
<!--}-->

<!--.danger-text {-->
<!--  color: #f56c6c;-->
<!--  font-weight: bold;-->
<!--}-->

<!--.result-image {-->
<!--  margin-top: 20px;-->
<!--  text-align: center;-->
<!--}-->

<!--.result-image img {-->
<!--  max-width: 100%;-->
<!--  border-radius: 8px;-->
<!--  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);-->
<!--}-->
<!--</style>-->

<template>
  <div class="detect-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="main-title">


        </h1>
        <p class="subtitle">上传驾驶员照片，AI智能分析疲劳状态</p>
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
        <div class="panel glass-effect upload-panel">
          <div class="panel-header">
            <h3 class="panel-title">
              <el-icon><Upload /></el-icon>
              上传图片
            </h3>
          </div>

          <div class="upload-area">
            <el-upload
                class="uploader"
                drag
                action="#"
                :auto-upload="false"
                :on-change="handleFileChange"
                accept="image/*"
                :limit="1"
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

          <div class="upload-actions">
            <el-button
                type="primary"
                @click="handleDetect"
                :loading="loading"
                :disabled="!selectedFile"
                size="large"
                class="detect-btn"
            >
              <el-icon><Aim /></el-icon>
              {{ loading ? '检测中...' : '开始检测' }}
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

        <!-- 历史记录快捷入口 -->
        <div class="panel glass-effect tips-panel">
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
            <li><el-icon><Check /></el-icon> 检测过程通常需要 1-3 秒</li>
          </ul>
        </div>
      </el-col>

      <!-- 右侧：检测结果 -->
      <el-col :xs="24" :lg="12">
        <div class="panel glass-effect result-panel" v-if="result">
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
                <span class="detail-value">{{ result.recordId }}</span>
              </div>

              <div class="detail-item">
                <span class="detail-label">置信度</span>
                <div class="detail-progress">
                  <el-progress
                      :percentage="Math.round(result.confidence * 100)"
                      :color="result.resultClass === 'drowsy' ? '#ef4444' : '#10b981'"
                      :stroke-width="10"
                      class="custom-progress"
                  />
                  <span class="progress-text">{{ (result.confidence * 100).toFixed(1) }}%</span>
                </div>
              </div>

              <div class="detail-item">
                <span class="detail-label">清醒概率</span>
                <div class="prob-bar">
                  <div class="prob-fill alert" :style="{ width: (result.alertProb * 100) + '%' }"></div>
                  <span class="prob-text">{{ (result.alertProb * 100).toFixed(2) }}%</span>
                </div>
              </div>

              <div class="detail-item">
                <span class="detail-label">疲劳概率</span>
                <div class="prob-bar">
                  <div class="prob-fill drowsy" :style="{ width: (result.drowsyProb * 100) + '%' }"></div>
                  <span class="prob-text" :class="{ 'danger': result.drowsyProb > 0.5 }">
                    {{ (result.drowsyProb * 100).toFixed(2) }}%
                  </span>
                </div>
              </div>

              <div class="detail-item">
                <span class="detail-label">处理耗时</span>
                <span class="detail-value">
                  <el-icon><Timer /></el-icon>
                  {{ result.processingTime }}ms
                </span>
              </div>
            </div>

            <!-- 结果图片 -->
            <div class="result-image-section" v-if="result.resultImageBase64">
              <h4 class="image-title">
                <el-icon><View /></el-icon>
                标注结果
              </h4>
              <div class="image-wrapper">
                <img :src="result.resultImageBase64" alt="检测结果" />
                <div class="image-overlay" @click="showImagePreview">
                  <el-icon><ZoomIn /></el-icon>
                  <span>点击查看大图</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div class="panel glass-effect empty-panel" v-else>
          <div class="empty-content">
            <div class="empty-icon-wrapper">
              <el-icon class="empty-icon"><Aim /></el-icon>
              <div class="empty-ring"></div>
            </div>
            <h3>等待检测</h3>
            <p>上传图片并点击"开始检测"查看结果</p>
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
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { detectImage, healthCheck } from '@/api'
import {
  Picture, CircleCheck, CircleClose, Upload, UploadFilled,
  Aim, Delete, Refresh, InfoFilled, Check, DocumentChecked,
  WarningFilled, CircleCheckFilled, Timer, View, ZoomIn
} from '@element-plus/icons-vue'

const selectedFile = ref(null)
const previewUrl = ref('')
const loading = ref(false)
const result = ref(null)
const aiStatus = ref('')
const previewDialogVisible = ref(false)

// 检查AI服务状态
onMounted(async () => {
  try {
    const res = await healthCheck()
    aiStatus.value = res.data.data.aiService
  } catch (error) {
    aiStatus.value = 'DOWN'
  }
})

const handleFileChange = (file) => {
  selectedFile.value = file.raw
  previewUrl.value = URL.createObjectURL(file.raw)
  result.value = null
}

const clearFile = () => {
  selectedFile.value = null
  previewUrl.value = ''
  result.value = null
}

const handleDetect = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择图片')
    return
  }

  loading.value = true
  try {
    const res = await detectImage(selectedFile.value, 'user001')
    if (res.data.code === 200) {
      result.value = res.data.data
      ElMessage.success('检测完成')
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('检测失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const showImagePreview = () => {
  previewDialogVisible.value = true
}
</script>

<style scoped>
.detect-page {
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

.status-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
}

/* 玻璃效果 */
.glass-effect {
  background: rgba(30, 41, 59, 0.5);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(94, 234, 212, 0.1);
  border-radius: 12px;
}

/* 面板样式 */
.panel {
  padding: 24px;
  margin-bottom: 20px;
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
  color: #f8fafc;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 上传区域 */
.upload-area {
  margin-bottom: 20px;
}

:deep(.uploader) {
  width: 100%;
}

:deep(.uploader .el-upload) {
  width: 100%;
}

:deep(.uploader .el-upload-dragger) {
  width: 100%;
  height: 280px;
  background: rgba(15, 23, 42, 0.5);
  border: 2px dashed rgba(94, 234, 212, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

:deep(.uploader .el-upload-dragger:hover) {
  border-color: #5eead4;
  background: rgba(94, 234, 212, 0.05);
}

:deep(.uploader.has-file .el-upload-dragger) {
  padding: 0;
  border-style: solid;
  border-color: rgba(94, 234, 212, 0.3);
}

.upload-content {
  text-align: center;
}

.upload-icon-wrapper {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-icon {
  font-size: 48px;
  color: #5eead4;
  z-index: 1;
}

.upload-ring {
  position: absolute;
  inset: 0;
  border: 2px solid rgba(94, 234, 212, 0.2);
  border-radius: 50%;
  animation: pulse-ring 2s infinite;
}

@keyframes pulse-ring {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.1); opacity: 0.5; }
}

.upload-text .main-text {
  font-size: 16px;
  color: #e2e8f0;
  margin: 0 0 8px 0;
}

.upload-text .main-text em {
  color: #5eead4;
  font-style: normal;
  font-weight: 600;
}

.upload-text .sub-text {
  font-size: 13px;
  color: #64748b;
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
  background: rgba(15, 23, 42, 0.8);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #5eead4;
  opacity: 0;
  transition: opacity 0.3s ease;
  gap: 8px;
}

.preview-overlay .el-icon {
  font-size: 32px;
}

.upload-preview:hover .preview-overlay {
  opacity: 1;
}

/* 操作按钮 */
.upload-actions {
  display: flex;
  gap: 12px;
}

.detect-btn {
  flex: 1;
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  border: none;
  height: 44px;
}

.detect-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(14, 165, 233, 0.4);
}

.detect-btn:disabled {
  background: rgba(100, 116, 139, 0.3);
}

.clear-btn {
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(94, 234, 212, 0.2);
  color: #94a3b8;
  height: 44px;
}

.clear-btn:hover {
  border-color: rgba(94, 234, 212, 0.4);
  color: #5eead4;
}

/* 提示面板 */
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
  color: #94a3b8;
  font-size: 13px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.1);
}

.tips-list li:last-child {
  border-bottom: none;
}

.tips-list .el-icon {
  color: #10b981;
  font-size: 16px;
}

/* 结果面板 */
.result-panel {
  margin-bottom: 0;
}

.result-tag {
  padding: 6px 16px;
  font-size: 14px;
  border-radius: 6px;
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
  background: rgba(15, 23, 42, 0.5);
}

.overview-item.alert {
  border: 1px solid rgba(16, 185, 129, 0.3);
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.1) 0%, rgba(16, 185, 129, 0.05) 100%);
}

.overview-item.drowsy {
  border: 1px solid rgba(239, 68, 68, 0.3);
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.1) 0%, rgba(239, 68, 68, 0.05) 100%);
}

.overview-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.overview-item.alert .overview-icon {
  background: rgba(16, 185, 129, 0.2);
  color: #34d399;
}

.overview-item.drowsy .overview-icon {
  background: rgba(239, 68, 68, 0.2);
  color: #f87171;
}

.overview-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.overview-label {
  font-size: 13px;
  color: #94a3b8;
}

.overview-value {
  font-size: 20px;
  font-weight: 700;
  color: #f8fafc;
}

.overview-item.drowsy .overview-value {
  color: #f87171;
}

.overview-item.alert .overview-value {
  color: #34d399;
}

/* 详细数据 */
.result-details {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.detail-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
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
}

.detail-value {
  flex: 1;
  color: #e2e8f0;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}

.detail-value .el-icon {
  color: #5eead4;
}

.detail-progress {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

:deep(.custom-progress) {
  flex: 1;
}

:deep(.custom-progress .el-progress-bar__outer) {
  background: rgba(148, 163, 184, 0.2);
  border-radius: 5px;
}

:deep(.custom-progress .el-progress-bar__inner) {
  border-radius: 5px;
}

.progress-text {
  min-width: 50px;
  text-align: right;
  font-weight: 600;
  color: #f8fafc;
}

/* 概率条 */
.prob-bar {
  flex: 1;
  height: 8px;
  background: rgba(148, 163, 184, 0.2);
  border-radius: 4px;
  position: relative;
  overflow: hidden;
}

.prob-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.8s ease;
}

.prob-fill.alert {
  background: linear-gradient(90deg, #10b981, #34d399);
}

.prob-fill.drowsy {
  background: linear-gradient(90deg, #ef4444, #f87171);
}

.prob-text {
  position: absolute;
  right: 0;
  top: -20px;
  font-size: 12px;
  font-weight: 600;
  color: #94a3b8;
}

.prob-text.danger {
  color: #f87171;
}

/* 结果图片 */
.result-image-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid rgba(148, 163, 184, 0.1);
}

.image-title {
  font-size: 14px;
  font-weight: 600;
  color: #f8fafc;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.image-wrapper {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(94, 234, 212, 0.2);
}

.image-wrapper img {
  width: 100%;
  display: block;
}

.image-overlay {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.7);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #5eead4;
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

/* 空状态 */
.empty-panel {
  min-height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-content {
  text-align: center;
}

.empty-icon-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-icon {
  font-size: 60px;
  color: #5eead4;
  z-index: 1;
}

.empty-ring {
  position: absolute;
  inset: 0;
  border: 2px solid rgba(94, 234, 212, 0.2);
  border-radius: 50%;
  animation: pulse-ring 3s infinite;
}

.empty-content h3 {
  font-size: 20px;
  color: #f8fafc;
  margin: 0 0 8px 0;
}

.empty-content p {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

/* 预览对话框 */
:deep(.preview-dialog) {
  background: rgba(15, 23, 42, 0.95);
}

:deep(.preview-dialog .el-dialog__header) {
  color: #f8fafc;
  border-bottom: 1px solid rgba(94, 234, 212, 0.1);
  margin-right: 0;
  padding: 20px;
}

:deep(.preview-dialog .el-dialog__body) {
  padding: 20px;
}

.preview-dialog-img {
  width: 100%;
  border-radius: 8px;
}

/* 响应式 */
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
    height: 220px;
  }

  .overview-item {
    flex-direction: column;
    text-align: center;
  }
}
</style>