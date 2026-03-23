"use strict";
const common_vendor = require("../../common/vendor.js");
const stores_modules_user = require("../../stores/modules/user.js");
const stores_modules_userProfile = require("../../stores/modules/userProfile.js");
const api_user = require("../../api/user.js");
require("../../utils/http.js");
const common_assets = require("../../common/assets.js");
if (!Math) {
  pushMsg();
}
const pushMsg = () => "../../components/message/pushMsg.js";
const baseUrl = "http://121.41.59.61:8081";
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "my",
  setup(__props) {
    var _a;
    const userStore = stores_modules_user.useUserStore();
    const profileStore = stores_modules_userProfile.useUserProfileStore();
    const childComp = common_vendor.ref(null);
    const user = common_vendor.reactive({
      id: ((_a = userStore.profile) == null ? void 0 : _a.id) || 0,
      name: "",
      gender: 1,
      phone: "未设置",
      pic: ""
    });
    const monthlySpend = common_vendor.ref("0.00");
    const monthlyOrders = common_vendor.ref(0);
    const todayCalories = common_vendor.ref(0);
    const todayProtein = common_vendor.ref(0);
    const targetCalories = common_vendor.computed(() => profileStore.suggestIntake || 2e3);
    const caloriesStatus = common_vendor.computed(() => {
      if (todayCalories.value > targetCalories.value * 1.1)
        return { text: "超标 🔺", class: "tag-over" };
      if (todayCalories.value < targetCalories.value * 0.8)
        return { text: "偏低 ⚠️", class: "tag-low" };
      return { text: "达标 ✅", class: "tag-ok" };
    });
    const proteinStatus = common_vendor.computed(() => {
      if (todayProtein.value < 40)
        return { text: "偏低 ⚠️", class: "tag-low" };
      return { text: "达标 ✅", class: "tag-ok" };
    });
    const fetchCostSummary = async () => {
      const token = common_vendor.index.getStorageSync("token");
      common_vendor.index.request({
        url: baseUrl + "/analysis/cost/summary",
        method: "GET",
        header: { authentication: token },
        success: (res) => {
          console.log("Cost API Response:", res.data);
          if (res.data && res.data.code === 0) {
            const data = res.data.data || {};
            const spend = data.monthSpent || data.totalAmount || data.amount || data.totalCost || data.cost || 0;
            monthlySpend.value = spend.toFixed(2);
            monthlyOrders.value = data.totalOrders || data.ordersCount || data.orderCount || data.count || 0;
          }
        },
        fail: (err) => {
          console.error("获取消费摘要失败:", err);
        }
      });
    };
    const fetchHealthSummary = async () => {
      const token = common_vendor.index.getStorageSync("token");
      common_vendor.index.request({
        url: baseUrl + "/analysis/health/summary",
        method: "GET",
        header: { authentication: token },
        success: (res) => {
          console.log("Health API Response:", res.data);
          if (res.data && res.data.code === 0) {
            const data = res.data.data || {};
            todayCalories.value = data.todayIntakeKcal || data.todayCalories || data.calories || data.totalCalories || data.intake || 0;
            const macros = data.macros || {};
            todayProtein.value = macros.proteinG || macros.todayProtein || macros.protein || macros.totalProtein || 0;
          }
        },
        fail: (err) => {
          console.error("获取健康摘要失败:", err);
        }
      });
    };
    common_vendor.onShow(async () => {
      await profileStore.fetchProfile();
      fetchCostSummary();
      fetchHealthSummary();
    });
    common_vendor.onLoad(async (options) => {
      if (user.id) {
        await getUserInfo(user.id);
      }
    });
    const getUserInfo = async (id) => {
      try {
        const res = await api_user.getUserInfoAPI(id);
        user.name = res.data.name;
        user.gender = res.data.gender ?? 1;
        user.phone = res.data.phone;
        user.pic = res.data.pic;
      } catch (e) {
        console.error(e);
      }
    };
    const goWeeklyReport = () => {
      common_vendor.index.navigateTo({ url: "/pages/user/weekly-report" });
    };
    const goHistory = () => {
      common_vendor.index.switchTab({ url: "/pages/history/history" }).catch(() => {
        common_vendor.index.navigateTo({ url: "/pages/history/history" });
      });
    };
    const goMyself = () => {
      common_vendor.index.navigateTo({ url: "/pages/info-setting/info-setting" });
    };
    const goFavorites = () => {
      common_vendor.index.navigateTo({ url: "/pages/favorite/favorite" });
    };
    const handleLogout = () => {
      common_vendor.index.showModal({
        title: "提示",
        content: "确定要退出当前账号吗？",
        confirmColor: "#ff4d4f",
        success: function(res) {
          if (res.confirm) {
            common_vendor.index.removeStorageSync("token");
            common_vendor.index.removeStorageSync("userInfo");
            userStore.clearProfile();
            common_vendor.index.reLaunch({ url: "/pages/login/login" });
          }
        }
      });
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.unref(profileStore).displayAvatar,
        b: common_vendor.t(common_vendor.unref(profileStore).displayName),
        c: common_vendor.unref(profileStore).profile.gender === 2
      }, common_vendor.unref(profileStore).profile.gender === 2 ? {} : {}, {
        d: common_vendor.t(common_vendor.unref(profileStore).bodyStats),
        e: common_vendor.t(common_vendor.unref(profileStore).calculatedBMI || "--"),
        f: common_vendor.t(common_vendor.unref(profileStore).bmiCategory),
        g: common_vendor.unref(common_assets.spendingIcon),
        h: common_vendor.t(monthlySpend.value),
        i: common_vendor.t(monthlyOrders.value),
        j: common_vendor.unref(common_assets.dietIcon),
        k: common_vendor.t(todayCalories.value),
        l: common_vendor.t(caloriesStatus.value.text),
        m: common_vendor.n(caloriesStatus.value.class),
        n: common_vendor.t(todayProtein.value),
        o: common_vendor.t(proteinStatus.value.text),
        p: common_vendor.n(proteinStatus.value.class),
        q: common_vendor.o(goHistory),
        r: common_vendor.o(goWeeklyReport),
        s: common_vendor.o(goFavorites),
        t: common_vendor.o(goMyself),
        v: common_vendor.o(handleLogout),
        w: common_vendor.sr(childComp, "d3687551-0", {
          "k": "childComp"
        })
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-d3687551"], ["__file", "C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/smart-canteen-app/src/pages/my/my.vue"]]);
wx.createPage(MiniProgramPage);
