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
  __name: "submit",
  setup(__props) {
    const diningType = common_vendor.ref(1);
    const cartList = common_vendor.ref([]);
    const remark = common_vendor.ref("");
    const tablewareNumber = common_vendor.ref(1);
    const selectedTimeStr = common_vendor.ref("立即取餐");
    const timeSlots = common_vendor.ref([]);
    const resolveImageUrl = (image) => {
      if (!image)
        return "/static/default_dish.png";
      if (image.startsWith("http://") || image.startsWith("https://"))
        return image;
      if (image.startsWith("/static/dish/"))
        return baseUrl + image;
      return image;
    };
    const safeAreaTop = common_vendor.ref(40);
    const pickupLocation = common_vendor.computed(() => {
      return diningType.value === 1 ? "智能食堂一楼取餐口" : "智能食堂二楼取餐口(打包)";
    });
    const totalPrice = common_vendor.computed(() => {
      let sum = 0;
      cartList.value.forEach((item) => sum += item.amount * item.number);
      return sum.toFixed(2);
    });
    const estimatedTimeStr = common_vendor.computed(() => {
      const now = /* @__PURE__ */ new Date();
      now.setMinutes(now.getMinutes() + 15);
      const h = now.getHours().toString().padStart(2, "0");
      const m = now.getMinutes().toString().padStart(2, "0");
      return `${h}:${m}`;
    });
    common_vendor.onLoad(() => {
      const sysInfo = common_vendor.index.getSystemInfoSync();
      if (sysInfo.safeArea) {
        safeAreaTop.value = sysInfo.safeArea.top + 10;
      }
    });
    common_vendor.onShow(() => {
      loadCartData();
      generateTimeSlots();
    });
    const goBack = () => common_vendor.index.navigateBack();
    const loadCartData = () => {
      common_vendor.index.request({
        url: baseUrl + "/user/shoppingCart/list",
        method: "GET",
        header: { "authentication": common_vendor.index.getStorageSync("token") },
        success: (res) => {
          if (res.data.code === 0 || res.data.code === 1) {
            cartList.value = res.data.data || [];
          }
        }
      });
    };
    const generateTimeSlots = () => {
      const slots = ["立即取餐"];
      const now = /* @__PURE__ */ new Date();
      let m = Math.ceil(now.getMinutes() / 10) * 10;
      now.setMinutes(m);
      for (let i = 0; i < 12; i++) {
        now.setMinutes(now.getMinutes() + 10);
        const h = now.getHours().toString().padStart(2, "0");
        const min = now.getMinutes().toString().padStart(2, "0");
        slots.push(`${h}:${min}`);
      }
      timeSlots.value = slots;
    };
    const onTimeChange = (e) => {
      const index = e.detail.value;
      selectedTimeStr.value = timeSlots.value[index];
    };
    const updateTableware = (delta) => {
      const newVal = tablewareNumber.value + delta;
      if (newVal >= 1 && newVal <= 10)
        tablewareNumber.value = newVal;
    };
    const submitOrder = () => {
      if (cartList.value.length === 0)
        return;
      common_vendor.index.showLoading({ title: "提交中..." });
      const now = /* @__PURE__ */ new Date();
      const y = now.getFullYear();
      const mo = (now.getMonth() + 1).toString().padStart(2, "0");
      const d = now.getDate().toString().padStart(2, "0");
      let timePart = estimatedTimeStr.value + ":00";
      if (selectedTimeStr.value !== "立即取餐")
        timePart = selectedTimeStr.value + ":00";
      const deliveryTimeStr = `${y}-${mo}-${d}T${timePart}`;
      const payload = {
        addressBookId: null,
        payMethod: 1,
        remark: remark.value,
        amount: parseFloat(totalPrice.value),
        address: pickupLocation.value,
        estimatedDeliveryTime: deliveryTimeStr,
        packAmount: diningType.value === 2 ? 1 : 0,
        tablewareNumber: tablewareNumber.value,
        tablewareStatus: 1
      };
      common_vendor.index.request({
        url: baseUrl + "/user/order/submit",
        method: "POST",
        data: payload,
        header: {
          "authentication": common_vendor.index.getStorageSync("token"),
          "Content-Type": "application/json"
        },
        success: (res) => {
          common_vendor.index.hideLoading();
          if (res.data.code === 0 || res.data.code === 1) {
            const orderData = res.data.data;
            const orderId = (orderData == null ? void 0 : orderData.id) || "";
            const orderNumber = (orderData == null ? void 0 : orderData.orderNumber) || "";
            common_vendor.index.redirectTo({
              url: `/pages/pay/pay?orderId=${orderId}&orderNumber=${orderNumber}&amount=${totalPrice.value}&diningType=${diningType.value}`
            });
          } else {
            common_vendor.index.showToast({ title: res.data.msg || "失败", icon: "none" });
          }
        }
      });
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.p({
          type: "back",
          size: "24",
          color: "#333"
        }),
        b: common_vendor.o(goBack),
        c: safeAreaTop.value + "px",
        d: diningType.value === 1
      }, diningType.value === 1 ? {} : {}, {
        e: diningType.value === 1 ? 1 : "",
        f: common_vendor.o(($event) => diningType.value = 1),
        g: diningType.value === 2
      }, diningType.value === 2 ? {} : {}, {
        h: diningType.value === 2 ? 1 : "",
        i: common_vendor.o(($event) => diningType.value = 2),
        j: common_vendor.t(pickupLocation.value),
        k: common_vendor.p({
          type: "location-filled",
          size: "20",
          color: "#00BA9D"
        }),
        l: common_vendor.p({
          type: "calendar",
          size: "20",
          color: "#00BA9D"
        }),
        m: common_vendor.t(selectedTimeStr.value),
        n: selectedTimeStr.value === "立即取餐"
      }, selectedTimeStr.value === "立即取餐" ? {
        o: common_vendor.t(estimatedTimeStr.value)
      } : {}, {
        p: common_vendor.p({
          type: "right",
          size: "16",
          color: "#999"
        }),
        q: timeSlots.value,
        r: common_vendor.o(onTimeChange),
        s: common_vendor.f(cartList.value, (item, index, i0) => {
          return {
            a: resolveImageUrl(item.image || item.pic),
            b: common_vendor.t(item.name),
            c: common_vendor.t(item.amount),
            d: common_vendor.t(item.dishFlavor || "正常"),
            e: common_vendor.t(item.number),
            f: index
          };
        }),
        t: common_vendor.p({
          type: "compose",
          size: "24",
          color: "#94a3b8"
        }),
        v: remark.value,
        w: common_vendor.o(($event) => remark.value = $event.detail.value),
        x: common_vendor.p({
          type: "staff-filled",
          size: "24",
          color: "#94a3b8"
        }),
        y: common_vendor.o(($event) => updateTableware(-1)),
        z: common_vendor.t(tablewareNumber.value),
        A: common_vendor.o(($event) => updateTableware(1)),
        B: common_vendor.t(totalPrice.value),
        C: common_vendor.p({
          type: "arrowright",
          size: "18",
          color: "#fff"
        }),
        D: common_vendor.o(submitOrder)
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__file", "C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/smart-canteen-app/src/pages/submit/submit.vue"]]);
wx.createPage(MiniProgramPage);
