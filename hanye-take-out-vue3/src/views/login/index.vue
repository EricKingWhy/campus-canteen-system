<script setup lang="ts">
import { loginAPI } from '@/api/employee'
import { useRouter } from 'vue-router'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserInfoStore } from '@/store'

const userInfoStore = useUserInfoStore()

const form = ref({
  username: '',
  password: ''
});
// 表单校验的ref
const loginRef = ref()

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9]{1,10}$/, message: '用户名必须是1-10的字母数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { pattern: /^\S{6,15}$/, message: '密码必须是6-15的非空字符', trigger: 'blur' }
  ]
}

const router = useRouter()

const loginFn = async () => {
  // 先校验输入格式是否合法
  const valid = await loginRef.value.validate()
  if (valid) {
    // 调用登录接口
    const { data: res } = await loginAPI(form.value)
    console.log(res)
    // 登录失败，提示用户，这个提示已经在响应拦截器中统一处理了，这里直接return就行
    if (res.code !== 0) {
      return false
    }
    // 登录成功，提示用户
    ElMessage.success('登录成功')
    // 把后端返回的当前登录用户信息(包括token)存储到Pinia里
    userInfoStore.userInfo = res.data
    console.log(userInfoStore.userInfo)
    // 跳转到首页
    router.push('/')
  } else {
    return false
  }
}
</script>

<template>
  <div class="login-container">
    <!-- 左侧品牌视觉区 -->
    <div class="left-panel">
      <!-- 装饰光圈 -->
      <div class="deco-circle deco-circle-1"></div>
      <div class="deco-circle deco-circle-2"></div>

      <div class="brand-content">
        <div class="brand-icon-box">
          <span class="brand-icon">🍽️</span>
        </div>
        <h1 class="brand-title">智能食堂推荐系统</h1>
        <p class="brand-subtitle">后台管理端欢迎您</p>
        <div class="illustration-ring">
          <img
            class="illustration-img"
            src="https://lh3.googleusercontent.com/aida-public/AB6AXuD3vT_eko0juYR5RnSHcimMm5uY9UcHxhXYM8ITMxUw_geT3HVFaXTAYXnMJTRXx0xo3M_u6rTFJ1ioMjKKN7MravT2NS3tsg-xmzf230FYXhsxl30ix_i62uT6kDJM6Sq1wkl1HpcUUQgMO02xxmE-8dtcA1vHC2qjcvJOlCGLTRaFdcSLNnulcPXnUGoJE3hDjFjFqwFcEKBtsUeeiaZ4ROtvTU_Oge75zNyo649m1fuWHpRzhXmWF4tTertQIsozyP7JADg1L_w"
            alt="健康食物插图"
          />
        </div>
      </div>
    </div>

    <!-- 右侧登录表单区 -->
    <div class="right-panel">
      <div class="form-wrapper">
        <h2 class="form-title">欢迎登录</h2>
        <p class="form-desc">请输入您的凭据以访问后台管理系统</p>

        <el-form
          :model="form"
          :rules="rules"
          ref="loginRef"
          label-position="top"
          class="login-form"
          @submit.prevent
        >
          <el-form-item label="账号" prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入账号"
              :prefix-icon="'User'"
              size="large"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              show-password
              :prefix-icon="'Lock'"
              size="large"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              class="login-btn"
              size="large"
              @click="loginFn"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          没有账号？
          <span class="link-text" @click="$router.push('/reg')">去注册</span>
        </div>

        <div class="copyright">
          © 2026 智能食堂推荐系统 by 王浩宇 | 版权所有
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.login-container {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background-color: #ffffff;
}

/* ========== 左侧品牌区 ========== */
.left-panel {
  width: 50%;
  background: linear-gradient(135deg, rgba(255,140,0,0.15) 0%, rgba(255,140,0,0.08) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,140,0,0.08);
  filter: blur(60px);
}
.deco-circle-1 {
  width: 260px;
  height: 260px;
  top: -60px;
  right: -60px;
}
.deco-circle-2 {
  width: 300px;
  height: 300px;
  bottom: -80px;
  left: -80px;
}

.brand-content {
  position: relative;
  z-index: 2;
  text-align: center;
  max-width: 380px;
}
.brand-icon-box {
  width: 90px;
  height: 90px;
  margin: 0 auto 24px;
  background: #ffffff;
  border-radius: 22px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
  display: flex;
  align-items: center;
  justify-content: center;
}
.brand-icon {
  font-size: 48px;
}
.brand-title {
  font-size: 32px;
  font-weight: 900;
  color: #1a1a2e;
  margin: 0 0 8px;
  letter-spacing: 1px;
}
.brand-subtitle {
  font-size: 16px;
  color: #6b7280;
  margin: 0 0 40px;
}
.illustration-ring {
  width: 260px;
  height: 260px;
  margin: 0 auto;
  border-radius: 50%;
  background: rgba(255,255,255,0.45);
  border: 1px solid rgba(255,255,255,0.3);
  padding: 24px;
  box-sizing: border-box;
  backdrop-filter: blur(4px);
}
.illustration-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 50%;
}

/* ========== 右侧表单区 ========== */
.right-panel {
  width: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  padding: 40px;
  box-sizing: border-box;
}
.form-wrapper {
  width: 100%;
  max-width: 400px;
}
.form-title {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px;
}
.form-desc {
  font-size: 14px;
  color: #9ca3af;
  margin: 0 0 36px;
}

.login-form {
  .login-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 700;
    border-radius: 12px;
    background-color: #ff8c00;
    border-color: #ff8c00;
    letter-spacing: 6px;
    &:hover, &:focus {
      background-color: #e67e00;
      border-color: #e67e00;
    }
  }
}

// 覆盖 Element Plus 输入框样式贴合暖色调
:deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 4px 12px;
  box-shadow: 0 0 0 1px #e5e7eb inset;
  transition: box-shadow 0.25s;
  &.is-focus {
    box-shadow: 0 0 0 1px #ff8c00 inset !important;
  }
}
:deep(.el-form-item__label) {
  font-weight: 600;
  color: #374151;
}

.form-footer {
  text-align: center;
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid #f3f4f6;
  font-size: 14px;
  color: #9ca3af;
}
.link-text {
  color: #ff8c00;
  font-weight: 700;
  cursor: pointer;
  &:hover {
    text-decoration: underline;
  }
}
.copyright {
  text-align: center;
  margin-top: 40px;
  font-size: 12px;
  color: #d1d5db;
}

/* ========== 响应式：小屏隐藏左侧 ========== */
@media (max-width: 900px) {
  .left-panel {
    display: none;
  }
  .right-panel {
    width: 100%;
  }
}
</style>