<!--<template>-->
<!--  <div class="history-container">-->
<!--    <el-card>-->
<!--      <template #header>-->
<!--        <div class="card-header">-->
<!--          <span>检测记录</span>-->
<!--          <el-button type="primary" @click="loadData" :icon="'Refresh'">刷新</el-button>-->
<!--        </div>-->
<!--      </template>-->

<!--      <el-table :data="records" v-loading="loading" stripe style="width: 100%">-->
<!--        <el-table-column prop="recordId" label="记录ID" width="180" show-overflow-tooltip />-->
<!--        <el-table-column prop="createdAt" label="检测时间" width="180">-->
<!--          <template #default="scope">-->
<!--            {{ formatTime(scope.row.createdAt) }}-->
<!--          </template>-->
<!--        </el-table-column>-->
<!--        <el-table-column prop="resultClass" label="检测结果" width="120">-->
<!--          <template #default="scope">-->
<!--            <el-tag :type="scope.row.resultClass === 'drowsy' ? 'danger' : 'success'">-->
<!--              {{ scope.row.resultClass === 'drowsy' ? '疲劳' : '清醒' }}-->
<!--            </el-tag>-->
<!--          </template>-->
<!--        </el-table-column>-->
<!--        <el-table-column prop="confidence" label="置信度" width="120">-->
<!--          <template #default="scope">-->
<!--            <el-progress-->
<!--                :percentage="Math.round(scope.row.confidence * 100)"-->
<!--                :color="scope.row.resultClass === 'drowsy' ? '#f56c6c' : '#67c23a'"-->
<!--            />-->
<!--          </template>-->
<!--        </el-table-column>-->
<!--        <el-table-column prop="fileName" label="文件名" show-overflow-tooltip />-->
<!--        <el-table-column prop="processingTime" label="处理耗时" width="120">-->
<!--          <template #default="scope">-->
<!--            {{ scope.row.processingTime }}ms-->
<!--          </template>-->
<!--        </el-table-column>-->
<!--        <el-table-column label="操作" width="150" fixed="right">-->
<!--          <template #default="scope">-->
<!--            <el-button-->
<!--                v-if="scope.row.filePath"-->
<!--                type="primary"-->
<!--                link-->
<!--                @click="viewImage(scope.row.filePath)"-->
<!--            >-->
<!--              查看图片-->
<!--            </el-button>-->
<!--          </template>-->
<!--        </el-table-column>-->
<!--      </el-table>-->

<!--      <div class="pagination">-->
<!--        <el-pagination-->
<!--            v-model:current-page="page"-->
<!--            v-model:page-size="size"-->
<!--            :page-sizes="[10, 20, 50]"-->
<!--            :total="total"-->
<!--            layout="total, sizes, prev, pager, next"-->
<!--            @size-change="loadData"-->
<!--            @current-change="loadData"-->
<!--        />-->
<!--      </div>-->
<!--    </el-card>-->

<!--    &lt;!&ndash; 图片预览对话框 &ndash;&gt;-->
<!--    <el-dialog v-model="dialogVisible" title="检测结果图" width="60%">-->
<!--      <img v-if="currentImage" :src="currentImage" style="width: 100%;" />-->
<!--    </el-dialog>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup>-->
<!--import { ref, onMounted } from 'vue'-->
<!--import { getHistory } from '@/api'-->

<!--const records = ref([])-->
<!--const loading = ref(false)-->
<!--const page = ref(1)-->
<!--const size = ref(10)-->
<!--const total = ref(0)-->
<!--const dialogVisible = ref(false)-->
<!--const currentImage = ref('')-->

<!--const loadData = async () => {-->
<!--  loading.value = true-->
<!--  try {-->
<!--    const res = await getHistory('user001', page.value - 1, size.value)-->
<!--    if (res.data.code === 200) {-->
<!--      records.value = res.data.data.content-->
<!--      total.value = res.data.data.totalElements-->
<!--    }-->
<!--  } catch (error) {-->
<!--    console.error('加载失败:', error)-->
<!--  } finally {-->
<!--    loading.value = false-->
<!--  }-->
<!--}-->

<!--const formatTime = (time) => {-->
<!--  if (!time) return '-'-->
<!--  return new Date(time).toLocaleString()-->
<!--}-->

<!--const viewImage = (path) => {-->
<!--  currentImage.value = `/api/detection/image/result?path=${encodeURIComponent(path)}`-->
<!--  dialogVisible.value = true-->
<!--}-->

<!--onMounted(() => {-->
<!--  loadData()-->
<!--})-->
<!--</script>-->

<!--<style scoped>-->
<!--.history-container {-->
<!--  max-width: 1200px;-->
<!--  margin: 0 auto;-->
<!--}-->

<!--.card-header {-->
<!--  display: flex;-->
<!--  justify-content: space-between;-->
<!--  align-items: center;-->
<!--}-->

<!--.pagination {-->
<!--  margin-top: 20px;-->
<!--  text-align: right;-->
<!--}-->
<!--</style>-->
'<template>
  <div class="history-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-actions">
        <el-button type="primary" @click="loadData" :loading="loading" class="refresh-btn">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-button class="export-btn" @click="exportData">
          <el-icon><Download /></el-icon>
          导出记录
        </el-button>
      </div>
    </div>

    <!-- 统计概览 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="8">
        <div class="stat-item glass-effect">
          <div class="stat-icon-wrapper blue">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ total }}</span>
            <span class="stat-label">总记录数</span>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="stat-item glass-effect">
          <div class="stat-icon-wrapper green">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ alertCount }}</span>
            <span class="stat-label">清醒次数</span>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="stat-item glass-effect">
          <div class="stat-icon-wrapper red">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ drowsyCount }}</span>
            <span class="stat-label">疲劳次数</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <div class="table-panel glass-effect">
      <div class="table-header">
        <div class="search-box">
          <el-input
              v-model="searchKeyword"
              placeholder="搜索记录ID或文件名..."
              prefix-icon="Search"
              clearable
              class="search-input"
          />
        </div>
        <el-radio-group v-model="filterType" size="default">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="alert">清醒</el-radio-button>
          <el-radio-button label="drowsy">疲劳</el-radio-button>
        </el-radio-group>
      </div>

      <el-table
          :data="filteredRecords"
          v-loading="loading"
          style="width: 100%"
          :header-cell-style="headerStyle"
          :cell-style="cellStyle"
          class="custom-table"
          stripe
      >
        <el-table-column prop="recordId" label="记录ID" min-width="160" show-overflow-tooltip>
          <template #default="scope">
            <div class="id-cell">
              <el-icon><Document /></el-icon>
              <span>{{ scope.row.recordId }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="检测时间" width="180">
          <template #default="scope">
            <div class="time-cell">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTime(scope.row.createdAt) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="resultClass" label="检测结果" width="120">
          <template #default="scope">
            <el-tag
                :type="scope.row.resultClass === 'drowsy' ? 'danger' : 'success'"
                effect="light"
                class="result-tag"
            >
              <el-icon v-if="scope.row.resultClass === 'drowsy'"><Warning /></el-icon>
              <el-icon v-else><CircleCheck /></el-icon>
              {{ scope.row.resultClass === 'drowsy' ? '疲劳' : '清醒' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="confidence" label="置信度" width="160">
          <template #default="scope">
            <div class="confidence-cell">
              <el-progress
                  :percentage="Math.round(scope.row.confidence * 100)"
                  :color="scope.row.resultClass === 'drowsy' ? '#ef4444' : '#10b981'"
                  :stroke-width="6"
                  class="confidence-progress"
              />
              <span class="confidence-text">{{ (scope.row.confidence * 100).toFixed(1) }}%</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <div class="filename-cell">
              <el-icon><Document /></el-icon>
              <span>{{ scope.row.fileName || '-' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="processingTime" label="处理耗时" width="120">
          <template #default="scope">
            <div class="time-cost-cell">
              <el-icon><Timer /></el-icon>
              <span>{{ scope.row.processingTime }}ms</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button
                v-if="scope.row.filePath"
                type="primary"
                link
                class="view-btn"
                @click="viewImage(scope.row.filePath)"
            >
              <el-icon><View /></el-icon>
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
            v-model:current-page="page"
            v-model:page-size="size"
            :page-sizes="[10, 20, 50]"
            :total="filteredTotal"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
            class="custom-pagination"
            background
        />
      </div>
    </div>

    <!-- 图片预览对话框 -->
    <el-dialog
        v-model="dialogVisible"
        title="检测结果图"
        width="70%"
        class="preview-dialog"
        center
        destroy-on-close
    >
      <div class="preview-container">
        <img v-if="currentImage" :src="currentImage" class="preview-img" />
        <div class="preview-placeholder" v-else>
          <el-icon><Picture /></el-icon>
          <span>加载中...</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getHistory } from '@/api'
import {
  List, Refresh, Download, Document, CircleCheck, Warning,
  Clock, Timer, View, Picture, Search
} from '@element-plus/icons-vue'

const records = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const currentImage = ref('')
const searchKeyword = ref('')
const filterType = ref('all')

// 计算统计数据
const alertCount = computed(() => {
  return records.value.filter(r => r.resultClass === 'alert').length
})

const drowsyCount = computed(() => {
  return records.value.filter(r => r.resultClass === 'drowsy').length
})

// 过滤记录
const filteredRecords = computed(() => {
  let result = records.value

  // 按类型过滤
  if (filterType.value !== 'all') {
    result = result.filter(r => r.resultClass === filterType.value)
  }

  // 按关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(r =>
        r.recordId.toLowerCase().includes(keyword) ||
        (r.fileName && r.fileName.toLowerCase().includes(keyword))
    )
  }

  return result
})

const filteredTotal = computed(() => filteredRecords.value.length)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getHistory('user001', page.value - 1, size.value)
    if (res.data.code === 200) {
      records.value = res.data.data.content
      total.value = res.data.data.totalElements
    }
  } catch (error) {
    console.error('加载失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const viewImage = (path) => {
  currentImage.value = `/api/detection/image/result?path=${encodeURIComponent(path)}`
  dialogVisible.value = true
}

const handleSizeChange = (val) => {
  size.value = val
  loadData()
}

const handlePageChange = (val) => {
  page.value = val
  loadData()
}

const exportData = () => {
  ElMessage.success('导出功能开发中...')
}

// 表格样式 - 浅色主题
const headerStyle = () => ({
  background: 'rgba(248, 250, 252, 0.8)',
  color: '#475569',
  fontWeight: 600,
  fontSize: '13px',
  borderBottom: '1px solid rgba(148, 163, 184, 0.2)'
})

const cellStyle = () => ({
  background: 'transparent',
  color: '#334155',
  borderBottom: '1px solid rgba(148, 163, 184, 0.1)'
})

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.history-page {
  padding: 24px;
  color: #334155;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  min-height: 100%;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.refresh-btn {
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(14, 165, 233, 0.25);
}

.refresh-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(14, 165, 233, 0.35);
}

.export-btn {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.3);
  color: #64748b;
}

.export-btn:hover {
  border-color: #0ea5e9;
  color: #0ea5e9;
  background: rgba(14, 165, 233, 0.05);
}

/* 玻璃效果 - 浅色主题 */
.glass-effect {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(148, 163, 184, 0.15);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

/* 统计行 */
.stats-row {
  margin-bottom: 24px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  transition: all 0.3s ease;
}

.stat-item:hover {
  transform: translateY(-3px);
  border-color: rgba(14, 165, 233, 0.3);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
}

.stat-icon-wrapper {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 26px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon-wrapper.blue {
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
}

.stat-icon-wrapper.green {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.stat-icon-wrapper.red {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

/* 表格面板 */
.table-panel {
  padding: 24px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
}

.search-box {
  flex: 1;
  max-width: 320px;
}

:deep(.search-input .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.2);
  box-shadow: none;
  border-radius: 10px;
  padding: 4px 12px;
}

:deep(.search-input .el-input__wrapper:hover) {
  border-color: rgba(14, 165, 233, 0.4);
}

:deep(.search-input .el-input__inner) {
  color: #334155;
  font-size: 14px;
}

:deep(.search-input .el-input__icon) {
  color: #94a3b8;
}

/* 表格样式 - 浅色主题 */
:deep(.custom-table) {
  background: transparent;
  margin: 0 -24px;
  width: calc(100% + 48px) !important;
}

:deep(.custom-table .el-table__body) {
  background: transparent;
}

:deep(.custom-table tr:hover > td) {
  background: rgba(14, 165, 233, 0.04) !important;
}

.id-cell,
.time-cell,
.filename-cell,
.time-cost-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.id-cell {
  color: #475569;
  font-weight: 500;
}

.id-cell .el-icon,
.filename-cell .el-icon {
  color: #94a3b8;
}

.time-cell .el-icon,
.time-cost-cell .el-icon {
  color: #0ea5e9;
}

.time-cell span,
.time-cost-cell span {
  color: #64748b;
  font-family: 'Monaco', monospace;
}

.filename-cell span {
  color: #475569;
}

.result-tag {
  padding: 6px 12px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 12px;
  border: none;
}

.result-tag.el-tag--success {
  background: rgba(16, 185, 129, 0.1);
  color: #059669;
}

.result-tag.el-tag--danger {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
}

.confidence-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.confidence-progress {
  flex: 1;
}

:deep(.confidence-progress .el-progress-bar__outer) {
  background: rgba(148, 163, 184, 0.15);
  border-radius: 3px;
}

:deep(.confidence-progress .el-progress-bar__inner) {
  border-radius: 3px;
}

.confidence-text {
  min-width: 45px;
  text-align: right;
  font-weight: 600;
  font-size: 12px;
  color: #475569;
}

.view-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #0ea5e9;
  font-weight: 500;
}

.view-btn:hover {
  color: #0284c7;
}

/* 分页 - 浅色主题 */
.pagination-wrapper {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid rgba(148, 163, 184, 0.1);
  display: flex;
  justify-content: flex-end;
}

:deep(.custom-pagination) {
  --el-pagination-button-color: #64748b;
  --el-pagination-hover-color: #0ea5e9;
}

:deep(.custom-pagination .el-pagination__total) {
  color: #64748b;
  font-weight: 500;
}

:deep(.custom-pagination .el-pager li) {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 8px;
  margin: 0 4px;
  font-weight: 500;
}

:deep(.custom-pagination .el-pager li:hover) {
  border-color: #0ea5e9;
  color: #0ea5e9;
}

:deep(.custom-pagination .el-pager li.active) {
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  border-color: #0ea5e9;
  color: white;
  box-shadow: 0 4px 12px rgba(14, 165, 233, 0.3);
}

:deep(.custom-pagination .btn-prev),
:deep(.custom-pagination .btn-next) {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 8px;
}

:deep(.custom-pagination .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 8px;
}

/* 预览对话框 - 浅色主题 */
:deep(.preview-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.preview-dialog .el-dialog__header) {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  color: #1e293b;
  border-bottom: 1px solid rgba(148, 163, 184, 0.15);
  margin-right: 0;
  padding: 20px 24px;
  font-weight: 600;
}

:deep(.preview-dialog .el-dialog__body) {
  padding: 24px;
  background: #f8fafc;
}

.preview-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  background: white;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.1);
  padding: 20px;
}

.preview-img {
  max-width: 100%;
  max-height: 70vh;
  border-radius: 10px;
  border: 1px solid rgba(148, 163, 184, 0.1);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.preview-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #94a3b8;
}

.preview-placeholder .el-icon {
  font-size: 48px;
  opacity: 0.5;
}

/* 单选按钮组 - 浅色主题 */
:deep(.el-radio-button__inner) {
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(148, 163, 184, 0.2);
  color: #64748b;
  font-weight: 500;
  padding: 8px 20px;
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  border-color: #0ea5e9;
  color: white;
  box-shadow: 0 4px 12px rgba(14, 165, 233, 0.3);
}

:deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 10px 0 0 10px;
}

:deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 0 10px 10px 0;
}

/* 加载动画颜色 */
:deep(.el-loading-spinner .path) {
  stroke: #0ea5e9;
}

:deep(.el-loading-text) {
  color: #64748b;
}

/* 响应式 */
@media (max-width: 768px) {
  .history-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions {
    justify-content: flex-end;
  }

  .table-header {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    max-width: 100%;
  }

  .stat-item {
    margin-bottom: 12px;
  }
}
</style>