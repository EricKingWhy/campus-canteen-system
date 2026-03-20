<!-- <template>
  <view class="container">
    <image src="../../static/icon/饿饿.png" mode="scaleToFill" />
    <view class="pay">支付成功</view>
    <view class="time">您的美食预计{{ arrivalTime }}完成制作/view>
    <view class="success_desc"> 后厨疯狂备餐ing, 请耐心等待~ </view>
    <view class="btn_box">
      <button class="return_btn" @click="toHome()">返回首页</button>
      <button class="detail_btn" @click="toDetail()">查看订单</button>
    </view>
  </view>
</template>

<script lang="ts" setup>
import {onLoad} from '@dcloudio/uni-app'
import {ref} from 'vue'

const orderId = ref(0)
const orderNumber = ref('')
const orderAmount = ref(0)
const orderTime = ref('')
const arrivalTime = ref('')

onLoad(async (options: any) => {
  console.log('options', options)
  orderId.value = options.orderId
  orderNumber.value = options.orderNumber
  orderAmount.value = options.orderAmount
  orderTime.value = options.orderTime
  getHarfAnOur()
})

// 获取一小时以后的时const getHarfAnOur = () => {
  const date = new Date()
  date.setTime(date.getTime() + 3600000)
  let hours = date.getHours().toString()
  let minutes = date.getMinutes().toString()
  if (hours.length === 1) hours = '0' + hours
  if (minutes.length === 1) minutes = '0' + minutes
  arrivalTime.value = hours + ':' + minutes
}

const toHome = () => {
  uni.switchTab({
    url: '/pages/index/index',
  })
}
const toDetail = () => {
  uni.redirectTo({
    url: '/pages/orderDetail/orderDetail?orderId=' + orderId.value,
  })
}
</script>

<style lang="less" scoped>
.container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
}
image {
  width: 300rpx;
  height: 300rpx;
}
.pay {
  font-size: 32rpx;
  color: #333;
  text-align: center;
  margin-top: 100rpx;
}
.time {
  font-size: 28rpx;
  color: #0af;
  text-align: center;
  margin-top: 20rpx;
}
.success_desc {
  font-size: 28rpx;
  color: #666;
  text-align: center;
  margin-top: 50rpx;
}
.btn_box {
  display: flex;
  justify-content: space-around;
  margin-top: 100rpx;
  .return_btn {
    margin: 10px;
    width: 250rpx;
    height: 78rpx;
    line-height: 78rpx;
    border: #00aaff solid 1rpx;
    border-radius: 40rpx;
    // background: #00aaff;
    color: #00aaff;
    font-size: 30rpx;
    text-align: center;
  }
  .detail_btn {
    margin: 10px;
    width: 250rpx;
    height: 80rpx;
    line-height: 80rpx;
    border-radius: 40rpx;
    background: #00aaff;
    border: none;
    color: #fff;
    font-size: 30rpx;
    text-align: center;
  }
}
</style>

<style lang="less">
.page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
}
</style> -->


<template>
  <view class="container">
    <image src="../../static/icon/饿饿.png" mode="scaleToFill" />
    <view class="pay">支付成功</view>

    <view class="pickup_box">
      <view class="label">取餐号码</view>
      <view class="number">{{ pickupNo }}</view>
      
      <view class="time_tip">
        您的美食预计 <text class="highlight">{{ pickupTime }}</text> 完成制作      </view>
    </view>

    <view class="success_desc"> 后厨疯狂备餐ing, 请留意叫号屏~ </view>
    
    <view class="btn_box">
      <button class="return_btn" @click="toHome()">返回首页</button>
      <button class="detail_btn" @click="toDetail()">查看订单</button>
    </view>
  </view>
</template>

<script lang="ts" setup>
import { onLoad } from '@dcloudio/uni-app'
import { ref } from 'vue'

const orderId = ref(0)
const orderNumber = ref('')
const orderAmount = ref(0)
const orderTime = ref('')

// 【新增】定义新变量
const pickupTime = ref('') // 预约取餐时间
const pickupNo = ref('')   // 取餐号码

onLoad(async (options: any) => {
  console.log('支付成功页接收参数', options)
  orderId.value = options.orderId
  orderNumber.value = options.orderNumber
  orderAmount.value = options.orderAmount
  orderTime.value = options.orderTime

  // 【修复】接收上一页传来的预约时间 (不再自己在本地点点计算了)
  if (options.pickupTime) {
    pickupTime.value = options.pickupTime
  } else {
    // 防止没传参的兜底
    pickupTime.value = '尽快'
  }

  // 【修复】生成取餐号：截取订单号的后4位
  if (options.orderNumber) {
    const str = options.orderNumber.toString()
    // 如果订单号长于4位，取后4位；否则直接显示
    pickupNo.value = str.length > 4 ? str.substring(str.length - 4) : str
  }
})

// 【删除】原来的 getHarfAnOur 函数已经不需要了，删掉它

const toHome = () => {
  uni.switchTab({
    url: '/pages/index/index',
  })
}
const toDetail = () => {
  uni.redirectTo({
    url: '/pages/orderDetail/orderDetail?orderId=' + orderId.value,
  })
}
</script>

<style lang="less" scoped>
.container {
  display: flex;
  flex-direction: column;
  align-items: center;
  // justify-content: center; // 改为顶部对齐，防止内容过多挤  padding-top: 100rpx; 
  height: 100vh;
  box-sizing: border-box;
  background-color: #fff;
}

image {
  width: 240rpx;
  height: 240rpx;
}

.pay {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  text-align: center;
  margin-top: 20rpx;
}

/* 【新增】取餐信息盒子样*/
.pickup_box {
  margin-top: 60rpx;
  text-align: center;
  width: 100%;
  
  .label {
    font-size: 28rpx;
    color: #888;
    margin-bottom: 10rpx;
  }
  
  .number {
    font-size: 100rpx; /* 超大字体显示取餐*/
    font-weight: bold;
    color: #00aaff;
    font-family: Arial, Helvetica, sans-serif;
    letter-spacing: 4rpx;
    line-height: 1.2;
  }
  
  .time_tip {
    margin-top: 30rpx;
    font-size: 30rpx;
    color: #333;
    
    .highlight {
      color: #e94e3c; /* 醒目的时间颜*/
      font-weight: bold;
      font-size: 36rpx;
      margin: 0 10rpx;
    }
  }
}

.success_desc {
  font-size: 26rpx;
  color: #999;
  text-align: center;
  margin-top: 40rpx;
  background: #f8f8f8;
  padding: 10rpx 30rpx;
  border-radius: 30rpx;
}

.btn_box {
  display: flex;
  justify-content: space-around;
  margin-top: 100rpx;
  width: 100%;
  padding: 0 40rpx;
  box-sizing: border-box;

  .return_btn {
    width: 300rpx;
    height: 80rpx;
    line-height: 80rpx;
    border: #00aaff solid 2rpx;
    border-radius: 40rpx;
    background: #fff;
    color: #00aaff;
    font-size: 30rpx;
    text-align: center;
    font-weight: bold;
  }
  .detail_btn {
    width: 300rpx;
    height: 80rpx;
    line-height: 80rpx;
    border-radius: 40rpx;
    background: #00aaff;
    border: none;
    color: #fff;
    font-size: 30rpx;
    text-align: center;
    font-weight: bold;
  }
}
</style>
