<template>
  <view class="white_box">
    <view class="orderDetail">{{ statusList[order.status].name }}</view>
    <view class="pickup_box" v-if="pickupNo">
      <view class="pickup_number">取餐码: {{ pickupNo }}</view>
    </view>
    <view class="time_box" v-if="order.status === 1">
      <view class="time" v-if="countdownStore.showM <= 0 && countdownStore.showS <= 0">订单已超时</view>
      <view class="time" v-else>
        支付剩余时间
        <uni-countdown color="#888" :show-day="false" :show-hour="false" :minute="countdownStore.showM"
          :second="countdownStore.showS"></uni-countdown>
      </view>
    </view>
    <view class="btn_box">
      <view class="reOrder" v-if="order.status <= 2" @click="cancelOrder">取消订单</view>
      <view class="toPay" v-if="order.status === 1 && (countdownStore.showM > 0 || countdownStore.showS > 0)"
        @click="toPay">立即支付
      </view>
      <view class="pushOrder" v-if="order.status === 2" @click="pushOrder">催单</view>
      <view class="reOrder" v-if="order.status === 2 || order.status === 6" @click="reOrder">再来一单</view>
    </view>
  </view>

  <view class="white_box">
    <view class="word_text">
      <text class="word_style">寒页餐厅</text>
    </view>
    <view class="order-type">
      <view class="type_item" v-for="(obj, index) in order.orderDetailList" :key="index">
        <view class="dish_img">
          <image mode="aspectFill" :src="obj.pic" class="dish_img_url"></image>
        </view>
        <view class="dish_info">
          <view class="dish_name"> {{ obj.name }} </view>
          <view v-if="obj.dishFlavor" class="dish_flavor"> {{ obj.dishFlavor }} </view>
          <view class="dish_amount">
            <text v-if="obj.number && obj.number > 0" class="dish_number">x {{ obj.number }}</text>
          </view>
          <view class="dish_price"> <text class="ico">￥</text> {{ obj.amount }} </view>
        </view>
      </view>

      <view class="word_text">
        <view class="word_left">打包费</view>
        <view class="word_right">￥{{ realPackFee }}</view>
      </view>

      <view class="word_text">
        <view class="word_left">就餐方式</view>
        <view class="word_right" style="color: #00aaff; font-weight: bold;">
          {{ realPackFee > 0 ? '打包自取' : '堂食' }}
        </view>
      </view>

      <view class="all_price">
        <text class="word_right">总价 ￥{{ order.amount }}</text>
      </view>
    </view>
  </view>

  <view class="white_box">
    <view class="text_center" @click="connectShop">联系商家</view>
  </view>

  <view class="white_box">
    <view class="bottom_text">
      <view class="text_left">备注</view>
      <view class="text_right">{{ order.remark }}</view>
    </view>
    <view class="bottom_text">
      <view class="text_left">餐具份数</view>
      <view class="text_right">{{
        order.tablewareNumber == -1
        ? '无需餐具'
        : order.tablewareNumber == 0
        ? '商家根据餐量提供'
        : order.tablewareNumber
        }}</view>
    </view>
    <view class="bottom_text">
      <view class="text_left">发票</view>
      <view class="text_right">本店不支持线上发票，请致电商家提供</view>
    </view>
  </view>

  <view class="white_box">
    <view class="bottom_text">
      <view class="text_left">订单号</view>
      <view class="text_right">{{ order.number }}</view>
    </view>
    <view class="bottom_text">
      <view class="text_left">下单时间</view>
      <view class="text_right">{{ order.orderTime }}</view>
    </view>
    <view class="bottom_text">
      <view class="text_left">地址</view>
      <view class="text_right">{{ order.address }}</view>
    </view>
  </view>

  <pushMsg ref="childComp"></pushMsg>
</template>

<script lang="ts" setup>
  import pushMsg from '../../components/message/pushMsg.vue'
  // 【关键修改】引入 computed
  import { ref, reactive, computed } from 'vue'
  import { onLoad } from '@dcloudio/uni-app'
  import { getOrderAPI, cancelOrderAPI, reOrderAPI, urgeOrderAPI, payOrderAPI } from '@/api/order'
  import { cleanCartAPI } from '@/api/cart'
  import { useCountdownStore } from '@/stores/modules/countdown'
  import type { OrderVO } from '@/types/order'

  const childComp: any = ref(null)

  const statusList = [
    { status: 0, name: '全部订单', },
    { status: 1, name: '等待支付', },
    { status: 2, name: '等待商家接单', },
    { status: 3, name: '商家已接单', },
    { status: 4, name: '正在派送中', },
    { status: 5, name: '订单已完成', },
    { status: 6, name: '订单已取消', },
  ]

  const countdownStore = useCountdownStore()

  const order = reactive<OrderVO>({
    id: 0,
    number: '',
    status: 0,
    userId: 0,
    addressBookId: 0,
    orderTime: new Date(),
    orderDetailList: [],
    packAmount: 0, 
    amount: 0
  })

  const pickupNo = computed(() => {
    const str = (order.number ?? '').toString()
    if (!str) return ''
    return str.length > 4 ? str.substring(str.length - 4) : str
  })

  // 【新增】智能计算真实的打包费
  // 逻辑：总价 - 菜品总价 = 打包费
  const realPackFee = computed(() => {
    if (!order.orderDetailList || order.orderDetailList.length === 0) return 0
    // 算出所有菜品的总价
    const dishTotal = order.orderDetailList.reduce((sum, item) => sum + item.amount * item.number, 0)
    // 差价就是打包费
    const fee = order.amount - dishTotal
    // 处理浮点数精度
    const result = parseFloat(fee.toFixed(2))
    return result < 0 ? 0 : result // 防止负数
  })

  onLoad(async (options) => {
    console.log('options', options)
    order.id = options!.orderId
    await getOrderDetail()
  })

  const getOrderDetail = async () => {
    console.log('获取订单详情')
    const res = await getOrderAPI(order.id as number)
    console.log('res', res)
    Object.assign(order, res.data)
  }

  // 只有待付款，或者商家接单前，才能取消订单
  const cancelOrder = async () => {
    const res = await cancelOrderAPI(order.id as number)
    if (res.code === 0) {
      uni.showToast({ title: '订单已取消', icon: 'none', })
    } else {
      uni.showModal({
        title: '提示',
        content: '商家已接单，欲取消订单请与商家联系！',
        showCancel: false,
      })
    }
    await getOrderDetail()
  }

  // 催单
  const pushOrder = async () => {
    const res = await urgeOrderAPI(order.id as number)
    if (childComp.value) {
      childComp.value.openPopup()
    }
  }

  // 再来一单
  const reOrder = async () => {
    await cleanCartAPI()
    await reOrderAPI(order.id as number)
    uni.redirectTo({ url: '/pages/order/order', })
  }

  // 联系商家
  const connectShop = () => {
    uni.makePhoneCall({ phoneNumber: '1999', })
  }

  // 支付成功
  const toPay = async () => {
    const payDTO = {
      orderNumber: order.number as string,
      payMethod: 1,
    }
    await payOrderAPI(payDTO)
    if (countdownStore.timer !== undefined) {
      clearInterval(countdownStore.timer)
      countdownStore.timer = undefined
    }
    uni.redirectTo({
      url: '/pages/pay/pay?orderId=' + order.id + '&orderNumber=' + order.number + '&orderAmount=' + order.amount + '&orderTime=' + order.orderTime,
    })
  }
</script>

<style lang="less" scoped>
  .white_box {
    margin: 20rpx;
    background-color: #fff;
    border-radius: 20rpx;

    .orderDetail {
      padding: 20rpx 0;
      font-size: 36rpx;
      color: #333333;
      font-weight: bold;
      text-align: center;
    }

    .pickup_box {
      padding: 10rpx 0 30rpx 0;
      text-align: center;

      .pickup_label {
        font-size: 28rpx;
        color: #888888;
      }

      .pickup_number {
        margin-top: 10rpx;
        font-size: 64rpx;
        font-weight: bold;
        color: #00aaff;
        letter-spacing: 4rpx;
        font-family: Arial, Helvetica, sans-serif;
      }
    }

    .time_box {
      padding: 20rpx 0;
      font-size: 24rpx;
      color: #333333;
      text-align: center;

      .time {
        display: flex;
        justify-content: center;
        font-size: 32rpx;
        color: #666666;
        font-weight: bold;
      }
    }

    .btn_box {
      display: flex;
      justify-content: center;

      .reOrder {
        width: 25%;
        padding: 15rpx 0;
        border: 1px solid #cccccc;
        border-radius: 10rpx;
        margin: 15rpx 10rpx;
        font-size: 28rpx;
        color: #333333;
        text-align: center;
      }

      .toPay,
      .pushOrder {
        width: 25%;
        padding: 15rpx 0;
        border: 1px solid #22ccff;
        background-color: #22ccff;
        border-radius: 10rpx;
        margin: 15rpx 10rpx;
        font-size: 28rpx;
        color: #ffffff;
        text-align: center;
      }
    }

    .order-type {
      padding: 40rpx 0 10rpx 0;

      .type_item {
        display: flex;
        margin-bottom: 30rpx;

        .dish_img {
          width: 100rpx;
          margin: 0 20rpx 0 32rpx;

          .dish_img_url {
            display: block;
            width: 100rpx;
            height: 100rpx;
            border-radius: 8rpx;
          }
        }

        .dish_info {
          position: relative;
          flex: 1;
          margin-right: 20rpx;

          .dish_name {
            font-size: 30rpx;
            font-weight: bold;
            color: #20232a;
          }

          .dish_flavor {
            font-size: 24rpx;
            color: #818693;
            height: 30rpx;
            line-height: 30rpx;
            margin-top: 10rpx;
          }

          .dish_amount {
            font-size: 24rpx;
            color: #818693;
            height: 30rpx;
            line-height: 30rpx;
            margin-top: 10rpx;

            .dish_number {
              padding: 10rpx 0;
              font-size: 24rpx;
            }
          }

          .dish_price {
            position: absolute;
            right: 20rpx;
            bottom: 40rpx;
            display: flex;
            font-size: 32rpx;
            color: #e94e3c;
            font-family: DIN, DIN-Medium;
            font-weight: 500;

            .ico {
              line-height: 42rpx;
              font-size: 24rpx;
            }
          }
        }
      }
    }

    .text_center {
      text-align: center;
      font-size: 32rpx;
      color: #333333;
      font-weight: bold;
      padding: 20rpx 0;
    }

    .word_text {
      display: flex;
      align-items: center;
      margin: 0 20rpx 0 30rpx;
      border-bottom: 1px solid #efefef;
      height: 120rpx;
      line-height: 120rpx;

      .word_left {
        width: 50%;
        height: 44rpx;
        font-size: 32rpx;
        text-align: left;
        color: #333333;
        line-height: 44rpx;
      }

      .word_right {
        width: 50%;
        height: 44rpx;
        font-size: 32rpx;
        text-align: right;
        color: #333333;
        line-height: 44rpx;
        padding-right: 20rpx;
      }
    }

    .all_price {
      position: relative;
      margin: 0 16rpx 0 22rpx;
      height: 120rpx;
      line-height: 120rpx;

      .word_right {
        position: absolute;
        height: 44rpx;
        font-size: 32rpx;
        text-align: left;
        color: #333333;
        line-height: 44rpx;
        top: 30rpx;
        right: 28rpx;
      }
    }

    .bottom_text {
      display: flex;
      align-items: center;
      margin: 0 20rpx 0 30rpx;
      height: 100rpx;
      line-height: 100rpx;

      .text_left {
        width: 30%;
        height: 44rpx;
        font-size: 32rpx;
        text-align: left;
        color: #333333;
        line-height: 44rpx;
      }

      .text_right {
        width: 70%;
        height: 44rpx;
        font-size: 24rpx;
        text-align: right;
        color: #666666;
        line-height: 44rpx;
        padding-right: 20rpx;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
</style>

<style>
  page {
    background-color: #f8f8f8;
  }
</style>
