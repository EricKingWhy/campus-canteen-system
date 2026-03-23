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
  __name: "orderDetail",
  setup(__props) {
    const resolveImageUrl = (image) => {
      if (!image)
        return "/static/default_dish.png";
      if (image.startsWith("http://") || image.startsWith("https://"))
        return image;
      if (image.startsWith("/static/dish/"))
        return baseUrl + image;
      return image;
    };
    const safeAreaTop = common_vendor.ref(44);
    const loading = common_vendor.ref(true);
    const orderId = common_vendor.ref("");
    const order = common_vendor.ref({});
    const orderDetailList = common_vendor.ref([]);
    const statusConfig = {
      1: { text: "待付款", subtitle: "请尽快完成支付", icon: "wallet", class: "status-pending" },
      2: { text: "待接单", subtitle: "订单等待商家接单", icon: "loop", class: "status-active" },
      3: { text: "后厨制作中", subtitle: "🔥 后厨制作中，请耐心等待", icon: "fire", class: "status-active" },
      4: { text: "待取餐", subtitle: "🟢 餐品已备好，请前往窗口取餐", icon: "flag", class: "status-ready" },
      5: { text: "已完成", subtitle: "感谢您的光临，期待下次再见", icon: "checkmarkempty", class: "status-success" },
      6: { text: "已取消", subtitle: "订单已取消", icon: "closeempty", class: "status-cancelled" }
    };
    const statusText = common_vendor.computed(() => {
      var _a;
      return ((_a = statusConfig[order.value.status]) == null ? void 0 : _a.text) || "未知状态";
    });
    const statusSubtitle = common_vendor.computed(() => {
      var _a;
      return ((_a = statusConfig[order.value.status]) == null ? void 0 : _a.subtitle) || "";
    });
    const statusIcon = common_vendor.computed(() => {
      var _a;
      return ((_a = statusConfig[order.value.status]) == null ? void 0 : _a.icon) || "info";
    });
    const statusClass = common_vendor.computed(() => {
      var _a;
      return ((_a = statusConfig[order.value.status]) == null ? void 0 : _a.class) || "status-pending";
    });
    const pickupNumber = common_vendor.computed(() => {
      const num = order.value.number || order.value.id;
      return String(num).slice(-5) || "00000";
    });
    const estimatedTimeStr = common_vendor.computed(() => {
      if (!order.value.estimatedDeliveryTime)
        return "--:--";
      return String(order.value.estimatedDeliveryTime).substring(11, 16);
    });
    const orderTimeStr = common_vendor.computed(() => {
      if (!order.value.orderTime)
        return "--";
      return String(order.value.orderTime).replace("T", " ").substring(0, 16);
    });
    const canCancel = common_vendor.computed(() => order.value.status === 1 || order.value.status === 2);
    const canReorder = common_vendor.computed(() => order.value.status >= 4);
    common_vendor.onLoad((options) => {
      const sysInfo = common_vendor.index.getSystemInfoSync();
      if (sysInfo.safeArea)
        safeAreaTop.value = sysInfo.safeArea.top + 10;
      const paramId = (options == null ? void 0 : options.id) || (options == null ? void 0 : options.orderId);
      console.log("OrderDetail onLoad - options:", options);
      console.log("OrderDetail onLoad - paramId:", paramId);
      if (paramId) {
        orderId.value = String(paramId);
        fetchOrderDetail();
      } else {
        common_vendor.index.showToast({ title: "订单ID缺失", icon: "none" });
        loading.value = false;
      }
    });
    common_vendor.onPullDownRefresh(() => {
      fetchOrderDetail();
    });
    common_vendor.onShow(() => {
      if (orderId.value) {
        console.log("OrderDetail onShow - 刷新订单状态");
        fetchOrderDetail();
      }
    });
    const fetchOrderDetail = () => {
      loading.value = true;
      common_vendor.index.request({
        url: `${baseUrl}/user/order/orderDetail/${orderId.value}`,
        method: "GET",
        header: { "authentication": common_vendor.index.getStorageSync("token") },
        success: (res) => {
          console.log("Order Detail Response:", res.data);
          if (res.data.code === 1 || res.data.code === 0) {
            const data = res.data.data;
            order.value = data || {};
            orderDetailList.value = (data == null ? void 0 : data.orderDetailList) || [];
            console.log("Order:", order.value);
            console.log("Detail List:", orderDetailList.value);
          } else {
            common_vendor.index.showToast({ title: res.data.msg || "加载失败", icon: "none" });
          }
        },
        fail: (err) => {
          console.error("Fetch order detail failed:", err);
          common_vendor.index.showToast({ title: "网络错误", icon: "none" });
        },
        complete: () => {
          loading.value = false;
          common_vendor.index.stopPullDownRefresh();
        }
      });
    };
    const cancelOrder = () => {
      common_vendor.index.showModal({
        title: "确认取消",
        content: "确定要取消此订单吗？",
        success: (res) => {
          if (res.confirm) {
            common_vendor.index.showLoading({ title: "取消中..." });
            common_vendor.index.request({
              url: `${baseUrl}/user/order/cancel/${orderId.value}`,
              method: "PUT",
              header: { "authentication": common_vendor.index.getStorageSync("token") },
              success: (res2) => {
                common_vendor.index.hideLoading();
                if (res2.data.code === 1 || res2.data.code === 0) {
                  common_vendor.index.showToast({ title: "已取消", icon: "success" });
                  fetchOrderDetail();
                } else {
                  common_vendor.index.showToast({ title: res2.data.msg || "取消失败", icon: "none" });
                }
              },
              fail: () => {
                common_vendor.index.hideLoading();
                common_vendor.index.showToast({ title: "网络错误", icon: "none" });
              }
            });
          }
        }
      });
    };
    const reOrder = () => {
      common_vendor.index.showLoading({ title: "加载中..." });
      common_vendor.index.request({
        url: `${baseUrl}/user/order/repetition/${orderId.value}`,
        method: "POST",
        header: { "authentication": common_vendor.index.getStorageSync("token") },
        success: (res) => {
          common_vendor.index.hideLoading();
          if (res.data.code === 1 || res.data.code === 0) {
            common_vendor.index.showToast({ title: "已加入购物车", icon: "success" });
            setTimeout(() => common_vendor.index.switchTab({ url: "/pages/index/index_v2" }), 1e3);
          } else {
            common_vendor.index.showToast({ title: res.data.msg || "操作失败", icon: "none" });
          }
        }
      });
    };
    const completeOrder = () => {
      common_vendor.index.showModal({
        title: "确认取餐",
        content: "您确认已经收到餐品了吗？",
        success: (res) => {
          if (res.confirm) {
            common_vendor.index.showLoading({ title: "处理中..." });
            common_vendor.index.request({
              url: `${baseUrl}/user/order/complete/${orderId.value}`,
              method: "PUT",
              header: { "authentication": common_vendor.index.getStorageSync("token") },
              success: (res2) => {
                common_vendor.index.hideLoading();
                if (res2.data.code === 1 || res2.data.code === 0) {
                  common_vendor.index.showToast({ title: "取餐成功", icon: "success" });
                  fetchOrderDetail();
                } else {
                  common_vendor.index.showToast({ title: res2.data.msg || "操作失败", icon: "none" });
                }
              },
              fail: () => {
                common_vendor.index.hideLoading();
                common_vendor.index.showToast({ title: "网络错误", icon: "none" });
              }
            });
          }
        }
      });
    };
    const goBack = () => common_vendor.index.navigateBack();
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.p({
          type: "back",
          size: "22",
          color: "#333"
        }),
        b: common_vendor.o(goBack),
        c: safeAreaTop.value + "px",
        d: loading.value
      }, loading.value ? {
        e: common_vendor.p({
          type: "spinner-cycle",
          size: "32",
          color: "#00b89c"
        })
      } : order.value.id ? common_vendor.e({
        g: common_vendor.p({
          type: statusIcon.value,
          size: "36",
          color: "#fff"
        }),
        h: common_vendor.t(statusText.value),
        i: common_vendor.t(statusSubtitle.value),
        j: common_vendor.n(statusClass.value),
        k: order.value.status && order.value.status <= 4
      }, order.value.status && order.value.status <= 4 ? {
        l: common_vendor.p({
          type: "shop",
          size: "14",
          color: "#ea580c"
        }),
        m: common_vendor.t(order.value.packAmount > 0 ? "打包 · 二楼" : "堂食 · 一楼"),
        n: common_vendor.t(order.value.packAmount > 0 ? "B" : "A"),
        o: common_vendor.t(pickupNumber.value),
        p: common_vendor.t(order.value.packAmount > 0 ? "二楼" : "一楼"),
        q: common_vendor.t(estimatedTimeStr.value),
        r: common_vendor.t(order.value.address || (order.value.packAmount > 0 ? "智能食堂二楼取餐口" : "智能食堂一楼取餐口"))
      } : {}, {
        s: order.value.status === 3 || order.value.status === 4
      }, order.value.status === 3 || order.value.status === 4 ? {
        t: common_vendor.p({
          type: "info",
          size: "18",
          color: "#f59e0b"
        })
      } : {}, {
        v: common_vendor.p({
          type: "cart",
          size: "20",
          color: "#333"
        }),
        w: common_vendor.t(orderDetailList.value.length),
        x: orderDetailList.value.length > 0
      }, orderDetailList.value.length > 0 ? {
        y: common_vendor.f(orderDetailList.value, (item, index, i0) => {
          return {
            a: resolveImageUrl(item.pic || item.image),
            b: common_vendor.t(item.name),
            c: common_vendor.t(item.dishFlavor || "正常"),
            d: common_vendor.t(item.amount),
            e: common_vendor.t(item.number),
            f: index
          };
        })
      } : {}, {
        z: common_vendor.p({
          type: "info",
          size: "20",
          color: "#333"
        }),
        A: common_vendor.t(order.value.remark || "无备注"),
        B: common_vendor.t(order.value.tablewareNumber || 1),
        C: common_vendor.t(order.value.number),
        D: common_vendor.t(orderTimeStr.value),
        E: common_vendor.t(order.value.amount || "0.00"),
        F: order.value.status === 6 && order.value.cancelReason
      }, order.value.status === 6 && order.value.cancelReason ? {
        G: common_vendor.p({
          type: "closeempty",
          size: "18",
          color: "#ef4444"
        }),
        H: common_vendor.t(order.value.cancelReason)
      } : {}, {
        I: canCancel.value
      }, canCancel.value ? {
        J: common_vendor.o(cancelOrder)
      } : {}, {
        K: canReorder.value
      }, canReorder.value ? {
        L: common_vendor.o(reOrder)
      } : {}, {
        M: order.value.status === 4
      }, order.value.status === 4 ? {
        N: common_vendor.o(completeOrder)
      } : {}) : {
        O: common_vendor.p({
          type: "info",
          size: "48",
          color: "#999"
        }),
        P: common_vendor.o(fetchOrderDetail)
      }, {
        f: order.value.id
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__file", "C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/smart-canteen-app/src/pages/orderDetail/orderDetail.vue"]]);
wx.createPage(MiniProgramPage);
