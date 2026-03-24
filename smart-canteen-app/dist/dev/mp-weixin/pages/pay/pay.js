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
  __name: "pay",
  setup(__props) {
    const safeAreaTop = common_vendor.ref(44);
    const safeAreaBottom = common_vendor.ref(34);
    const orderId = common_vendor.ref("");
    const orderNumber = common_vendor.ref("");
    const amount = common_vendor.ref("0.00");
    const diningType = common_vendor.ref(1);
    const payMethod = common_vendor.ref(1);
    common_vendor.onLoad((options) => {
      const sysInfo = common_vendor.index.getSystemInfoSync();
      if (sysInfo.safeArea) {
        safeAreaTop.value = sysInfo.safeArea.top + 10;
        safeAreaBottom.value = sysInfo.screenHeight - sysInfo.safeArea.bottom + 10;
      }
      console.log("Pay page options:", options);
      if (options) {
        orderId.value = options.orderId || "";
        orderNumber.value = options.orderNumber || "";
        amount.value = options.amount || "0.00";
        diningType.value = parseInt(options.diningType) || 1;
      }
      if (orderId.value) {
        fetchLatestOrderInfo(orderId.value);
      }
    });
    const goBack = () => common_vendor.index.navigateBack();
    const confirmPay = () => {
      if (!orderNumber.value) {
        common_vendor.index.showToast({ title: "订单信息缺失", icon: "none" });
        return;
      }
      common_vendor.index.showLoading({ title: "支付中..." });
      common_vendor.index.request({
        url: `${baseUrl}/user/order/payment`,
        method: "PUT",
        header: {
          "authentication": common_vendor.index.getStorageSync("token"),
          "Content-Type": "application/json"
        },
        data: {
          orderNumber: orderNumber.value,
          payMethod: payMethod.value
        },
        success: (res) => {
          common_vendor.index.hideLoading();
          console.log("Payment response:", res.data);
          if (res.data.code === 1 || res.data.code === 0) {
            common_vendor.index.showToast({ title: "支付成功", icon: "success" });
            setTimeout(() => {
              common_vendor.index.redirectTo({
                url: `/pages/pay/success?orderId=${orderId.value}&amount=${amount.value}&packAmount=${diningType.value === 2 ? 1 : 0}&orderTime=${(/* @__PURE__ */ new Date()).toISOString()}`
              });
            }, 1e3);
          } else {
            common_vendor.index.showToast({ title: res.data.msg || "支付失败", icon: "none" });
          }
        },
        fail: (err) => {
          common_vendor.index.hideLoading();
          console.error("Payment failed:", err);
          common_vendor.index.showToast({ title: "网络错误", icon: "none" });
        }
      });
    };
    const fetchLatestOrderInfo = (id) => {
      common_vendor.index.request({
        url: `${baseUrl}/user/order/orderDetail/${id}`,
        method: "GET",
        header: { "authentication": common_vendor.index.getStorageSync("token") },
        success: (res) => {
          var _a, _b, _c;
          const data = (_a = res == null ? void 0 : res.data) == null ? void 0 : _a.data;
          if ((((_b = res == null ? void 0 : res.data) == null ? void 0 : _b.code) === 1 || ((_c = res == null ? void 0 : res.data) == null ? void 0 : _c.code) === 0) && data) {
            if (data.number) {
              orderNumber.value = String(data.number);
            }
            if (data.amount !== void 0 && data.amount !== null) {
              amount.value = Number(data.amount).toFixed(2);
            }
            if (data.packAmount !== void 0 && data.packAmount !== null) {
              diningType.value = Number(data.packAmount) > 0 ? 2 : 1;
            }
          }
        }
      });
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.p({
          type: "back",
          size: "22",
          color: "#333"
        }),
        b: common_vendor.o(goBack),
        c: safeAreaTop.value + "px",
        d: common_vendor.t(amount.value),
        e: payMethod.value === 1
      }, payMethod.value === 1 ? {
        f: common_vendor.p({
          type: "checkbox-filled",
          size: "24",
          color: "#07c160"
        })
      } : {}, {
        g: payMethod.value === 1 ? 1 : "",
        h: common_vendor.o(($event) => payMethod.value = 1),
        i: payMethod.value === 2
      }, payMethod.value === 2 ? {
        j: common_vendor.p({
          type: "checkbox-filled",
          size: "24",
          color: "#f59e0b"
        })
      } : {}, {
        k: payMethod.value === 2 ? 1 : "",
        l: common_vendor.o(($event) => payMethod.value = 2),
        m: orderNumber.value
      }, orderNumber.value ? {
        n: common_vendor.t(orderNumber.value)
      } : {}, {
        o: common_vendor.t(payMethod.value === 1 ? "确认支付" : "确认下单"),
        p: payMethod.value === 2 ? 1 : "",
        q: common_vendor.o(confirmPay),
        r: safeAreaBottom.value + "px"
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__file", "C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/smart-canteen-app/src/pages/pay/pay.vue"]]);
wx.createPage(MiniProgramPage);
