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
    <view
      class="history_item"
      v-for="(item, index) in historyOrders"
      :key="index"
      @click="toOrderDetail(item.id as number)"
    >
      <view class="item_info_box">
        <view class="history_item_left">
          <view class="history_item_order_id">订单号：{{ item.number }}</view>
          <scroll-view class="scroll_container" scroll-x>
            <view v-for="(dish, index) in item.orderDetailList" :key="index" class="image_box">
              <image :src="resolveImageUrl(dish.pic || dish.image)" />
            </view>
          </scroll-view>
          <view class="history_item_order_time">{{ item.orderTime }}</view>
        </view>
        <view class="history_item_right">
          <view class="history_item_status">{{ statusList[item.status as number].name }}</view>
          <view class="history_item_price">￥{{ item.amount }}</view>
          <view class="history_item_dish_amount">共{{ item.totalNum }}份</view>
        </view>
      </view>
      <view class="btn_box">
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
import {ref} from 'vue'
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
const baseUrl = 'http://127.0.0.1:8081'

const resolveImageUrl = (image?: string) => {
  if (!image) return '/static/default_dish.png'
  if (image.startsWith('http://') || image.startsWith('https://')) return image
  if (image.startsWith('/static/dish/')) return baseUrl + image
  return image
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
const reOrder = async (id: number) => {
  console.log('再来一单', id)
  // 菜品批量加入购物车之前，要先清购物车，避免批量加入购物车后数据并不完全一样
  await cleanCartAPI()
  // 再来一单会将当前订单的菜品批量加入购物车，跳转到订单页面后，购物车将高亮显示
  await reOrderAPI(id as number)
  uni.redirectTo({
    url: '/pages/order/order',
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
  height: 80rpx;
  display: flex;
  justify-content: space-around;
  padding-top: 20rpx;
  background-color: #fff;
  .history_title {
    width: 25%;
    text-align: center;
    font-size: 30rpx;
    color: #333;
  }
  .active {
    color: #00aaff;
  }
}
.blank {
  height: 100rpx;
}
.history_content {
  padding: 0rpx 20rpx 20rpx 20rpx;
  .title {
    font-size: 28rpx;
    color: #333;
    padding-top: 10rpx;
    font-weight: bold;
  }
  .history_item {
    // display: flex;
    // justify-content: space-between;
    height: 300rpx;
    padding: 40rpx 20rpx;
    background-color: #fff;
    margin-top: 20rpx;
    border-radius: 20rpx;
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
        .scroll_container {
          width: 400rpx;
          height: 130rpx;
          overflow-x: auto;
          white-space: nowrap;
          .image_box {
            width: 100rpx;
            display: inline-block;
            align-items: center;
            margin-right: 20rpx;
            text-align: center;
            image {
              display: inline-block;
              border-radius: 10rpx;
              width: 100rpx;
              height: 100rpx;
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
          color: #0af;
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
        border: #0af solid 1rpx;
        border-radius: 30rpx;
        font-size: 28rpx;
        color: #0af;
      }
      .history_item_push_order {
        float: right;
        width: 140rpx;
        height: 62rpx;
        text-align: center;
        line-height: 62rpx;
        background-color: #0af;
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
  background-color: #eeeeee;
}
</style>
