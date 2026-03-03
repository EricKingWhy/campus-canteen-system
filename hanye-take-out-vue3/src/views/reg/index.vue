<script setup lang="ts" name:="my-register">
// 导出是命名导出，所以这里导入要加{}
import { registerAPI } from '@/api/employee'
import { useRouter } from 'vue-router'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const form = ref({ // 表单的数据对象
  account: '', // 用户名
  password: '', // 密码
  repassword: '' // 确认密码
})
// 表单校验的ref
const registerRef = ref()

// 自定义校验规则: 两次密码是否一致
// 注意：必须在data函数里定义此箭头函数，才能确保this.from能使用，从而获取到password的值
const samePwd = (rules: any, value: any, callback: any) => {
  if (value !== form.value.password) {
    // 如果验证失败，则调用 回调函数时，指定一个 Error 对象。
    callback(new Error('两次输入的密码不一致!'))
  } else {
    // 如果验证成功，则直接调用 callback 回调函数即可。
    callback()
  }
}
const rules = { // 表单的规则检验对象
  account: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9]{1,10}$/,
      message: '用户名必须是1-10的大小写字母数字',
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    {
      pattern: /^\S{6,15}$/,
      message: '密码必须是6-15的非空字符',
      trigger: 'blur'
    }
  ],
  repassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { pattern: /^\S{6,15}$/, message: '密码必须是6-15的非空字符', trigger: 'blur' },
    { validator: samePwd, trigger: 'blur' }
  ]
}

const router = useRouter()

const registerFn = async () => {
  // 先校验输入格式是否合法
  const valid = await registerRef.value.validate()
  if (valid) {
    // 通过校验，拿到绑定的数据
    console.log('注册的表单ref:  ', registerRef)
    console.log('form.value:  ', form.value)
    // 1.调用注册接口，通过接口的return request，拿到promise对象
    const { data: res } = await registerAPI(form.value)
    console.log(res)
    // 2.注册失败，响应拦截器已经ElMessage提示用户，这里直接返回
    if (res.code !== 0) {
      console.log('注册失败！')
      return false
    }
    // 3.注册成功，提示用户
    ElMessage.success('注册成功!')
    // 4.路由跳转到登录页面
    router.push('/login')
  } else {
    return false // 阻止默认提交行为（表单下面红色提示）
  }
}
</script>

<template>
  <div class="reg-container">
    <!-- 左侧品牌视觉区 -->
    <div class="left-panel">
      <!-- 装饰光圈 -->
      <div class="deco-circle deco-circle-1"></div>
      <div class="deco-circle deco-circle-2"></div>

      <!-- 左上角品牌标志 -->
      <div class="brand-badge">
        <span class="badge-icon">🍽️</span>
        <span class="badge-text">智能食堂推荐系统</span>
      </div>

      <div class="brand-content">
        <div class="illustration-ring">
          <span class="illustration-emoji">🍔</span>
        </div>
        <h1 class="brand-title">高效管理，智能推荐</h1>
        <p class="brand-subtitle">加入我们，为校园师生打造更智能、更健康的美食新生态。</p>
      </div>
    </div>

    <!-- 右侧注册表单区 -->
    <div class="right-panel">
      <div class="form-wrapper">
        <h2 class="form-title">注册管理员账号</h2>
        <p class="form-desc">请填写以下信息以创建您的管理后台账户</p>

        <el-form
          :model="form"
          :rules="rules"
          ref="registerRef"
          label-position="top"
          class="reg-form"
          @submit.prevent
        >
          <el-form-item label="账号" prop="account">
            <el-input
              v-model="form.account"
              placeholder="请输入用户名"
              :prefix-icon="'User'"
              size="large"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入您的密码"
              show-password
              :prefix-icon="'Lock'"
              size="large"
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="repassword">
            <el-input
              v-model="form.repassword"
              type="password"
              placeholder="请再次输入您的密码"
              show-password
              :prefix-icon="'CircleCheck'"
              size="large"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              class="reg-btn"
              size="large"
              @click="registerFn"
            >
              注 册
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          已有账号？
          <span class="link-text" @click="router.push('/login')">去登录</span>
        </div>

        <div class="copyright">
          © 2026 智能食堂推荐系统 by 王浩宇 | 版权所有
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.reg-container {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background-color: #ffffff;
}

/* ========== 左侧品牌区 ========== */
.left-panel {
  width: 50%;
  background: linear-gradient(135deg, rgba(255,140,0,0.12) 0%, rgba(255,140,0,0.05) 100%);
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
  width: 300px;
  height: 300px;
  top: -80px;
  right: -80px;
}
.deco-circle-2 {
  width: 260px;
  height: 260px;
  bottom: -60px;
  left: -60px;
}

.brand-badge {
  position: absolute;
  top: 32px;
  left: 32px;
  display: flex;
  align-items: center;
  gap: 8px;
  z-index: 3;
  .badge-icon {
    font-size: 24px;
  }
  .badge-text {
    font-size: 16px;
    font-weight: 700;
    color: #1a1a2e;
  }
}

.brand-content {
  position: relative;
  z-index: 2;
  text-align: center;
  max-width: 400px;
  padding: 0 24px;
}
.illustration-ring {
  width: 220px;
  height: 220px;
  margin: 0 auto 32px;
  border-radius: 50%;
  background: rgba(255,140,0,0.15);
  display: flex;
  align-items: center;
  justify-content: center;
}
.illustration-emoji {
  font-size: 100px;
}
.brand-title {
  font-size: 30px;
  font-weight: 900;
  color: #1a1a2e;
  margin: 0 0 12px;
}
.brand-subtitle {
  font-size: 15px;
  color: #6b7280;
  margin: 0;
  line-height: 1.6;
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
  margin: 0 0 32px;
}

.reg-form {
  .reg-btn {
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
  margin-top: 36px;
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
