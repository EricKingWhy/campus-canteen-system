<template>
  <view class="login-container">
    <view class="login-header">
      <!-- <image class="logo-img" src="/static/logo.png" mode="aspectFill"></image> -->
      <text class="app-title">智能食堂</text>
    </view>

    <view class="login-card">
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
          <input class="uni-input" type="password" v-model="loginForm.password" placeholder="请输入密码" placeholder-class="placeholder-style"/>
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
          <input class="uni-input" type="password" v-model="registerForm.password" placeholder="设置密码" placeholder-class="placeholder-style"/>
        </view>
        <view class="input-item">
          <text class="input-icon">😊</text>
          <input class="uni-input" type="text" v-model="registerForm.nickname" placeholder="昵称 (如: 大大怪)" placeholder-class="placeholder-style"/>
        </view>
         <view class="input-item">
          <text class="input-icon">📧</text>
          <input class="uni-input" type="text" v-model="registerForm.email" placeholder="电子邮箱 (选填)" placeholder-class="placeholder-style"/>
        </view>
        <button class="submit-btn register-btn" hover-class="btn-hover" @click="handleRegister" :disabled="isLoading">
          {{ isLoading ? '注册中...' : '注册并登录' }}
        </button>
      </view>
    </view>
    
    <view class="footer-tips">
        © 2026 智能食堂推荐系统 by 王浩宇
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      isLoginMode: true,
      isLoading: false,
      loginForm: { username: '', password: '' },
      registerForm: { username: '', password: '', nickname: '', email: '' },
      baseUrl: 'http://127.0.0.1:8081'
    };
  },
  methods: {
    handleLogin() {
      // 防止重复点击
      if (this.isLoading) return;
      
      if (!this.loginForm.username || !this.loginForm.password) {
        return uni.showToast({ title: '请填写完整', icon: 'none' });
      }
      
      this.isLoading = true;
      console.log('===== 发起登录请求 =====');
      console.log('请求地点点址:', `${this.baseUrl}/user/user/login`);
      console.log('请求数据:', JSON.stringify(this.loginForm));
      
      uni.request({
        url: `${this.baseUrl}/user/user/login`,
        method: 'POST',
        header: { 'Content-Type': 'application/json' },
        data: this.loginForm,
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
              username: this.loginForm.username 
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
      
      if (!this.registerForm.username || !this.registerForm.password) {
        return uni.showToast({ title: '账号密码不能为', icon: 'none' });
      }
      
      this.isLoading = true;
      console.log('===== 发起注册请求 =====');
      
      uni.request({
        url: `${this.baseUrl}/user/user/register`,
        method: 'POST',
        header: { 'Content-Type': 'application/json' },
        data: this.registerForm,
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
            this.loginForm.username = this.registerForm.username;
            this.loginForm.password = this.registerForm.password;
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
  background: linear-gradient(135deg, #ffbe76 0%, #ff9f43 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx;
  box-sizing: border-box;
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 80rpx;
  margin-bottom: 50rpx;
  .logo-img {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    margin-bottom: 20rpx;
    background-color: #fff;
  }
  .app-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #fff;
    letter-spacing: 2rpx;
  }
}

.login-card {
  width: 100%;
  background-color: #fff;
  border-radius: 24rpx;
  padding: 40rpx 30rpx;
  box-shadow: 0 10rpx 30rpx rgba(0,0,0,0.08);

  .tabs {
    display: flex;
    justify-content: space-around;
    margin-bottom: 50rpx;
    border-bottom: 2rpx solid #f0f0f0;
    .tab-item {
      font-size: 32rpx;
      color: #999;
      padding-bottom: 20rpx;
      position: relative;
      transition: all 0.3s;
      &.active {
        color: #ff9f43;
        font-weight: bold;
      }
      .tab-line {
        position: absolute;
        bottom: -2rpx;
        left: 50%;
        transform: translateX(-50%);
        width: 60rpx;
        height: 6rpx;
        background-color: #ff9f43;
        border-radius: 6rpx;
      }
    }
  }

  .form-box {
    .input-item {
      display: flex;
      align-items: center;
      background-color: #f8f9fa;
      border-radius: 50rpx;
      padding: 24rpx 36rpx;
      margin-bottom: 30rpx;
      .input-icon {
        font-size: 36rpx;
        margin-right: 20rpx;
      }
      .uni-input {
        flex: 1;
        font-size: 30rpx;
        color: #333;
      }
    }

    .submit-btn {
      width: 100%;
      height: 90rpx;
      line-height: 90rpx;
      background: linear-gradient(to right, #ffbe76, #ff9f43);
      color: #fff;
      font-size: 34rpx;
      font-weight: bold;
      border-radius: 50rpx;
      margin-top: 50rpx;
      box-shadow: 0 8rpx 20rpx rgba(255, 159, 67, 0.3);
      &.register-btn {
        background: linear-gradient(to right, #fab1a0, #e17055);
        box-shadow: 0 8rpx 20rpx rgba(225, 112, 85, 0.3);
      }
      &.btn-hover {
        opacity: 0.9;
        transform: scale(0.98);
      }
      &[disabled] {
        opacity: 0.6;
      }
    }
  }
}

.footer-tips {
  margin-top: auto;
  color: #fff;
  font-size: 24rpx;
  opacity: 0.8;
  margin-bottom: 30rpx;
}

.animate-fade-in {
  animation: fadeIn 0.4s ease-in-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10rpx); }
  to { opacity: 1; transform: translateY(0); }
}

.placeholder-style {
  color: #c0c4cc;
}
</style>
