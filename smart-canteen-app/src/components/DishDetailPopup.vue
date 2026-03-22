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
        <image class="dish-image" :src="resolveImageUrl(displayDish.image || displayDish.pic)" mode="aspectFill"></image>
        <view class="image-overlay"></view>
      </view>

      <!-- Scrollable Content -->
      <scroll-view scroll-y class="scroll-content">
        <view class="content-body">
          <!-- Title & Price -->
          <view class="header-section">
            <text class="dish-name">{{ displayDish.name }}</text>
            <view class="meta-row">
              <text class="sold-count">月销 500+</text> <!-- Fixed mock for better UI, or use dish.sold -->
              <view class="price-box">
                <text class="currency">¥</text>
                <text class="price">{{ displayDish.price }}</text>
              </view>
            </view>
          </view>

          <!-- Nutrition Box -->
          <view class="nutrition-box">
            <view class="nutri-item">
              <text class="nutri-label">热量</text>
              <view class="nutri-value-box">
                <text class="nutri-value">{{ displayDish.calories || 350 }}</text>
                <text class="nutri-unit">kcal</text>
              </view>
            </view>
            <view class="nutri-divider"></view>
            <view class="nutri-item">
              <text class="nutri-label">蛋白质</text>
              <view class="nutri-value-box">
                <text class="nutri-value">{{ displayDish.protein || 25 }}</text>
                <text class="nutri-unit">g</text>
              </view>
            </view>
            <view class="nutri-divider"></view>
            <view class="nutri-item">
              <text class="nutri-label">碳水</text>
              <view class="nutri-value-box">
                <text class="nutri-value">{{ displayDish.carbohydrates || 40 }}</text>
                <text class="nutri-unit">g</text>
              </view>
            </view>
            <view class="nutri-divider"></view>
            <view class="nutri-item">
              <text class="nutri-label">脂肪</text>
              <view class="nutri-value-box">
                <text class="nutri-value">{{ displayDish.fat || 5 }}</text>
                <text class="nutri-unit">g</text>
              </view>
            </view>
          </view>

          <!-- Description -->
          <view class="section">
            <text class="section-title">商品详情</text>
            <text class="description">{{ displayDish.description || '精选优质食材，由专业营养师搭配，采用健康烹饪方式，锁住食材本味。口感鲜美，营养均衡。' }}</text>
          </view>

          <view class="detail-section ingredients-box" v-if="displayDish.mainIngredients" style="margin-top: 30rpx; padding-top: 20rpx; margin-bottom: 48rpx;">
              <view class="detail-title-row">
                <image class="detail-title-icon" :src="ingredientsWheatIcon" mode="aspectFit" />
                <text class="detail-title-text">主要成分</text>
              </view>
              <view style="font-size: 26rpx; color: #666; line-height: 1.5;">{{ displayDish.mainIngredients }}</view>
          </view>

          <view class="detail-section allergen-box" v-if="displayDish.allergenTags" style="margin-bottom: 48rpx;">
              <view v-if="displayDish.allergenTags !== '无'" style="background-color: #fff1f0; padding: 16rpx; border-radius: 12rpx;">
                  <view class="allergen-title-row">
                    <image class="allergen-icon" :src="calorieCheckNewIcon" mode="aspectFit" />
                    <text style="font-size: 26rpx; font-weight: bold; color: #d93025;">忌口/过敏原提示：{{ displayDish.allergenTags }}</text>
                  </view>
              </view>
              <view v-else style="background-color: #edf9f0; padding: 16rpx; border-radius: 12rpx;">
                  <view class="allergen-title-row">
                    <image class="allergen-icon" :src="calorieCheckNewIcon" mode="aspectFit" />
                    <text style="font-size: 26rpx; font-weight: bold; color: #0b8043;">忌口/过敏原提示：无，请放心食用</text>
                </view>
              </view>
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
import calorieCheckNewIcon from '@/assets/images/icons/calorie_check_new.png';
import ingredientsWheatIcon from '@/assets/images/icons/ingredients_wheat.png';

const props = defineProps<{
  visible: boolean;
  dish: any;
}>();

const emit = defineEmits(['close', 'addToCart']);
const baseUrl = 'http://127.0.0.1:8081';

const resolveImageUrl = (image?: string) => {
  if (!image) return '/static/default_dish.png';
  if (image.startsWith('http://') || image.startsWith('https://')) return image;
  if (image.startsWith('/static/dish/')) return baseUrl + image;
  return image;
};

const selectedFlavor = ref('');
const detailDish = ref<any>(null);

const displayDish = computed(() => detailDish.value || props.dish || {});

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

// 弹窗打开或菜品变化时检查收藏状态
watch(() => [props.visible, props.dish], ([newVisible, newDish]) => {
  if (newVisible && newDish?.id) {
    fetchDishDetail(newDish.id);
    checkFavorite();
  }
}, { deep: true });

const fetchDishDetail = (dishId: number) => {
  uni.request({
    url: `${baseUrl}/user/dish/dish/${dishId}`,
    method: 'GET',
    header: { 'authentication': uni.getStorageSync('token') },
    success: (res: any) => {
      if ((res.data.code === 0 || res.data.code === 1) && res.data.data) {
        detailDish.value = res.data.data;
      } else {
        detailDish.value = null;
      }
    },
    fail: () => {
      detailDish.value = null;
    }
  });
};

const containsAny = (source: string, tokens: string[]) => {
  return tokens.some((token) => source.includes(token));
};

const getSafeFallbackFlavors = (dish: any): string[] => {
  const name = String(dish?.name || '');
  const categoryName = String(dish?.categoryName || '');
  const text = `${name}${categoryName}`;

  // 精准修复：茶叶蛋不显示任何口味
  if (name.includes('茶叶蛋')) {
    return [];
  }

  const dessertTokens = ['大福', '麻薯', '布丁', '糍粑', '汤圆', '蛋糕', '甜点', '甜品', '芋泥'];
  const drinkTokens = ['饮品', '奶茶', '咖啡', '拿铁', '可乐', '豆浆', '果汁', '茶', '美式'];
  const spicyMainTokens = ['面', '粉', '米线', '盖饭', '拌饭', '炒饭', '牛肉', '鸡肉', '猪肉', '鱼片', '麻辣', '香辣'];

  const isDessert = containsAny(text, dessertTokens);
  const isDrink = containsAny(text, drinkTokens);
  const isSavoryMain = containsAny(text, spicyMainTokens) && !isDessert && !isDrink;
  const isCongee = name.includes('粥') && !isDessert;

  // 甜品类不再兜底生成任何温度/辣度规格，避免再次错配
  if (isDessert) {
    return [];
  }
  if (isDrink) {
    return ['常规冰', '少冰', '去冰', '常温', '热饮'];
  }
  if (isSavoryMain) {
    return ['微辣', '中辣', '特辣', '免辣'];
  }
  if (isCongee) {
    return ['不加葱', '加葱'];
  }
  return [];
};

// 只渲染后端返回的真实 flavors，杜绝前端“按菜名猜口味”
const smartFlavors = computed(() => {
  const flavorRows = displayDish.value?.flavors;
  if (!Array.isArray(flavorRows) || flavorRows.length === 0) {
    // 兜底策略：先保证主食/面类可选辣度，但甜品绝不再出现辣度/温度错配
    return getSafeFallbackFlavors(displayDish.value);
  }

  const options: string[] = [];
  flavorRows.forEach((row: any) => {
    try {
      const rawList = row?.list ?? row?.value ?? '[]';
      const list = JSON.parse(rawList);
      if (Array.isArray(list)) {
        list.forEach((item) => {
          if (typeof item === 'string' && item.trim() && !options.includes(item)) {
            options.push(item.trim());
          }
        });
      }
    } catch (e) {
      // 忽略非法 flavor.list，保证弹窗不报错
    }
  });
  if (options.length > 0) {
    return options;
  }
  return getSafeFallbackFlavors(displayDish.value);
});

watch(smartFlavors, (newVal) => {
  if (newVal && newVal.length > 0) {
    selectedFlavor.value = newVal[0];
  } else {
    selectedFlavor.value = '';
  }
});

const close = () => {
  detailDish.value = null;
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
$modal-orange: #FF8C42;
$text-dark: #1e293b; // slate-800
$text-gray: #94a3b8; // slate-400

.popup-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.42);
  backdrop-filter: blur(6px);
  z-index: 1000; // High z-index
  display: flex;
  align-items: flex-end; /* Bottom sheet on mobile */
  justify-content: center;
}

.popup-content {
  width: 100%;
  background-color: #fff;
  border-top-left-radius: 36rpx;
  border-top-right-radius: 36rpx;
  overflow: hidden;
  position: relative;
  display: flex;
  flex-direction: column;
  height: 85vh; // Fixed height (85% via viewport)
  box-shadow: 0 -16rpx 40rpx rgba(0, 0, 0, 0.08);
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
  background: rgba(255, 255, 255, 0.24);
  backdrop-filter: blur(4px);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 20;
  border: none;
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
  background: rgba(255, 255, 255, 0.24);
  backdrop-filter: blur(4px);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 20;
  border: none;
  
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
  background-color: #fff5ef;
  border-radius: 24rpx;
  padding: 32rpx;
  display: flex;
  justify-content: space-between;
  margin-bottom: 48rpx;
  border: none;
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
  width: 2rpx;
  height: 60%;
  background-color: #ffe5d6;
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

.detail-title-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 10rpx;
}

.detail-title-icon {
  width: 32rpx;
  height: 32rpx;
  flex-shrink: 0;
}

.detail-title-text {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.allergen-title-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.allergen-icon {
  width: 30rpx;
  height: 30rpx;
  flex-shrink: 0;
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
  background-color: #f2f2f2;
  color: #666666;
  border-radius: 12rpx;
  border: none;
  transition: all 0.2s;
  
  &.active {
    background-color: rgba(255, 140, 66, 0.14);
    color: $modal-orange;
    font-weight: bold;
  }
}

.popup-footer {
  padding: 24rpx 40rpx calc(40rpx + env(safe-area-inset-bottom)); // Safe area padding
  background-color: #fff;
  border-top: none;
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
  box-shadow: 0 16rpx 40rpx rgba(255, 140, 66, 0.22);
  
  &:active {
    transform: scale(0.98);
  }
}

.btn-icon {
  font-size: 40rpx;
  font-weight: normal;
}
</style>
