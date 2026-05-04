<!--<template>-->
<!--  <div class="app-layout">-->
<!--    &lt;!&ndash; 侧边栏 &ndash;&gt;-->
<!--    <aside class="sidebar" :class="{ 'collapsed': isCollapsed }">-->
<!--      <div class="sidebar-header">-->
<!--        <div class="logo">-->
<!--          <el-icon class="logo-icon"><Monitor /></el-icon>-->
<!--          <span class="logo-text" v-show="!isCollapsed">疲劳监测</span>-->
<!--        </div>-->
<!--        <el-button-->
<!--            class="collapse-btn"-->
<!--            :icon="isCollapsed ? Expand : Fold"-->
<!--            circle-->
<!--            @click="toggleSidebar"-->
<!--        />-->
<!--      </div>-->

<!--      <nav class="sidebar-nav">-->
<!--        <router-link-->
<!--            v-for="route in menuRoutes"-->
<!--            :key="route.path"-->
<!--            :to="route.path"-->
<!--            class="nav-item"-->
<!--            :class="{ active: $route.path === route.path }"-->
<!--        >-->
<!--          <el-icon class="nav-icon">-->
<!--            <component :is="route.meta.icon" />-->
<!--          </el-icon>-->
<!--          <span class="nav-text" v-show="!isCollapsed">{{ route.meta.title }}</span>-->
<!--          <div class="nav-glow"></div>-->
<!--        </router-link>-->
<!--      </nav>-->

<!--      <div class="sidebar-footer" v-show="!isCollapsed">-->
<!--        <div class="system-status">-->
<!--          <span class="status-dot" :class="systemStatus"></span>-->
<!--          <span class="status-text">{{ statusText }}</span>-->
<!--        </div>-->
<!--        <div class="version">v2.0.0</div>-->
<!--      </div>-->
<!--    </aside>-->

<!--    &lt;!&ndash; 主内容区 &ndash;&gt;-->
<!--    <main class="main-content">-->
<!--      &lt;!&ndash; 顶部栏 &ndash;&gt;-->
<!--      <header class="top-header glass-effect">-->
<!--        <div class="breadcrumb">-->
<!--          <el-icon class="breadcrumb-icon"><Location /></el-icon>-->
<!--          <span class="breadcrumb-current">{{ $route.meta.title }}</span>-->
<!--        </div>-->

<!--        <div class="header-actions">-->
<!--          <el-tooltip content="全屏" placement="bottom">-->
<!--            <el-button circle class="action-btn" @click="toggleFullscreen">-->
<!--              <el-icon><FullScreen /></el-icon>-->
<!--            </el-button>-->
<!--          </el-tooltip>-->

<!--          <el-tooltip content="通知" placement="bottom">-->
<!--            <el-button circle class="action-btn" @click="showNotifications">-->
<!--              <el-icon><Bell /></el-icon>-->
<!--              <span class="badge" v-if="notificationCount > 0">{{ notificationCount }}</span>-->
<!--            </el-button>-->
<!--          </el-tooltip>-->

<!--          <el-dropdown trigger="click">-->
<!--            <div class="user-info">-->
<!--              <el-avatar :size="32" class="user-avatar">-->
<!--                <el-icon><User /></el-icon>-->
<!--              </el-avatar>-->
<!--              <span class="user-name" v-if="!isMobile">管理员</span>-->
<!--              <el-icon><ArrowDown /></el-icon>-->
<!--            </div>-->
<!--            <template #dropdown>-->
<!--              <el-dropdown-menu class="custom-dropdown">-->
<!--                <el-dropdown-item @click="goToProfile">-->
<!--                  <el-icon><User /></el-icon>个人中心-->
<!--                </el-dropdown-item>-->
<!--                <el-dropdown-item @click="goToSettings">-->
<!--                  <el-icon><Setting /></el-icon>系统设置-->
<!--                </el-dropdown-item>-->
<!--                <el-dropdown-item divided @click="logout">-->
<!--                  <el-icon><SwitchButton /></el-icon>退出登录-->
<!--                </el-dropdown-item>-->
<!--              </el-dropdown-menu>-->
<!--            </template>-->
<!--          </el-dropdown>-->
<!--        </div>-->
<!--      </header>-->

<!--      &lt;!&ndash; 页面内容 &ndash;&gt;-->
<!--      <div class="page-container">-->
<!--        <router-view v-slot="{ Component }">-->
<!--          <transition name="page-slide" mode="out-in">-->
<!--            <component :is="Component" />-->
<!--          </transition>-->
<!--        </router-view>-->
<!--      </div>-->
<!--    </main>-->

<!--    &lt;!&ndash; 通知抽屉 &ndash;&gt;-->
<!--    <el-drawer-->
<!--        v-model="notificationVisible"-->
<!--        title="系统通知"-->
<!--        size="380px"-->
<!--        class="notification-drawer"-->
<!--    >-->
<!--      <div class="notification-list">-->
<!--        <div-->
<!--            v-for="(notice, index) in notifications"-->
<!--            :key="index"-->
<!--            class="notification-item"-->
<!--            :class="notice.type"-->
<!--        >-->
<!--          <div class="notice-icon">-->
<!--            <el-icon v-if="notice.type === 'warning'"><WarningFilled /></el-icon>-->
<!--            <el-icon v-else-if="notice.type === 'success'"><CircleCheckFilled /></el-icon>-->
<!--            <el-icon v-else><InfoFilled /></el-icon>-->
<!--          </div>-->
<!--          <div class="notice-content">-->
<!--            <p class="notice-title">{{ notice.title }}</p>-->
<!--            <p class="notice-desc">{{ notice.content }}</p>-->
<!--            <span class="notice-time">{{ notice.time }}</span>-->
<!--          </div>-->
<!--        </div>-->
<!--      </div>-->
<!--    </el-drawer>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup>-->
<!--import { ref, computed, onMounted, onUnmounted } from 'vue'-->
<!--import { useRoute, useRouter } from 'vue-router'-->
<!--import { ElMessage, ElMessageBox } from 'element-plus'-->
<!--import {-->
<!--  Monitor, Picture, VideoCamera, List, TrendCharts,-->
<!--  Expand, Fold, Location, FullScreen, Bell, User, ArrowDown,-->
<!--  Setting, SwitchButton, WarningFilled, CircleCheckFilled, InfoFilled-->
<!--} from '@element-plus/icons-vue'-->

<!--const route = useRoute()-->
<!--const router = useRouter()-->

<!--// 侧边栏状态-->
<!--const isCollapsed = ref(false)-->
<!--const isMobile = ref(false)-->

<!--// 系统状态-->
<!--const systemStatus = ref('running')-->
<!--const notificationCount = ref(3)-->
<!--const notificationVisible = ref(false)-->

<!--// 通知列表-->
<!--const notifications = ref([-->
<!--  {-->
<!--    type: 'warning',-->
<!--    title: '疲劳预警',-->
<!--    content: '检测到驾驶员出现疲劳状态，请及时提醒休息',-->
<!--    time: '5分钟前'-->
<!--  },-->
<!--  {-->
<!--    type: 'success',-->
<!--    title: '检测完成',-->
<!--    content: '今日检测任务已全部完成，系统运行正常',-->
<!--    time: '30分钟前'-->
<!--  },-->
<!--  {-->
<!--    type: 'info',-->
<!--    title: '系统更新',-->
<!--    content: '系统已自动更新至最新版本 v2.0.0',-->
<!--    time: '2小时前'-->
<!--  }-->
<!--])-->

<!--// 菜单路由-->
<!--const menuRoutes = [-->
<!--  { path: '/detect', meta: { title: '图片检测', icon: 'Picture' } },-->
<!--  { path: '/realtime', meta: { title: '实时监测', icon: 'VideoCamera' } },-->
<!--  { path: '/history', meta: { title: '检测记录', icon: 'List' } },-->
<!--  { path: '/statistics', meta: { title: '统计分析', icon: 'TrendCharts' } }-->
<!--]-->

<!--// 状态文本-->
<!--const statusText = computed(() => {-->
<!--  const map = { running: '运行中', stopped: '已停止', error: '异常' }-->
<!--  return map[systemStatus.value] || '未知'-->
<!--})-->

<!--// 切换侧边栏-->
<!--const toggleSidebar = () => {-->
<!--  isCollapsed.value = !isCollapsed.value-->
<!--}-->

<!--// 监听窗口大小-->
<!--const checkMobile = () => {-->
<!--  isMobile.value = window.innerWidth < 768-->
<!--  if (isMobile.value) {-->
<!--    isCollapsed.value = true-->
<!--  }-->
<!--}-->

<!--// 全屏切换-->
<!--const toggleFullscreen = () => {-->
<!--  if (!document.fullscreenElement) {-->
<!--    document.documentElement.requestFullscreen()-->
<!--  } else {-->
<!--    document.exitFullscreen()-->
<!--  }-->
<!--}-->

<!--// 显示通知-->
<!--const showNotifications = () => {-->
<!--  notificationVisible.value = true-->
<!--  notificationCount.value = 0-->
<!--}-->

<!--// 个人中心-->
<!--const goToProfile = () => {-->
<!--  ElMessage.info('个人中心功能开发中')-->
<!--}-->

<!--// 系统设置-->
<!--const goToSettings = () => {-->
<!--  ElMessage.info('系统设置功能开发中')-->
<!--}-->

<!--// 退出登录-->
<!--const logout = () => {-->
<!--  ElMessageBox.confirm('确定要退出登录吗？', '提示', {-->
<!--    confirmButtonText: '确定',-->
<!--    cancelButtonText: '取消',-->
<!--    type: 'warning'-->
<!--  }).then(() => {-->
<!--    ElMessage.success('已退出登录')-->
<!--    router.push('/login')-->
<!--  }).catch(() => {})-->
<!--}-->

<!--onMounted(() => {-->
<!--  checkMobile()-->
<!--  window.addEventListener('resize', checkMobile)-->
<!--})-->

<!--onUnmounted(() => {-->
<!--  window.removeEventListener('resize', checkMobile)-->
<!--})-->
<!--</script>-->

<!--<style scoped>-->
<!--.app-layout {-->
<!--  display: flex;-->
<!--  min-height: 100vh;-->
<!--  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #0f172a 100%);-->
<!--}-->

<!--/* 侧边栏 */-->
<!--.sidebar {-->
<!--  width: 260px;-->
<!--  background: rgba(15, 23, 42, 0.95);-->
<!--  backdrop-filter: blur(20px);-->
<!--  border-right: 1px solid rgba(94, 234, 212, 0.1);-->
<!--  display: flex;-->
<!--  flex-direction: column;-->
<!--  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);-->
<!--  position: fixed;-->
<!--  height: 100vh;-->
<!--  z-index: 100;-->
<!--}-->

<!--.sidebar.collapsed {-->
<!--  width: 80px;-->
<!--}-->

<!--.sidebar-header {-->
<!--  padding: 20px;-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  justify-content: space-between;-->
<!--  border-bottom: 1px solid rgba(94, 234, 212, 0.1);-->
<!--}-->

<!--.logo {-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  gap: 12px;-->
<!--}-->

<!--.logo-icon {-->
<!--  font-size: 28px;-->
<!--  color: #5eead4;-->
<!--  animation: pulse 2s infinite;-->
<!--}-->

<!--@keyframes pulse {-->
<!--  0%, 100% { opacity: 1; }-->
<!--  50% { opacity: 0.7; }-->
<!--}-->

<!--.logo-text {-->
<!--  font-size: 18px;-->
<!--  font-weight: 700;-->
<!--  background: linear-gradient(135deg, #5eead4 0%, #0ea5e9 100%);-->
<!--  -webkit-background-clip: text;-->
<!--  -webkit-text-fill-color: transparent;-->
<!--  white-space: nowrap;-->
<!--}-->

<!--.collapse-btn {-->
<!--  background: rgba(94, 234, 212, 0.1);-->
<!--  border: 1px solid rgba(94, 234, 212, 0.2);-->
<!--  color: #5eead4;-->
<!--  transition: all 0.3s ease;-->
<!--}-->

<!--.collapse-btn:hover {-->
<!--  background: rgba(94, 234, 212, 0.2);-->
<!--  transform: scale(1.1);-->
<!--}-->

<!--/* 导航菜单 */-->
<!--.sidebar-nav {-->
<!--  flex: 1;-->
<!--  padding: 16px 12px;-->
<!--  display: flex;-->
<!--  flex-direction: column;-->
<!--  gap: 8px;-->
<!--}-->

<!--.nav-item {-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  gap: 12px;-->
<!--  padding: 14px 16px;-->
<!--  border-radius: 12px;-->
<!--  color: #94a3b8;-->
<!--  text-decoration: none;-->
<!--  transition: all 0.3s ease;-->
<!--  position: relative;-->
<!--  overflow: hidden;-->
<!--}-->

<!--.nav-item:hover {-->
<!--  background: rgba(94, 234, 212, 0.05);-->
<!--  color: #e2e8f0;-->
<!--  transform: translateX(4px);-->
<!--}-->

<!--.nav-item.active {-->
<!--  background: linear-gradient(135deg, rgba(14, 165, 233, 0.2) 0%, rgba(94, 234, 212, 0.1) 100%);-->
<!--  color: #5eead4;-->
<!--  border: 1px solid rgba(94, 234, 212, 0.2);-->
<!--}-->

<!--.nav-item.active .nav-glow {-->
<!--  opacity: 1;-->
<!--}-->

<!--.nav-icon {-->
<!--  font-size: 20px;-->
<!--  min-width: 20px;-->
<!--}-->

<!--.nav-text {-->
<!--  font-size: 14px;-->
<!--  font-weight: 500;-->
<!--  white-space: nowrap;-->
<!--}-->

<!--.nav-glow {-->
<!--  position: absolute;-->
<!--  right: -20px;-->
<!--  top: 50%;-->
<!--  transform: translateY(-50%);-->
<!--  width: 40px;-->
<!--  height: 40px;-->
<!--  background: radial-gradient(circle, rgba(94, 234, 212, 0.3) 0%, transparent 70%);-->
<!--  opacity: 0;-->
<!--  transition: opacity 0.3s ease;-->
<!--}-->

<!--/* 侧边栏底部 */-->
<!--.sidebar-footer {-->
<!--  padding: 16px;-->
<!--  border-top: 1px solid rgba(94, 234, 212, 0.1);-->
<!--}-->

<!--.system-status {-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  gap: 8px;-->
<!--  margin-bottom: 8px;-->
<!--}-->

<!--.status-dot {-->
<!--  width: 8px;-->
<!--  height: 8px;-->
<!--  border-radius: 50%;-->
<!--  animation: blink 2s infinite;-->
<!--}-->

<!--.status-dot.running {-->
<!--  background: #10b981;-->
<!--  box-shadow: 0 0 8px #10b981;-->
<!--}-->

<!--.status-dot.stopped {-->
<!--  background: #64748b;-->
<!--}-->

<!--.status-dot.error {-->
<!--  background: #ef4444;-->
<!--  box-shadow: 0 0 8px #ef4444;-->
<!--}-->

<!--@keyframes blink {-->
<!--  0%, 100% { opacity: 1; }-->
<!--  50% { opacity: 0.5; }-->
<!--}-->

<!--.status-text {-->
<!--  font-size: 12px;-->
<!--  color: #64748b;-->
<!--}-->

<!--.version {-->
<!--  font-size: 11px;-->
<!--  color: #475569;-->
<!--  text-align: center;-->
<!--}-->

<!--/* 主内容区 */-->
<!--.main-content {-->
<!--  flex: 1;-->
<!--  margin-left: 260px;-->
<!--  display: flex;-->
<!--  flex-direction: column;-->
<!--  transition: margin-left 0.3s ease;-->
<!--  min-height: 100vh;-->
<!--}-->

<!--.sidebar.collapsed + .main-content {-->
<!--  margin-left: 80px;-->
<!--}-->

<!--/* 顶部栏 */-->
<!--.top-header {-->
<!--  height: 64px;-->
<!--  padding: 0 24px;-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  justify-content: space-between;-->
<!--  position: sticky;-->
<!--  top: 0;-->
<!--  z-index: 50;-->
<!--  border-bottom: 1px solid rgba(94, 234, 212, 0.1);-->
<!--}-->

<!--.glass-effect {-->
<!--  background: rgba(30, 41, 59, 0.7);-->
<!--  backdrop-filter: blur(12px);-->
<!--}-->

<!--.breadcrumb {-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  gap: 8px;-->
<!--  color: #94a3b8;-->
<!--}-->

<!--.breadcrumb-icon {-->
<!--  font-size: 16px;-->
<!--  color: #5eead4;-->
<!--}-->

<!--.breadcrumb-current {-->
<!--  font-size: 16px;-->
<!--  font-weight: 600;-->
<!--  color: #f8fafc;-->
<!--}-->

<!--.header-actions {-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  gap: 12px;-->
<!--}-->

<!--.action-btn {-->
<!--  background: rgba(15, 23, 42, 0.5);-->
<!--  border: 1px solid rgba(94, 234, 212, 0.2);-->
<!--  color: #94a3b8;-->
<!--  position: relative;-->
<!--  transition: all 0.3s ease;-->
<!--}-->

<!--.action-btn:hover {-->
<!--  background: rgba(94, 234, 212, 0.1);-->
<!--  border-color: rgba(94, 234, 212, 0.4);-->
<!--  color: #5eead4;-->
<!--  transform: scale(1.05);-->
<!--}-->

<!--.badge {-->
<!--  position: absolute;-->
<!--  top: -4px;-->
<!--  right: -4px;-->
<!--  width: 18px;-->
<!--  height: 18px;-->
<!--  background: #ef4444;-->
<!--  border-radius: 50%;-->
<!--  font-size: 11px;-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  justify-content: center;-->
<!--  color: white;-->
<!--  animation: shake 0.5s ease-in-out;-->
<!--}-->

<!--@keyframes shake {-->
<!--  0%, 100% { transform: translateX(0); }-->
<!--  25% { transform: translateX(-3px); }-->
<!--  75% { transform: translateX(3px); }-->
<!--}-->

<!--.user-info {-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  gap: 8px;-->
<!--  padding: 6px 12px;-->
<!--  border-radius: 8px;-->
<!--  cursor: pointer;-->
<!--  transition: all 0.3s ease;-->
<!--}-->

<!--.user-info:hover {-->
<!--  background: rgba(94, 234, 212, 0.1);-->
<!--}-->

<!--.user-avatar {-->
<!--  background: linear-gradient(135deg, #0ea5e9 0%, #5eead4 100%);-->
<!--  color: white;-->
<!--}-->

<!--.user-name {-->
<!--  font-size: 14px;-->
<!--  color: #e2e8f0;-->
<!--  font-weight: 500;-->
<!--}-->

<!--/* 页面容器 */-->
<!--.page-container {-->
<!--  flex: 1;-->
<!--  padding: 24px;-->
<!--  overflow-y: auto;-->
<!--}-->

<!--/* 页面切换动画 */-->
<!--.page-slide-enter-active,-->
<!--.page-slide-leave-active {-->
<!--  transition: all 0.3s ease;-->
<!--}-->

<!--.page-slide-enter-from {-->
<!--  opacity: 0;-->
<!--  transform: translateX(20px);-->
<!--}-->

<!--.page-slide-leave-to {-->
<!--  opacity: 0;-->
<!--  transform: translateX(-20px);-->
<!--}-->

<!--/* 通知抽屉 */-->
<!--:deep(.notification-drawer) {-->
<!--  background: rgba(15, 23, 42, 0.98);-->
<!--}-->

<!--:deep(.notification-drawer .el-drawer__header) {-->
<!--  color: #f8fafc;-->
<!--  border-bottom: 1px solid rgba(94, 234, 212, 0.1);-->
<!--  padding: 20px;-->
<!--}-->

<!--.notification-list {-->
<!--  display: flex;-->
<!--  flex-direction: column;-->
<!--  gap: 12px;-->
<!--  padding: 16px;-->
<!--}-->

<!--.notification-item {-->
<!--  display: flex;-->
<!--  gap: 12px;-->
<!--  padding: 16px;-->
<!--  background: rgba(30, 41, 59, 0.6);-->
<!--  border-radius: 12px;-->
<!--  border-left: 3px solid;-->
<!--  transition: all 0.3s ease;-->
<!--}-->

<!--.notification-item:hover {-->
<!--  background: rgba(30, 41, 59, 0.8);-->
<!--  transform: translateX(4px);-->
<!--}-->

<!--.notification-item.warning {-->
<!--  border-left-color: #f59e0b;-->
<!--}-->

<!--.notification-item.success {-->
<!--  border-left-color: #10b981;-->
<!--}-->

<!--.notification-item.info {-->
<!--  border-left-color: #0ea5e9;-->
<!--}-->

<!--.notice-icon {-->
<!--  width: 40px;-->
<!--  height: 40px;-->
<!--  border-radius: 10px;-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  justify-content: center;-->
<!--  font-size: 20px;-->
<!--  flex-shrink: 0;-->
<!--}-->

<!--.notification-item.warning .notice-icon {-->
<!--  background: rgba(245, 158, 11, 0.2);-->
<!--  color: #fbbf24;-->
<!--}-->

<!--.notification-item.success .notice-icon {-->
<!--  background: rgba(16, 185, 129, 0.2);-->
<!--  color: #34d399;-->
<!--}-->

<!--.notification-item.info .notice-icon {-->
<!--  background: rgba(14, 165, 233, 0.2);-->
<!--  color: #38bdf8;-->
<!--}-->

<!--.notice-content {-->
<!--  flex: 1;-->
<!--}-->

<!--.notice-title {-->
<!--  font-size: 14px;-->
<!--  font-weight: 600;-->
<!--  color: #f8fafc;-->
<!--  margin: 0 0 4px 0;-->
<!--}-->

<!--.notice-desc {-->
<!--  font-size: 13px;-->
<!--  color: #94a3b8;-->
<!--  margin: 0 0 8px 0;-->
<!--  line-height: 1.5;-->
<!--}-->

<!--.notice-time {-->
<!--  font-size: 12px;-->
<!--  color: #64748b;-->
<!--}-->

<!--/* 下拉菜单 */-->
<!--:deep(.custom-dropdown) {-->
<!--  background: rgba(30, 41, 59, 0.95);-->
<!--  border: 1px solid rgba(94, 234, 212, 0.2);-->
<!--  backdrop-filter: blur(10px);-->
<!--}-->

<!--:deep(.custom-dropdown .el-dropdown-menu__item) {-->
<!--  color: #94a3b8;-->
<!--  padding: 10px 16px;-->
<!--}-->

<!--:deep(.custom-dropdown .el-dropdown-menu__item:hover) {-->
<!--  background: rgba(94, 234, 212, 0.1);-->
<!--  color: #5eead4;-->
<!--}-->

<!--:deep(.custom-dropdown .el-dropdown-menu__item .el-icon) {-->
<!--  margin-right: 8px;-->
<!--}-->

<!--/* 响应式 */-->
<!--@media (max-width: 768px) {-->
<!--  .sidebar {-->
<!--    width: 80px;-->
<!--  }-->

<!--  .sidebar .logo-text,-->
<!--  .sidebar .nav-text,-->
<!--  .sidebar .sidebar-footer {-->
<!--    display: none;-->
<!--  }-->

<!--  .main-content {-->
<!--    margin-left: 80px;-->
<!--  }-->

<!--  .page-container {-->
<!--    padding: 16px;-->
<!--  }-->

<!--  .user-name {-->
<!--    display: none;-->
<!--  }-->
<!--}-->
<!--</style>-->


<template>
  <div class="app-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ 'collapsed': isCollapsed }">
      <div class="sidebar-header">
        <div class="logo">
          <el-icon class="logo-icon"><Monitor /></el-icon>
          <span class="logo-text" v-show="!isCollapsed">疲劳监测</span>
        </div>
        <el-button
            class="collapse-btn"
            :icon="isCollapsed ? Expand : Fold"
            circle
            @click="toggleSidebar"
        />
      </div>

      <nav class="sidebar-nav">
        <router-link
            v-for="route in menuRoutes"
            :key="route.path"
            :to="route.path"
            class="nav-item"
            :class="{ active: $route.path === route.path }"
        >
          <el-icon class="nav-icon">
            <component :is="route.meta.icon" />
          </el-icon>
          <span class="nav-text" v-show="!isCollapsed">{{ route.meta.title }}</span>
          <div class="nav-glow"></div>
        </router-link>
      </nav>

      <div class="sidebar-footer" v-show="!isCollapsed">
        <div class="system-status">
          <span class="status-dot" :class="systemStatus"></span>
          <span class="status-text">{{ statusText }}</span>
        </div>
        <div class="version">v2.0.0</div>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 顶部栏 -->
      <header class="top-header glass-effect">
        <div class="breadcrumb">
          <el-icon class="breadcrumb-icon"><Location /></el-icon>
          <span class="breadcrumb-current">{{ $route.meta.title }}</span>
        </div>

        <div class="header-actions">
          <el-tooltip content="全屏" placement="bottom">
            <el-button circle class="action-btn" @click="toggleFullscreen">
              <el-icon><FullScreen /></el-icon>
            </el-button>
          </el-tooltip>

          <el-tooltip content="通知" placement="bottom">
            <el-button circle class="action-btn" @click="showNotifications">
              <el-icon><Bell /></el-icon>
              <span class="badge" v-if="notificationCount > 0">{{ notificationCount }}</span>
            </el-button>
          </el-tooltip>

          <el-dropdown trigger="click">
            <div class="user-info">
              <el-avatar :size="32" class="user-avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="user-name" v-if="!isMobile">管理员</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="custom-dropdown">
                <el-dropdown-item @click="goToProfile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item @click="goToSettings">
                  <el-icon><Setting /></el-icon>系统设置
                </el-dropdown-item>
                <el-dropdown-item divided @click="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 页面内容 -->
      <div class="page-container">
        <router-view v-slot="{ Component }">
          <transition name="page-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>

    <!-- 通知抽屉 -->
    <el-drawer
        v-model="notificationVisible"
        title="系统通知"
        size="380px"
        class="notification-drawer"
    >
      <div class="notification-list">
        <div
            v-for="(notice, index) in notifications"
            :key="index"
            class="notification-item"
            :class="notice.type"
        >
          <div class="notice-icon">
            <el-icon v-if="notice.type === 'warning'"><WarningFilled /></el-icon>
            <el-icon v-else-if="notice.type === 'success'"><CircleCheckFilled /></el-icon>
            <el-icon v-else><InfoFilled /></el-icon>
          </div>
          <div class="notice-content">
            <p class="notice-title">{{ notice.title }}</p>
            <p class="notice-desc">{{ notice.content }}</p>
            <span class="notice-time">{{ notice.time }}</span>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Monitor, Picture, VideoCamera, List, TrendCharts,Film,
  Expand, Fold, Location, FullScreen, Bell, User, ArrowDown,
  Setting, SwitchButton, WarningFilled, CircleCheckFilled, InfoFilled
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// 侧边栏状态
const isCollapsed = ref(false)
const isMobile = ref(false)

// 系统状态
const systemStatus = ref('running')
const notificationCount = ref(3)
const notificationVisible = ref(false)

// 通知列表
const notifications = ref([
  {
    type: 'warning',
    title: '疲劳预警',
    content: '检测到驾驶员出现疲劳状态，请及时提醒休息',
    time: '5分钟前'
  },
  {
    type: 'success',
    title: '检测完成',
    content: '今日检测任务已全部完成，系统运行正常',
    time: '30分钟前'
  },
  {
    type: 'info',
    title: '系统更新',
    content: '系统已自动更新至最新版本 v2.0.0',
    time: '2小时前'
  }
])

// 菜单路由
const menuRoutes = [
  { path: '/detect', meta: { title: '图片检测', icon: 'Picture' } },
  { path: '/video', meta: { title: '视频检测', icon: 'Film' } },
  { path: '/realtime', meta: { title: '实时监测', icon: 'VideoCamera' } },
  { path: '/history', meta: { title: '检测记录', icon: 'List' } },
  { path: '/statistics', meta: { title: '统计分析', icon: 'TrendCharts' } }
]

// 状态文本
const statusText = computed(() => {
  const map = { running: '运行中', stopped: '已停止', error: '异常' }
  return map[systemStatus.value] || '未知'
})

// 切换侧边栏
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

// 监听窗口大小
const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
  if (isMobile.value) {
    isCollapsed.value = true
  }
}

// 全屏切换
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

// 显示通知
const showNotifications = () => {
  notificationVisible.value = true
  notificationCount.value = 0
}

// 个人中心
const goToProfile = () => {
  ElMessage.info('个人中心功能开发中')
}

// 系统设置
const goToSettings = () => {
  ElMessage.info('系统设置功能开发中')
}

// 退出登录
const logout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 50%, #f1f5f9 100%);
}

/* 侧边栏 - 浅色主题 */
.sidebar {
  width: 260px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-right: 1px solid rgba(148, 163, 184, 0.2);
  display: flex;
  flex-direction: column;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: fixed;
  height: 100vh;
  z-index: 100;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.05);
}

.sidebar.collapsed {
  width: 80px;
}

.sidebar-header {
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(148, 163, 184, 0.15);
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 28px;
  color: #0ea5e9;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.05); }
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  background: linear-gradient(135deg, #0ea5e9 0%, #06b6d4 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  white-space: nowrap;
}

.collapse-btn {
  background: rgba(14, 165, 233, 0.1) !important;
  border: none !important;
  color: #0ea5e9 !important;
  transition: all 0.3s ease;
}

.collapse-btn:hover {
  background: rgba(14, 165, 233, 0.2) !important;
  transform: rotate(180deg);
}

/* 导航菜单 - 浅色主题 */
.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  margin-bottom: 8px;
  border-radius: 12px;
  color: #64748b;
  text-decoration: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.nav-item:hover {
  background: rgba(14, 165, 233, 0.08);
  color: #0ea5e9;
  transform: translateX(4px);
}

.nav-item.active {
  background: linear-gradient(135deg, #0ea5e9 0%, #06b6d4 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(14, 165, 233, 0.3);
}

.nav-icon {
  font-size: 20px;
  flex-shrink: 0;
  transition: transform 0.3s ease;
}

.nav-item:hover .nav-icon {
  transform: scale(1.1);
}

.nav-text {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
}

.nav-glow {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at center, rgba(14, 165, 233, 0.15) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.nav-item:hover .nav-glow {
  opacity: 1;
}

/* 侧边栏底部 - 浅色主题 */
.sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid rgba(148, 163, 184, 0.15);
}

.system-status {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  animation: blink 2s infinite;
}

.status-dot.running {
  background: #10b981;
  box-shadow: 0 0 8px rgba(16, 185, 129, 0.5);
}

.status-dot.stopped {
  background: #f59e0b;
  animation: none;
}

.status-dot.error {
  background: #ef4444;
  box-shadow: 0 0 8px rgba(239, 68, 68, 0.5);
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.status-text {
  font-size: 12px;
  color: #64748b;
}

.version {
  font-size: 11px;
  color: #94a3b8;
  text-align: center;
}

/* 主内容区 */
.main-content {
  flex: 1;
  margin-left: 260px;
  display: flex;
  flex-direction: column;
  transition: margin-left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar.collapsed + .main-content {
  margin-left: 80px;
}

/* 顶部栏 - 浅色毛玻璃效果 */
.top-header {
  height: 64px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(148, 163, 184, 0.15);
  position: sticky;
  top: 0;
  z-index: 50;
}

.glass-effect {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
}

.breadcrumb-icon {
  color: #0ea5e9;
  font-size: 18px;
}

.breadcrumb-current {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.action-btn {
  background: rgba(255, 255, 255, 0.8) !important;
  border: 1px solid rgba(148, 163, 184, 0.2) !important;
  color: #64748b !important;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: rgba(14, 165, 233, 0.1) !important;
  border-color: rgba(14, 165, 233, 0.3) !important;
  color: #0ea5e9 !important;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(14, 165, 233, 0.15);
}

.badge {
  position: absolute;
  top: -2px;
  right: -2px;
  background: linear-gradient(135deg, #ef4444 0%, #f97316 100%);
  color: white;
  font-size: 10px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 10px;
  border: 2px solid white;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.2);
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-info:hover {
  background: rgba(14, 165, 233, 0.08);
  border-color: rgba(14, 165, 233, 0.3);
  box-shadow: 0 4px 12px rgba(14, 165, 233, 0.1);
}

.user-avatar {
  background: linear-gradient(135deg, #0ea5e9 0%, #06b6d4 100%) !important;
  color: white !important;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
}

/* 页面容器 */
.page-container {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

/* 页面切换动画 */
.page-slide-enter-active,
.page-slide-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.page-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* 通知抽屉 - 浅色主题 */
.notification-drawer :deep(.el-drawer__header) {
  background: linear-gradient(135deg, #0ea5e9 0%, #06b6d4 100%);
  color: white;
  padding: 20px 24px;
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.notification-drawer :deep(.el-drawer__close-btn) {
  color: white;
}

.notification-list {
  padding: 16px;
}

.notification-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  margin-bottom: 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.1);
  transition: all 0.3s ease;
}

.notification-item:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.notification-item.warning {
  border-left: 4px solid #f59e0b;
  background: linear-gradient(90deg, rgba(245, 158, 11, 0.05) 0%, rgba(255, 255, 255, 0.8) 100%);
}

.notification-item.success {
  border-left: 4px solid #10b981;
  background: linear-gradient(90deg, rgba(16, 185, 129, 0.05) 0%, rgba(255, 255, 255, 0.8) 100%);
}

.notification-item.info {
  border-left: 4px solid #0ea5e9;
  background: linear-gradient(90deg, rgba(14, 165, 233, 0.05) 0%, rgba(255, 255, 255, 0.8) 100%);
}

.notice-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notification-item.warning .notice-icon {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.notification-item.success .notice-icon {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.notification-item.info .notice-icon {
  background: rgba(14, 165, 233, 0.1);
  color: #0ea5e9;
}

.notice-content {
  flex: 1;
}

.notice-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 4px 0;
}

.notice-desc {
  font-size: 13px;
  color: #64748b;
  margin: 0 0 8px 0;
  line-height: 1.5;
}

.notice-time {
  font-size: 11px;
  color: #94a3b8;
}

/* 下拉菜单 - 浅色主题 */
.custom-dropdown {
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(148, 163, 184, 0.15);
  padding: 8px;
}

.custom-dropdown :deep(.el-dropdown-menu__item) {
  border-radius: 8px;
  padding: 10px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #475569;
  transition: all 0.2s ease;
}

.custom-dropdown :deep(.el-dropdown-menu__item:hover) {
  background: rgba(14, 165, 233, 0.08);
  color: #0ea5e9;
}

.custom-dropdown :deep(.el-dropdown-menu__item .el-icon) {
  font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
    box-shadow: 4px 0 24px rgba(0, 0, 0, 0.1);
  }

  .sidebar:not(.collapsed) {
    transform: translateX(0);
  }

  .main-content {
    margin-left: 0;
  }

  .sidebar.collapsed + .main-content {
    margin-left: 0;
  }
}

/* 滚动条美化 - 浅色主题 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: rgba(148, 163, 184, 0.1);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.3);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(148, 163, 184, 0.5);
}
</style>