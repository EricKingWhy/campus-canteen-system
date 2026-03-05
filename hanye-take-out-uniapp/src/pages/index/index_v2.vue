<template>
  <view class="container">
    <!-- 1. Header Section -->
    <!-- 1. Hero Section (Unified Orange Card) -->
    <view class="hero-wrapper">
       <view class="hero-card">
          <!-- Top Row: Greeting & Weather -->
          <view class="hero-top">
             <view class="user-box">
                <image 
                   class="avatar" 
                   :src="profileStore.displayAvatar" 
                   mode="aspectFill"
                />
                <view class="text-box">
                   <text class="sub">{{ timeGreeting }}</text>
                   <text class="name">{{ profileStore.displayName }}</text>
                </view>
             </view>
             <view class="weather-box">
                <text class="icon">☀</text>
                <view class="w-info">
                   <text class="temp">24°C</text>
                   <text class="cond">晴朗</text>
                </view>
             </view>
          </view>

          <!-- Middle: Main Title -->
          <view class="hero-middle">
             <view class="tag">
                <text>🍴 {{ mealTimeSlot }}</text>
             </view>
             <view class="main-text">
                <text>想吃点</text>
                <text>健康的吗？</text>
             </view>
          </view>

          <!-- Bottom: Transparent Data Cards -->
          <view class="hero-bottom">
             <view class="glass-card">
                <view class="label">BMI指数</view>
                <view class="value-row">
                   <text class="num">{{ profileStore.calculatedBMI?.toFixed(1) || '--' }}</text>
                   <text class="badge">{{ profileStore.bmiCategory || '未知' }}</text>
                </view>
             </view>
             <view class="glass-card">
                <view class="label">今日推荐</view>
                <view class="value-row">
                   <text class="num">{{ profileStore.suggestIntake?.toFixed(0) || '--' }}</text>
                   <text class="unit">kcal</text>
                </view>
             </view>
          </view>
       </view>
    </view>

    <!-- 2. 智选6道菜 (Horizontal Scroll) -->
    <view class="section">
      <view class="section-header">
        <text class="title">智选6道菜 🎯</text>
        <text class="subtitle">根据您的健康画像定制</text>
      </view>
      <scroll-view class="recommend-scroll" scroll-x show-scrollbar="false">
        <view class="rec-card" v-for="(item, index) in recommendList" :key="index" @click="openDishDetail(item)">
          <image class="rec-img" :src="item.image" mode="aspectFill" />
          <view class="rec-info">
            <view class="rec-name">{{ item.name }}</view>
            <view class="rec-tags">
              <text class="tag" v-for="tag in item.tags" :key="tag">{{ tag }}</text>
            </view>
            <view class="rec-meta">
              <text class="calories">🔥 {{ item.calories }} kcal</text>
              <text class="stock">仅剩 5 份</text>
            </view>
            <view class="rec-action">
              <text class="price">¥{{ item.price }}</text>
              <view class="add-btn" @click.stop="openDishDetail(item)">+</view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 3. Campus Bestsellers (Flex List) -->
    <view class="section">
      <view class="section-header">
        <text class="title">全校热销榜 🔥</text>
      </view>
      <view class="bestseller-list">
        <view class="dish-row" v-for="(dish, index) in dishList" :key="dish.id" @click="openDishDetail(dish)">
           <!-- Ranking Column -->
           <view class="rank-col">
              <text v-if="index === 0" class="trophy">🏆</text>
              <text v-else-if="index === 1" class="rank-num silver">#2</text>
              <text v-else-if="index === 2" class="rank-num bronze">#3</text>
              <text v-else class="rank-num normal">{{ index + 1 }}</text>
           </view>
           
           <!-- 【核心修复】图片路径处理: 以前端传入的 baseUrl 为前缀 (如果不是http开头) -->
           <image class="dish-img" :src="dish.image && dish.image.startsWith('http') ? dish.image : (baseUrl + dish.image)" mode="aspectFill"/>
           <view class="dish-content">
              <text class="dish-name">{{ dish.name }}</text>
              <text class="dish-desc">{{ dish.detail || '暂无描述' }}</text>
              <!-- 【核心新增】真实销量展示 -->
              <text class="dish-sold" style="font-size: 20rpx; color: #ff6b00; margin-top: 6rpx;">已售 {{ dish.sold || 0 }} 份</text>
              <view class="dish-bottom">
                 <text class="dish-price">¥{{ dish.price }}</text>
                 <view class="add-circle" @click.stop="openDishDetail(dish)">+</view>
              </view>
           </view>
        </view>
      </view>
    </view>

    <!-- Spacer for fixed cart -->
    <view class="safe-area-spacer"></view>

    <!-- 4. Floating Cart Bar -->
    <view class="cart-floater">
      <view class="cart-content" @click="toggleCart">
        <view class="price-section">
          <view class="cart-icon-wrap">
            <text class="badge" v-if="cartTotalCount > 0">{{ cartTotalCount }}</text>
            <text class="cart-emoji">🛒</text>
          </view>
          <text class="total-price">¥{{ cartTotalPrice }}</text>
        </view>
        <view class="checkout-btn" @click.stop="submitOrder">
          去结算
        </view>
      </view>
    </view>
    
    <!-- Cart Popup (Simplified for stability) -->
    <view class="cart-popup-mask" v-if="openCartList" @click="openCartList = false">
       <view class="cart-popup" @click.stop>
          <view class="popup-header">
             <text>购物车</text>
             <text class="clear-btn" @click="clearCart">清空</text>
          </view>
          <scroll-view scroll-y class="popup-list">
             <view class="cart-item" v-for="(item, idx) in cartList" :key="idx">
                <text class="name">{{ item.name }}</text>
                <view class="ops">
                   <text class="btn" @click="subCart(item)">-</text>
                   <text class="num">{{ item.number }}</text>
                   <text class="btn" @click="addCart(item)">+</text>
                </view>
             </view>
          </scroll-view>
       </view>
    </view>

    <!-- Dish Detail Popup integration -->
    <DishDetailPopup 
       :visible="showDishDetail" 
       :dish="currentDetailDish"
       @close="showDishDetail = false"
       @addToCart="addToCart"
    />

  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/modules/user'
import { useUserProfileStore } from '@/stores/modules/userProfile'
import DishDetailPopup from '@/components/DishDetailPopup.vue' // Import Popup
// Keep API imports for future real data integration, but use mocks primarily now
import { getDishListAPI } from '@/api/dish'
import type { DishItem, CartItem } from '@/types/dish'

const userStore = useUserStore()
const profileStore = useUserProfileStore()
const openCartList = ref(false)

// Dish Detail Popup State
const showDishDetail = ref(false)
const currentDetailDish = ref({})

const openDishDetail = (dish: any) => {
   // Normalize image field for popup (popup uses dish.image || dish.pic)
   currentDetailDish.value = {
      ...dish,
      image: dish.image || dish.pic // ensure image is available
   }
   showDishDetail.value = true
}

// 智选6道菜 - 从后端 4层漏斗引擎获取
const recommendList = ref<any[]>([])
// 今日营养数据(用于组装 DTO)
const todayCalories = ref(0)
const todayProtein = ref(0)

// 【核心新增】动态时段与问候
const currentHour = new Date().getHours()
const mealTimeSlot = computed(() => {
   const h = new Date().getHours()
   if (h >= 6 && h < 10) return '早餐时段'
   if (h >= 10 && h < 16) return '午餐时段'
   if (h >= 16 && h < 21) return '晚餐时段'
   return '夜宵时段'
})
const timeGreeting = computed(() => {
   const h = new Date().getHours()
   if (h >= 6 && h < 12) return '早安'
   if (h >= 12 && h < 18) return '午安'
   return '晚安'
})

// Bestsellers (Standard Dish List)
const dishList = ref<DishItem[]>([])
// Simple Cart Logic - 改为从后端同步
const cartList = ref<any[]>([])
const baseUrl = 'http://localhost:8081' // 后端地址

// 【核心】获取今日营养数据(为智选6道菜提供参数)
const fetchTodayNutrition = () => {
   return new Promise<void>((resolve) => {
      uni.request({
         url: baseUrl + '/analysis/health/summary',
         method: 'GET',
         header: { 'authentication': uni.getStorageSync('token') },
         success: (res: any) => {
            const data = res.data?.data
            if (data) {
               todayCalories.value = data.todayIntakeKcal || 0
               if (data.macros) {
                  todayProtein.value = data.macros.proteinG || 0
               }
            }
            resolve()
         },
         fail: () => resolve()
      })
   })
}

// 【核心重写】智选6道菜 - POST /user/dish/smartPick6
const getRecommendData = () => {
   console.log('===== 智选6道菜引擎调用 =====');

   // 1. 组装 DTO
   const p = profileStore.profile
   const tdee = profileStore.calculatedTDEE
   const hasProfile = !!(p.gender && p.age && p.height && p.weight && tdee)

   const dto = {
      hasProfile: hasProfile,
      tdee: tdee || 2200,
      todayCalories: todayCalories.value,
      todayProtein: todayProtein.value,
      healthGoal: p.healthGoal || 3,
      avoidTags: Array.isArray(p.avoidTags) ? p.avoidTags.join(',') : (p.avoidTags || ''),
      tasteTags: Array.isArray(p.tasteTags) ? p.tasteTags.join(',') : (p.tasteTags || '')
   }

   console.log('智选6道菜 DTO:', dto);

   // 2. 调用漏斗引擎
   uni.request({
      url: baseUrl + '/user/dish/smartPick6',
      method: 'POST',
      data: dto,
      header: {
         'authentication': uni.getStorageSync('token'),
         'Content-Type': 'application/json'
      },
      success: (res: any) => {
         console.log('智选6道菜响应:', res.data);
         if (res.data.code === 0 || res.data.code === 1) {
            const dishes = res.data.data || [];
            recommendList.value = dishes.map((dish: any) => ({
               id: dish.id,
               name: dish.name,
               price: dish.price,
               calories: dish.calories || 0,
               tags: buildSmartTags(dish, dto),
               image: dish.image || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c'
            }));
            console.log('智选6道菜渲染:', recommendList.value.length, '道');
         }
      },
      fail: (err) => {
         console.error('智选6道菜请求失败，降级到普通列表:', err);
         // 降级：调普通 dish/list
         fallbackRecommend();
      }
   });
}

// 智能标签生成
const buildSmartTags = (dish: any, dto: any): string[] => {
   const tags: string[] = []
   if (dish.calories && dish.calories < 400) tags.push('低卡')
   if (dish.protein && dish.protein > 20) tags.push('高蛋白')
   if (dto.healthGoal === 1 && dish.fat && dish.fat < 10) tags.push('减脂友好')
   if (dto.healthGoal === 2 && dish.protein && dish.protein > 25) tags.push('增肌之选')
   if (tags.length === 0) tags.push('推荐')
   return tags
}

// 降级推荐(智选接口失败时)
const fallbackRecommend = () => {
   uni.request({
      url: baseUrl + '/user/dish/list',
      method: 'GET',
      data: { status: 1 },
      header: { 'authentication': uni.getStorageSync('token') },
      success: (res: any) => {
         if (res.data.code === 0 || res.data.code === 1) {
            const dishes = res.data.data || [];
            recommendList.value = dishes.slice(0, 6).map((dish: any) => ({
               id: dish.id,
               name: dish.name,
               price: dish.price,
               calories: dish.calories || 0,
               tags: ['推荐'],
               image: dish.image || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c'
            }));
         }
      }
   });
}

// 【重构】获取真实热销榜菜品 - 调用专用接口
const getDishData = () => {
   console.log('Fetching true bestseller dishes...');
   uni.request({
      url: baseUrl + '/user/dish/hotSales',
      method: 'GET',
      header: { 'authentication': uni.getStorageSync('token') },
      success: (res: any) => {
         console.log('True Bestseller dishes response:', res.data);
         if (res.data.code === 0 || res.data.code === 1) {
            // 真实榜单直接拿过来（后端已经 limit 5 了）
            const dishes = res.data.data || [];
            dishList.value = dishes.map((dish: any) => ({
               ...dish,
               pic: dish.image, // 模板使用 dish.pic
               detail: dish.description || '暂无描述'
               // sold 字段后端已包含
            }));
         }
      }
   });
}

// 【核心修复】从后端获取购物车数据
const getCartList = () => {
   console.log('=== Index_v2 getCartList called ===');
   uni.request({
      url: baseUrl + '/user/shoppingCart/list',
      method: 'GET',
      header: { 'authentication': uni.getStorageSync('token') },
      success: (res: any) => {
         console.log('Index_v2 Cart API Response:', res.data);
         if (res.data.code === 0 || res.data.code === 1) {
            cartList.value = res.data.data || [];
            console.log('Index_v2 Cart loaded:', cartList.value.length, 'items');
         }
      }
   });
}

// 【核心修复】添加到购物车 - 调用后端API
const addToCart = (item: any) => {
   console.log('=== addToCart called ===', item);
   uni.request({
      url: baseUrl + '/user/shoppingCart/add',
      method: 'POST',
      data: { 
         dishId: item.id,
         dishFlavor: item.selectedFlavor // 【核心新增】传递口味信息
      },
      header: { 
         'authentication': uni.getStorageSync('token'),
         'Content-Type': 'application/json'
      },
      success: (res: any) => {
         console.log('Add to cart response:', res.data);
         if (res.data.code === 0 || res.data.code === 1) {
            getCartList(); // 刷新购物车
            uni.showToast({ title: '已加入购物车', icon: 'success', duration: 1000 });
         } else {
            console.error('Add to cart failed:', res.data);
            uni.showToast({ title: res.data.msg || '添加失败', icon: 'none' });
         }
      },
      fail: (err) => {
         console.error('Add to cart network error:', err);
         uni.showToast({ title: '网络错误', icon: 'none' });
      }
   });
}

// 购物车弹窗内加减
const addCart = (item: any) => {
   uni.request({
      url: baseUrl + '/user/shoppingCart/add',
      method: 'POST',
      data: { dishId: item.dishId },
      header: { 'authentication': uni.getStorageSync('token') },
      success: () => getCartList()
   });
}
const subCart = (item: any) => {
   uni.request({
      url: baseUrl + '/user/shoppingCart/sub',
      method: 'POST',
      data: { dishId: item.dishId },
      header: { 'authentication': uni.getStorageSync('token') },
      success: () => getCartList()
   });
}

// 【核心修复】清空购物车 - 调用后端API
const clearCart = () => {
   uni.request({
      url: baseUrl + '/user/shoppingCart/clean',
      method: 'DELETE',
      header: { 'authentication': uni.getStorageSync('token') },
      success: (res: any) => {
         if (res.data.code === 0 || res.data.code === 1) {
            cartList.value = [];
            openCartList.value = false;
         }
      }
   });
}

const toggleCart = () => {
   if (cartList.value.length > 0) openCartList.value = !openCartList.value
}

const submitOrder = () => {
   uni.navigateTo({ url: '/pages/submit/submit' })
}

const toDetail = (dish: any) => {
   uni.navigateTo({ url: `/pages/detail/detail?dishId=${dish.id}` })
}

// Computeds - 兼容后端返回格式 (amount * number)
const cartTotalCount = computed(() => {
   return cartList.value.reduce((sum, item) => sum + (item.number || 0), 0)
})

const cartTotalPrice = computed(() => {
   return cartList.value.reduce((sum, item) => sum + ((item.amount || item.price) * (item.number || 0)), 0).toFixed(1)
})

onLoad(async () => {
   await profileStore.fetchProfile()    // 先加载画像(TDEE依赖)
   await fetchTodayNutrition()          // 再获取今日营养
   getRecommendData()                   // 然后调智选6道菜
   getDishData()                        // 获取热销榜
   getCartList()                        // 页面加载时获取购物车
})

onShow(async () => {
   console.log('=== Index_v2 PAGE onShow ===')
   await profileStore.fetchProfile()    // 同步用户画像数据
   await fetchTodayNutrition()          // 刷新营养数据
   getRecommendData()                   // 刷新智选推荐
   getDishData()                        // 刷新热销榜(确保销量实时性)
   getCartList()                        // 刷新购物车
})

</script>

<style lang="scss" scoped>
/* Vitality Orange System Tokens */
$primary: #FF6B00;
$bg-page: #F7F8FA;
$text-main: #1A1A1A;
$spacing: 32rpx;

.container {
  min-height: 100vh;
  background: $bg-page;
  padding-bottom: 200rpx; /* Space for cart bar */
}

/* Hero Section */
.hero-wrapper {
  padding: 88rpx $spacing 20rpx; // Status bar padding adaptation
  background: white; 
  
  .hero-card {
    background: linear-gradient(135deg, #FF8C00 0%, #FF6B00 100%);
    border-radius: 48rpx; // 24px
    padding: 40rpx;
    color: white;
    box-shadow: 0 16rpx 40rpx rgba(255, 107, 0, 0.35);
    position: relative;
    overflow: hidden;

    // Background decoration circle
    &::after {
       content: '';
       position: absolute;
       top: -100rpx;
       right: -100rpx;
       width: 300rpx;
       height: 300rpx;
       background: rgba(255,255,255,0.1);
       border-radius: 50%;
    }

    .hero-top {
       display: flex;
       justify-content: space-between;
       align-items: center;
       margin-bottom: 40rpx;

       .user-box {
          display: flex;
          align-items: center;
          gap: 20rpx;
          .avatar {
             width: 80rpx; height: 80rpx; border-radius: 50%; border: 2rpx solid rgba(255,255,255,0.5);
          }
          .text-box {
             display: flex; flex-direction: column;
             .sub { font-size: 22rpx; opacity: 0.9; }
             .name { font-size: 32rpx; font-weight: bold; }
          }
       }

       .weather-box {
          text-align: right;
          .icon { font-size: 40rpx; display: block; margin-bottom: 4rpx; }
          .w-info { 
             font-size: 20rpx; opacity: 0.9;
             text { margin-left: 8rpx; }
          }
       }
    }

    .hero-middle {
       margin-bottom: 40rpx;
       .tag {
          background: rgba(255,255,255,0.2);
          display: inline-block;
          padding: 6rpx 20rpx;
          border-radius: 30rpx;
          font-size: 22rpx;
          margin-bottom: 16rpx;
          backdrop-filter: blur(4px);
       }
       .main-text {
          font-size: 48rpx;
          font-weight: 800;
          line-height: 1.2;
          text { display: block; }
       }
    }

    .hero-bottom {
       display: flex;
       justify-content: space-between;
       gap: 24rpx;

       .glass-card {
          flex: 1;
          background: rgba(255,255,255,0.2);
          border-radius: 24rpx;
          padding: 20rpx;
          backdrop-filter: blur(4px);

          .label { font-size: 22rpx; opacity: 0.9; margin-bottom: 8rpx; }
          .value-row {
             display: flex; align-items: baseline; gap: 8rpx;
             .num { font-size: 36rpx; font-weight: bold; font-family: 'DIN', sans-serif;}
             .unit { font-size: 20rpx; opacity: 0.8; }
             .badge { font-size: 20rpx; background: white; color: $primary; padding: 2rpx 8rpx; border-radius: 8rpx; font-weight: bold; transform: translateY(-4rpx);}
          }
       }
    }
  }
}

/* Section Common */
.section {
  margin-top: 40rpx;
  
  .section-header {
    padding: 0 $spacing;
    margin-bottom: 24rpx;
    display: flex;
    align-items: baseline;
    gap: 16rpx;

    .title { font-size: 34rpx; font-weight: 800; color: $text-main; }
    .subtitle { font-size: 22rpx; color: #999; }
  }
}

/* Smart Recommendations */
.recommend-scroll {
  white-space: nowrap;
  padding-left: $spacing;
  height: 420rpx; // Fixed height to ensure rendering

  .rec-card {
    display: inline-block;
    width: 320rpx;
    background: white;
    border-radius: 32rpx;
    margin-right: 24rpx;
    box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.06);
    overflow: hidden;
    vertical-align: top; // Align correctly

    .rec-img {
      width: 320rpx;
      height: 220rpx;
    }

    .rec-info {
      padding: 20rpx;
      
      .rec-name { font-size: 28rpx; font-weight: bold; color: $text-main; margin-bottom: 8rpx; white-space: normal; } // Allow wrap
      
      .rec-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 8rpx;
        margin-bottom: 12rpx;
        .tag { font-size: 18rpx; color: $primary; border: 1px solid $primary; padding: 2rpx 8rpx; border-radius: 8rpx; }
      }

      .rec-meta {
        display: flex;
        justify-content: space-between;
        margin-bottom: 16rpx;
        .calories { font-size: 20rpx; color: #666; }
        .stock { font-size: 20rpx; color: #FF4B4B; } // Inventory Warning System
      }

      .rec-action {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .price { font-size: 32rpx; font-weight: bold; color: $text-main; }
        .add-btn { 
          width: 56rpx; height: 56rpx; background: $text-main; border-radius: 50%; 
          color: white; font-size: 40rpx; display: flex; align-items: center; justify-content: center;
          box-shadow: 0 4rpx 10rpx rgba(0,0,0,0.3);
        }
      }
    }
  }
}

/* Bestseller List */
.bestseller-list {
  padding: 0 $spacing;

  .dish-row {
     display: flex;
     background: white;
     padding: 24rpx;
     border-radius: 24rpx;
     margin-bottom: 24rpx;
     box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.02);
     align-items: center;

     .rank-col {
        width: 60rpx;
        display: flex;
        justify-content: center;
        margin-right: 16rpx;
        
        .trophy { font-size: 44rpx; }
        .rank-num { font-size: 32rpx; font-weight: bold; font-style: italic; font-family: 'DIN', sans-serif;}
        
        .silver { color: #A0A0A0; }
        .bronze { color: #CD7F32; }
        .normal { color: #999; font-size: 28rpx; font-style: normal; font-weight: normal; }
     }

     .dish-img { width: 140rpx; height: 140rpx; border-radius: 20rpx; margin-right: 24rpx; }
     
     .dish-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        height: 140rpx; // align height with img

        .dish-name { font-size: 30rpx; font-weight: bold; color: $text-main; }
        .dish-desc { font-size: 22rpx; color: #999; }
        
        .dish-bottom {
           display: flex;
           justify-content: space-between;
           align-items: center;
           .dish-price { font-size: 32rpx; font-weight: bold; color: $primary; }
           .add-circle { 
              width: 48rpx; height: 48rpx; border: 1px solid #eee; border-radius: 50%;
              display: flex; align-items: center; justify-content: center; color: $text-main; font-weight: bold;
           }
        }
     }
  }
}

/* Floating Cart */
.cart-floater {
  position: fixed;
  bottom: calc(var(--window-bottom) + 40rpx); // 20px = 40rpx
  left: 32rpx;
  right: 32rpx;
  z-index: 100;
  
  .cart-content {
    background: $text-main; // Dark theme for contrast (Thesis style)
    height: 100rpx;
    border-radius: 50rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 10rpx 0 32rpx; // Right padding smaller for button
    box-shadow: 0 10rpx 30rpx rgba(0,0,0,0.25);

    .price-section {
       display: flex;
       align-items: center;
       
       .cart-icon-wrap {
          position: relative;
          margin-right: 24rpx;
          .cart-emoji { font-size: 48rpx; }
          .badge { 
             position: absolute; top: -10rpx; right: -10rpx; 
             background: $primary; color: white; font-size: 20rpx; 
             width: 36rpx; height: 36rpx; border-radius: 50%; 
             text-align: center; line-height: 36rpx; border: 2rpx solid $text-main;
          }
       }
       .total-price { color: white; font-size: 36rpx; font-weight: bold; font-family: 'DIN', sans-serif;}
    }

    .checkout-btn {
       background: $primary;
       color: white;
       height: 80rpx;
       padding: 0 48rpx;
       border-radius: 40rpx;
       display: flex;
       align-items: center;
       font-weight: bold;
       font-size: 28rpx;
    }
  }
}

/* Simple Cart Popup */
.cart-popup-mask {
   position: fixed; top:0; left:0; width:100%; height:100vh; background:rgba(0,0,0,0.5); z-index:99;
   .cart-popup {
      position: absolute; bottom: 0; width: 100%; background: white; border-radius: 32rpx 32rpx 0 0; padding-bottom: calc(var(--window-bottom) + 160rpx);
      .popup-header {
         display: flex; justify-content: space-between; padding: 32rpx; font-weight: bold; border-bottom: 1px solid #f5f5f5;
         .clear-btn { color: #999; font-weight: normal; font-size: 26rpx; }
      }
      .popup-list {
         max-height: 500rpx;
         .cart-item {
            display: flex; justify-content: space-between; align-items: center; padding: 24rpx 32rpx; border-bottom: 1px solid #f9f9f9;
            .name { font-size: 28rpx; font-weight: 500; }
            .ops { 
               display: flex; align-items: center; gap: 20rpx;
               .btn { width: 40rpx; height: 40rpx; background: #f5f5f5; border-radius: 50%; text-align: center; line-height: 40rpx; }
            }
         }
      }
   }
}
</style>
