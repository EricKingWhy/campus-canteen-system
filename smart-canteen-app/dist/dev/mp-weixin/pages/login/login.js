"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const _sfc_main = {
  data() {
    return {
      authBrandIcon: common_assets.authBrandIcon,
      eyeOpenIcon: common_assets.eyeOpenIcon,
      eyeClosedIcon: common_assets.eyeClosedIcon,
      isLoginMode: true,
      isLoading: false,
      showLoginPassword: false,
      showRegisterPassword: false,
      loginForm: { username: "", password: "" },
      registerForm: { username: "", password: "", nickname: "", email: "" },
      baseUrl: "http://127.0.0.1:8081"
    };
  },
  methods: {
    handleLogin() {
      if (this.isLoading)
        return;
      const loginPayload = {
        username: (this.loginForm.username || "").trim(),
        password: (this.loginForm.password || "").trim()
      };
      if (!loginPayload.username || !loginPayload.password) {
        return common_vendor.index.showToast({ title: "请填写完整", icon: "none" });
      }
      this.isLoading = true;
      console.log("===== 发起登录请求 =====");
      console.log("请求地点点址:", `${this.baseUrl}/user/user/login`);
      console.log("请求数据:", JSON.stringify(loginPayload));
      common_vendor.index.request({
        url: `${this.baseUrl}/user/user/login`,
        method: "POST",
        header: { "Content-Type": "application/json" },
        data: loginPayload,
        success: (res) => {
          console.log("===== 登录响应 =====");
          console.log("完整响应:", JSON.stringify(res));
          console.log("响应数据:", JSON.stringify(res.data));
          console.log("状态码:", res.statusCode);
          this.isLoading = false;
          if (res.statusCode !== 200) {
            common_vendor.index.showToast({ title: `服务器错误: ${res.statusCode}`, icon: "none" });
            return;
          }
          if (res.data && res.data.code === 0) {
            console.log("登录成功! Token:", res.data.data.token);
            common_vendor.index.setStorageSync("token", res.data.data.token);
            common_vendor.index.setStorageSync("userInfo", {
              id: res.data.data.id,
              username: loginPayload.username
            });
            common_vendor.index.showToast({ title: "登录成功", icon: "success" });
            setTimeout(() => {
              common_vendor.index.switchTab({ url: "/pages/index/index_v2" });
            }, 1e3);
          } else {
            console.log("登录失败:", res.data.msg);
            common_vendor.index.showToast({ title: res.data.msg || "登录失败", icon: "none" });
          }
        },
        fail: (err) => {
          console.log("===== 登录请求失败 =====");
          console.log("错误信息:", JSON.stringify(err));
          this.isLoading = false;
          common_vendor.index.showToast({ title: "网络连接失败", icon: "none" });
        },
        complete: () => {
          console.log("===== 登录请求完成 =====");
        }
      });
    },
    handleRegister() {
      if (this.isLoading)
        return;
      const registerPayload = {
        username: (this.registerForm.username || "").trim(),
        password: (this.registerForm.password || "").trim(),
        nickname: (this.registerForm.nickname || "").trim() || (this.registerForm.username || "").trim(),
        email: (this.registerForm.email || "").trim()
      };
      if (!registerPayload.username || !registerPayload.password) {
        return common_vendor.index.showToast({ title: "账号密码不能为", icon: "none" });
      }
      this.isLoading = true;
      console.log("===== 发起注册请求 =====");
      common_vendor.index.request({
        url: `${this.baseUrl}/user/user/register`,
        method: "POST",
        header: { "Content-Type": "application/json" },
        data: registerPayload,
        success: (res) => {
          console.log("===== 注册响应 =====");
          console.log("完整响应:", JSON.stringify(res));
          this.isLoading = false;
          if (res.statusCode !== 200) {
            common_vendor.index.showToast({ title: `服务器错误: ${res.statusCode}`, icon: "none" });
            return;
          }
          if (res.data && res.data.code === 0) {
            console.log("注册成功!");
            common_vendor.index.showToast({ title: "注册成功", icon: "success" });
            this.isLoginMode = true;
            this.loginForm.username = registerPayload.username;
            this.loginForm.password = registerPayload.password;
          } else {
            common_vendor.index.showToast({ title: res.data.msg || "注册失败", icon: "none" });
          }
        },
        fail: (err) => {
          console.log("===== 注册请求失败 =====");
          console.log("错误信息:", JSON.stringify(err));
          this.isLoading = false;
          common_vendor.index.showToast({ title: "网络连接失败", icon: "none" });
        }
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.authBrandIcon,
    b: common_vendor.t($data.isLoginMode ? "SMART CANTEEN SAAS" : "开启您的智能膳食新体验"),
    c: $data.isLoginMode
  }, $data.isLoginMode ? {} : {}, {
    d: $data.isLoginMode ? 1 : "",
    e: common_vendor.o(($event) => $data.isLoginMode = true),
    f: !$data.isLoginMode
  }, !$data.isLoginMode ? {} : {}, {
    g: !$data.isLoginMode ? 1 : "",
    h: common_vendor.o(($event) => $data.isLoginMode = false),
    i: $data.isLoginMode
  }, $data.isLoginMode ? {
    j: $data.loginForm.username,
    k: common_vendor.o(($event) => $data.loginForm.username = $event.detail.value),
    l: !$data.showLoginPassword,
    m: $data.loginForm.password,
    n: common_vendor.o(($event) => $data.loginForm.password = $event.detail.value),
    o: $data.showLoginPassword ? $data.eyeOpenIcon : $data.eyeClosedIcon,
    p: common_vendor.o(($event) => $data.showLoginPassword = !$data.showLoginPassword),
    q: common_vendor.t($data.isLoading ? "登录中..." : "立即登录"),
    r: common_vendor.o((...args) => $options.handleLogin && $options.handleLogin(...args)),
    s: $data.isLoading
  } : {
    t: $data.registerForm.username,
    v: common_vendor.o(($event) => $data.registerForm.username = $event.detail.value),
    w: !$data.showRegisterPassword,
    x: $data.registerForm.password,
    y: common_vendor.o(($event) => $data.registerForm.password = $event.detail.value),
    z: $data.showRegisterPassword ? $data.eyeOpenIcon : $data.eyeClosedIcon,
    A: common_vendor.o(($event) => $data.showRegisterPassword = !$data.showRegisterPassword),
    B: $data.registerForm.nickname,
    C: common_vendor.o(($event) => $data.registerForm.nickname = $event.detail.value),
    D: $data.registerForm.email,
    E: common_vendor.o(($event) => $data.registerForm.email = $event.detail.value),
    F: common_vendor.t($data.isLoading ? "注册中..." : "注册并登录"),
    G: common_vendor.o((...args) => $options.handleRegister && $options.handleRegister(...args)),
    H: $data.isLoading
  }, {
    I: !$data.isLoginMode ? 1 : ""
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-cdfe2409"], ["__file", "C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/smart-canteen-app/src/pages/login/login.vue"]]);
wx.createPage(MiniProgramPage);
