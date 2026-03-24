<template>
  <view class="history_top">
    <view
      v-for="(item, index) in statusOptions"
      :key="index"
      class="history_title"
      :class="{active: index === activeIndex}"
      @tap="getOrderPage(index, '更改状态')"
    >
      <text class="name"> {{ item.name }} </text>
    </view>
  </view>
  <view class="blank"></view>
  <view class="history_content">
    <view v-if="showPendingEmptyState" class="pending_empty_state">
      <image
        src="@/assets/images/icons/empty_pending.png"
        mode="widthFix"
        class="pending_empty_image"
      />
      <text class="pending_empty_text">暂无待付款订单</text>
    </view>
    <view
      v-else
      class="history_item"
      v-for="(item, index) in historyOrders"
      :key="index"
      @click="toOrderDetail(item.id as number)"
    >
      <view class="item_info_box">
        <view class="history_item_left">
          <view class="history_item_order_id">订单号：{{ item.number }}</view>
          <view class="dish_list">
            <view
              v-for="(dish, dishIndex) in item.orderDetailList"
              :key="dishIndex"
              class="dish-item"
            >
              <image :src="resolveImageUrl(dish.pic || dish.image)" mode="aspectFill" class="dish-img" />
              <view class="dish-info">
                <text class="dish-name">{{ dish.name || '菜品' }}</text>
                <text class="dish-num">x{{ dish.number || 1 }}</text>
              </view>
            </view>
          </view>
          <view class="history_item_order_time">{{ item.orderTime }}</view>
        </view>
        <view class="history_item_right">
          <view class="history_item_status">{{ statusList[item.status as number].name }}</view>
          <view class="history_item_price">￥{{ item.amount }}</view>
          <view class="history_item_dish_amount">共{{ item.totalNum }}份</view>
        </view>
      </view>
      <view class="btn_box">
        <view class="history_item_go_pay" v-if="item.status === 1" @click.stop="goToPay(item)">
          去付款
        </view>
        <view class="history_item_reOrder" @click.stop="reOrder(item.id as number)">再来一单</view>
        <view class="history_item_push_order" v-if="item.status === 2" @click.stop="pushOrder(item.id as number)">
          催单
        </view>
      </view>
    </view>
  </view>
  <!-- 催单massageBox -->
  <pushMsg ref="childComp"></pushMsg>
</template>

<script lang="ts" setup>
import pushMsg from '../../components/message/pushMsg.vue'
import {computed, ref} from 'vue'
import {onLoad, onReachBottom, onShow} from '@dcloudio/uni-app'
import {getOrderPageAPI, reOrderAPI} from '@/api/order'
import {cleanCartAPI} from '@/api/cart'
import type {OrderPageDTO, OrderVO} from '@/types/order'

const childComp: any = ref(null)

// 顶部tab栏
const statusOptions = [
  {
    status: 0,
    name: '全部订单',
  },
  {
    status: 1,
    name: '待付款',
  },
  {
    status: 5,
    name: '已完成',
  },
  {
    status: 6,
    name: '已取消',
  },
]
// 所有状态
const statusList = [
  {
    status: 0,
    name: '全部订单',
  },
  {
    status: 1,
    name: '待付款',
  },
  {
    status: 2,
    name: '待接单',
  },
  {
    status: 3,
    name: '制作中',
  },
  {
    status: 4,
    name: '待取餐',
  },
  {
    status: 5,
    name: '已完成',
  },
  {
    status: 6,
    name: '已取消',
  },
]

const activeIndex = ref(0)
const historyOrders = ref<OrderVO[]>([])
const showPendingEmptyState = computed(() => activeIndex.value === 1 && historyOrders.value.length === 0)
const baseURL = 'http://121.41.59.61:8081'

const resolveImageUrl = (image?: string) => {
  if (!image) return '/static/default_dish.png'
  if (image.startsWith('http://') || image.startsWith('https://')) return image
  if (image.startsWith('/')) return baseURL + image
  return `${baseURL}/static/dish/${image.replace(/^\/+/, '')}`
}

const orderDTO = ref<OrderPageDTO>({
  page: 1,
  pageSize: 6,
  // status: 0,
})
const total = ref(0)

onLoad(async () => {
  console.log('首先分页获取所有订单信息', orderDTO.value)
  // 分页获取所有订单信息（刚开始只展示前6条）
  const res = await getOrderPage(0)
})

// 【核心修复】用 onShow 实现实时状态同步
// 每次页面显示时重新拉取订单列表，确保管理员取消的订单状态同步
onShow(async () => {
  console.log('History onShow - 刷新订单列表获取最新状态')
  orderDTO.value.page = 1
  historyOrders.value = []
  await getOrderPage(activeIndex.value)
})

// 页面上拉触底事件的处理函数
onReachBottom(() => {
  console.log('Page:', orderDTO.value.page)
  console.log('Page Size:', orderDTO.value.pageSize)
  if (orderDTO.value.page * orderDTO.value.pageSize >= total.value) {
    console.log('end!')
    // 没有下一页数据，提示用户
    uni.showToast({
      title: 'end!',
      icon: 'none',
    })
    return
  }
  orderDTO.value.page += 1
  getOrderPage(activeIndex.value)
})

const getOrderPage = async (index: number, type?: string) => {
  activeIndex.value = index
  console.log('根据status获取订单信息')
  // != 0 说明不是全部订单，需要传入status条件分页查询
  if (index !== 0) {
    orderDTO.value.status = statusOptions[index].status
  } else {
    delete orderDTO.value.status
  }
  console.log('orderDTO', orderDTO.value)
  const res = await getOrderPageAPI(orderDTO.value)
  if (type === '更改状态') {
    historyOrders.value = res.data.records
    orderDTO.value.page = 1
  } else {
    historyOrders.value = historyOrders.value.concat(res.data.records)
  }
  total.value = res.data.total
}

const toOrderDetail = (id: number | string) => {
  // 【核心修复】确保传递字符串ID，防止JS精度丢失
  uni.navigateTo({
    url: '/pages/orderDetail/orderDetail?orderId=' + String(id),
  })
}

// 再来一单
const goToPay = (item: OrderVO) => {
  if (!item?.id) {
    uni.showToast({ title: '订单信息缺失', icon: 'none' })
    return
  }
  const orderId = String(item.id)
  const orderNumber = encodeURIComponent(String(item.number || ''))
  const amount = encodeURIComponent(String(item.amount ?? '0.00'))
  const diningType = Number(item.packAmount || 0) > 0 ? 2 : 1
  uni.navigateTo({
    url: `/subpkg-pay/pay/pay?orderId=${orderId}&orderNumber=${orderNumber}&amount=${amount}&diningType=${diningType}`,
  })
}

const reOrder = async (id: number) => {
  console.log('再来一单', id)
  // 菜品批量加入购物车之前，要先清购物车，避免批量加入购物车后数据并不完全一样
  await cleanCartAPI()
  // 再来一单会将当前订单的菜品批量加入购物车，跳转到订单页面后，购物车将高亮显示
  await reOrderAPI(id as number)
  uni.switchTab({
    url: '/pages/category/category',
  })
}

// 催单
const pushOrder = (id: number) => {
  console.log('催单', id)
  childComp.value.openPopup()
  // uni.showToast({
  //   title: '已催单',
  //   icon: 'none',
  // })
}
</script>

<style lang="less" scoped>
.history_top {
  position: fixed;
  width: 100%;
  height: 96rpx;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding-top: calc(env(safe-area-inset-top) + 8rpx);
  background-color: rgba(255, 250, 245, 0.96);
  backdrop-filter: blur(18rpx);
  border-bottom: 1rpx solid #f3e8dc;
  .history_title {
    width: 25%;
    text-align: center;
    font-size: 30rpx;
    color: #6b5c53;
  }
  .active {
    color: #ff8c42;
    font-weight: 700;
  }
}
.blank {
  height: calc(116rpx + env(safe-area-inset-top));
}
.history_content {
  padding: 0 24rpx 24rpx;
  .pending_empty_state {
    min-height: calc(100vh - 220rpx - env(safe-area-inset-top));
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80rpx 0 140rpx;
    box-sizing: border-box;
  }
  .pending_empty_image {
    width: 220rpx;
    height: auto;
  }
  .pending_empty_text {
    margin-top: 26rpx;
    font-size: 26rpx;
    line-height: 36rpx;
    color: #999999;
    text-align: center;
  }
  .title {
    font-size: 28rpx;
    color: #333;
    padding-top: 10rpx;
    font-weight: bold;
  }
  .history_item {
    // display: flex;
    // justify-content: space-between;
    min-height: 300rpx;
    height: auto;
    padding: 36rpx 24rpx;
    background-color: #fff;
    margin-top: 24rpx;
    border-radius: 24rpx;
    box-shadow: 0 8rpx 24rpx rgba(45, 36, 31, 0.05);
    .item_info_box {
      display: flex;
      justify-content: space-between;
      width: 100%;
      .history_item_left {
        .history_item_order_id {
          font-size: 30rpx;
          line-height: 40rpx;
          color: #333;
          margin-bottom: 20rpx;
        }
        .dish_list {
          width: 440rpx;
          margin-bottom: 12rpx;
          .dish-item {
            display: flex;
            align-items: center;
            margin: 15rpx 0;
            .dish-img {
              width: 90rpx;
              height: 90rpx;
              border-radius: 16rpx;
              margin-right: 20rpx;
              background-color: #f5f5f5;
            }
            .dish-info {
              flex: 1;
              display: flex;
              justify-content: space-between;
              align-items: center;
              min-width: 0;
              .dish-name {
                flex: 1;
                font-size: 26rpx;
                color: #2d241f;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                margin-right: 12rpx;
              }
              .dish-num {
                font-size: 24rpx;
                color: #666;
              }
            }
          }
        }
        .history_item_order_time {
          font-size: 26rpx;
          color: #666;
        }
      }
      .history_item_right {
        text-align: right;
        .history_item_status {
          font-size: 30rpx;
          color: #ff8c42;
          margin-bottom: 40rpx;
        }
        .history_item_price {
          font-size: 32rpx;
          line-height: 50rpx;
          color: #333;
        }
        .history_item_dish_amount {
          font-size: 26rpx;
          color: #666;
          margin-bottom: 40rpx;
        }
      }
    }
    .btn_box {
      width: 100%;
      display: inline-block;
      .history_item_reOrder {
        float: right;
        margin-left: 20rpx;
        width: 140rpx;
        height: 60rpx;
        text-align: center;
        line-height: 60rpx;
        border: 1rpx solid rgba(255, 140, 66, 0.7);
        border-radius: 30rpx;
        font-size: 28rpx;
        color: #ff8c42;
        background: #fff9f3;
      }
      .history_item_go_pay {
        float: right;
        margin-left: 20rpx;
        min-width: 160rpx;
        height: 66rpx;
        padding: 0 32rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, #ffc94a 0%, #ffb000 60%, #f59e0b 100%);
        border-radius: 999rpx;
        font-size: 26rpx;
        font-weight: 500;
        letter-spacing: 1rpx;
        color: #FFFFFF;
        box-shadow: 0 8rpx 18rpx rgba(255, 176, 0, 0.24), inset 0 2rpx 0 rgba(255, 255, 255, 0.32);
        transition: transform 0.15s ease, opacity 0.15s ease;
        &:active {
          transform: scale(0.98) translateY(1rpx);
          opacity: 0.95;
        }
      }
      .history_item_push_order {
        float: right;
        width: 140rpx;
        height: 62rpx;
        text-align: center;
        line-height: 62rpx;
        background: linear-gradient(90deg, #ffb17a 0%, #ff8c42 100%);
        border-radius: 30rpx;
        font-size: 28rpx;
        color: #fff;
      }
    }
  }
}
</style>

<style>
page {
  /* width: 700rpx; */
  background-color: #fffaf5;
}
</style>
