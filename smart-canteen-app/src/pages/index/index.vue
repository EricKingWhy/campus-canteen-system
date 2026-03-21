<template>
  <Navbar />
  <view class="page">
    <view class="health-board">
      <view class="health-title">????</view>
      <view class="health-main">
        <view class="health-greet">Hi, {{ userName || '??' }}</view>
        <view v-if="healthStats && healthStats.bmi !== null" class="health-metrics">
          <view class="metric">BMI: {{ healthStats.bmi }}</view>
          <view class="metric">??: {{ healthStats.bmiStatus }}</view>
          <view class="metric">????: {{ healthStats.targetCalories }} kcal</view>
        </view>
        <view v-else class="health-empty">???????</view>
        <view v-if="healthStats && healthStats.suggestion" class="health-tip">
          {{ healthStats.suggestion }}
        </view>
      </view>
    </view>

    <view class="recommend-section" v-if="recommendList.length > 0">
      <view class="section-title">????</view>
      <scroll-view class="recommend-scroll" scroll-x show-scrollbar="false">
        <view class="recommend-card" v-for="dish in recommendList" :key="dish.id" @tap="toDetail(dish)">
          <image class="recommend-image" :src="resolveImageUrl(dish.pic || dish.image)" mode="aspectFill" />
          <view class="recommend-info">
            <view class="recommend-name ellipsis">{{ dish.name }}</view>
            <view class="recommend-stall">{{ dish.stallName || '????' }}</view>
            <view class="recommend-reason" v-if="dish.recommendReason">{{ dish.recommendReason }}</view>
            <view class="recommend-price">?{{ dish.price }}</view>
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="menu-section">
      <view class="viewport">
        <!-- ?? -->
        <view class="categories">
          <!-- ???????-->
          <scroll-view class="primary" scroll-y>
            <view
              v-for="(item, index) in categoryList"
              :key="item.id"
              class="item"
              :class="{active: index === activeIndex}"
              @tap="getDishOrSetmealList(index)"
            >
              <text class="name"> {{ item.name }} </text>
            </view>
          </scroll-view>
          <!-- ?????/???? -->
          <scroll-view class="secondary" scroll-y>
            <view class="section">
              <navigator
                v-for="dish in dishList"
                :key="dish.id"
                class="dish"
                hover-class="none"
                :url="`/pages/detail/detail?${categoryList[activeIndex].sort < 20 ? 'dishId' : 'setmealId'}=${dish.id}`"
              >
                <image class="image" :src="resolveImageUrl(dish.pic || dish.image)"></image>
                <view class="dishinfo">
                  <view class="name ellipsis">{{ dish.name }}</view>
                  <view class="detail">{{ dish.detail }}</view>
                  <view class="price">
                    <text class="symbol">?</text>
                    <text class="number">{{ dish.price }}</text>
                  </view>
                  <!-- 1?????(??) -->
                  <image
                    v-if="'flavors' in dish && dish.flavors.length > 0"
                    class="choosenorm"
                    src="../../static/images/????.png"
                    @tap.stop="chooseNorm(dish)"
                    mode="scaleToFill"
                  />
                  <!-- 2?????-->
                  <view v-else class="sub_add">
                    <!-- ???? -->
                    <image
                      v-if="getCopies(dish) > 0"
                      src="../../static/icon/sub.png"
                      @tap.stop="subDishAction(dish, '??')"
                      class="sub"
                    ></image>
                    <!-- ???? -->
                    <text v-if="getCopies(dish) > 0" class="dish_number">{{ getCopies(dish) }}</text>
                    <!-- ???? -->
                    <image src="../../static/icon/add.png" @tap.stop="addDishAction(dish, '??')" class="add" />
                  </view>
                </view>
              </navigator>
            </view>
          </scroll-view>
        </view>
      </view>
    </view>
  </view>

  <!-- ??????dialog?? -->
  <view class="dialog" v-show="visible">
    <view class="flavor_pop">
      <view class="title">????</view>
      <scroll-view class="scroll" scroll-y>
        <!-- ?????? -->
        <view v-for="flavor in flavors" :key="flavor.name" class="flavor">
          <view>{{ flavor.name }}</view>
          <view
            :class="{flavorItem: true, active: chosedflavors.findIndex((it) => item === it) !== -1}"
            v-for="item in JSON.parse(flavor.list)"
            :key="`${flavor.name}-${item}`"
            @tap="chooseFlavor(JSON.parse(flavor.list), item)"
          >
            {{ item }}
          </view>
        </view>
      </scroll-view>
      <view class="addToCart" @tap="addToCart(dialogDish as DishToCartItem)">?????</view>
    </view>
    <view class="close_dialog" @click="visible = false">?</view>
  </view>

  <!-- ?????? -->
  <view class="footer_order_buttom" v-if="cartList.length === 0">
    <view class="order_number">
      <image src="../../static/images/cart_empty.png" class="order_number_icon"></image>
    </view>
    <view class="order_price"> <text class="ico">?</text> 0 </view>
    <view class="order_btn"> ?????? </view>
  </view>
  <!-- ?????? -->
  <view class="footer_order_buttom" @click="() => (openCartList = !openCartList)" v-else>
    <view class="order_number">
      <image src="../../static/images/cart_active.png" class="order_number_icon"></image>
      <view class="order_dish_num"> {{ CartAllNumber }} </view>
    </view>
    <view class="order_price">
      <text class="ico">?</text> {{ parseFloat((Math.round(CartAllPrice * 100) / 100).toFixed(2)) }}
    </view>
    <view class="order_btn_active" @click.stop="submitOrder()"> ???</view>
  </view>

  <!-- ?????????-->
  <view class="pop_mask" v-show="openCartList" @click="openCartList = !openCartList">
    <view class="cart_pop" @click.stop="openCartList = openCartList">
      <view class="top_title">
        <view class="tit"> ???</view>
        <view class="clear" @click.stop="clearCart()">
          <image class="clear_icon" src="../../static/icon/clear.png"></image>
          <text class="clear-des">?? </text>
        </view>
      </view>
      <scroll-view class="card_order_list" scroll-y scroll-top="40rpx">
        <view class="type_item" v-for="obj in cartList" :key="`${obj.dishId || obj.setmealId || obj.id || obj.name}-${obj.dishFlavor || ''}`">
          <view class="dish_img">
            <image mode="aspectFill" :src="resolveImageUrl(obj.pic || obj.image)" class="dish_img_url"></image>
          </view>
          <view class="dish_info">
            <view class="dish_name"> {{ obj.name }} </view>
            <view class="dish_price"> <text class="ico">?</text> {{ obj.amount }} </view>
            <view class="dish_flavor"> {{ obj.dishFlavor }} </view>
            <view class="dish_active">
              <image
                v-if="obj.number && obj.number > 0"
                src="../../static/icon/sub.png"
                @click.stop="subDishAction(obj, '???')"
                class="dish_sub"
              ></image>
              <text v-if="obj.number && obj.number > 0" class="dish_number">{{ obj.number }}</text>
              <image src="../../static/icon/add.png" class="dish_add" @click.stop="addDishAction(obj, '???')">
              </image>
            </view>
          </view>
        </view>
        <view class="seize_seat"></view>
      </scroll-view>
    </view>
  </view>

  <view v-show="!status" class="close" @click="goBack">
    <view class="text">?????</view>
  </view>
</template>

<script setup lang="ts">
import { getStatusAPI } from '@/api/shop'
import { getCategoryAPI } from '@/api/category'
import { getDishListAPI, getHealthStatsAPI, getRecommendDishAPI } from '@/api/dish'
import { getSetmealListAPI } from '@/api/setmeal'
import { addToCartAPI, subCartAPI, getCartAPI, cleanCartAPI } from '@/api/cart'
import { getUserInfoAPI } from '@/api/user'
import type { CategoryItem } from '@/types/category'
import type { DishItem, FlavorItem, DishToCartItem, HealthStats } from '@/types/dish'
import type { SetmealItem } from '@/types/setmeal'
import type { CartDTO, CartItem } from '@/types/cart'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { ref } from 'vue'
import { useUserStore } from '@/stores/modules/user'
import Navbar from '../order/components/Navbar.vue'

const userStore = useUserStore()

// ------ data ------
const status = ref(true)
const categoryList = ref<CategoryItem[]>([])
const activeIndex = ref(0)
const activeFlavorIndex = ref(0)
const dishList = ref<(DishItem | SetmealItem)[]>([])
const setmealList = ref<SetmealItem[]>([])
const openCartList = ref(false)
const cartList = ref<CartItem[]>([])
const CartAllNumber = ref(0)
const CartAllPrice = ref(0)
const visible = ref(false)
const dialogDish = ref<DishToCartItem>()
const flavors = ref<FlavorItem[]>([])
const chosedflavors = ref<string[]>([])

const healthStats = ref<HealthStats | null>(null)
const recommendList = ref<DishItem[]>([])
const userName = ref('')
const baseUrl = 'http://127.0.0.1:8081'

const resolveImageUrl = (image?: string) => {
  if (!image) return '/static/default_dish.png'
  if (image.startsWith('http://') || image.startsWith('https://')) return image
  if (image.startsWith('/static/dish/')) return baseUrl + image
  return image
}

// ------ method ------
const getCategoryData = async () => {
  const res = await getCategoryAPI()
  categoryList.value = res.data
}

const getDishOrSetmealList = async (index: number) => {
  activeIndex.value = index
  let res
  if (categoryList.value[index].type === 1) {
    res = await getDishListAPI(categoryList.value[index].id)
  } else {
    res = await getSetmealListAPI(categoryList.value[index].id)
  }
  dishList.value = res.data
}

const getCartList = () => {
  console.log('=== Index getCartList called ===');
  uni.request({
    url: 'http://127.0.0.1:8081/user/shoppingCart/list',
    method: 'GET',
    header: { authentication: uni.getStorageSync('token') },
    success: (res: any) => {
      console.log('Index Cart API Response:', res.data);
      if (res.data.code === 0 || res.data.code === 1) { // 兼容两种成功
        cartList.value = res.data.data
        CartAllNumber.value = cartList.value.reduce((acc: number, cur: any) => acc + cur.number, 0)
        CartAllPrice.value = cartList.value.reduce((acc: number, cur: any) => acc + cur.amount * cur.number, 0)
        console.log('Index Cart items loaded:', cartList.value.length, 'items, totalNum:', CartAllNumber.value, 'totalPrice:', CartAllPrice.value);
        if (cartList.value.length === 0) {
          openCartList.value = false
        }
      } else {
        console.error('Index Cart API Failed:', res.data);
      }
    },
    fail: (err) => {
      console.error('Index Cart API Network Error:', err);
    }
  })
}

const chooseNorm = async (dish: DishItem) => {
  flavors.value = dish.flavors
  const tmpdish = Object.assign({}, dish) as unknown as DishToCartItem
  delete tmpdish.flavors
  dialogDish.value = tmpdish
  const moreNormdata = dish.flavors.map((obj) => ({ ...obj, list: JSON.parse(obj.list) }))
  moreNormdata.forEach((item) => {
    if (item.list && item.list.length > 0) {
      chosedflavors.value.push(item.list[0])
    }
  })
  visible.value = true
}

const chooseFlavor = (obj: string[], flavor: string) => {
  let ind = -1
  let findst = obj.some((n) => {
    ind = chosedflavors.value.findIndex((o) => o == n)
    return ind != -1
  })
  const indexInChosed = chosedflavors.value.findIndex((it) => it == flavor)
  if (indexInChosed == -1 && !findst) {
    chosedflavors.value.push(flavor)
  } else if (indexInChosed == -1 && findst && ind >= 0) {
    chosedflavors.value.splice(ind, 1)
    chosedflavors.value.push(flavor)
  } else {
    chosedflavors.value.splice(indexInChosed, 1)
  }
  dialogDish.value!.flavors = chosedflavors.value.join(',')
}

const getCopies = (dish: DishItem | SetmealItem) => {
  if (categoryList.value[activeIndex.value].sort < 20) {
    return cartList.value.find((item) => item.dishId === dish.id)?.number || 0
  } else {
    return cartList.value.find((item) => item.setmealId === dish.id)?.number || 0
  }
}

const addToCart = async (dish: DishToCartItem) => {
  if (!chosedflavors.value || chosedflavors.value.length <= 0) {
    uni.showToast({
      title: '?????',
      icon: 'none',
    })
    return false
  }
  const partialCart: Partial<CartDTO> = { dishId: dish.id, dishFlavor: chosedflavors.value.join(',') }
  await addToCartAPI(partialCart)
  await getCartList()
  chosedflavors.value = []
  visible.value = false
}

const addDishAction = async (item: any, form: string) => {
  if (form == '???') {
    const partialCart: Partial<CartDTO> = {
      dishId: item.dishId,
      setmealId: item.setmealId,
      dishFlavor: item.dishFlavor,
    }
    await addToCartAPI(partialCart)
  } else {
    if (categoryList.value[activeIndex.value].sort < 20) {
      const partialCart: Partial<CartDTO> = { dishId: item.id }
      await addToCartAPI(partialCart)
    } else {
      const partialCart: Partial<CartDTO> = { setmealId: item.id }
      await addToCartAPI(partialCart)
    }
  }
  await getCartList()
}

const subDishAction = async (item: any, form: string) => {
  if (form == '???') {
    const partialCart: Partial<CartDTO> = {
      dishId: item.dishId,
      setmealId: item.setmealId,
      dishFlavor: item.dishFlavor,
    }
    await subCartAPI(partialCart)
  } else {
    if (categoryList.value[activeIndex.value].sort < 20) {
      const partialCart: Partial<CartDTO> = { dishId: item.id }
      await subCartAPI(partialCart)
    } else {
      const partialCart: Partial<CartDTO> = { setmealId: item.id }
      await subCartAPI(partialCart)
    }
  }
  await getCartList()
}

const clearCart = async () => {
  await cleanCartAPI()
  await getCartList()
  openCartList.value = false
}

const submitOrder = () => {
  uni.navigateTo({
    url: '/pages/submit/submit',
  })
}

const goBack = () => {
  uni.switchTab({ url: '/pages/index/index' })
}

const fetchHealthStats = async () => {
  try {
    const res = await getHealthStatsAPI()
    healthStats.value = res.data
  } catch (e) {
    healthStats.value = null
  }
}

const fetchRecommend = async () => {
  try {
    const res = await getRecommendDishAPI()
    recommendList.value = res.data
  } catch (e) {
    recommendList.value = []
  }
}

const fetchUserName = async () => {
  if (!userStore.profile?.id) return
  try {
    const res = await getUserInfoAPI(userStore.profile.id)
    userName.value = res.data.name || ''
  } catch (e) {
    userName.value = ''
  }
}

const toDetail = (dish: DishItem) => {
  uni.navigateTo({
    url: `/pages/detail/detail?dishId=${dish.id}`,
  })
}

onLoad(async () => {
  const res = await getStatusAPI()
  status.value = res.data === 1 ? true : false
  await getCategoryData()
  await getDishOrSetmealList(0)
  await getCartList()
  await fetchHealthStats()
  await fetchRecommend()
  await fetchUserName()
})

onShow(async () => {
  console.log('=== INDEX PAGE onShow ===');
  // 调试提示 - 删除后取消注
  // uni.showToast({ title: '首页onShow触发', icon: 'none', duration: 1500 });
  await getCategoryData()
  getCartList() // 不需await，因getCartList 不是 Promise
  await fetchRecommend()
})
</script>

<style lang="less" scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  box-sizing: border-box;
  background-color: #f8f8f8;
}

.health-board {
  margin: 20rpx 20rpx 0 20rpx;
  padding: 20rpx;
  background: linear-gradient(135deg, #e6f7ff 0%, #ffffff 100%);
  border-radius: 20rpx;

  .health-title {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
  }

  .health-main {
    margin-top: 10rpx;

    .health-greet {
      font-size: 28rpx;
      color: #222;
      margin-bottom: 10rpx;
    }

    .health-metrics {
      display: flex;
      flex-wrap: wrap;
      gap: 10rpx 20rpx;

      .metric {
        font-size: 24rpx;
        color: #555;
      }
    }

    .health-empty {
      font-size: 24rpx;
      color: #999;
    }

    .health-tip {
      margin-top: 10rpx;
      font-size: 24rpx;
      color: #00aaff;
    }
  }
}

.recommend-section {
  margin: 20rpx 20rpx 0 20rpx;

  .section-title {
    font-size: 30rpx;
    font-weight: bold;
    margin-bottom: 10rpx;
    color: #333;
  }

  .recommend-scroll {
    white-space: nowrap;
  }

  .recommend-card {
    display: inline-flex;
    width: 280rpx;
    margin-right: 16rpx;
    background-color: #fff;
    border-radius: 16rpx;
    overflow: hidden;

    .recommend-image {
      width: 280rpx;
      height: 180rpx;
    }

    .recommend-info {
      padding: 10rpx;

      .recommend-name {
        font-size: 24rpx;
        color: #222;
      }

      .recommend-stall {
        font-size: 20rpx;
        color: #999;
        margin-top: 6rpx;
      }

      .recommend-reason {
        margin-top: 6rpx;
        font-size: 20rpx;
        color: #00aaff;
        background: #e6f7ff;
        display: inline-block;
        padding: 2rpx 8rpx;
        border-radius: 10rpx;
      }

      .recommend-price {
        margin-top: 6rpx;
        font-size: 24rpx;
        color: #e94e3c;
      }
    }
  }
}

.menu-section {
  flex: 1;
  min-height: 0;
  margin-top: 10rpx;
}

.dialog {
  position: fixed;
  width: 100%;
  height: 100%;
  z-index: 1000;
  top: 0;
  left: 0;
  background: rgba(0, 0, 0, 0.6);
  .flavor_pop {
    position: relative;
    top: 50%;
    left: 50%;
    transform: translateX(-50%) translateY(-50%);
    padding: 40rpx;
    border-radius: 20rpx;
    width: 70%;
    height: 35%;
    background-color: #fff;
    justify-content: center;

    .title {
      font-size: 36rpx;
      font-weight: bold;
      margin-bottom: 10rpx;
    }

    .scroll {
      height: 80%;
      padding-bottom: 20rpx;
    }

    .flavor {
      padding: 10rpx 0;
    }

    .flavorItem {
      margin: 10rpx 20rpx 10rpx 0;
      padding: 10rpx;
      display: inline-block;
      border: #00aaff 1rpx solid;
      border-radius: 20rpx;
      text-align: center;
      font-size: 20rpx;
    }
    .active {
      background-color: #00aaff;
      color: #fff;
    }

    .addToCart {
      position: absolute;
      bottom: 20rpx;
      right: 30rpx;
      width: 150rpx;
      height: 50rpx;
      background-color: #00aaff;
      color: #fff;
      font-size: 20rpx;
      text-align: center;
      line-height: 50rpx;
      border-radius: 30rpx;
    }
  }

  .close_dialog {
    position: fixed;
    top: 75%;
    left: 50%;
    transform: translateX(-50%) translateY(-50%);
    width: 60rpx;
    height: 60rpx;
    border-radius: 30rpx;
    background-color: rgba(0, 0, 0, 0.6);
    color: #fff;
    font-size: 40rpx;
    text-align: center;
    line-height: 60rpx;
  }
}

.viewport {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.categories {
  flex: 1;
  min-height: 0;
  display: flex;
}

.primary {
  overflow: hidden;
  width: 170rpx !important;
  flex: 0 0 170rpx !important; /* 绝对不放大，绝对不缩小，死锁 170rpx */
  background-color: #f6f6f6;

  .item {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 96rpx;
    font-size: 26rpx;
    color: #595c63;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      left: 42rpx;
      bottom: 0;
      width: 96rpx;
      border-top: 1rpx solid #e3e4e7;
    }
  }

  .active {
    background-color: #fff;
  }
}

.primary .item:last-child::after,
.primary .active::after {
  display: none;
}

.secondary {
  flex: 1 !important;
  width: 0 !important; /* Flex 经典神技：强制其宽度由父级分配，不被子元素撑开 */
  min-width: 0 !important;
  overflow: hidden !important; /* 防止溢出内容撑破布局 */
  background-color: #fff;

  .section {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    padding: 20rpx 0;

    .dish {
      width: 520rpx;
      margin: 10rpx 30rpx 10rpx 20rpx;
      display: flex;

      image {
        width: 150rpx;
        height: 150rpx;
        border-radius: 15rpx;
      }

      .dishinfo {
        width: 300rpx;
        padding: 10rpx;
        display: flex;
        position: relative;
        flex-direction: column;
        justify-content: space-between;
        flex: 1;
        min-width: 0; /* 这是 Flex 垂直排列防溢出的杀手锏 */

        .ellipsis {
          display: block !important; /* 强制块级元素，独占一楼*/
          width: 100% !important; /* 强制占满父容器宽*/
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .name {
          display: block !important; /* 强制块级，独占一楼*/
          width: 100% !important;
          padding: 5rpx;
          font-size: 24rpx;
          color: #222;
        }

        .detail {
          display: -webkit-box !important; /* 强制 webkit-box 布局 */
          -webkit-box-orient: vertical !important;
          -webkit-line-clamp: 2 !important; /* 核心：最多显示两*/
          overflow: hidden !important;
          width: 100% !important; /* 强制占满父容*/
          white-space: normal !important; /* 绝对允许换行 */
          word-break: break-all;
          padding: 5rpx;
          margin-top: 4rpx;
          font-size: 20rpx;
          color: #999;
        }

        .price {
          padding: 5rpx;
          font-size: 18rpx;
          color: #cf4444;
        }

        .number {
          font-size: 24rpx;
          margin-left: 2rpx;
        }

        .choosenorm {
          position: absolute;
          right: 20rpx;
          bottom: 10rpx;
          width: 112rpx;
          height: 45rpx;
        }

        .sub_add {
          display: flex;
          position: absolute;
          right: 20rpx;
          bottom: 10rpx;

          .sub {
            width: 35rpx;
            height: 35rpx;
          }

          .add {
            width: 35rpx;
            height: 35rpx;
          }

          .dish_number {
            padding: 0 10rpx;
            line-height: 30rpx;
            font-size: 20rpx;
            font-family: PingFangSC, PingFangSC-Medium;
            font-weight: 500;
          }
        }
      }

      .name {
        padding: 5rpx;
        font-size: 22rpx;
        color: #333;
      }

      .price {
        padding: 5rpx;
        font-size: 18rpx;
        color: #cf4444;
      }

      .number {
        font-size: 24rpx;
        margin-left: 2rpx;
      }
    }
  }
}

.footer_order_buttom {
  position: fixed;
  display: flex;
  bottom: 48rpx;
  width: calc(100% - 60rpx);
  height: 88rpx;
  margin: 0 30rpx;
  background: rgba(0, 0, 0, 0.9);
  border-radius: 50rpx;
  box-shadow: 0px 6rpx 10rpx 0px rgba(0, 0, 0, 0.25);
  z-index: 1000;
  padding: 0rpx 10rpx;
  box-sizing: border-box;

  .order_number {
    position: relative;
    width: 120rpx;

    .order_number_icon {
      position: absolute;
      display: block;
      width: 120rpx;
      height: 120rpx;
      left: 12rpx;
      bottom: 0px;
    }

    .order_dish_num {
      position: absolute;
      display: inline-block;
      z-index: 9;
      min-width: 12rpx;
      height: 36rpx;
      line-height: 36rpx;
      padding: 0 12rpx;
      left: 92rpx;
      font-size: 24rpx;
      top: -8rpx;
      border-radius: 20rpx;
      background-color: #e94e3c;
      color: #fff;
      font-weight: 500;
    }
  }

  .order_price {
    flex: 1;
    text-align: left;
    color: #fff;
    line-height: 88rpx;
    padding-left: 34rpx;
    box-sizing: border-box;
    font-size: 36rpx;
    font-family: DIN, DIN-Medium;
    font-weight: 500;

    .ico {
      font-size: 24rpx;
    }
  }

  .order_btn {
    background-color: #d8d8d8;
    width: 204rpx;
    height: 72rpx;
    line-height: 72rpx;
    border-radius: 72rpx;
    color: #fff;
    text-align: center;
    font-weight: bold;
    margin-top: 8rpx;
  }

  .order_btn_active {
    width: 204rpx;
    height: 72rpx;
    line-height: 72rpx;
    border-radius: 72rpx;
    color: #fff;
    text-align: center;
    font-weight: bold;
    margin-top: 8rpx;
    background: #00aaff;
  }
}

.pop_mask {
  position: fixed;
  width: 100%;
  height: 100vh;
  top: 0;
  left: 0;
  z-index: 500;
  background-color: rgba(0, 0, 0, 0.4);

  .cart_pop {
    width: 100%;
    position: absolute;
    bottom: 0;
    left: 0;
    height: 60vh;
    background-color: #fff;
    border-radius: 20rpx 20rpx 0 0;
    padding: 20rpx 30rpx 30rpx 30rpx;
    box-sizing: border-box;

    .top_title {
      display: flex;
      justify-content: space-between;
      border-bottom: solid 1px #ebeef5;
      padding-bottom: 20rpx;

      .tit {
        font-size: 40rpx;
        font-weight: bold;
        color: #20232a;
      }

      .clear {
        color: #999999;
        font-size: 28rpx;
        font-weight: 400;
        display: flex;
        align-items: center;
        font-family: PingFangSC, PingFangSC-Regular;

        .clear_icon {
          width: 30rpx;
          height: 30rpx;
          margin-right: 8rpx;
        }

        .clear-des {
          height: 56rpx;
          line-height: 56rpx;
        }
      }
    }

    .card_order_list {
      background-color: #fff;
      padding-top: 40rpx;
      box-sizing: border-box;
      height: calc(100% - 0rpx);
      flex: 1;
      position: relative;

      .type_item {
        display: flex;
        margin-bottom: 40rpx;

        .dish_img {
          width: 128rpx;
          margin-right: 30rpx;

          .dish_img_url {
            display: block;
            width: 128rpx;
            height: 128rpx;
            border-radius: 8rpx;
          }
        }

        .dish_info {
          position: relative;
          flex: 1;
          padding-bottom: 40rpx;
          border-bottom: solid 1px #ebeef5;

          .dish_name {
            font-size: 32rpx;
            color: #333333;
            font-family: PingFangSC, PingFangSC-Semibold;
            font-weight: 600;
          }

          .dish_price {
            font-size: 36rpx;
            color: #e94e3c;
            font-weight: bold;

            .ico {
              font-size: 24rpx;
            }
          }

          .dish_flavor {
            font-size: 20rpx;
            color: #666;
          }

          .dish_active {
            position: absolute;
            right: 20rpx;
            bottom: 20rpx;
            display: flex;

            .dish_add,
            .dish_sub {
              display: block;
              width: 50rpx;
              height: 50rpx;
              margin: 11rpx;
            }

            .dish_number {
              padding: 0 10rpx;
              line-height: 72rpx;
              font-size: 30rpx;
              font-family: PingFangSC, PingFangSC-Medium;
              font-weight: 500;
            }
          }
        }
      }

      &::before {
        content: '';
        position: absolute;
        width: 100vw;
        height: 120rpx;
        z-index: 99;
        background: linear-gradient(0deg, rgba(255, 255, 255, 1) 10%, rgba(255, 255, 255, 0));
        bottom: 0px;
        left: 0px;
      }

      .seize_seat {
        width: 100%;
        height: 120rpx;
      }
    }
  }
}

.close {
  position: absolute;
  top: 0;
  right: 0;
  width: 750rpx;
  height: 100%;
  z-index: 1000;
  background: rgba(0, 0, 0, 0.2);
  text-align: center;
  line-height: 40rpx;
  font-size: 24rpx;
  color: #000;
  .text {
    width: 750rpx;
    height: 200rpx;
    position: absolute;
    background-color: rgba(0, 0, 0, 0.5);
    bottom: 0;
    text-align: center;
    line-height: 200rpx;
    font-size: 40rpx;
    font-weight: bold;
    color: #fff;
  }
}

.ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
