"use strict";
const common_vendor = require("../../common/vendor.js");
const DishDetailPopup = () => "../../components/DishDetailPopup.js";
const _sfc_main = {
  components: {
    DishDetailPopup
  },
  data() {
    return {
      baseUrl: "http://127.0.0.1:8081",
      // 后端基准地点点址
      categoryList: [],
      dishList: [],
      cartList: [],
      activeCategoryIndex: 0,
      searchKeyword: "",
      // 【核心新增】搜索关键词绑定
      cartPopupShow: false,
      showNutritionPopup: false,
      currentDish: {}
      // Will hold full dish data including nutrition and sold
    };
  },
  computed: {
    currentCategory() {
      return this.categoryList[this.activeCategoryIndex] || null;
    },
    totalAmount() {
      let total = 0;
      this.cartList.forEach((item) => total += item.amount * item.number);
      return total.toFixed(2);
    },
    totalNum() {
      let num = 0;
      this.cartList.forEach((item) => num += item.number);
      return num;
    }
  },
  onLoad() {
  },
  onShow() {
    console.log("Category Page onShow - Initialization");
    this.getCartList();
    this.getCategoryList();
  },
  methods: {
    // 1. 获取分类
    getCategoryList() {
      console.log("Fetching Categories from:", this.baseUrl + "/user/category/list");
      common_vendor.index.request({
        url: this.baseUrl + "/user/category/list",
        method: "GET",
        data: { type: 1 },
        header: { "authentication": common_vendor.index.getStorageSync("token") },
        success: (res) => {
          console.log("Category Response:", res.data);
          if (res.data.code === 0 || res.data.code === 1) {
            this.categoryList = res.data.data;
            if (this.categoryList.length > 0) {
              console.log("Loading dishes for category:", this.categoryList[0].id);
              this.getDishList(this.categoryList[0].id);
            } else {
              console.warn("Category List is Empty!");
            }
          } else {
            console.error("Category API Failed:", res.data.msg);
          }
        },
        fail: (err) => {
          console.error("Category Request Network Error:", err);
        }
      });
    },
    // 2. 获取菜品 (带图片修复)
    getDishList(categoryId) {
      console.log("Fetching Dishes for Category:", categoryId);
      common_vendor.index.request({
        url: this.baseUrl + "/user/dish/list",
        method: "GET",
        data: { categoryId, status: 1 },
        // 起售状态
        header: { "authentication": common_vendor.index.getStorageSync("token") },
        success: (res) => {
          console.log("Dish Response:", res.data);
          if (res.data.code === 0 || res.data.code === 1) {
            const rawList = res.data.data;
            this.dishList = rawList.map((item) => {
              if (item.image && !item.image.startsWith("http")) {
                item.image = item.image.startsWith("/") ? this.baseUrl + item.image : item.image;
                if (!item.image.startsWith("http") && !item.image.startsWith("/"))
                  ;
              }
              return item;
            });
          }
        }
      });
    },
    // 3. 点击分类
    onCategoryClick(index) {
      this.activeCategoryIndex = index;
      this.searchKeyword = "";
      const catId = this.categoryList[index].id;
      this.getDishList(catId);
    },
    // 【核心新增】全局搜索处理
    handleSearch() {
      const keyword = this.searchKeyword.trim();
      if (!keyword) {
        if (this.categoryList.length > 0) {
          this.activeCategoryIndex = this.activeCategoryIndex === -1 ? 0 : this.activeCategoryIndex;
          this.getDishList(this.categoryList[this.activeCategoryIndex].id);
        }
        return;
      }
      console.log("Searching for:", keyword);
      this.activeCategoryIndex = -1;
      common_vendor.index.request({
        url: this.baseUrl + "/user/dish/list",
        method: "GET",
        data: { name: keyword, status: 1 },
        header: { "authentication": common_vendor.index.getStorageSync("token") },
        success: (res) => {
          if (res.data.code === 0 || res.data.code === 1) {
            const rawList = res.data.data || [];
            this.dishList = rawList.map((item) => {
              if (item.image && !item.image.startsWith("http")) {
                item.image = item.image.startsWith("/") ? this.baseUrl + item.image : item.image;
              }
              return item;
            });
          } else {
            console.error("Search API Failed:", res.data.msg);
          }
        },
        fail: (err) => {
          console.error("Search Request Network Error:", err);
        }
      });
    },
    // 4. 购物车列表
    getCartList() {
      console.log("=== Category getCartList called ===");
      common_vendor.index.request({
        url: this.baseUrl + "/user/shoppingCart/list",
        method: "GET",
        header: { "authentication": common_vendor.index.getStorageSync("token") },
        success: (res) => {
          console.log("Cart API Response:", res.data);
          if (res.data.code === 0 || res.data.code === 1) {
            this.cartList = res.data.data || [];
            console.log("Cart items loaded:", this.cartList.length, "items, total:", this.totalPrice);
          } else {
            console.error("Cart API Failed:", res.data);
          }
        },
        fail: (err) => {
          console.error("Cart API Network Error:", err);
        }
      });
    },
    // 5. 操作购物车
    handleCart(item, type) {
      const apiUrl = type === 1 ? "/user/shoppingCart/add" : "/user/shoppingCart/sub";
      let payload = {};
      if (item.dishId) {
        payload = { dishId: item.dishId };
        if (item.setmealId)
          payload.setmealId = item.setmealId;
        if (item.dishFlavor)
          payload.dishFlavor = item.dishFlavor;
      } else {
        payload = { dishId: item.id };
        if (item.selectedFlavor)
          payload.dishFlavor = item.selectedFlavor;
      }
      common_vendor.index.request({
        url: this.baseUrl + apiUrl,
        method: "POST",
        data: payload,
        header: { "authentication": common_vendor.index.getStorageSync("token") },
        success: (res) => {
          if (res.data.code === 0 || res.data.code === 1) {
            this.getCartList();
          }
        }
      });
    },
    addToCart(item) {
      this.handleCart(item, 1);
    },
    clearCart() {
      console.log("Clear cart called");
      common_vendor.index.request({
        url: this.baseUrl + "/user/shoppingCart/clean",
        method: "DELETE",
        header: { "authentication": common_vendor.index.getStorageSync("token") },
        success: (res) => {
          console.log("Clear cart response:", res.data);
          if (res.data.code === 0 || res.data.code === 1) {
            this.cartList = [];
            this.cartPopupShow = false;
            this.getCartList();
          }
        },
        fail: (err) => {
          console.error("Clear cart failed:", err);
        }
      });
    },
    toggleCart() {
      if (this.cartList.length > 0) {
        this.cartPopupShow = !this.cartPopupShow;
      }
    },
    goSubmit() {
      if (this.cartList.length === 0)
        return;
      common_vendor.index.navigateTo({ url: "/pages/submit/submit" });
    },
    // Nutrition Popup
    openNutrition(item) {
      this.currentDish = item;
      this.showNutritionPopup = true;
    },
    closeNutrition() {
      this.showNutritionPopup = false;
    },
    addToCartFromPopup(dishWithFlavor) {
      const dish = dishWithFlavor || this.currentDish;
      this.addToCart(dish);
      this.closeNutrition();
    },
    // 获取某个菜品在购物车中的数量
    getCartQuantity(dishId) {
      const cartItem = this.cartList.find((item) => item.dishId === dishId);
      return cartItem ? cartItem.number : 0;
    },
    // 一键修复数据逻辑
    fixData() {
      common_vendor.index.showLoading({ title: "正在修复..." });
      common_vendor.index.request({
        url: this.baseUrl + "/user/db/fix",
        method: "POST",
        header: { "authentication": common_vendor.index.getStorageSync("token") },
        success: (res) => {
          common_vendor.index.hideLoading();
          if (res.data.code === 0 || res.data.code === 1) {
            common_vendor.index.showToast({ title: "修复成功！刷新中...", icon: "success" });
            setTimeout(() => {
              this.getCategoryList();
            }, 1500);
          } else {
            common_vendor.index.showModal({ title: "修复失败", content: res.data.msg || "未知错误" });
          }
        },
        fail: () => {
          common_vendor.index.hideLoading();
          common_vendor.index.showToast({ title: "请求失败", icon: "none" });
        }
      });
    }
  }
};
if (!Array) {
  const _component_DishDetailPopup = common_vendor.resolveComponent("DishDetailPopup");
  _component_DishDetailPopup();
}
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.handleSearch && $options.handleSearch(...args)),
    b: $data.searchKeyword,
    c: common_vendor.o(($event) => $data.searchKeyword = $event.detail.value),
    d: common_vendor.f($data.categoryList, (item, index, i0) => {
      return common_vendor.e({
        a: $data.activeCategoryIndex === index
      }, $data.activeCategoryIndex === index ? {} : {}, {
        b: common_vendor.t(item.name),
        c: $data.activeCategoryIndex === index ? 1 : "",
        d: item.id,
        e: common_vendor.o(($event) => $options.onCategoryClick(index), item.id)
      });
    }),
    e: $options.currentCategory && $data.activeCategoryIndex !== -1
  }, $options.currentCategory && $data.activeCategoryIndex !== -1 ? {
    f: common_vendor.t($options.currentCategory.name)
  } : $data.activeCategoryIndex === -1 && $data.searchKeyword ? {
    h: common_vendor.t($data.searchKeyword)
  } : {}, {
    g: $data.activeCategoryIndex === -1 && $data.searchKeyword,
    i: common_vendor.f($data.dishList, (item, index, i0) => {
      return common_vendor.e({
        a: item.image,
        b: common_vendor.t(item.name),
        c: common_vendor.t(item.description || "暂无描述"),
        d: item.calories
      }, item.calories ? {
        e: common_vendor.t(item.calories)
      } : {}, {
        f: index % 2 === 0
      }, index % 2 === 0 ? {} : {}, {
        g: common_vendor.t(item.price),
        h: $options.getCartQuantity(item.id) > 0
      }, $options.getCartQuantity(item.id) > 0 ? {
        i: common_vendor.o(($event) => $options.handleCart(item, -1), item.id),
        j: common_vendor.t($options.getCartQuantity(item.id)),
        k: common_vendor.o(($event) => $options.handleCart(item, 1), item.id)
      } : {
        l: common_vendor.o(($event) => $options.addToCart(item), item.id)
      }, {
        m: item.id,
        n: common_vendor.o(($event) => $options.openNutrition(item), item.id)
      });
    }),
    j: $data.dishList.length === 0
  }, $data.dishList.length === 0 ? {
    k: common_vendor.o((...args) => $options.fixData && $options.fixData(...args))
  } : {}, {
    l: $options.totalNum > 0
  }, $options.totalNum > 0 ? {
    m: common_vendor.t($options.totalNum)
  } : {}, {
    n: common_vendor.t($options.totalAmount),
    o: common_vendor.o((...args) => $options.goSubmit && $options.goSubmit(...args)),
    p: common_vendor.o((...args) => $options.toggleCart && $options.toggleCart(...args)),
    q: $data.cartPopupShow
  }, $data.cartPopupShow ? {
    r: common_vendor.o(($event) => $data.cartPopupShow = false)
  } : {}, {
    s: common_vendor.o((...args) => $options.clearCart && $options.clearCart(...args)),
    t: common_vendor.f($data.cartList, (item, index, i0) => {
      return common_vendor.e({
        a: common_vendor.t(item.name),
        b: item.dishFlavor
      }, item.dishFlavor ? {
        c: common_vendor.t(item.dishFlavor)
      } : {}, {
        d: common_vendor.t(item.amount),
        e: common_vendor.o(($event) => $options.handleCart(item, -1), index),
        f: common_vendor.t(item.number),
        g: common_vendor.o(($event) => $options.handleCart(item, 1), index),
        h: index
      });
    }),
    v: $data.cartPopupShow ? 1 : "",
    w: common_vendor.o($options.closeNutrition),
    x: common_vendor.o($options.addToCartFromPopup),
    y: common_vendor.p({
      visible: $data.showNutritionPopup,
      dish: $data.currentDish
    })
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-4046d630"], ["__file", "C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/smart-canteen-app/src/pages/category/category.vue"]]);
wx.createPage(MiniProgramPage);
