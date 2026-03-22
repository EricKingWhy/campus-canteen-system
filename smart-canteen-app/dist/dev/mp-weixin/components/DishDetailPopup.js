"use strict";
const common_vendor = require("../common/vendor.js");
const api_favorite = require("../api/favorite.js");
const common_assets = require("../common/assets.js");
require("../utils/http.js");
require("../stores/modules/user.js");
const baseUrl = "http://127.0.0.1:8081";
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "DishDetailPopup",
  props: {
    visible: { type: Boolean },
    dish: {}
  },
  emits: ["close", "addToCart"],
  setup(__props, { emit: __emit }) {
    const props = __props;
    const emit = __emit;
    const resolveImageUrl = (image) => {
      if (!image)
        return "/static/default_dish.png";
      if (image.startsWith("http://") || image.startsWith("https://"))
        return image;
      if (image.startsWith("/static/dish/"))
        return baseUrl + image;
      return image;
    };
    const selectedFlavor = common_vendor.ref("");
    const detailDish = common_vendor.ref(null);
    const displayDish = common_vendor.computed(() => detailDish.value || props.dish || {});
    const isFavorite = common_vendor.ref(false);
    const favoriteLoading = common_vendor.ref(false);
    const checkFavorite = async () => {
      var _a;
      if (!((_a = props.dish) == null ? void 0 : _a.id))
        return;
      try {
        const res = await api_favorite.favoriteCheckAPI(props.dish.id);
        isFavorite.value = res.data === true;
      } catch (e) {
        console.error("检查收藏状态失败", e);
      }
    };
    const toggleFavorite = async () => {
      var _a;
      if (!((_a = props.dish) == null ? void 0 : _a.id) || favoriteLoading.value)
        return;
      favoriteLoading.value = true;
      try {
        if (isFavorite.value) {
          await api_favorite.favoriteRemoveAPI(props.dish.id);
          isFavorite.value = false;
          common_vendor.index.showToast({ title: "已取消收藏", icon: "none" });
        } else {
          await api_favorite.favoriteAddAPI(props.dish.id);
          isFavorite.value = true;
          common_vendor.index.showToast({ title: "已收藏", icon: "success" });
        }
      } catch (e) {
        common_vendor.index.showToast({ title: "操作失败", icon: "none" });
      } finally {
        favoriteLoading.value = false;
      }
    };
    common_vendor.watch(() => [props.visible, props.dish], ([newVisible, newDish]) => {
      if (newVisible && (newDish == null ? void 0 : newDish.id)) {
        fetchDishDetail(newDish.id);
        checkFavorite();
      }
    }, { deep: true });
    const fetchDishDetail = (dishId) => {
      common_vendor.index.request({
        url: `${baseUrl}/user/dish/dish/${dishId}`,
        method: "GET",
        header: { "authentication": common_vendor.index.getStorageSync("token") },
        success: (res) => {
          if ((res.data.code === 0 || res.data.code === 1) && res.data.data) {
            detailDish.value = res.data.data;
          } else {
            detailDish.value = null;
          }
        },
        fail: () => {
          detailDish.value = null;
        }
      });
    };
    const containsAny = (source, tokens) => {
      return tokens.some((token) => source.includes(token));
    };
    const getSafeFallbackFlavors = (dish) => {
      const name = String((dish == null ? void 0 : dish.name) || "");
      const categoryName = String((dish == null ? void 0 : dish.categoryName) || "");
      const text = `${name}${categoryName}`;
      if (name.includes("茶叶蛋")) {
        return [];
      }
      const dessertTokens = ["大福", "麻薯", "布丁", "糍粑", "汤圆", "蛋糕", "甜点", "甜品", "芋泥"];
      const drinkTokens = ["饮品", "奶茶", "咖啡", "拿铁", "可乐", "豆浆", "果汁", "茶", "美式"];
      const spicyMainTokens = ["面", "粉", "米线", "盖饭", "拌饭", "炒饭", "牛肉", "鸡肉", "猪肉", "鱼片", "麻辣", "香辣"];
      const isDessert = containsAny(text, dessertTokens);
      const isDrink = containsAny(text, drinkTokens);
      const isSavoryMain = containsAny(text, spicyMainTokens) && !isDessert && !isDrink;
      const isCongee = name.includes("粥") && !isDessert;
      if (isDessert) {
        return [];
      }
      if (isDrink) {
        return ["常规冰", "少冰", "去冰", "常温", "热饮"];
      }
      if (isSavoryMain) {
        return ["微辣", "中辣", "特辣", "免辣"];
      }
      if (isCongee) {
        return ["不加葱", "加葱"];
      }
      return [];
    };
    const smartFlavors = common_vendor.computed(() => {
      var _a;
      const flavorRows = (_a = displayDish.value) == null ? void 0 : _a.flavors;
      if (!Array.isArray(flavorRows) || flavorRows.length === 0) {
        return getSafeFallbackFlavors(displayDish.value);
      }
      const options = [];
      flavorRows.forEach((row) => {
        try {
          const rawList = (row == null ? void 0 : row.list) ?? (row == null ? void 0 : row.value) ?? "[]";
          const list = JSON.parse(rawList);
          if (Array.isArray(list)) {
            list.forEach((item) => {
              if (typeof item === "string" && item.trim() && !options.includes(item)) {
                options.push(item.trim());
              }
            });
          }
        } catch (e) {
        }
      });
      if (options.length > 0) {
        return options;
      }
      return getSafeFallbackFlavors(displayDish.value);
    });
    common_vendor.watch(smartFlavors, (newVal) => {
      if (newVal && newVal.length > 0) {
        selectedFlavor.value = newVal[0];
      } else {
        selectedFlavor.value = "";
      }
    });
    const close = () => {
      detailDish.value = null;
      emit("close");
    };
    const handleAddToCart = () => {
      const dishToAdd = {
        ...props.dish,
        selectedFlavor: selectedFlavor.value
      };
      emit("addToCart", dishToAdd);
      close();
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: _ctx.visible
      }, _ctx.visible ? common_vendor.e({
        b: common_vendor.o(close),
        c: common_vendor.t(isFavorite.value ? "♥" : "♡"),
        d: isFavorite.value ? 1 : "",
        e: common_vendor.o(toggleFavorite),
        f: favoriteLoading.value ? 1 : "",
        g: resolveImageUrl(displayDish.value.image || displayDish.value.pic),
        h: common_vendor.t(displayDish.value.name),
        i: common_vendor.t(displayDish.value.price),
        j: common_vendor.t(displayDish.value.calories || 350),
        k: common_vendor.t(displayDish.value.protein || 25),
        l: common_vendor.t(displayDish.value.carbohydrates || 40),
        m: common_vendor.t(displayDish.value.fat || 5),
        n: common_vendor.t(displayDish.value.description || "精选优质食材，由专业营养师搭配，采用健康烹饪方式，锁住食材本味。口感鲜美，营养均衡。"),
        o: displayDish.value.mainIngredients
      }, displayDish.value.mainIngredients ? {
        p: common_vendor.unref(common_assets.ingredientsWheatIcon),
        q: common_vendor.t(displayDish.value.mainIngredients)
      } : {}, {
        r: displayDish.value.allergenTags
      }, displayDish.value.allergenTags ? common_vendor.e({
        s: displayDish.value.allergenTags !== "无"
      }, displayDish.value.allergenTags !== "无" ? {
        t: common_vendor.unref(common_assets.calorieCheckNewIcon),
        v: common_vendor.t(displayDish.value.allergenTags)
      } : {
        w: common_vendor.unref(common_assets.calorieCheckNewIcon)
      }) : {}, {
        x: smartFlavors.value.length > 0
      }, smartFlavors.value.length > 0 ? {
        y: common_vendor.f(smartFlavors.value, (flavor, index, i0) => {
          return {
            a: common_vendor.t(flavor),
            b: index,
            c: selectedFlavor.value === flavor ? 1 : "",
            d: common_vendor.o(($event) => selectedFlavor.value = flavor, index)
          };
        })
      } : {}, {
        z: common_vendor.o(handleAddToCart),
        A: common_vendor.o(() => {
        }),
        B: common_vendor.o(close)
      }) : {});
    };
  }
});
const Component = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-121ded9a"], ["__file", "C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/smart-canteen-app/src/components/DishDetailPopup.vue"]]);
wx.createComponent(Component);
