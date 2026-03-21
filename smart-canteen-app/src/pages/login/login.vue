<template>
  <view class="login-container">
    <view class="login-header">
      <view class="brand-badge">
        <image class="brand-icon" :src="authBrandIcon" mode="aspectFit" />
      </view>
      <text class="app-title">智能食堂</text>
      <text class="app-subtitle">{{ isLoginMode ? 'SMART CANTEEN SAAS' : '开启您的智能膳食新体验' }}</text>
    </view>

    <view class="login-card" :class="{ 'register-mode': !isLoginMode }">
      <view class="tabs">
        <view class="tab-item" :class="{ active: isLoginMode }" @click="isLoginMode = true">
          登录
          <view class="tab-line" v-if="isLoginMode"></view>
        </view>
        <view class="tab-item" :class="{ active: !isLoginMode }" @click="isLoginMode = false">
          注册
           <view class="tab-line" v-if="!isLoginMode"></view>
        </view>
      </view>

      <view v-if="isLoginMode" class="form-box animate-fade-in">
        <view class="input-item">
          <text class="input-icon">👤</text>
          <input class="uni-input" type="text" v-model="loginForm.username" placeholder="请输入账号" placeholder-class="placeholder-style"/>
        </view>
        <view class="input-item">
          <text class="input-icon">🔒</text>
          <input
            class="uni-input"
            type="text"
            :password="!showLoginPassword"
            v-model="loginForm.password"
            placeholder="请输入密码"
            placeholder-class="placeholder-style"
          />
          <image
            class="password-eye"
            :src="showLoginPassword ? eyeOpenIcon : eyeClosedIcon"
            mode="aspectFit"
            @click="showLoginPassword = !showLoginPassword"
          />
        </view>
        <button class="submit-btn" hover-class="btn-hover" @click="handleLogin" :disabled="isLoading">
          {{ isLoading ? '登录中...' : '立即登录' }}
        </button>
      </view>

      <view v-else class="form-box animate-fade-in">
        <view class="input-item">
          <text class="input-icon">👤</text>
          <input class="uni-input" type="text" v-model="registerForm.username" placeholder="设置账号" placeholder-class="placeholder-style"/>
        </view>
        <view class="input-item">
          <text class="input-icon">🔒</text>
          <input
            class="uni-input"
            type="text"
            :password="!showRegisterPassword"
            v-model="registerForm.password"
            placeholder="设置密码"
            placeholder-class="placeholder-style"
          />
          <image
            class="password-eye"
            :src="showRegisterPassword ? eyeOpenIcon : eyeClosedIcon"
            mode="aspectFit"
            @click="showRegisterPassword = !showRegisterPassword"
          />
        </view>
        <view class="input-item">
          <text class="input-icon">😊</text>
          <input class="uni-input" type="text" v-model="registerForm.nickname" placeholder="昵称" placeholder-class="placeholder-style"/>
        </view>
         <view class="input-item">
          <text class="input-icon">📧</text>
          <input class="uni-input" type="text" v-model="registerForm.email" placeholder="电子邮箱" placeholder-class="placeholder-style"/>
        </view>
        <button class="submit-btn register-btn" hover-class="btn-hover" @click="handleRegister" :disabled="isLoading">
          {{ isLoading ? '注册中...' : '注册并登录' }}
        </button>
      </view>
    </view>
    
    <view class="footer-tips">
      <view class="policy-row">
        <text class="policy-link">服务协议</text>
        <text class="policy-dot">•</text>
        <text class="policy-link">隐私政策</text>
      </view>
      <text class="copyright">© 2026 智能食堂推荐系统 by 王浩宇</text>
    </view>
  </view>
</template>

<script>
import authBrandIcon from '@/assets/images/icons/auth_brand_icon.png'
import eyeOpenIcon from '@/assets/images/icons/eye_open.png'
import eyeClosedIcon from '@/assets/images/icons/eye_closed.png'

export default {
  data() {
    return {
      authBrandIcon,
      eyeOpenIcon,
      eyeClosedIcon,
      isLoginMode: true,
      isLoading: false,
      showLoginPassword: false,
      showRegisterPassword: false,
      loginForm: { username: '', password: '' },
      registerForm: { username: '', password: '', nickname: '', email: '' },
      baseUrl: 'http://127.0.0.1:8081'
    };
  },
  methods: {
    handleLogin() {
      // 防止重复点击
      if (this.isLoading) return;

      const loginPayload = {
        username: (this.loginForm.username || '').trim(),
        password: (this.loginForm.password || '').trim()
      };
      
      if (!loginPayload.username || !loginPayload.password) {
        return uni.showToast({ title: '请填写完整', icon: 'none' });
      }
      
      this.isLoading = true;
      console.log('===== 发起登录请求 =====');
      console.log('请求地点点址:', `${this.baseUrl}/user/user/login`);
      console.log('请求数据:', JSON.stringify(loginPayload));
      
      uni.request({
        url: `${this.baseUrl}/user/user/login`,
        method: 'POST',
        header: { 'Content-Type': 'application/json' },
        data: loginPayload,
        success: (res) => {
          console.log('===== 登录响应 =====');
          console.log('完整响应:', JSON.stringify(res));
          console.log('响应数据:', JSON.stringify(res.data));
          console.log('状态码:', res.statusCode);
          
          this.isLoading = false;
          
          // 检查 HTTP 状态码
          if (res.statusCode !== 200) {
            uni.showToast({ title: `服务器错误: ${res.statusCode}`, icon: 'none' });
            return;
          }
          
          // 检查业务状态码
          if (res.data && res.data.code === 0) {
            console.log('登录成功! Token:', res.data.data.token);
            
            // 保存 Token 和用户信息
            uni.setStorageSync('token', res.data.data.token);
            uni.setStorageSync('userInfo', { 
              id: res.data.data.id, 
              username: loginPayload.username 
            });
            
            uni.showToast({ title: '登录成功', icon: 'success' });
            
            // 跳转到首页
            setTimeout(() => {
              uni.switchTab({ url: '/pages/index/index_v2' });
            }, 1000);
          } else {
            console.log('登录失败:', res.data.msg);
            uni.showToast({ title: res.data.msg || '登录失败', icon: 'none' });
          }
        },
        fail: (err) => {
          console.log('===== 登录请求失败 =====');
          console.log('错误信息:', JSON.stringify(err));
          
          this.isLoading = false;
          uni.showToast({ title: '网络连接失败', icon: 'none' });
        },
        complete: () => {
          console.log('===== 登录请求完成 =====');
        }
      });
    },
    
    handleRegister() {
      if (this.isLoading) return;

      const registerPayload = {
        username: (this.registerForm.username || '').trim(),
        password: (this.registerForm.password || '').trim(),
        nickname: (this.registerForm.nickname || '').trim() || (this.registerForm.username || '').trim(),
        email: (this.registerForm.email || '').trim()
      };
      
      if (!registerPayload.username || !registerPayload.password) {
        return uni.showToast({ title: '账号密码不能为', icon: 'none' });
      }
      
      this.isLoading = true;
      console.log('===== 发起注册请求 =====');
      
      uni.request({
        url: `${this.baseUrl}/user/user/register`,
        method: 'POST',
        header: { 'Content-Type': 'application/json' },
        data: registerPayload,
        success: (res) => {
          console.log('===== 注册响应 =====');
          console.log('完整响应:', JSON.stringify(res));
          
          this.isLoading = false;
          
          if (res.statusCode !== 200) {
            uni.showToast({ title: `服务器错误: ${res.statusCode}`, icon: 'none' });
            return;
          }
          
          if (res.data && res.data.code === 0) {
            console.log('注册成功!');
            uni.showToast({ title: '注册成功', icon: 'success' });
            
            // 自动填充登录表单
            this.isLoginMode = true;
            this.loginForm.username = registerPayload.username;
            this.loginForm.password = registerPayload.password;
          } else {
            uni.showToast({ title: res.data.msg || '注册失败', icon: 'none' });
          }
        },
        fail: (err) => {
          console.log('===== 注册请求失败 =====');
          console.log('错误信息:', JSON.stringify(err));
          
          this.isLoading = false;
          uni.showToast({ title: '网络连接失败', icon: 'none' });
        }
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  background-color: #f7f8fa;
  background-image:
    radial-gradient(at 8% 10%, rgba(255, 140, 66, 0.09) 0rpx, transparent 46%),
    radial-gradient(at 92% 88%, rgba(255, 140, 66, 0.05) 0rpx, transparent 44%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: calc(env(safe-area-inset-top) + 36rpx) 42rpx 36rpx;
  box-sizing: border-box;
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 46rpx;
  margin-bottom: 44rpx;

  .brand-badge {
    width: 136rpx;
    height: 136rpx;
    border-radius: 34rpx;
    background: #ffffff;
    box-shadow: 0 20rpx 44rpx rgba(0, 0, 0, 0.03);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 22rpx;
  }

  .brand-symbol {
    font-size: 62rpx;
    line-height: 1;
  }

  .brand-icon {
    width: 100rpx;
    height: 100rpx;
    display: block;
  }

  .app-title {
    font-size: 62rpx;
    font-weight: 700;
    color: #1a1c1e;
    letter-spacing: 1.2rpx;
  }

  .app-subtitle {
    margin-top: 10rpx;
    font-size: 24rpx;
    font-weight: 500;
    color: #999999;
    letter-spacing: 3rpx;
  }
}

.login-card {
  width: 100%;
  background-color: #fff;
  border-radius: 40rpx;
  padding: 40rpx 34rpx 42rpx;
  box-shadow: 0 24rpx 60rpx rgba(0, 0, 0, 0.03);
  transition: all 0.25s ease;

  &.register-mode {
    padding-bottom: 52rpx;
  }

  .tabs {
    display: flex;
    justify-content: space-between;
    margin-bottom: 42rpx;
    background: #f3f4f6;
    border-radius: 26rpx;
    padding: 8rpx;

    .tab-item {
      flex: 1;
      text-align: center;
      font-size: 30rpx;
      color: #9ca3af;
      font-weight: 500;
      padding: 18rpx 0;
      position: relative;
      transition: all 0.3s;
      border-radius: 20rpx;

      &.active {
        color: #1a1c1e;
        font-weight: 700;
        background: #ffffff;
        box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.04);
      }

      .tab-line {
        position: absolute;
        bottom: 6rpx;
        left: 50%;
        transform: translateX(-50%);
        width: 54rpx;
        height: 6rpx;
        background-color: #ff8c42;
        border-radius: 999rpx;
      }
    }
  }

  .form-box {
    .input-item {
      display: flex;
      align-items: center;
      background-color: #f5f5f7;
      border-radius: 24rpx;
      height: 96rpx;
      padding: 0 30rpx;
      margin-bottom: 24rpx;
      border: 2rpx solid transparent;
      transition: all 0.22s ease;

      &:focus-within {
        background-color: #ffffff;
        border-color: rgba(255, 140, 66, 0.36);
        box-shadow: 0 0 0 8rpx rgba(255, 140, 66, 0.11);
      }

      .input-icon {
        font-size: 34rpx;
        margin-right: 18rpx;
        color: #b0b0b0;
      }

      .uni-input {
        flex: 1;
        font-size: 30rpx;
        color: #333;
        font-weight: 500;
      }

      .password-eye {
        width: 40rpx;
        height: 40rpx;
        margin-left: 14rpx;
        flex-shrink: 0;
        opacity: 0.88;
      }
    }

    .submit-btn {
      width: 100%;
      height: 100rpx;
      line-height: 100rpx;
      background: #ff8c42;
      color: #fff;
      font-size: 36rpx;
      font-weight: 700;
      border-radius: 999rpx;
      margin-top: 40rpx;
      box-shadow: 0 16rpx 32rpx rgba(255, 140, 66, 0.25);
      transition: all 0.2s ease;

      &.register-btn {
        background: #ff8c42;
        box-shadow: 0 16rpx 32rpx rgba(255, 140, 66, 0.25);
      }

      &.btn-hover {
        opacity: 0.94;
        transform: scale(0.98);
      }

      &[disabled] {
        opacity: 0.58;
      }
    }
  }
}

.footer-tips {
  margin-top: auto;
  margin-bottom: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;

  .policy-row {
    display: flex;
    align-items: center;
    gap: 16rpx;
  }

  .policy-link {
    font-size: 24rpx;
    color: #9ca3af;
    font-weight: 500;
  }

  .policy-dot {
    font-size: 24rpx;
    color: #d1d5db;
  }

  .copyright {
    font-size: 22rpx;
    color: #b7bdc6;
    letter-spacing: 1rpx;
  }
}

.animate-fade-in {
  animation: fadeIn 0.4s ease-in-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10rpx); }
  to { opacity: 1; transform: translateY(0); }
}

.placeholder-style {
  color: #b0b0b0;
}
</style>
