<template>
  <view class="popup-mask" v-if="visible" @click="close">
    <view class="popup-content" @click.stop>
      <!-- Close Button -->
      <view class="close-btn" @click="close">
        <text class="close-icon">×</text>
      </view>

      <!-- 【核心功能】收藏按钮 -->
      <view class="favorite-btn" @click.stop="toggleFavorite" :class="{ loading: favoriteLoading }">
        <text class="heart-icon" :class="{ active: isFavorite }">{{ isFavorite ? '♥' : '♡' }}</text>
      </view>

      <!-- Image Header -->
      <view class="image-header">
        <image class="dish-image" :src="dish.image || dish.pic" mode="aspectFill"></image>
        <view class="image-overlay"></view>
      </view>

      <!-- Scrollable Content -->
      <scroll-view scroll-y class="scroll-content">
        <view class="content-body">
          <!-- Title & Price -->
          <view class="header-section">
            <text class="dish-name">{{ dish.name }}</text>
            <view class="meta-row">
              <text class="sold-count">月销 500+</text> <!-- Fixed mock for better UI, or use dish.sold -->
              <view class="price-box">
                <text class="currency">¥</text>
                <text class="price">{{ dish.price }}</text>
              </view>
            </view>
          </view>

          <!-- Nutrition Box -->
          <view class="nutrition-box">
            <view class="nutri-item">
              <text class="nutri-label">热量</text>
              <view class="nutri-value-box">
                <text class="nutri-value">{{ dish.calories || 350 }}</text>
                <text class="nutri-unit">kcal</text>
              </view>
            </view>
            <view class="nutri-divider"></view>
            <view class="nutri-item">
              <text class="nutri-label">蛋白质</text>
              <view class="nutri-value-box">
                <text class="nutri-value">{{ dish.protein || 25 }}</text>
                <text class="nutri-unit">g</text>
              </view>
            </view>
            <view class="nutri-divider"></view>
            <view class="nutri-item">
              <text class="nutri-label">碳水</text>
              <view class="nutri-value-box">
                <text class="nutri-value">{{ dish.carbohydrates || 40 }}</text>
                <text class="nutri-unit">g</text>
              </view>
            </view>
            <view class="nutri-divider"></view>
            <view class="nutri-item">
              <text class="nutri-label">脂肪</text>
              <view class="nutri-value-box">
                <text class="nutri-value">{{ dish.fat || 5 }}</text>
                <text class="nutri-unit">g</text>
              </view>
            </view>
          </view>

          <!-- Description -->
          <view class="section">
            <text class="section-title">商品详情</text>
            <text class="description">{{ dish.description || '精选优质食材，由专业营养师搭配，采用健康烹饪方式，锁住食材本味。口感鲜美，营养均衡。' }}</text>
          </view>

          <!-- Smart Flavors -->
          <view class="section" v-if="smartFlavors.length > 0">
             <text class="section-title">选择口味</text>
             <view class="flavor-list">
               <view 
                 v-for="(flavor, index) in smartFlavors" 
                 :key="index" 
                 class="flavor-tag"
                 :class="{ active: selectedFlavor === flavor }"
                 @click="selectedFlavor = flavor"
               >
                 {{ flavor }}
               </view>
             </view>
          </view>

          <!-- Bottom Spacer to prevent overlap with footer -->
          <view style="height: 160rpx;"></view>
        </view>
      </scroll-view>

      <!-- Footer Action -->
      <view class="popup-footer safe-area-bottom">
        <button class="add-cart-btn" @click="handleAddToCart">
          加入购物车
        </button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { favoriteCheckAPI, favoriteAddAPI, favoriteRemoveAPI } from '@/api/favorite';

const props = defineProps<{
  visible: boolean;
  dish: any;
}>();

const emit = defineEmits(['close', 'addToCart']);

const selectedFlavor = ref('');

// 【核心功能】收藏状态
const isFavorite = ref(false);
const favoriteLoading = ref(false);

// 检查是否已收藏
const checkFavorite = async () => {
  if (!props.dish?.id) return;
  try {
    const res = await favoriteCheckAPI(props.dish.id);
    isFavorite.value = res.data === true;
  } catch (e) {
    console.error('检查收藏状态失败', e);
  }
};

// 切换收藏状态
const toggleFavorite = async () => {
  if (!props.dish?.id || favoriteLoading.value) return;
  
  favoriteLoading.value = true;
  try {
    if (isFavorite.value) {
      await favoriteRemoveAPI(props.dish.id);
      isFavorite.value = false;
      uni.showToast({ title: '已取消收藏', icon: 'none' });
    } else {
      await favoriteAddAPI(props.dish.id);
      isFavorite.value = true;
      uni.showToast({ title: '已收藏', icon: 'success' });
    }
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' });
  } finally {
    favoriteLoading.value = false;
  }
};

// 弹窗打开时检查收藏状态
watch(() => props.visible, (newVal) => {
  if (newVal && props.dish?.id) {
    checkFavorite();
  }
});

// Smart Flavor Logic
const smartFlavors = computed(() => {
  if (!props.dish || !props.dish.name) return [];
  
  const name = props.dish.name;
  
  if (name.includes('面') || name.includes('粉') || name.includes('辣') || name.includes('麻婆') || name.includes('鸡') || name.includes('肉')) {
     if (!name.includes('蛋糕') && !name.includes('甜') && !name.includes('奶')) {
        return ['微辣', '中辣', '特辣', '免辣'];
     }
  }
  
  if (name.includes('饮') || name.includes('茶') || name.includes('奶') || name.includes('拿铁') || name.includes('美式') || name.includes('可乐')) {
     return ['常规冰', '少冰', '去冰', '常温', '热饮'];
  }
  
  if (name.includes('粥')) {
      return ['不加葱', '加葱'];
  }

  return [];
});

watch(smartFlavors, (newVal) => {
  if (newVal && newVal.length > 0) {
    selectedFlavor.value = newVal[0];
  } else {
    selectedFlavor.value = '';
  }
});

const close = () => {
  emit('close');
};

const handleAddToCart = () => {
  const dishToAdd = {
    ...props.dish,
    selectedFlavor: selectedFlavor.value
  };
  emit('addToCart', dishToAdd);
  close();
};
</script>

<style lang="scss" scoped>
/* Colors */
$mint-teal: #00BA9D;
$coral-red: #FF6B6B;
$modal-orange: #FF9900;
$text-dark: #1e293b; // slate-800
$text-gray: #94a3b8; // slate-400

.popup-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5); // Darker mask
  backdrop-filter: blur(4px);
  z-index: 1000; // High z-index
  display: flex;
  align-items: flex-end; /* Bottom sheet on mobile */
  justify-content: center;
}

.popup-content {
  width: 100%;
  background-color: #fff;
  border-top-left-radius: 48rpx;
  border-top-right-radius: 48rpx;
  overflow: hidden;
  position: relative;
  display: flex;
  flex-direction: column;
  height: 85vh; // Fixed height (85% via viewport)
  box-shadow: 0 -4px 20px rgba(0,0,0,0.1);
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.close-btn {
  position: absolute;
  top: 32rpx;
  right: 32rpx;
  width: 64rpx;
  height: 64rpx;
  background: rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(4px);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 20;
  border: 1px solid rgba(255,255,255,0.2);
}

.close-icon {
  color: #fff;
  font-size: 40rpx;
  line-height: 1;
}

/* 【核心功能】收藏按钮样式 */
.favorite-btn {
  position: absolute;
  top: 32rpx;
  left: 32rpx;
  width: 64rpx;
  height: 64rpx;
  background: rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(4px);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 20;
  border: 1px solid rgba(255,255,255,0.2);
  
  &.loading {
    opacity: 0.5;
    pointer-events: none;
  }
}

.heart-icon {
  color: #fff;
  font-size: 36rpx;
  line-height: 1;
  transition: all 0.2s;
  
  &.active {
    color: #FF4B4B;
    transform: scale(1.1);
  }
}

.image-header {
  position: relative;
  height: 450rpx; // Slightly reduced height
  width: 100%;
  flex-shrink: 0;
}

.dish-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 160rpx;
  background: linear-gradient(to top, rgba(0,0,0,0.4), transparent);
}

.scroll-content {
  flex: 1;
  background-color: #fff;
  height: 0; // Important for flex scroll
}

.content-body {
  padding: 40rpx;
}

.header-section {
  margin-bottom: 30rpx;
}

.dish-name {
  font-size: 40rpx;
  font-weight: bold;
  color: $text-dark;
  line-height: 1.2;
  margin-bottom: 16rpx;
  display: block;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sold-count {
  font-size: 28rpx;
  color: $text-gray;
}

.price-box {
  display: flex;
  align-items: baseline;
  color: $coral-red;
}

.currency {
  font-size: 24rpx;
  font-weight: bold;
  margin-right: 4rpx;
}

.price {
  font-size: 48rpx;
  font-weight: bold;
}

.nutrition-box {
  background-color: rgba(255, 153, 0, 0.05); /* modal-orange/5 */
  border-radius: 24rpx;
  padding: 32rpx;
  display: flex;
  justify-content: space-between;
  margin-bottom: 48rpx;
  border: 1px solid rgba(255, 153, 0, 0.1);
}

.nutri-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.nutri-label {
  font-size: 22rpx;
  color: $text-gray;
}

.nutri-value-box {
  display: flex;
  align-items: baseline;
}

.nutri-value {
  font-size: 28rpx;
  font-weight: bold;
  color: $text-dark;
}

.nutri-unit {
  font-size: 20rpx;
  color: $text-gray;
  margin-left: 2rpx;
  transform: scale(0.8);
}

.nutri-divider {
  width: 1px;
  height: 60%;
  background-color: rgba(255, 153, 0, 0.1);
  align-self: center;
}

.section {
  margin-bottom: 48rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: $text-dark;
  margin-bottom: 16rpx;
  display: block;
}

.description {
  font-size: 28rpx;
  color: #64748b; // slate-500
  line-height: 1.6;
  text-align: justify;
}

.flavor-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.flavor-tag {
  font-size: 26rpx;
  padding: 16rpx 32rpx;
  background-color: #fff;
  color: #475569;
  border-radius: 12rpx;
  border: 1px solid #e2e8f0;
  transition: all 0.2s;
  
  &.active {
    background-color: rgba(255, 153, 0, 0.1);
    border-color: $modal-orange;
    color: $modal-orange;
    font-weight: bold;
  }
}

.popup-footer {
  padding: 24rpx 40rpx calc(40rpx + env(safe-area-inset-bottom)); // Safe area padding
  background-color: #fff;
  border-top: 1px solid #f1f5f9;
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 10;
}

.add-cart-btn {
  width: 100%;
  height: 96rpx;
  border-radius: 48rpx;
  background: $modal-orange; // Orange as per design
  color: white;
  font-size: 32rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  box-shadow: 0 8rpx 24rpx rgba(255, 153, 0, 0.3);
  
  &:active {
    transform: scale(0.98);
  }
}

.btn-icon {
  font-size: 40rpx;
  font-weight: normal;
}
</style>
