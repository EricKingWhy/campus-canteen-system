"use strict";
const common_vendor = require("../../common/vendor.js");
const api_favorite = require("../../api/favorite.js");
const api_cart = require("../../api/cart.js");
require("../../utils/http.js");
require("../../stores/modules/user.js");
const baseUrl = "http://121.41.59.61:8081";
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "favorite",
  setup(__props) {
    const loading = common_vendor.ref(true);
    const favoriteList = common_vendor.ref([]);
    const resolveImageUrl = (image) => {
      if (!image)
        return "/static/images/default-dish.png";
      if (image.startsWith("http"))
        return image;
      return baseUrl + image;
    };
    const loadFavorites = async () => {
      loading.value = true;
      try {
        const res = await api_favorite.favoriteListAPI();
        favoriteList.value = res.data || [];
      } catch (e) {
        console.error("加载收藏列表失败", e);
        common_vendor.index.showToast({ title: "加载失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    };
    const removeFavorite = async (dishId) => {
      common_vendor.index.showModal({
        title: "提示",
        content: "确定要取消收藏吗？",
        success: async (res) => {
          if (res.confirm) {
            try {
              await api_favorite.favoriteRemoveAPI(dishId);
              common_vendor.index.showToast({ title: "已取消收藏", icon: "success" });
              favoriteList.value = favoriteList.value.filter((d) => d.id !== dishId);
            } catch (e) {
              common_vendor.index.showToast({ title: "操作失败", icon: "none" });
            }
          }
        }
      });
    };
    const handleAddToCart = async (dish) => {
      try {
        await api_cart.addToCartAPI({
          dishId: dish.id
        });
        common_vendor.index.showToast({ title: "已加入购物车", icon: "success" });
      } catch (e) {
        console.error(e);
        common_vendor.index.showToast({ title: "请进入详情选规格", icon: "none" });
        setTimeout(() => {
          goDetail(dish);
        }, 1e3);
      }
    };
    const goDetail = (dish) => {
      common_vendor.index.showToast({ title: dish.name, icon: "none" });
    };
    common_vendor.onShow(() => {
      loadFavorites();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: loading.value
      }, loading.value ? {} : favoriteList.value.length === 0 ? {} : {
        c: common_vendor.f(favoriteList.value, (dish, k0, i0) => {
          return {
            a: resolveImageUrl(dish.image),
            b: common_vendor.t(dish.name),
            c: common_vendor.t(dish.description || "暂无描述"),
            d: common_vendor.t(dish.price),
            e: common_vendor.o(($event) => handleAddToCart(dish), dish.id),
            f: common_vendor.o(($event) => removeFavorite(dish.id), dish.id),
            g: dish.id,
            h: common_vendor.o(($event) => goDetail(dish), dish.id)
          };
        })
      }, {
        b: favoriteList.value.length === 0
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-89f6d808"], ["__file", "C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/smart-canteen-app/src/pages/favorite/favorite.vue"]]);
wx.createPage(MiniProgramPage);
