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
const baseUrl = "http://121.41.59.61:8081";
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
    const pad2 = (v) => String(v).padStart(2, "0");
    const formatToHHmm = (value) => {
      if (!value)
        return "--:--";
      const normalized = value.includes("T") ? value : value.replace(" ", "T");
      const date = new Date(normalized);
      if (!Number.isNaN(date.getTime())) {
        return `${pad2(date.getHours())}:${pad2(date.getMinutes())}`;
      }
      return value.length >= 16 ? value.substring(11, 16) : "--:--";
    };
    const fetchOrderDetail = () => {
      if (!orderId.value)
        return;
      common_vendor.index.request({
        url: `${baseUrl}/user/order/orderDetail/${orderId.value}`,
        method: "GET",
        header: { authentication: common_vendor.index.getStorageSync("token") },
        success: (res) => {
          var _a, _b, _c;
          const data = (_a = res == null ? void 0 : res.data) == null ? void 0 : _a.data;
          if ((((_b = res == null ? void 0 : res.data) == null ? void 0 : _b.code) === 1 || ((_c = res == null ? void 0 : res.data) == null ? void 0 : _c.code) === 0) && data) {
            if (data.amount !== void 0 && data.amount !== null) {
              amount.value = Number(data.amount).toFixed(2);
            }
            if (data.packAmount !== void 0 && data.packAmount !== null) {
              diningType.value = Number(data.packAmount) > 0 ? 2 : 1;
            }
            estimatedTime.value = formatToHHmm(data.estimatedDeliveryTime);
            orderTime.value = formatToHHmm(data.orderTime);
          }
        }
      });
    };
    common_vendor.onLoad((options) => {
      const sysInfo = common_vendor.index.getSystemInfoSync();
      if (sysInfo.safeArea)
        safeAreaTop.value = sysInfo.safeArea.top + 10;
      if (options) {
        orderId.value = options.orderId || options.id || "";
        amount.value = options.amount || "0.00";
        diningType.value = parseInt(options.packAmount) > 0 ? 2 : 1;
        if (options.estimatedTime) {
          estimatedTime.value = formatToHHmm(options.estimatedTime);
        }
        if (options.orderTime) {
          orderTime.value = formatToHHmm(options.orderTime);
        }
      }
      fetchOrderDetail();
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
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__file", "C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/smart-canteen-app/src/subpkg-pay/pay/success.vue"]]);
wx.createPage(MiniProgramPage);
