<template>
  <div class="register-page">
    <div class="register-card">
      <div class="card-header">
        <el-icon class="header-icon" @click="$router.push('/login')"><ArrowLeft /></el-icon>
        <h2>注册账号</h2>
        <p>创建您的账号以使用疲劳检测系统</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="register-form"
        label-position="top"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="form.nickname" placeholder="请输入昵称" size="large" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码（至少6位）" size="large" show-password />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" size="large" show-password />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" size="large" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" size="large" />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="loading"
            @click="handleRegister"
          >
            {{ loading ? '注册中...' : '立即注册' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="card-footer">
        <span>已有账号？</span>
        <el-button link type="primary" @click="$router.push('/login')">立即登录</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: '',
  phone: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 64, message: '用户名长度3-64位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await register({
        username: form.username,
        password: form.password,
        nickname: form.nickname || undefined,
        email: form.email || undefined,
        phone: form.phone || undefined
      })

      if (res.data.code === 200) {
        const data = res.data.data
        localStorage.setItem('token', data.token)
        localStorage.setItem('user', JSON.stringify(data))
        ElMessage.success('注册成功')
        router.push('/detect')
      } else {
        ElMessage.error(res.data.message || '注册失败')
      }
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '网络错误')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e0f2fe 0%, #f0f9ff 50%, #ecfeff 100%);
  padding: 24px;
}

.register-card {
  width: 520px;
  max-width: 100%;
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.08);
  padding: 40px;
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.header-icon {
  font-size: 20px;
  color: #0ea5e9;
  cursor: pointer;
  float: left;
  padding: 4px;
  border-radius: 8px;
  transition: background 0.2s;
}

.header-icon:hover {
  background: rgba(14, 165, 233, 0.1);
}

.card-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 6px;
}

.card-header p {
  font-size: 14px;
  color: #94a3b8;
  margin: 0;
}

.register-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  background: #f8fafc;
  border-color: #e2e8f0;
}

.register-form :deep(.el-form-item__label) {
  color: #475569;
  font-weight: 500;
  font-size: 13px;
  padding-bottom: 4px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  border-radius: 10px;
  font-weight: 600;
  font-size: 15px;
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%) !important;
  border: none !important;
  margin-top: 8px;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(14, 165, 233, 0.35);
}

.card-footer {
  text-align: center;
  font-size: 13px;
  color: #94a3b8;
}
</style>
