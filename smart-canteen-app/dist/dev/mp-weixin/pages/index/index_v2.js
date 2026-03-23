"use strict";
const common_vendor = require("../../common/vendor.js");
const stores_modules_user = require("../../stores/modules/user.js");
const stores_modules_userProfile = require("../../stores/modules/userProfile.js");
const common_assets = require("../../common/assets.js");
require("../../utils/http.js");
require("../../api/user.js");
if (!Math) {
  DishDetailPopup();
}
const DishDetailPopup = () => "../../components/DishDetailPopup.js";
const defaultDishImage = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c";
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "index_v2",
  setup(__props) {
    stores_modules_user.useUserStore();
    const profileStore = stores_modules_userProfile.useUserProfileStore();
    const openCartList = common_vendor.ref(false);
    const temperature = common_vendor.ref("--");
    const weatherText = common_vendor.ref("--");
    const showDishDetail = common_vendor.ref(false);
    const currentDetailDish = common_vendor.ref({});
    const openDishDetail = (dish) => {
      currentDetailDish.value = {
        ...dish,
        image: dish.image || dish.pic
        // ensure image is available
      };
      showDishDetail.value = true;
    };
    const recommendList = common_vendor.ref([]);
    const recommendMode = common_vendor.ref("NORMAL");
    const isDietMode = common_vendor.computed(() => recommendMode.value === "DIET");
    const recommendTitle = common_vendor.computed(() => isDietMode.value ? "轻负控卡推荐" : "智选6道菜");
    const recommendSubtitle = common_vendor.computed(
      () => isDietMode.value ? "今日热量已达标，为您优先推荐低负担菜品" : "根据您的健康画像定制"
    );
    const todayCalories = common_vendor.ref(0);
    const todayProtein = common_vendor.ref(0);
    (/* @__PURE__ */ new Date()).getHours();
    const mealTimeSlot = common_vendor.computed(() => {
      const h = (/* @__PURE__ */ new Date()).getHours();
      if (h >= 6 && h < 10)
        return "早餐时段";
      if (h >= 10 && h < 16)
        return "午餐时段";
      if (h >= 16 && h < 21)
        return "晚餐时段";
      return "夜宵时段";
    });
    const greetings = [
      "周日慢生活，吃点清淡的给肠胃放个假！",
      "周一能量满满，来点高蛋白美食吧！",
      "周二工作辛苦，吃顿好的犒劳自己！",
      "周三过半，补充点维生素充充电！",
      "周四坚持住，美味轻食伴你同行！",
      "周五狂欢前，保持一份清爽健康！",
      "周末好时光，享受均衡营养的一餐！"
    ];
    const dailyGreeting = common_vendor.computed(() => {
      const day = (/* @__PURE__ */ new Date()).getDay();
      return greetings[day];
    });
    const timeGreeting = common_vendor.computed(() => {
      const h = (/* @__PURE__ */ new Date()).getHours();
      if (h >= 6 && h < 12)
        return "早安";
      if (h >= 12 && h < 18)
        return "午安";
      return "晚安";
    });
    const dishList = common_vendor.ref([]);
    const cartList = common_vendor.ref([]);
    const baseUrl = common_vendor.ref("http://121.41.59.61:8081");
    const resolveDishImage = (image) => {
      if (!image)
        return defaultDishImage;
      if (image.startsWith("http"))
        return image;
      if (image.startsWith("/static/dish/"))
        return baseUrl.value + image;
      return baseUrl.value + "/static/dish/" + image.replace(/^\/+/, "");
    };
    const toNumber = (value) => {
      const num = Number(value);
      return Number.isFinite(num) ? num : null;
    };
    const resolveRecommendMode = (payload) => {
      if (!payload || Array.isArray(payload)) {
        return "NORMAL";
      }
      const mode = String(payload.recommendMode || "").toUpperCase();
      if (mode === "DIET" || mode === "COLD_START" || mode === "NORMAL") {
        return mode;
      }
      if (payload.isDietMode === true) {
        return "DIET";
      }
      return "NORMAL";
    };
    const fetchTodayNutrition = () => {
      return new Promise((resolve) => {
        common_vendor.index.request({
          url: baseUrl.value + "/analysis/health/summary",
          method: "GET",
          header: { "authentication": common_vendor.index.getStorageSync("token") },
          success: (res) => {
            var _a;
            const data = (_a = res.data) == null ? void 0 : _a.data;
            if (data) {
              todayCalories.value = data.todayIntakeKcal || 0;
              if (data.macros) {
                todayProtein.value = data.macros.proteinG || 0;
              }
            }
            resolve();
          },
          fail: () => resolve()
        });
      });
    };
    const getRecommendData = () => {
      console.log("===== 智选6道菜引擎调用 =====");
      const p = profileStore.profile;
      const tdee = profileStore.calculatedTDEE;
      const hasProfile = !!(p.gender && p.age && p.height && p.weight && tdee);
      const dto = {
        hasProfile,
        tdee: tdee || 2200,
        todayCalories: todayCalories.value,
        todayProtein: todayProtein.value,
        healthGoal: p.healthGoal || 3,
        avoidTags: Array.isArray(p.avoidTags) ? p.avoidTags.join(",") : p.avoidTags || "",
        tasteTags: Array.isArray(p.tasteTags) ? p.tasteTags.join(",") : p.tasteTags || ""
      };
      console.log("智选6道菜 DTO:", dto);
      common_vendor.index.request({
        url: baseUrl.value + "/user/dish/smartPick6",
        method: "POST",
        data: dto,
        header: {
          "authentication": common_vendor.index.getStorageSync("token"),
          "Content-Type": "application/json"
        },
        success: (res) => {
          console.log("智选6道菜响应:", res.data);
          if (res.data.code === 0 || res.data.code === 1) {
            const payload = res.data.data;
            const mode = resolveRecommendMode(payload);
            const dishes = Array.isArray(payload) ? payload : (payload == null ? void 0 : payload.dishes) || [];
            recommendMode.value = mode;
            recommendList.value = dishes.map((dish) => ({
              ...dish,
              tags: buildSmartTags(dish, dto, mode),
              dietHint: buildDietHint(dish, mode),
              image: dish.image || "https://images.unsplash.com/photo-1546069901-ba9599a7e63c"
            }));
            console.log("智选6道菜渲染:", recommendList.value.length, "道", "mode=", mode);
          } else {
            recommendMode.value = "NORMAL";
            fallbackRecommend();
          }
        },
        fail: (err) => {
          console.error("智选6道菜请求失败，降级到普通列表:", err);
          recommendMode.value = "NORMAL";
          fallbackRecommend();
        }
      });
    };
    const buildSmartTags = (dish, dto, mode = "NORMAL") => {
      const tags = [];
      const calories = toNumber(dish.calories);
      const protein = toNumber(dish.protein);
      const fat = toNumber(dish.fat);
      const fiber = toNumber(dish.fiber);
      if (mode === "DIET") {
        if (fiber !== null && fiber >= 5)
          tags.push("高纤维");
        if (calories !== null && calories <= 280)
          tags.push("超低卡");
        if (fat !== null && fat <= 10)
          tags.push("低脂");
        if (tags.length === 0)
          tags.push("轻负担");
        return tags;
      }
      if (calories !== null && calories < 400)
        tags.push("低卡");
      if (protein !== null && protein > 20)
        tags.push("高蛋白");
      if (dto.healthGoal === 1 && fat !== null && fat < 10)
        tags.push("减脂友好");
      if (dto.healthGoal === 2 && protein !== null && protein > 25)
        tags.push("增肌之选");
      if (tags.length === 0)
        tags.push("推荐");
      return tags;
    };
    const buildDietHint = (dish, mode) => {
      if (mode !== "DIET")
        return "匹配度 98%";
      const calories = toNumber(dish.calories);
      const fat = toNumber(dish.fat);
      const fiber = toNumber(dish.fiber);
      if (calories !== null && calories <= 280)
        return "超低卡优先";
      if (fat !== null && fat <= 10)
        return "低脂优先";
      if (fiber !== null && fiber >= 5)
        return "高纤维优先";
      return "轻负担推荐";
    };
    const fallbackRecommend = () => {
      recommendMode.value = "NORMAL";
      common_vendor.index.request({
        url: baseUrl.value + "/user/dish/list",
        method: "GET",
        data: { status: 1 },
        header: { "authentication": common_vendor.index.getStorageSync("token") },
        success: (res) => {
          if (res.data.code === 0 || res.data.code === 1) {
            const dishes = res.data.data || [];
            recommendList.value = dishes.slice(0, 6).map((dish) => ({
              ...dish,
              tags: buildSmartTags(dish, { healthGoal: 3 }, "NORMAL"),
              dietHint: "匹配度 98%",
              image: dish.image || "https://images.unsplash.com/photo-1546069901-ba9599a7e63c"
            }));
          }
        }
      });
    };
    const getDishData = () => {
      console.log("Fetching true bestseller dishes...");
      common_vendor.index.request({
        url: baseUrl.value + "/user/dish/hotSales",
        method: "GET",
        header: { "authentication": common_vendor.index.getStorageSync("token") },
        success: (res) => {
          console.log("True Bestseller dishes response:", res.data);
          if (res.data.code === 0 || res.data.code === 1) {
            const dishes = res.data.data || [];
            dishList.value = dishes.map((dish) => ({
              ...dish,
              pic: dish.image,
              detail: dish.description || "暂无描述"
            }));
          }
        }
      });
    };
    const getCartList = () => {
      console.log("=== Index_v2 getCartList called ===");
      common_vendor.index.request({
        url: baseUrl.value + "/user/shoppingCart/list",
        method: "GET",
        header: { "authentication": common_vendor.index.getStorageSync("token") },
        success: (res) => {
          console.log("Index_v2 Cart API Response:", res.data);
          if (res.data.code === 0 || res.data.code === 1) {
            cartList.value = res.data.data || [];
            console.log("Index_v2 Cart loaded:", cartList.value.length, "items");
          }
        }
      });
    };
    const addToCart = (item) => {
      console.log("=== addToCart called ===", item);
      common_vendor.index.request({
        url: baseUrl.value + "/user/shoppingCart/add",
        method: "POST",
        data: {
          dishId: item.id,
          dishFlavor: item.selectedFlavor
        },
        header: {
          "authentication": common_vendor.index.getStorageSync("token"),
          "Content-Type": "application/json"
        },
        success: (res) => {
          console.log("Add to cart response:", res.data);
          if (res.data.code === 0 || res.data.code === 1) {
            getCartList();
            common_vendor.index.showToast({ title: "已加入购物车", icon: "success", duration: 1e3 });
          } else {
            console.error("Add to cart failed:", res.data);
            common_vendor.index.showToast({ title: res.data.msg || "添加失败", icon: "none" });
          }
        },
        fail: (err) => {
          console.error("Add to cart network error:", err);
          common_vendor.index.showToast({ title: "网络错误", icon: "none" });
        }
      });
    };
    const addCart = (item) => {
      common_vendor.index.request({
        url: baseUrl.value + "/user/shoppingCart/add",
        method: "POST",
        data: { dishId: item.dishId },
        header: { "authentication": common_vendor.index.getStorageSync("token") },
        success: () => getCartList()
      });
    };
    const subCart = (item) => {
      common_vendor.index.request({
        url: baseUrl.value + "/user/shoppingCart/sub",
        method: "POST",
        data: { dishId: item.dishId },
        header: { "authentication": common_vendor.index.getStorageSync("token") },
        success: () => getCartList()
      });
    };
    const clearCart = () => {
      common_vendor.index.request({
        url: baseUrl.value + "/user/shoppingCart/clean",
        method: "DELETE",
        header: { "authentication": common_vendor.index.getStorageSync("token") },
        success: (res) => {
          if (res.data.code === 0 || res.data.code === 1) {
            cartList.value = [];
            openCartList.value = false;
          }
        }
      });
    };
    const toggleCart = () => {
      if (cartList.value.length > 0)
        openCartList.value = !openCartList.value;
    };
    const submitOrder = () => {
      common_vendor.index.navigateTo({ url: "/pages/submit/submit" });
    };
    const cartTotalCount = common_vendor.computed(() => {
      return cartList.value.reduce((sum, item) => sum + (item.number || 0), 0);
    });
    const cartTotalPrice = common_vendor.computed(() => {
      return cartList.value.reduce((sum, item) => sum + (item.amount || item.price) * (item.number || 0), 0).toFixed(2);
    });
    const getRealTimeWeather = () => {
      common_vendor.index.request({
        url: "https://pw5u9wqmtr.re.qweatherapi.com/v7/weather/now?location=101180101&key=5af48e8eef184bd2b780c7e570b06426",
        method: "GET",
        success: (res) => {
          if (res.data && res.data.code === "200" && res.data.now) {
            temperature.value = res.data.now.temp;
            weatherText.value = res.data.now.text;
          }
        },
        fail: (err) => {
          console.error("天气API请求失败", err);
        }
      });
    };
    common_vendor.onMounted(() => {
      getRealTimeWeather();
    });
    common_vendor.onLoad(async () => {
      await profileStore.fetchProfile();
      await fetchTodayNutrition();
      getRecommendData();
      getDishData();
      getCartList();
    });
    common_vendor.onShow(async () => {
      console.log("=== Index_v2 PAGE onShow ===");
      await profileStore.fetchProfile();
      await fetchTodayNutrition();
      getRecommendData();
      getDishData();
      getCartList();
    });
    return (_ctx, _cache) => {
      var _a, _b;
      return common_vendor.e({
        a: common_vendor.unref(profileStore).displayAvatar,
        b: common_vendor.t(timeGreeting.value),
        c: common_vendor.t(common_vendor.unref(profileStore).displayName),
        d: common_vendor.t(temperature.value),
        e: common_vendor.t(weatherText.value),
        f: common_vendor.t(mealTimeSlot.value),
        g: common_vendor.t(dailyGreeting.value),
        h: common_vendor.t(((_a = common_vendor.unref(profileStore).calculatedBMI) == null ? void 0 : _a.toFixed(1)) || "--"),
        i: common_vendor.t(common_vendor.unref(profileStore).bmiCategory || "未知"),
        j: common_vendor.t(((_b = common_vendor.unref(profileStore).suggestIntake) == null ? void 0 : _b.toFixed(0)) || "--"),
        k: common_vendor.t(recommendTitle.value),
        l: common_vendor.t(recommendSubtitle.value),
        m: common_vendor.f(recommendList.value, (item, index, i0) => {
          return {
            a: resolveDishImage(item.image),
            b: common_vendor.t(item.name),
            c: common_vendor.f(item.tags, (tag, k1, i1) => {
              return {
                a: common_vendor.t(tag),
                b: tag
              };
            }),
            d: common_vendor.t(item.calories),
            e: common_vendor.t(isDietMode.value ? item.dietHint || "轻负担推荐" : "匹配度 98%"),
            f: common_vendor.t(item.price),
            g: common_vendor.o(($event) => openDishDetail(item), index),
            h: index,
            i: common_vendor.o(($event) => openDishDetail(item), index)
          };
        }),
        n: common_vendor.n({
          "diet-tag": isDietMode.value
        }),
        o: common_vendor.unref(common_assets.iconHot),
        p: common_vendor.f(dishList.value, (dish, index, i0) => {
          return common_vendor.e({
            a: index === 0
          }, index === 0 ? {} : index === 1 ? {} : index === 2 ? {} : {
            d: common_vendor.t(index + 1)
          }, {
            b: index === 1,
            c: index === 2,
            e: resolveDishImage(dish.image),
            f: common_vendor.t(dish.name),
            g: common_vendor.t(dish.detail || "暂无描述"),
            h: common_vendor.t(dish.sold || 0),
            i: common_vendor.t(dish.price),
            j: common_vendor.o(($event) => openDishDetail(dish), dish.id),
            k: dish.id,
            l: common_vendor.o(($event) => openDishDetail(dish), dish.id)
          });
        }),
        q: cartTotalCount.value > 0
      }, cartTotalCount.value > 0 ? {
        r: common_vendor.t(cartTotalCount.value)
      } : {}, {
        s: common_vendor.t(cartTotalPrice.value),
        t: common_vendor.o(submitOrder),
        v: common_vendor.o(toggleCart),
        w: openCartList.value
      }, openCartList.value ? {
        x: common_vendor.o(clearCart),
        y: common_vendor.f(cartList.value, (item, idx, i0) => {
          return {
            a: common_vendor.t(item.name),
            b: common_vendor.o(($event) => subCart(item), idx),
            c: common_vendor.t(item.number),
            d: common_vendor.o(($event) => addCart(item), idx),
            e: idx
          };
        }),
        z: common_vendor.o(() => {
        }),
        A: common_vendor.o(($event) => openCartList.value = false)
      } : {}, {
        B: common_vendor.o(($event) => showDishDetail.value = false),
        C: common_vendor.o(addToCart),
        D: common_vendor.p({
          visible: showDishDetail.value,
          dish: currentDetailDish.value
        })
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-464d1350"], ["__file", "C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/smart-canteen-app/src/pages/index/index_v2.vue"]]);
wx.createPage(MiniProgramPage);
