<template>
  <view class="container">
    <!-- 1. Top Search Header -->
    <view class="header">
      <view class="search-box">
        <view class="search-icon">
          <image class="search-icon-img" src="/static/icon/sousuo.png" mode="aspectFit"></image>
        </view>
        <input class="search-input" 
               v-model="searchKeyword" 
               @confirm="handleSearch"
               @focus="handleSearchFocus"
               @blur="handleSearchBlur"
               :placeholder="isSearchFocused ? '' : searchPlaceholderText"
               placeholder-style="color:#9ca3af" />
      </view>
    </view>

    <view class="main-body">
      <!-- 2. Left Sidebar (Categories) -->
      <scroll-view class="sidebar" scroll-y>
        <view 
          class="menu-item" 
          :class="{ active: activeCategoryIndex === index }"
          v-for="(item, index) in categoryList" 
          :key="item.id"
          @click="onCategoryClick(index)"
        >
          <view class="active-indicator" v-if="activeCategoryIndex === index"></view>
          <text class="menu-text">{{ item.name }}</text>
        </view>
        <!-- Padding for bottom -->
        <view style="height: 100rpx;"></view>
      </scroll-view>

      <!-- 3. Right Content (Dishes) -->
      <scroll-view class="content" scroll-y>
        <view class="content-wrapper">
          <!-- Category Title -->
          <view class="category-title-sticky" v-if="currentCategory && activeCategoryIndex !== -1">
            <view class="title-row">
              <image :src="getCategoryIcon(currentCategory.name)" class="title-icon" mode="aspectFit" />
              <text class="title-text">{{ currentCategory.name }}</text>
            </view>
          </view>
          <view class="category-title-sticky" v-else-if="activeCategoryIndex === -1 && searchKeyword">
            <text class="title-text">搜索结果: "{{ searchKeyword }}"</text>
          </view>

          <!-- Dish List -->
          <view class="dish-item" v-for="(item, index) in dishList" :key="item.id" @click="openNutrition(item)">
            <!-- Dish Image -->
            <image class="dish-img" :src="resolveImageUrl(item.image || item.pic)" mode="aspectFill"></image>
            
            <view class="dish-info-col">
              <view>
                <text class="dish-name">{{ item.name }}</text>
                <text class="dish-desc">{{ item.description || '暂无描述' }}</text>
                <view class="tags-row">
                  <view class="tag-badge grey" v-if="item.calories">
                    <text>🔥 {{ item.calories }}kcal</text>
                  </view>
                  <view class="tag-badge orange" v-if="index % 2 === 0"> <!-- Mock tag logic -->
                    <text>💪 高蛋白</text>
                  </view>
                </view>
              </view>

              <view class="price-action-row">
                <view class="price-wrap">
                  <text class="symbol">¥</text>
                  <text class="amount">{{ item.price }}</text>
                </view>

                <!-- Add Button / Stepper - 根据购物车数量显示 -->
                <view class="add-btn-wrap">
                  <!-- 如果购物车里有这个菜品，显示数量控制 -->
                  <view class="stepper" v-if="getCartQuantity(item.id) > 0">
                    <view class="stepper-btn" @click.stop="handleCart(item, -1)">-</view>
                    <text class="stepper-num">{{ getCartQuantity(item.id) }}</text>
                    <view class="stepper-btn stepper-add" @click.stop="handleCart(item, 1)">+</view>
                  </view>
                  <!-- 如果购物车里没有，显示"选规格"按钮 -->
                  <view class="add-btn" v-else @click.stop="addToCart(item)">
                     <text class="plus-icon">+</text>
                     <text style="font-size: 24rpx; font-weight: bold;">选规格</text>
                  </view>
                </view>
              </view>
            </view>
          </view>

          <!-- Empty State -->
          <view class="empty-state" v-if="dishList.length === 0">
            <image src="/static/empty.png" style="width: 200rpx; height: 200rpx; margin-bottom: 20rpx;" />
            <text style="color: #999; font-size: 26rpx; margin-bottom: 20rpx;">该分类下暂无菜品</text>
            <button 
                @click="fixData"
                style="background: #ff9900; color: white; font-size: 24rpx; padding: 0 30rpx; border-radius: 30rpx;"
            >
                点我一键修复数据
            </button>
          </view>

          <!-- Padding for cart bar -->
          <view style="height: 180rpx;"></view>
        </view>
      </scroll-view>
    </view>


    <!-- 4. Floating Cart Bar -->
    <view class="cart-floater" @click="toggleCart">
      <view class="cart-bar">
        <view class="cart-left">
          <view class="icon-circle">
            <text class="iconfont icon-cart" style="font-size: 40rpx; color: white;">🛒</text>
             <view class="badge" v-if="totalNum > 0">{{ totalNum }}</view>
          </view>
          <view class="price-info">
            <view class="main-price">
              <text>¥{{ totalAmount }}</text>
            </view>
          </view>
        </view>
        <view class="checkout-btn" @click.stop="goSubmit">
          去结算
        </view>
      </view>
    </view>

    <!-- 5. Cart Popup -->
    <view class="mask" v-if="cartPopupShow" @click="cartPopupShow=false"></view>
    <view class="cart-popup" :class="{show: cartPopupShow}">
      <view class="popup-header">
        <text style="font-size: 32rpx; font-weight: bold;">购物车</text>
        <view class="clear-btn" @click="clearCart">
          <text>🗑️ 清</text>
        </view>
      </view>
      <scroll-view scroll-y class="popup-list">
        <view class="popup-item" v-for="(item, index) in cartList" :key="index">
          <view class="info">
             <text class="name">{{ item.name }}</text>
             <text class="spec" v-if="item.dishFlavor">{{ item.dishFlavor }}</text>
          </view>
          <text class="price">¥{{ item.amount }}</text>
          <view class="stepper">
             <view class="btn sub" @click="handleCart(item, -1)">-</view>
             <text class="num">{{ item.number }}</text>
             <view class="btn add" @click="handleCart(item, 1)">+</view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 6. Dish Detail Popup (Full Stack Integration) -->
    <DishDetailPopup 
       :visible="showNutritionPopup" 
       :dish="currentDish"
       @close="closeNutrition"
       @addToCart="addToCartFromPopup"
    />

  </view>
</template>

<script>
import DishDetailPopup from '@/components/DishDetailPopup.vue';
import iconHot from '@/static/icons/hot.png';
import iconBreakfast from '@/static/icons/breakfast.png';
import iconLunch from '@/static/icons/lunch.png';
import iconDinner from '@/static/icons/dinner.png';
import iconDrink from '@/static/icons/drink.png';
import iconStaple from '@/static/icons/staple.png';

export default {
  components: {
    DishDetailPopup
  },
  data() {
    return {
      baseUrl: 'http://127.0.0.1:8081', // 后端基准地点点址
      categoryList: [],
      dishList: [],
      cartList: [],
      activeCategoryIndex: 0,
      searchKeyword: '', // 【核心新增】搜索关键词绑定
      searchPlaceholders: ['低脂鸡胸肉', '黄焖鸡', '水果拼盘', '麻婆豆腐', '酸菜鱼'],
      currentPlaceholderIndex: 0,
      searchPlaceholderText: '搜索想吃的菜品 (如：低脂鸡胸肉)',
      placeholderTimer: null,
      isSearchFocused: false,
      cartPopupShow: false,
      showNutritionPopup: false,
      currentDish: {} // Will hold full dish data including nutrition and sold
    };
  },
  computed: {
    currentCategory() {
      return this.categoryList[this.activeCategoryIndex] || null;
    },
    totalAmount() {
      let total = 0;
      this.cartList.forEach(item => total += item.amount * item.number);
      return total.toFixed(2);
    },
    totalNum() {
      let num = 0;
      this.cartList.forEach(item => num += item.number);
      return num;
    }
  },
  onLoad() {
    // 移至 onShow 保证每次显示都刷新
  },
  onShow() {
    console.log('Category Page onShow - Initialization');
    this.getCartList();
    this.getCategoryList();
    this.startSearchPlaceholderTicker();
  },
  onHide() {
    this.stopSearchPlaceholderTicker();
    this.isSearchFocused = false;
  },
  onUnload() {
    this.stopSearchPlaceholderTicker();
  },
  methods: {
    handleSearchFocus() {
      this.isSearchFocused = true;
    },
    handleSearchBlur() {
      this.isSearchFocused = false;
    },
    startSearchPlaceholderTicker() {
      this.stopSearchPlaceholderTicker();
      this.updateSearchPlaceholder(true);
      this.placeholderTimer = setInterval(() => {
        if (this.isSearchFocused) return;
        this.currentPlaceholderIndex =
          (this.currentPlaceholderIndex + 1) % this.searchPlaceholders.length;
        this.updateSearchPlaceholder();
      }, 5000);
    },
    stopSearchPlaceholderTicker() {
      if (!this.placeholderTimer) return;
      clearInterval(this.placeholderTimer);
      this.placeholderTimer = null;
    },
    updateSearchPlaceholder(reset = false) {
      if (!this.searchPlaceholders.length) {
        this.searchPlaceholderText = '搜索想吃的菜品';
        return;
      }
      if (reset) this.currentPlaceholderIndex = 0;
      const current = this.searchPlaceholders[this.currentPlaceholderIndex];
      this.searchPlaceholderText = `搜索想吃的菜品 (如：${current})`;
    },
    getCategoryIcon(categoryName) {
      if (!categoryName) return iconHot;
      if (categoryName.includes('热销')) return iconHot;
      if (categoryName.includes('早餐')) return iconBreakfast;
      if (categoryName.includes('午餐')) return iconLunch;
      if (categoryName.includes('晚餐')) return iconDinner;
      if (categoryName.includes('甜点') || categoryName.includes('饮品')) return iconDrink;
      if (categoryName.includes('主食') || categoryName.includes('面点')) return iconStaple;
      return iconHot;
    },
    resolveImageUrl(image) {
      if (!image) return '/static/default_dish.png';
      if (image.startsWith('http://') || image.startsWith('https://')) return image;
      if (image.startsWith('/static/dish/')) return this.baseUrl + image;
      return image;
    },
    // 1. 获取分类
    getCategoryList() {
      console.log('Fetching Categories from:', this.baseUrl + '/user/category/list');
      uni.request({
        url: this.baseUrl + '/user/category/list',
        method: 'GET',
        data: { type: 1 }, 
        header: { 'authentication': uni.getStorageSync('token') },
        success: (res) => {
          console.log('Category Response:', res.data);
          if (res.data.code === 0 || res.data.code === 1) { // 兼容 0 和 1 两种成功码
            this.categoryList = res.data.data;
            if (this.categoryList.length > 0) {
              console.log('Loading dishes for category:', this.categoryList[0].id);
              this.getDishList(this.categoryList[0].id);
            } else {
              console.warn('Category List is Empty!');
            }
          } else {
            console.error('Category API Failed:', res.data.msg);
          }
        },
        fail: (err) => {
           console.error('Category Request Network Error:', err);
        }
      });
    },

    // 2. 获取菜品 (带图片修复)
    getDishList(categoryId) {
      console.log('Fetching Dishes for Category:', categoryId);
      uni.request({
        url: this.baseUrl + '/user/dish/list',
        method: 'GET',
        data: { categoryId: categoryId, status: 1 }, // 起售状态
        header: { 'authentication': uni.getStorageSync('token') },
        success: (res) => {
          console.log('Dish Response:', res.data);
          if (res.data.code === 0 || res.data.code === 1) { // 兼容 0 和 1 两种成功码
            const rawList = res.data.data;
            // 修复图片路径
            this.dishList = rawList.map(item => {
              if (item.image && !item.image.startsWith('http')) {
                // 如果是相对路径，拼接 baseUrl (针对 /static/images/xxx.jpg)
                // 这里假设后端存的是文件名或相对路径
                // 如果是完整路径则不动
                // 用户素材提示：item.image (图片)
                // 如果后端返回的是完整url则直接用，否则拼接
                // 通常系统存的是文件名，但也可能是完整oss路径
                // 若不含http，则拼接
                item.image = item.image.startsWith('/') ? (this.baseUrl + item.image) : item.image; 
                // 防止单纯文件名
                if (!item.image.startsWith('http') && !item.image.startsWith('/')) {
                    // 假设是阿里云OSS或其他，这里为了保险起见，如果不带http，暂且认为是本地点点资源或需特殊处理
                    // 但通常是完整URL。如果用户说"从后端正确加载"，这里做个兼容
                }
              }
              return item;
            });
          }
        }
      });
    },

    // 3. 点击分类
    onCategoryClick(index) {
      this.activeCategoryIndex = index;
      this.searchKeyword = ''; // 切换分类时清搜索框
      const catId = this.categoryList[index].id;
      this.getDishList(catId);
    },

    // 【核心新增】全局搜索处理
    handleSearch() {
      const keyword = this.searchKeyword.trim();
      if (!keyword) {
        // 清搜索框后回车，恢复到当前分类或者默认第一个分类
        if (this.categoryList.length > 0) {
           this.activeCategoryIndex = this.activeCategoryIndex === -1 ? 0 : this.activeCategoryIndex;
           this.getDishList(this.categoryList[this.activeCategoryIndex].id);
        }
        return;
      }
      
      console.log('Searching for:', keyword);
      // 取消左侧所有分类高亮
      this.activeCategoryIndex = -1;
      
      // 发送全局搜索请求 (不传 categoryId，传 name)
      uni.request({
        url: this.baseUrl + '/user/dish/list',
        method: 'GET',
        data: { name: keyword, status: 1 }, 
        header: { 'authentication': uni.getStorageSync('token') },
        success: (res) => {
          if (res.data.code === 0 || res.data.code === 1) { 
            const rawList = res.data.data || [];
            // 修复图片路径 (与 getDishList 保持一致)
            this.dishList = rawList.map(item => {
               if (item.image && !item.image.startsWith('http')) {
                  item.image = item.image.startsWith('/') ? (this.baseUrl + item.image) : item.image; 
               }
               return item;
            });
          } else {
             console.error('Search API Failed:', res.data.msg);
          }
        },
        fail: (err) => {
           console.error('Search Request Network Error:', err);
        }
      });
    },

    // 4. 购物车列表
    getCartList() {
      console.log('=== Category getCartList called ===');
      uni.request({
        url: this.baseUrl + '/user/shoppingCart/list',
        method: 'GET',
        header: { 'authentication': uni.getStorageSync('token') },
        success: (res) => {
          console.log('Cart API Response:', res.data);
          if (res.data.code === 0 || res.data.code === 1) { // 兼容两种成功码
            this.cartList = res.data.data || [];
            console.log('Cart items loaded:', this.cartList.length, 'items, total:', this.totalPrice);
          } else {
            console.error('Cart API Failed:', res.data);
          }
        },
        fail: (err) => {
          console.error('Cart API Network Error:', err);
        }
      });
    },

    // 5. 操作购物车
    handleCart(item, type) {
      // type: 1=add, -1=sub
      const apiUrl = type === 1 ? '/user/shoppingCart/add' : '/user/shoppingCart/sub';
      
      let payload = {};
      if (item.dishId) {
        // 已经在购物车的项
        payload = { dishId: item.dishId };
        if (item.setmealId) payload.setmealId = item.setmealId;
        if (item.dishFlavor) payload.dishFlavor = item.dishFlavor;
      } else {
        // 还没进购物车的项 (直接点列表加号)
        payload = { dishId: item.id };
        // 【核心新增】传递口味信息
        if (item.selectedFlavor) payload.dishFlavor = item.selectedFlavor;
      }

      uni.request({
        url: this.baseUrl + apiUrl,
        method: 'POST',
        data: payload,
        header: { 'authentication': uni.getStorageSync('token') },
        success: (res) => {
          if (res.data.code === 0 || res.data.code === 1) { // 兼容两种成功码
            this.getCartList(); // 刷新购物车
          }
        }
      });
    },

    addToCart(item) {
        this.handleCart(item, 1);
    },

    clearCart() {
       console.log('Clear cart called');
       uni.request({
          url: this.baseUrl + '/user/shoppingCart/clean',
          method: 'DELETE',
          header: { 'authentication': uni.getStorageSync('token') },
          success: (res) => {
             console.log('Clear cart response:', res.data);
             if (res.data.code === 0 || res.data.code === 1) {
               this.cartList = [];
               this.cartPopupShow = false;
               this.getCartList();
             }
          },
          fail: (err) => {
             console.error('Clear cart failed:', err);
          }
       });
    },

    toggleCart() {
      if (this.cartList.length > 0) {
        this.cartPopupShow = !this.cartPopupShow;
      }
    },
    
    goSubmit() {
       if (this.cartList.length === 0) return;
       uni.navigateTo({ url: '/pages/submit/submit' });
    },

    // Nutrition Popup
    openNutrition(item) {
       this.currentDish = item;
       this.showNutritionPopup = true;
    },
    closeNutrition() {
       this.showNutritionPopup = false;
    },
    addToCartFromPopup(dishWithFlavor) {
       // Check if we received the dish object from the event
       const dish = dishWithFlavor || this.currentDish;
       this.addToCart(dish);
       this.closeNutrition();
    },
    
    // 获取某个菜品在购物车中的数量
    getCartQuantity(dishId) {
        const cartItem = this.cartList.find(item => item.dishId === dishId);
        return cartItem ? cartItem.number : 0;
    },
    
    // 一键修复数据逻辑
    fixData() {
        uni.showLoading({ title: '正在修复...' });
        uni.request({
            url: this.baseUrl + '/user/db/fix',
            method: 'POST',
            header: { 'authentication': uni.getStorageSync('token') },
            success: (res) => {
                uni.hideLoading();
                if (res.data.code === 0 || res.data.code === 1) {
                    uni.showToast({ title: '修复成功！刷新中...', icon: 'success' });
                    setTimeout(() => {
                        this.getCategoryList(); // 重新加载
                    }, 1500);
                } else {
                    uni.showModal({ title: '修复失败', content: res.data.msg || '未知错误' });
                }
            },
            fail: () => {
                uni.hideLoading();
                uni.showToast({ title: '请求失败', icon: 'none' });
            }
        });
    }
  }
};
</script>

<style lang="scss" scoped>
/* Design Token Variables */
$primary: #ff9900;
$bg-page: #ffffff;
$bg-sidebar: #F7F8FA;
$text-dark: #1d160c;
$text-light: #9ca3af;
$price-red: #ef4444;

.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: $bg-page;
  overflow: hidden;
}

/* 1. Header */
.header {
   padding: 16rpx 32rpx;
   background: white;
   border-bottom: 1rpx solid #f3f4f6;
   flex: none;
   
   .search-box {
      display: flex; align-items: center;
      background: $bg-sidebar;
      height: 80rpx;
      border-radius: 16rpx;
      padding: 0 24rpx;
      
      .search-icon { margin-right: 16rpx; }
      .search-icon-img { width: 34rpx; height: 34rpx; display: block; }
      .search-input { flex: 1; font-size: 28rpx; color: $text-dark; }
   }
}

/* 2. Body Layout */
.main-body {
   flex: 1;
   display: flex;
   overflow: hidden;
   position: relative;
}

/* Sidebar */
.sidebar {
   width: 176rpx; /* 88px * 2 */
   background: $bg-sidebar;
   height: 100%;
   
   .menu-item {
      padding: 32rpx 16rpx;
      position: relative;
      display: flex; justify-content: center; align-items: center;
      transition: all 0.2s;
      
      .menu-text {
         font-size: 26rpx;
         color: #6b7280;
         text-align: center;
         line-height: 1.4;
      }
      
      &.active {
         background: white;
         
         .menu-text {
            color: $text-dark;
            font-weight: bold;
         }
         
         .active-indicator {
            position: absolute;
            left: 0; top: 0; bottom: 0;
            width: 8rpx;
            background: $primary;
            border-radius: 0 4rpx 4rpx 0;
         }
      }
   }
}

/* Right Content */
.content {
   flex: 1;
   height: 100%;
   background: white;
   
   .content-wrapper {
      padding: 0 24rpx;
   }
   
   .category-title-sticky {
      position: sticky;
      top: 0;
      background: rgba(255,255,255,0.95);
      z-index: 10;
      padding: 24rpx 0 16rpx 0;

      .title-row {
         display: flex;
         align-items: center;
         gap: 10rpx;
      }

      .title-icon {
         width: 34rpx;
         height: 34rpx;
         flex: none;
      }
      
      .title-text {
         font-size: 28rpx;
         font-weight: bold;
         color: $text-dark;
      }
   }
   
   .dish-item {
      display: flex;
      margin-bottom: 48rpx;
      
      .dish-img {
         width: 192rpx; /* 24 * 4 * 2 = 192rpx (Tailwind w-24) */
         height: 192rpx;
         border-radius: 16rpx;
         background: #f3f4f6;
         flex: none;
         margin-right: 24rpx;
      }
      
      .dish-info-col {
         flex: 1;
         display: flex;
         flex-direction: column;
         justify-content: space-between;
         min-width: 0; /* truncate fix */
         
         .dish-name {
            font-size: 30rpx;
            font-weight: bold;
            color: $text-dark;
            white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
         }
         
         .dish-desc {
            font-size: 24rpx;
            color: $text-light;
            margin-top: 8rpx;
            white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
         }
         
         .tags-row {
            display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 12rpx;
            
            .tag-badge {
               padding: 4rpx 12rpx;
               border-radius: 8rpx;
               font-size: 20rpx;
               font-weight: 500;
               
               &.grey { background: #f3f4f6; color: #6b7280; }
               &.orange { background: #fff7ed; color: $primary; }
               &.green { background: #f0fdf4; color: #16a34a; }
            }
         }
         
         .price-action-row {
            display: flex; justify-content: space-between; align-items: flex-end; margin-top: 16rpx;
            
            .price-wrap {
               padding-bottom: 4rpx;
               .symbol { font-size: 24rpx; font-weight: bold; color: $price-red; }
               .amount { font-size: 36rpx; font-weight: bold; color: $price-red; margin-left: 4rpx; }
            }
            
            .add-btn-wrap {
               display: flex;
               align-items: center;
            }
            
            .add-btn {
               background: $primary;
               color: white;
               padding: 8rpx 24rpx;
               border-radius: 999px;
               display: flex; align-items: center; gap: 8rpx;
               box-shadow: 0 4rpx 12rpx rgba(255,107,0,0.2);
               
               .plus-icon { font-size: 28rpx; font-weight: bold; }
            }
            
            .stepper {
               display: flex;
               align-items: center;
               gap: 16rpx;
               
               .stepper-btn {
                  width: 48rpx;
                  height: 48rpx;
                  border-radius: 50%;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  font-size: 32rpx;
                  font-weight: bold;
                  background: #f5f5f5;
                  color: #666;
                  
                  &.stepper-add {
                     background: $primary;
                     color: white;
                  }
               }
               
               .stepper-num {
                  font-size: 28rpx;
                  font-weight: bold;
                  min-width: 36rpx;
                  text-align: center;
               }
            }
         }
      }
   }
   
   .empty-state {
      display: flex; flex-direction: column; align-items: center; justify-content: center;
      padding-top: 100rpx;
   }
}

/* Floating Cart Bar */
.cart-floater {
   position: fixed;
   bottom: calc(var(--window-bottom) + 40rpx);
   left: 32rpx;
   right: 32rpx;
   z-index: 100;
   
   .cart-bar {
      height: 100rpx;
      background: #1A1A1A;
      border-radius: 50rpx;
      display: flex; align-items: center; justify-content: space-between;
      padding: 0 10rpx 0 20rpx;
      box-shadow: 0 10rpx 30rpx rgba(0,0,0,0.25);
      
      .cart-left {
         display: flex; align-items: center;
         
         .icon-circle {
            width: 72rpx; height: 72rpx;
            background: #333;
            border-radius: 50%;
            display: flex; justify-content: center; align-items: center;
            position: relative;
            margin-right: 20rpx;
            
            .badge {
               position: absolute; top: -10rpx; right: -10rpx;
               background: #ff4d4f; color: white; font-size: 20rpx;
               width: 36rpx; height: 36rpx; border-radius: 50%;
               text-align: center; line-height: 36rpx; border: 2rpx solid #1A1A1A;
            }
         }
         
         .price-info {
            display: flex; flex-direction: column;
            .main-price { color: white; font-weight: bold; font-size: 44rpx; letter-spacing: 1rpx; }
            .sub-text { color: #9ca3af; font-size: 20rpx; }
         }
      }
      
      .checkout-btn {
         background: #ffa000;
         color: white;
         font-size: 28rpx; font-weight: bold;
         height: 80rpx;
         padding: 0 48rpx;
         border-radius: 999rpx;
         display: flex; align-items: center;
      }
   }
}

/* Cart Popup */
.mask {
   position: fixed; top: 0; left: 0; width: 100%; height: 100%;
   background: rgba(0,0,0,0.5); z-index: 40;
}
.cart-popup {
   position: fixed; bottom: 0; left: 0; width: 100%;
   background: white; z-index: 45;
   border-radius: 32rpx 32rpx 0 0;
   padding-bottom: 200rpx; /* Space for floater */
   transform: translateY(100%); transition: transform 0.3s;
   
   &.show { transform: translateY(0); }
   
   .popup-header {
      padding: 32rpx;
      display: flex; justify-content: space-between; align-items: center;
      border-bottom: 1rpx solid #f3f4f6;
      
      .clear-btn { color: #9ca3af; font-size: 26rpx; }
   }
   
   .popup-list {
      max-height: 600rpx;
   }
   
   .popup-item {
      display: flex; align-items: center; justify-content: space-between;
      padding: 24rpx 32rpx;
      border-bottom: 1rpx solid #f9fafb;
      
      .info {
         flex: 1;
         .name { font-size: 30rpx; color: $text-dark; font-weight: 500; }
         .spec { font-size: 22rpx; color: #9ca3af; margin-top: 4rpx; }
      }
      
      .price { font-size: 32rpx; font-weight: bold; color: $text-dark; margin-right: 32rpx; }
      
      .stepper {
         display: flex; align-items: center;
         .btn { 
            width: 48rpx; height: 48rpx; 
            border-radius: 50%; 
            display: flex; align-items: center; justify-content: center;
            font-weight: bold;
         }
         .sub { border: 2rpx solid #e5e7eb; color: #6b7280; background: white; }
         .add { background: $primary; color: white; }
         .num { margin: 0 24rpx; font-size: 30rpx; font-weight: 500; }
      }
   }
}

/* Nutrition Popup */
.nutrition-mask {
   position: fixed; top:0; left:0; width:100%; height:100%;
   background: rgba(0,0,0,0.7); z-index: 100;
   display: flex; align-items: center; justify-content: center;
   
   .nutrition-card {
      width: 600rpx;
      background: white;
      border-radius: 40rpx;
      overflow: hidden;
      
      .card-img { width: 100%; height: 450rpx; background: #eee; }
      
      .card-content {
         padding: 32rpx;
         
         .card-header {
            display: flex; justify-content: space-between; align-items: center;
            margin-bottom: 16rpx;
            .c-name { font-size: 36rpx; font-weight: bold; color: $text-dark; }
            .c-price { font-size: 40rpx; font-weight: bold; color: $primary; }
         }
         
         .c-desc { font-size: 26rpx; color: $text-light; margin-bottom: 40rpx; display: block; }
         
         .bento-grid {
            display: grid; grid-template-columns: 1fr 1fr; gap: 20rpx; margin-bottom: 48rpx;
            
            .bento-box {
               background: #F7F8FA; border-radius: 24rpx; padding: 20rpx;
               display: flex; flex-direction: column; align-items: center;
               
               .lbl { font-size: 22rpx; color: $text-light; margin-bottom: 8rpx; }
               .val { font-size: 30rpx; font-weight: bold; color: $text-dark; }
               .val.orange { color: $primary; }
               .val.green { color: #16a34a; }
            }
         }
         
         .card-action {
            .add-cart-pill {
               width: 100%; height: 96rpx;
               background: $primary; color: white;
               border-radius: 999px;
               font-size: 32rpx; font-weight: bold;
               display: flex; align-items: center; justify-content: center;
            }
         }
      }
   }
}
</style>

