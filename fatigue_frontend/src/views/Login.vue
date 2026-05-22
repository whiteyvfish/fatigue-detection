<template>
  <div class="login-page">
    <div class="login-card">
      <!-- 左侧品牌区 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="brand-icon">
            <el-icon><Monitor /></el-icon>
          </div>
          <h1 class="brand-title">疲劳检测系统</h1>
          <p class="brand-desc">AI 智能驾驶疲劳监测平台</p>
          <div class="brand-features">
            <div class="feature-item">
              <el-icon><PictureFilled /></el-icon>
              <span>图片智能检测</span>
            </div>
            <div class="feature-item">
              <el-icon><VideoCamera /></el-icon>
              <span>实时视频监测</span>
            </div>
            <div class="feature-item">
              <el-icon><DataAnalysis /></el-icon>
              <span>数据统计分析</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="form-section">
        <div class="form-header">
          <h2>{{ isRegister ? '创建账号' : '欢迎回来' }}</h2>
          <p>{{ isRegister ? '注册新账号以使用系统' : '登录您的账号' }}</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="login-form"
          @submit.prevent="handleSubmit"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              size="large"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <template v-if="isRegister">
            <el-form-item prop="nickname">
              <el-input
                v-model="form.nickname"
                placeholder="昵称（选填）"
                size="large"
                :prefix-icon="UserFilled"
              />
            </el-form-item>

            <el-form-item prop="email">
              <el-input
                v-model="form.email"
                placeholder="邮箱（选填）"
                size="large"
                :prefix-icon="Message"
              />
            </el-form-item>

            <el-form-item prop="phone">
              <el-input
                v-model="form.phone"
                placeholder="手机号（选填）"
                size="large"
                :prefix-icon="Phone"
              />
            </el-form-item>
          </template>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="loading"
              @click="handleSubmit"
            >
              {{ loading ? '处理中...' : (isRegister ? '注册' : '登录') }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <span>{{ isRegister ? '已有账号？' : '没有账号？' }}</span>
          <el-button link type="primary" @click="toggleMode">
            {{ isRegister ? '立即登录' : '免费注册' }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '@/api'
import { User, Lock, UserFilled, Message, Phone, Monitor, PictureFilled, VideoCamera, DataAnalysis } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const isRegister = ref(false)

const form = reactive({
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 64, message: '用户名长度3-64位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

const toggleMode = () => {
  isRegister.value = !isRegister.value
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      let res
      if (isRegister.value) {
        res = await register({
          username: form.username,
          password: form.password,
          nickname: form.nickname || undefined,
          email: form.email || undefined,
          phone: form.phone || undefined
        })
      } else {
        res = await login({
          username: form.username,
          password: form.password
        })
      }

      if (res.data.code === 200) {
        const data = res.data.data
        localStorage.setItem('token', data.token)
        localStorage.setItem('user', JSON.stringify(data))
        ElMessage.success(isRegister.value ? '注册成功' : '登录成功')
        router.push('/detect')
      } else {
        ElMessage.error(res.data.message || '操作失败')
      }
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '网络错误，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e0f2fe 0%, #f0f9ff 50%, #ecfeff 100%);
  padding: 24px;
}

.login-card {
  display: flex;
  width: 900px;
  max-width: 100%;
  min-height: 560px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

/* 左侧品牌区 */
.brand-section {
  flex: 1;
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 50%, #0369a1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 36px;
  color: white;
}

.brand-content {
  text-align: center;
}

.brand-icon {
  width: 72px;
  height: 72px;
  margin: 0 auto 20px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  backdrop-filter: blur(10px);
}

.brand-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 8px;
}

.brand-desc {
  font-size: 14px;
  opacity: 0.85;
  margin: 0 0 36px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 14px;
  text-align: left;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  opacity: 0.9;
}

.feature-item .el-icon {
  font-size: 20px;
  flex-shrink: 0;
}

/* 右侧表单区 */
.form-section {
  flex: 1;
  padding: 48px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  margin-bottom: 32px;
}

.form-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 6px;
}

.form-header p {
  font-size: 14px;
  color: #94a3b8;
  margin: 0;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 4px 12px;
  background: #f8fafc;
  border-color: #e2e8f0;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: #0ea5e9;
}

.submit-btn {
  width: 100%;
  height: 44px;
  border-radius: 10px;
  font-weight: 600;
  font-size: 15px;
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%) !important;
  border: none !important;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(14, 165, 233, 0.35);
}

.form-footer {
  text-align: center;
  font-size: 13px;
  color: #94a3b8;
}

@media (max-width: 768px) {
  .login-card {
    flex-direction: column;
  }

  .brand-section {
    padding: 32px 24px;
  }

  .form-section {
    padding: 32px 24px;
  }
}
</style>
