"use strict";
const common_vendor = require("../../common/vendor.js");
if (!Array) {
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  _easycom_uni_icons2();
}
const _easycom_uni_icons = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-icons/uni-icons.js";
if (!Math) {
  _easycom_uni_icons();
}
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "success",
  setup(__props) {
    const safeAreaTop = common_vendor.ref(44);
    const orderId = common_vendor.ref("");
    const amount = common_vendor.ref("0.00");
    const estimatedTime = common_vendor.ref("--:--");
    const orderTime = common_vendor.ref("--:--");
    const diningType = common_vendor.ref(1);
    const numberPrefix = common_vendor.computed(() => diningType.value === 1 ? "A" : "B");
    const pickupNumber = common_vendor.computed(() => {
      const idStr = String(orderId.value);
      return idStr.slice(-5) || "00000";
    });
    common_vendor.onLoad((options) => {
      const sysInfo = common_vendor.index.getSystemInfoSync();
      if (sysInfo.safeArea)
        safeAreaTop.value = sysInfo.safeArea.top + 10;
      if (options) {
        orderId.value = options.orderId || options.id || "";
        amount.value = options.amount || "0.00";
        diningType.value = parseInt(options.packAmount) > 0 ? 2 : 1;
        if (options.estimatedTime) {
          estimatedTime.value = options.estimatedTime.substring(11, 16);
        }
        if (options.orderTime) {
          orderTime.value = options.orderTime.substring(11, 16);
        }
      }
    });
    const goBack = () => common_vendor.index.navigateBack();
    const goHome = () => common_vendor.index.switchTab({ url: "/pages/index/index_v2" });
    const viewDetail = () => {
      common_vendor.index.redirectTo({ url: `/pages/orderDetail/orderDetail?id=${orderId.value}` });
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.p({
          type: "back",
          size: "22",
          color: "#333"
        }),
        b: common_vendor.o(goBack),
        c: safeAreaTop.value + "px",
        d: common_vendor.p({
          type: "checkmarkempty",
          size: "52",
          color: "#fff"
        }),
        e: common_vendor.p({
          type: "shop-filled",
          size: "16",
          color: "#ea580c"
        }),
        f: common_vendor.t(diningType.value === 1 ? "堂食 · 一楼" : "打包 · 二楼"),
        g: common_vendor.t(numberPrefix.value),
        h: common_vendor.t(pickupNumber.value),
        i: common_vendor.t(diningType.value === 1 ? "一楼" : "二楼"),
        j: common_vendor.t(amount.value),
        k: common_vendor.t(estimatedTime.value),
        l: common_vendor.t(orderTime.value),
        m: common_vendor.p({
          type: "arrowright",
          size: "18",
          color: "#fff"
        }),
        n: common_vendor.o(viewDetail),
        o: common_vendor.o(goHome)
      };
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__file", "C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/smart-canteen-app/src/pages/pay/success.vue"]]);
wx.createPage(MiniProgramPage);
