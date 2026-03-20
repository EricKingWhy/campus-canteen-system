import { useUserStore } from '@/stores/modules/user'

// 请求基地址
// ✅ 正确写法：去掉后面的 /user，只要端口号
export const baseURL = 'http://127.0.0.1:8081'

// 拦截器配置
const httpInterceptor = {
  // 拦截前触发
  invoke(options: UniApp.RequestOptions) {
    // 1. 非 http 开头需拼接地址
    if (!options.url.startsWith('http')) {
      options.url = baseURL + options.url
    }
    // 2. 请求超时
    options.timeout = 10000
    // 3. 添加小程序端请求头标识
    options.header = {
      'source-client': 'miniapp',
      ...options.header,
    }
    // 4. 添加 token 请求头标识
    const userStore = useUserStore()
    const token = userStore.profile?.token || uni.getStorageSync('token')
    console.log('token', token)
    if (token) {
      // 【核心修复】后端配置的 Token 名称是 authentication
      options.header['authentication'] = token
      // 兼容性：同时加上 Authorization (可选，以防万一)
      // options.header.Authorization = token
    }
    console.log('Request Header:', options.header)
  },
}

// 拦截 request 请求
uni.addInterceptor('request', httpInterceptor)
// 拦截 uploadFile 文件上传
// uni.addInterceptor('uploadFile', httpInterceptor)

// 定义泛型接口
interface Data<T> {
  code: number
  msg: string
  data: T
}
// 相比axios，uniapp对ts不友好，因此自己封装函数升级request，实现响应拦截器的功能
// 直观体现： wx.request({}) -> async await promise对象
export const http = <T>(options: UniApp.RequestOptions) => {
  return new Promise<Data<T>>((resolve, reject) => {
    uni.request({
      ...options,
      // 响应成功
      success(res) {
        console.log('响应  ', res)
        if (res.statusCode >= 200 && res.statusCode < 300) {
          // 获取数据成功, 调用resolve，返回的是res.data而不是res，能省掉一层嵌套
          // console.log('请求成功', res)
          resolve(res.data as Data<T>) // 类型断言
        } else if (res.statusCode === 401) {
          // 401错误, 说明token过期, 调用reject,
          // 清理用户信息
          const userStore = useUserStore()
          userStore.clearProfile()
          // 跳转到登录页
          uni.navigateTo({ url: '/pages/login/login' })
          reject(res)
        } else {
          // 通用错误, 调用reject, 轻量提示框
          const errorMsg = (res.data as Data<T>).msg || '请求失败'
          uni.showToast({
            title: errorMsg,
            icon: 'none',
          })
          reject(new Error(errorMsg)) // 必须 reject 否则 Promise 悬挂
        }
      },
      // 响应失败
      fail(err) {
        // 网络错误, 调用reject
        uni.showToast({
          title: '网络不行，换个试试？',
          icon: 'none',
          // mask: true,
        })
        reject(err)
      },
    })
  })
}