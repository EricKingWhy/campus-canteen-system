import { isMpWeixin, isWeixinDevtools, LOCAL_API_BASE, CLOUD_API_BASE, resolveApiBaseUrl } from '@/utils/runtime'

const WECHAT_IMAGE_PROXY = 'https://images.weserv.nl/?url='

const trimSlash = (value: string) => value.replace(/\/+$/, '')

const isLocalLikeHost = (url: string) => {
  try {
    const { hostname } = new URL(url)
    if (!hostname) return false
    if (hostname === 'localhost' || hostname === '127.0.0.1' || hostname === '0.0.0.0') return true
    if (/^10\./.test(hostname)) return true
    if (/^192\.168\./.test(hostname)) return true
    if (/^172\.(1[6-9]|2\d|3[0-1])\./.test(hostname)) return true
    return false
  } catch (e) {
    return false
  }
}

const normalizeAbsoluteHost = (url: string) => {
  if (!/^https?:\/\//i.test(url)) return url
  const devBase = trimSlash(LOCAL_API_BASE)
  const realBase = trimSlash(CLOUD_API_BASE)

  // 开发者工具里强制看本地服务，便于验证本地修改
  if (isWeixinDevtools()) {
    return url.replace(/^https?:\/\/[^/]+/i, devBase)
  }

  // 真机统一走云端主机
  if (isMpWeixin()) {
    return url.replace(/^https?:\/\/[^/]+/i, realBase)
  }

  return url
}

export const resolveRuntimeBaseUrl = (baseUrl: string) => {
  const runtime = trimSlash(resolveApiBaseUrl())
  if (!baseUrl) return runtime
  if (isMpWeixin()) return runtime
  return trimSlash(baseUrl)
}

export const toWechatSafeImageUrl = (url: string) => {
  if (!url) return url
  const normalized = normalizeAbsoluteHost(url)

  if (!/^https?:\/\//i.test(normalized)) {
    return normalized
  }

  // 本地私网地址在第三方代理无法访问，保持原地址供本地联调使用
  if (isLocalLikeHost(normalized)) {
    return normalized
  }

  // 真机端 <image> 会拦截 http，开发者工具不需要代理
  if (isMpWeixin() && !isWeixinDevtools() && /^http:\/\//i.test(normalized)) {
    return `${WECHAT_IMAGE_PROXY}${encodeURIComponent(normalized)}`
  }

  return normalized
}

export const buildDishImageUrl = (image: string | undefined, baseUrl: string, fallback: string) => {
  if (!image) return fallback

  if (/^https?:\/\//i.test(image)) {
    return toWechatSafeImageUrl(image)
  }

  const runtimeBase = resolveRuntimeBaseUrl(baseUrl)
  if (image.startsWith('/static/dish/')) {
    return toWechatSafeImageUrl(`${runtimeBase}${image}`)
  }

  if (image.startsWith('/')) {
    return toWechatSafeImageUrl(`${runtimeBase}${image}`)
  }

  return toWechatSafeImageUrl(`${runtimeBase}/static/dish/${image.replace(/^\/+/, '')}`)
}
