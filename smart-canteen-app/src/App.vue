<script setup lang="ts">
import {onLaunch, onShow, onHide} from '@dcloudio/uni-app'
onLaunch(() => {
  console.log('App Launch')
})
onShow(() => {
  console.log('App Show')
})
onHide(() => {
  console.log('App Hide')
})
</script>

<style>
@import '@/static/styles/iconfont.css';

:root {
  --ui-primary: #ff8c42;
  --ui-success: #34c759;
  --ui-bg: #f7f8fa;
  --ui-surface: #ffffff;
  --ui-text-1: #333333;
  --ui-text-2: #666666;
  --ui-text-3: #999999;
  --ui-radius-card: 24rpx;
  --ui-radius-pill: 16rpx;
  --ui-shadow-soft: 0 16rpx 40rpx rgba(0, 0, 0, 0.04);
}

page {
  background-color: var(--ui-bg);
  color: var(--ui-text-1);
}
</style>
