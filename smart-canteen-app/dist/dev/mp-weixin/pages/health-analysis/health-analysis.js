"use strict";
const common_vendor = require("../../common/vendor.js");
const stores_modules_userProfile = require("../../stores/modules/userProfile.js");
require("../../api/user.js");
require("../../utils/http.js");
require("../../stores/modules/user.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "health-analysis",
  setup(__props) {
    const profileStore = stores_modules_userProfile.useUserProfileStore();
    const toNum = (v, d = 0) => {
      if (v === null || v === void 0)
        return d;
      const n = Number(v);
      return isNaN(n) ? d : n;
    };
    const formatMoney = (v) => toNum(v).toFixed(2);
    const formatInt = (v) => Math.round(toNum(v)).toString();
    const activeTab = common_vendor.ref("health");
    const pageState = common_vendor.computed(() => {
      const p = profileStore.profile;
      if (!p.gender || !p.age || !p.height || !p.weight) {
        return "incomplete";
      }
      if (!hasAnyData.value) {
        return "noData";
      }
      return "normal";
    });
    const todayIntake = common_vendor.ref(0);
    const goalKcal = common_vendor.computed(() => profileStore.suggestIntake || 2200);
    const remainingKcal = common_vendor.computed(() => Math.max(0, goalKcal.value - todayIntake.value));
    const intakeProgress = common_vendor.computed(() => Math.min(100, todayIntake.value / goalKcal.value * 100));
    const activityLabel = common_vendor.computed(() => {
      const level = profileStore.profile.activityLevel;
      const labels = ["久坐", "轻度", "中等", "重度"];
      return labels[(level || 1) - 1] || "未知";
    });
    const bmiPointerPosition = common_vendor.computed(() => {
      const bmi = profileStore.calculatedBMI || 22;
      return Math.min(100, Math.max(0, (bmi - 16) / 14 * 100));
    });
    const hasNutritionData = common_vendor.ref(false);
    const macros = common_vendor.ref({
      proteinG: 0,
      carbG: 0,
      fatG: 0,
      proteinPct: 30,
      carbPct: 50,
      fatPct: 20
    });
    const donutStyle = common_vendor.computed(() => {
      const { proteinPct, carbPct, fatPct } = macros.value;
      return {
        background: `conic-gradient(#13ec5b 0% ${proteinPct}%, #4A90E2 ${proteinPct}% ${proteinPct + carbPct}%, #FFB347 ${proteinPct + carbPct}% 100%)`
      };
    });
    const nutritionSuggestion = common_vendor.ref("");
    const weeklyHealthTrend = common_vendor.ref([]);
    const trendChange = common_vendor.ref("");
    const weekDays = ["周一", "周二", "周三", "周四", "周五", "周六", "周日"];
    const monthSpent = common_vendor.ref(0);
    const predictedTotal = common_vendor.ref(0);
    const baseline = common_vendor.ref(0);
    const isOverspending = common_vendor.computed(() => predictedTotal.value > baseline.value && baseline.value > 0);
    const spendingProgress = common_vendor.computed(() => {
      if (baseline.value <= 0)
        return 50;
      return Math.min(100, monthSpent.value / baseline.value * 100);
    });
    const weeklyCostTrend = common_vendor.ref([]);
    const categoryBreakdown = common_vendor.ref([]);
    const topCategory = common_vendor.computed(() => {
      if (!categoryBreakdown.value.length)
        return "--";
      return categoryBreakdown.value.reduce((a, b) => a.amount > b.amount ? a : b).name;
    });
    const compositionDonutStyle = common_vendor.computed(() => {
      if (!categoryBreakdown.value.length)
        return { background: "#eee" };
      let acc = 0;
      const gradientParts = categoryBreakdown.value.map((cat) => {
        const start = acc;
        acc += cat.amount / monthSpent.value * 100;
        return `${cat.color} ${start}% ${acc}%`;
      });
      return { background: `conic-gradient(${gradientParts.join(", ")})` };
    });
    const costTip = common_vendor.ref("");
    const hasAnyData = common_vendor.computed(() => monthSpent.value > 0 || todayIntake.value > 0);
    const goToInfoSetting = () => {
      common_vendor.index.navigateTo({ url: "/pages/info-setting/info-setting" });
    };
    const goToOrder = () => {
      common_vendor.index.switchTab({ url: "/pages/category/category" });
    };
    const goToRecommend = () => {
      common_vendor.index.switchTab({ url: "/pages/index/index_v2" });
    };
    const fetchHealthSummary = async () => {
      var _a;
      try {
        const res = await common_vendor.index.request({
          url: "http://127.0.0.1:8081/analysis/health/summary",
          method: "GET",
          header: { "authentication": common_vendor.index.getStorageSync("token") }
        });
        const data = (_a = res.data) == null ? void 0 : _a.data;
        if (data) {
          todayIntake.value = data.todayIntakeKcal || 0;
          if (data.macros) {
            hasNutritionData.value = true;
            macros.value = data.macros;
          } else {
            hasNutritionData.value = false;
          }
          nutritionSuggestion.value = data.suggestion || "";
        }
      } catch (e) {
        console.error("获取健康分析数据失败:", e);
      }
    };
    const fetchHealthTrend = async () => {
      var _a, _b, _c;
      try {
        const res = await common_vendor.index.request({
          url: "http://127.0.0.1:8081/analysis/health/trend?range=7",
          method: "GET",
          header: { "authentication": common_vendor.index.getStorageSync("token") }
        });
        const data = (_a = res.data) == null ? void 0 : _a.data;
        if (data && Array.isArray(data) && data.length > 0) {
          const maxIntake = Math.max(...data.map((t) => t.intakeKcal || 0));
          weeklyHealthTrend.value = data.map((t) => ({
            value: t.intakeKcal || 0,
            day: t.day,
            heightPct: maxIntake > 0 ? (t.intakeKcal || 0) / maxIntake * 80 + 10 : 10
          }));
          if (data.length >= 2) {
            const latest = ((_b = data[data.length - 1]) == null ? void 0 : _b.completionRate) || 0;
            const prev = ((_c = data[data.length - 2]) == null ? void 0 : _c.completionRate) || 0;
            if (prev > 0) {
              const change = ((latest - prev) / prev * 100).toFixed(1);
              trendChange.value = change.startsWith("-") ? change + "%" : "+" + change + "%";
            }
          }
        }
      } catch (e) {
        console.error("获取健康趋势失败:", e);
      }
    };
    const getDayName = (dateStr) => {
      try {
        const d = new Date(dateStr);
        const names = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];
        return names[d.getDay()] || dateStr;
      } catch {
        return dateStr;
      }
    };
    const isToday = (dateStr) => {
      try {
        const d = new Date(dateStr);
        const now = /* @__PURE__ */ new Date();
        return d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth() && d.getDate() === now.getDate();
      } catch {
        return false;
      }
    };
    const fetchCostSummary = async () => {
      var _a;
      try {
        const res = await common_vendor.index.request({
          url: "http://127.0.0.1:8081/analysis/cost/summary",
          method: "GET",
          header: { "authentication": common_vendor.index.getStorageSync("token") }
        });
        const data = (_a = res.data) == null ? void 0 : _a.data;
        console.log("===== 餐费分析完整响应 =====", JSON.stringify(data));
        if (data) {
          monthSpent.value = toNum(data.monthSpent);
          predictedTotal.value = toNum(data.predictedMonthTotal);
          baseline.value = toNum(data.baseline);
          costTip.value = data.tip || "";
          const trendArr = data.trendData;
          console.log("===== 趋势原始数据 =====", JSON.stringify(trendArr));
          if (trendArr && Array.isArray(trendArr) && trendArr.length > 0) {
            const maxValue = Math.max(...trendArr.map((t) => parseFloat(t.amount || t.value || 0))) || 1;
            const MAX_BAR_RPX = 380;
            const MIN_BAR_RPX = 20;
            weeklyCostTrend.value = JSON.parse(JSON.stringify(
              trendArr.map((t) => {
                const amt = parseFloat(t.amount || t.value || 0);
                const ratio = maxValue > 0 ? amt / maxValue : 0;
                const barHeight = amt > 0 ? Math.max(MIN_BAR_RPX, Math.round(ratio * MAX_BAR_RPX)) : MIN_BAR_RPX;
                return {
                  day: getDayName(t.date || t.day || ""),
                  value: amt,
                  barHeight,
                  isToday: isToday(t.date || "")
                };
              })
            ));
            console.log("===== 趋势组装结果 =====", JSON.stringify(weeklyCostTrend.value));
          } else {
            const defaultDays = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];
            weeklyCostTrend.value = defaultDays.map((day, i) => ({
              day,
              value: 0,
              barHeight: 20,
              isToday: i === (/* @__PURE__ */ new Date()).getDay()
            }));
          }
          const catArr = data.byCategory;
          console.log("===== 构成原始数据 =====", JSON.stringify(catArr));
          if (catArr && Array.isArray(catArr) && catArr.length > 0) {
            const colorPalette = ["#4A90E2", "#34C759", "#FF9500", "#FF3B30", "#AF52DE", "#5AC8FA"];
            categoryBreakdown.value = JSON.parse(JSON.stringify(
              catArr.map((c, index) => ({
                name: c.name || c.categoryName || "其他",
                amount: parseFloat(c.amount || c.value || 0),
                color: c.color || colorPalette[index % colorPalette.length]
              }))
            ));
            console.log("===== 构成组装结果 =====", JSON.stringify(categoryBreakdown.value));
          } else {
            categoryBreakdown.value = [];
          }
        }
      } catch (e) {
        console.error("获取餐费分析数据失败:", e);
      }
    };
    const loadAllData = async () => {
      await profileStore.fetchProfile();
      await Promise.all([
        fetchHealthSummary(),
        fetchHealthTrend(),
        fetchCostSummary()
      ]);
    };
    common_vendor.onShow(() => {
      loadAllData();
    });
    common_vendor.onMounted(() => {
      loadAllData();
    });
    return (_ctx, _cache) => {
      var _a;
      return common_vendor.e({
        a: activeTab.value === "health" ? 1 : "",
        b: common_vendor.o(($event) => activeTab.value = "health"),
        c: activeTab.value === "cost" ? 1 : "",
        d: common_vendor.o(($event) => activeTab.value = "cost"),
        e: pageState.value === "incomplete"
      }, pageState.value === "incomplete" ? {
        f: common_vendor.o(goToInfoSetting)
      } : pageState.value === "noData" ? {
        h: common_vendor.o(goToOrder)
      } : activeTab.value === "health" ? common_vendor.e({
        j: common_vendor.t(remainingKcal.value),
        k: common_vendor.t(activityLabel.value),
        l: common_vendor.t(todayIntake.value),
        m: common_vendor.t(goalKcal.value),
        n: intakeProgress.value + "%",
        o: common_vendor.t(((_a = common_vendor.unref(profileStore).calculatedBMI) == null ? void 0 : _a.toFixed(1)) || "--"),
        p: bmiPointerPosition.value + "%",
        q: hasNutritionData.value
      }, hasNutritionData.value ? {
        r: common_vendor.s(donutStyle.value),
        s: common_vendor.t(todayIntake.value)
      } : {}, {
        t: common_vendor.t(macros.value.proteinPct),
        v: common_vendor.t(macros.value.proteinG),
        w: common_vendor.t(macros.value.carbPct),
        x: common_vendor.t(macros.value.carbG),
        y: common_vendor.t(macros.value.fatPct),
        z: common_vendor.t(macros.value.fatG),
        A: nutritionSuggestion.value
      }, nutritionSuggestion.value ? {
        B: common_vendor.t(nutritionSuggestion.value)
      } : {}, {
        C: common_vendor.o(goToRecommend),
        D: trendChange.value
      }, trendChange.value ? {
        E: common_vendor.t(trendChange.value)
      } : {}, {
        F: weeklyHealthTrend.value.length
      }, weeklyHealthTrend.value.length ? {
        G: common_vendor.f(weeklyHealthTrend.value, (point, index, i0) => {
          return {
            a: index,
            b: point.heightPct + "%",
            c: index * 14.28 + "%"
          };
        }),
        H: common_vendor.f(weekDays, (day, k0, i0) => {
          return {
            a: common_vendor.t(day),
            b: day
          };
        })
      } : {}) : activeTab.value === "cost" ? common_vendor.e({
        J: isOverspending.value
      }, isOverspending.value ? {} : {}, {
        K: common_vendor.t(formatMoney(monthSpent.value)),
        L: spendingProgress.value + "%",
        M: common_vendor.t(formatInt(predictedTotal.value)),
        N: weeklyCostTrend.value && weeklyCostTrend.value.length > 0
      }, weeklyCostTrend.value && weeklyCostTrend.value.length > 0 ? {
        O: common_vendor.f(weeklyCostTrend.value, (item, index, i0) => {
          return {
            a: common_vendor.t(item.value),
            b: item.barHeight + "rpx",
            c: common_vendor.t(item.day),
            d: index,
            e: item.isToday ? 1 : ""
          };
        })
      } : {}, {
        P: categoryBreakdown.value && categoryBreakdown.value.length > 0
      }, categoryBreakdown.value && categoryBreakdown.value.length > 0 ? {
        Q: common_vendor.s(compositionDonutStyle.value),
        R: common_vendor.t(topCategory.value),
        S: common_vendor.f(categoryBreakdown.value, (cat, k0, i0) => {
          return {
            a: cat.color,
            b: common_vendor.t(cat.name),
            c: common_vendor.t(formatMoney(cat.amount)),
            d: cat.name
          };
        })
      } : {}, {
        T: costTip.value
      }, costTip.value ? {
        U: common_vendor.t(costTip.value)
      } : {}) : {}, {
        g: pageState.value === "noData",
        i: activeTab.value === "health",
        I: activeTab.value === "cost"
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-85d0b700"], ["__file", "C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/smart-canteen-app/src/pages/health-analysis/health-analysis.vue"]]);
wx.createPage(MiniProgramPage);
