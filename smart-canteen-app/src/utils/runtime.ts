export const LOCAL_API_BASE = 'http://121.41.59.61:8081'
export const CLOUD_API_BASE = 'http://121.41.59.61:8081'

const trimSlash = (value: string) => value.replace(/\/+$/, '')

export const isMpWeixin = () => {
  // #ifdef MP-WEIXIN
  return true
  // #endif
  return false
}

export const isWeixinDevtools = () => {
  if (!isMpWeixin()) return false
  try {
    return uni.getSystemInfoSync().platform === 'devtools'
  } catch (e) {
    return false
  }
}

export const resolveApiBaseUrl = () => {
  // 小程序开发者工具: 本地联调
  if (isWeixinDevtools()) return LOCAL_API_BASE
  // 真机/体验版: 云端联调
  if (isMpWeixin()) return CLOUD_API_BASE
  // 其他端默认本地
  return LOCAL_API_BASE
}

export const rewriteAbsoluteApiUrl = (url: string) => {
  if (!/^https?:\/\//i.test(url)) return url
  const base = trimSlash(resolveApiBaseUrl())
  return url.replace(/^https?:\/\/[^/]+/i, base)
}
