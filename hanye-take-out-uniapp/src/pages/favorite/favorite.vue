<template>
  <view class="page-container">
    <!-- Header -->
    <view class="header">
      <view class="back-btn" @click="goBack">
        <text class="icon">←</text>
      </view>
      <text class="title">我的收藏</text>
      <view class="placeholder"></view>
    </view>

    <!-- Loading -->
    <view v-if="loading" class="loading-box">
      <text>加载中...</text>
    </view>

    <!-- Empty State -->
    <view v-else-if="favoriteList.length === 0" class="empty-box">
      <text class="empty-icon">💔</text>
      <text class="empty-text">暂无收藏</text>
      <text class="empty-sub">快去发现美食吧~</text>
    </view>

    <!-- Favorite List -->
    <view v-else class="list-container">
      <view 
        class="dish-card" 
        v-for="dish in favoriteList" 
        :key="dish.id"
        @click="goDetail(dish)"
      >
        <image 
          class="dish-img" 
          :src="resolveImageUrl(dish.image)" 
          mode="aspectFill"
        />
        <view class="dish-info">
          <text class="dish-name">{{ dish.name }}</text>
          <text class="dish-desc">{{ dish.description || '暂无描述' }}</text>
          <view class="dish-bottom">
            <text class="dish-price">¥{{ dish.price }}</text>
            <view class="remove-btn" @click.stop="removeFavorite(dish.id)">
              <text>取消收藏</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { favoriteListAPI, favoriteRemoveAPI } from '@/api/favorite'

const baseUrl = 'http://localhost:8081'
const loading = ref(true)
const favoriteList = ref<any[]>([])

// 解析图片URL
const resolveImageUrl = (image: string) => {
  if (!image) return '/static/images/default-dish.png'
  if (image.startsWith('http')) return image
  return baseUrl + image
}

// 加载收藏列表
const loadFavorites = async () => {
  loading.value = true
  try {
    const res = await favoriteListAPI()
    favoriteList.value = res.data || []
  } catch (e) {
    console.error('加载收藏列表失败', e)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 取消收藏
const removeFavorite = async (dishId: number) => {
  uni.showModal({
    title: '提示',
    content: '确定要取消收藏吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await favoriteRemoveAPI(dishId)
          uni.showToast({ title: '已取消收藏', icon: 'success' })
          // 从列表中移除
          favoriteList.value = favoriteList.value.filter(d => d.id !== dishId)
        } catch (e) {
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
}

// 跳转详情
const goDetail = (dish: any) => {
  // 可以跳转到菜品详情页或打开弹窗
  uni.showToast({ title: dish.name, icon: 'none' })
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 页面显示时加载
onShow(() => {
  loadFavorites()
})
</script>

<style lang="scss" scoped>
$primary: #FF6B00;
$bg: #F7F8FA;

.page-container {
  min-height: 100vh;
  background: $bg;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 40rpx 30rpx 30rpx;
  background: white;
  
  .back-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    .icon { font-size: 40rpx; color: #333; }
  }
  
  .title {
    font-size: 36rpx;
    font-weight: bold;
    color: #1A1A1A;
  }
  
  .placeholder { width: 60rpx; }
}

.loading-box, .empty-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 200rpx;
  
  .empty-icon { font-size: 100rpx; margin-bottom: 30rpx; }
  .empty-text { font-size: 32rpx; color: #999; margin-bottom: 10rpx; }
  .empty-sub { font-size: 26rpx; color: #ccc; }
}

.list-container {
  padding: 30rpx;
}

.dish-card {
  display: flex;
  background: white;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04);
  
  .dish-img {
    width: 180rpx;
    height: 180rpx;
    border-radius: 16rpx;
    margin-right: 24rpx;
    flex-shrink: 0;
  }
  
  .dish-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    
    .dish-name {
      font-size: 32rpx;
      font-weight: bold;
      color: #1A1A1A;
      margin-bottom: 8rpx;
    }
    
    .dish-desc {
      font-size: 24rpx;
      color: #999;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .dish-bottom {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 16rpx;
      
      .dish-price {
        font-size: 36rpx;
        font-weight: bold;
        color: $primary;
      }
      
      .remove-btn {
        padding: 12rpx 24rpx;
        background: #FFF0E5;
        border-radius: 30rpx;
        text { font-size: 24rpx; color: $primary; }
      }
    }
  }
}
</style>
