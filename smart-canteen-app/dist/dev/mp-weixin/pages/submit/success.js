"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "success",
  setup(__props) {
    const orderId = common_vendor.ref(0);
    const orderNumber = common_vendor.ref("");
    const orderAmount = common_vendor.ref(0);
    const orderTime = common_vendor.ref("");
    const pickupTime = common_vendor.ref("");
    const pickupNo = common_vendor.ref("");
    common_vendor.onLoad(async (options) => {
      console.log("支付成功页接收参数", options);
      orderId.value = options.orderId;
      orderNumber.value = options.orderNumber;
      orderAmount.value = options.orderAmount;
      orderTime.value = options.orderTime;
      if (options.pickupTime) {
        pickupTime.value = options.pickupTime;
      } else {
        pickupTime.value = "尽快";
      }
      if (options.orderNumber) {
        const str = options.orderNumber.toString();
        pickupNo.value = str.length > 4 ? str.substring(str.length - 4) : str;
      }
    });
    const toHome = () => {
      common_vendor.index.switchTab({
        url: "/pages/index/index"
      });
    };
    const toDetail = () => {
      common_vendor.index.redirectTo({
        url: "/pages/orderDetail/orderDetail?orderId=" + orderId.value
      });
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.t(pickupNo.value),
        b: common_vendor.t(pickupTime.value),
        c: common_vendor.o(($event) => toHome()),
        d: common_vendor.o(($event) => toDetail())
      };
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-03f045ab"], ["__file", "C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/smart-canteen-app/src/pages/submit/success.vue"]]);
wx.createPage(MiniProgramPage);
