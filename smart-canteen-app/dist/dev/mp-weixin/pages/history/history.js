"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const api_order = require("../../api/order.js");
const api_cart = require("../../api/cart.js");
require("../../utils/http.js");
require("../../stores/modules/user.js");
if (!Math) {
  pushMsg();
}
const pushMsg = () => "../../components/message/pushMsg.js";
const baseURL = "http://121.41.59.61:8081";
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "history",
  setup(__props) {
    const childComp = common_vendor.ref(null);
    const statusOptions = [
      {
        status: 0,
        name: "全部订单"
      },
      {
        status: 1,
        name: "待付款"
      },
      {
        status: 5,
        name: "已完成"
      },
      {
        status: 6,
        name: "已取消"
      }
    ];
    const statusList = [
      {
        status: 0,
        name: "全部订单"
      },
      {
        status: 1,
        name: "待付款"
      },
      {
        status: 2,
        name: "待接单"
      },
      {
        status: 3,
        name: "制作中"
      },
      {
        status: 4,
        name: "待取餐"
      },
      {
        status: 5,
        name: "已完成"
      },
      {
        status: 6,
        name: "已取消"
      }
    ];
    const activeIndex = common_vendor.ref(0);
    const historyOrders = common_vendor.ref([]);
    const showPendingEmptyState = common_vendor.computed(() => activeIndex.value === 1 && historyOrders.value.length === 0);
    const resolveImageUrl = (image) => {
      if (!image)
        return "/static/default_dish.png";
      if (image.startsWith("http://") || image.startsWith("https://"))
        return image;
      if (image.startsWith("/"))
        return baseURL + image;
      return `${baseURL}/static/dish/${image.replace(/^\/+/, "")}`;
    };
    const orderDTO = common_vendor.ref({
      page: 1,
      pageSize: 6
      // status: 0,
    });
    const total = common_vendor.ref(0);
    common_vendor.onLoad(async () => {
      console.log("首先分页获取所有订单信息", orderDTO.value);
      await getOrderPage(0);
    });
    common_vendor.onShow(async () => {
      console.log("History onShow - 刷新订单列表获取最新状态");
      orderDTO.value.page = 1;
      historyOrders.value = [];
      await getOrderPage(activeIndex.value);
    });
    common_vendor.onReachBottom(() => {
      console.log("Page:", orderDTO.value.page);
      console.log("Page Size:", orderDTO.value.pageSize);
      if (orderDTO.value.page * orderDTO.value.pageSize >= total.value) {
        console.log("end!");
        common_vendor.index.showToast({
          title: "end!",
          icon: "none"
        });
        return;
      }
      orderDTO.value.page += 1;
      getOrderPage(activeIndex.value);
    });
    const getOrderPage = async (index, type) => {
      activeIndex.value = index;
      console.log("根据status获取订单信息");
      if (index !== 0) {
        orderDTO.value.status = statusOptions[index].status;
      } else {
        delete orderDTO.value.status;
      }
      console.log("orderDTO", orderDTO.value);
      const res = await api_order.getOrderPageAPI(orderDTO.value);
      if (type === "更改状态") {
        historyOrders.value = res.data.records;
        orderDTO.value.page = 1;
      } else {
        historyOrders.value = historyOrders.value.concat(res.data.records);
      }
      total.value = res.data.total;
    };
    const toOrderDetail = (id) => {
      common_vendor.index.navigateTo({
        url: "/pages/orderDetail/orderDetail?orderId=" + String(id)
      });
    };
    const goToPay = (item) => {
      if (!(item == null ? void 0 : item.id)) {
        common_vendor.index.showToast({ title: "订单信息缺失", icon: "none" });
        return;
      }
      const orderId = String(item.id);
      const orderNumber = encodeURIComponent(String(item.number || ""));
      const amount = encodeURIComponent(String(item.amount ?? "0.00"));
      const diningType = Number(item.packAmount || 0) > 0 ? 2 : 1;
      common_vendor.index.navigateTo({
        url: `/pages/pay/pay?orderId=${orderId}&orderNumber=${orderNumber}&amount=${amount}&diningType=${diningType}`
      });
    };
    const reOrder = async (id) => {
      console.log("再来一单", id);
      await api_cart.cleanCartAPI();
      await api_order.reOrderAPI(id);
      common_vendor.index.switchTab({
        url: "/pages/category/category"
      });
    };
    const pushOrder = (id) => {
      console.log("催单", id);
      childComp.value.openPopup();
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.f(statusOptions, (item, index, i0) => {
          return {
            a: common_vendor.t(item.name),
            b: index,
            c: index === activeIndex.value ? 1 : "",
            d: common_vendor.o(($event) => getOrderPage(index, "更改状态"), index)
          };
        }),
        b: showPendingEmptyState.value
      }, showPendingEmptyState.value ? {
        c: common_assets._imports_0
      } : {
        d: common_vendor.f(historyOrders.value, (item, index, i0) => {
          return common_vendor.e({
            a: common_vendor.t(item.number),
            b: common_vendor.f(item.orderDetailList, (dish, dishIndex, i1) => {
              return {
                a: resolveImageUrl(dish.pic || dish.image),
                b: common_vendor.t(dish.name || "菜品"),
                c: common_vendor.t(dish.number || 1),
                d: dishIndex
              };
            }),
            c: common_vendor.t(item.orderTime),
            d: common_vendor.t(statusList[item.status].name),
            e: common_vendor.t(item.amount),
            f: common_vendor.t(item.totalNum),
            g: item.status === 1
          }, item.status === 1 ? {
            h: common_vendor.o(($event) => goToPay(item), index)
          } : {}, {
            i: common_vendor.o(($event) => reOrder(item.id), index),
            j: item.status === 2
          }, item.status === 2 ? {
            k: common_vendor.o(($event) => pushOrder(item.id), index)
          } : {}, {
            l: index,
            m: common_vendor.o(($event) => toOrderDetail(item.id), index)
          });
        })
      }, {
        e: common_vendor.sr(childComp, "73685b36-0", {
          "k": "childComp"
        })
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-73685b36"], ["__file", "C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/smart-canteen-app/src/pages/history/history.vue"]]);
wx.createPage(MiniProgramPage);
