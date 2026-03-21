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
        checkFavorite();
      }
    }, { deep: true });
    const smartFlavors = common_vendor.computed(() => {
      if (!props.dish || !props.dish.name)
        return [];
      const name = props.dish.name;
      if (name.includes("面") || name.includes("粉") || name.includes("辣") || name.includes("麻婆") || name.includes("鸡") || name.includes("肉")) {
        if (!name.includes("蛋糕") && !name.includes("甜") && !name.includes("奶")) {
          return ["微辣", "中辣", "特辣", "免辣"];
        }
      }
      if (name.includes("饮") || name.includes("茶") || name.includes("奶") || name.includes("拿铁") || name.includes("美式") || name.includes("可乐")) {
        return ["常规冰", "少冰", "去冰", "常温", "热饮"];
      }
      if (name.includes("粥")) {
        return ["不加葱", "加葱"];
      }
      return [];
    });
    common_vendor.watch(smartFlavors, (newVal) => {
      if (newVal && newVal.length > 0) {
        selectedFlavor.value = newVal[0];
      } else {
        selectedFlavor.value = "";
      }
    });
    const close = () => {
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
        g: resolveImageUrl(_ctx.dish.image || _ctx.dish.pic),
        h: common_vendor.t(_ctx.dish.name),
        i: common_vendor.t(_ctx.dish.price),
        j: common_vendor.t(_ctx.dish.calories || 350),
        k: common_vendor.t(_ctx.dish.protein || 25),
        l: common_vendor.t(_ctx.dish.carbohydrates || 40),
        m: common_vendor.t(_ctx.dish.fat || 5),
        n: common_vendor.t(_ctx.dish.description || "精选优质食材，由专业营养师搭配，采用健康烹饪方式，锁住食材本味。口感鲜美，营养均衡。"),
        o: _ctx.dish.mainIngredients
      }, _ctx.dish.mainIngredients ? {
        p: common_vendor.unref(common_assets.ingredientsWheatIcon),
        q: common_vendor.t(_ctx.dish.mainIngredients)
      } : {}, {
        r: _ctx.dish.allergenTags
      }, _ctx.dish.allergenTags ? common_vendor.e({
        s: _ctx.dish.allergenTags !== "无"
      }, _ctx.dish.allergenTags !== "无" ? {
        t: common_vendor.unref(common_assets.calorieCheckNewIcon),
        v: common_vendor.t(_ctx.dish.allergenTags)
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
