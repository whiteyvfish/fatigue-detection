<script setup>
</script>

<template>
  <router-view />
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  margin: 0;
  padding: 0;
}

body {
  font-family: 'Inter', 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
  'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 50%, #f8fafc 100%);
  color: #334155;
}

/* 全局 Element Plus 颜色覆盖 - 浅色主题 */
:root {
  --el-color-primary: #0ea5e9;
  --el-color-primary-light-3: rgba(14, 165, 233, 0.3);
  --el-color-primary-light-5: rgba(14, 165, 233, 0.5);
  --el-color-primary-light-7: rgba(14, 165, 233, 0.1);
  --el-color-primary-light-9: rgba(14, 165, 233, 0.05);
  --el-color-success: #10b981;
  --el-color-warning: #f59e0b;
  --el-color-danger: #ef4444;
  --el-color-info: #64748b;
  --el-bg-color: #ffffff;
  --el-bg-color-overlay: #ffffff;
  --el-text-color-primary: #1e293b;
  --el-text-color-regular: #475569;
  --el-text-color-secondary: #64748b;
  --el-border-color: rgba(148, 163, 184, 0.2);
  --el-border-color-light: rgba(148, 163, 184, 0.12);
  --el-fill-color: rgba(248, 250, 252, 0.8);
  --el-fill-color-light: rgba(248, 250, 252, 0.5);
}

/* 滚动条美化 */
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
