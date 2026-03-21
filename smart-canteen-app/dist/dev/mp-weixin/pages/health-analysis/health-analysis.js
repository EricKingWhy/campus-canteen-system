"use strict";
const common_vendor = require("../../common/vendor.js");
const stores_modules_userProfile = require("../../stores/modules/userProfile.js");
const common_assets = require("../../common/assets.js");
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
        background: `conic-gradient(#34c759 0% ${proteinPct}%, #4A90E2 ${proteinPct}% ${proteinPct + carbPct}%, #FFB347 ${proteinPct + carbPct}% 100%)`
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
    const selectedRange = common_vendor.ref("7days");
    const weeklyCostTrend = common_vendor.ref([]);
    const monthlyCostTrend = common_vendor.ref([]);
    const selectedAreaIndex = common_vendor.ref(0);
    const areaCanvasRect = common_vendor.ref(null);
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
    const areaTooltipText = common_vendor.computed(() => {
      const point = monthlyCostTrend.value[selectedAreaIndex.value];
      if (!point)
        return "";
      return `${point.date}  ¥${formatMoney(point.value)}`;
    });
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
    const formatMonthDay = (dateStr) => {
      try {
        const d = new Date(dateStr);
        const mm = d.getMonth() + 1;
        const dd = String(d.getDate()).padStart(2, "0");
        return `${mm}-${dd}`;
      } catch {
        return dateStr;
      }
    };
    const rpxToPx = (rpx) => {
      const { windowWidth } = common_vendor.index.getSystemInfoSync();
      return Math.round(windowWidth / 750 * rpx);
    };
    const queryAreaCanvasRect = () => {
      common_vendor.index.createSelectorQuery().select("#costTrendCanvas").boundingClientRect((rect) => {
        if (rect && rect.width) {
          areaCanvasRect.value = { left: rect.left, width: rect.width };
        }
      }).exec();
    };
    const draw30DayAreaChart = () => {
      if (selectedRange.value !== "30days" || !monthlyCostTrend.value.length)
        return;
      const ctx = common_vendor.index.createCanvasContext("costTrendCanvas");
      const width = rpxToPx(610);
      const height = rpxToPx(320);
      const padding = {
        top: rpxToPx(24),
        right: rpxToPx(16),
        bottom: rpxToPx(22),
        left: rpxToPx(16)
      };
      const chartWidth = width - padding.left - padding.right;
      const chartHeight = height - padding.top - padding.bottom;
      const baseY = height - padding.bottom;
      const values = monthlyCostTrend.value.map((item) => item.value);
      const max = Math.max(...values, 1);
      const min = Math.min(...values, 0);
      const range = Math.max(max - min, 1);
      const points = monthlyCostTrend.value.map((item, index, arr) => {
        const x = padding.left + (arr.length === 1 ? 0 : index / (arr.length - 1) * chartWidth);
        const y = padding.top + (1 - (item.value - min) / range) * chartHeight;
        return { x, y };
      });
      ctx.clearRect(0, 0, width, height);
      if (points.length > 1) {
        const areaGradient = ctx.createLinearGradient(0, padding.top, 0, baseY);
        areaGradient.addColorStop(0, "rgba(142, 124, 195, 0.30)");
        areaGradient.addColorStop(1, "rgba(142, 124, 195, 0.03)");
        ctx.beginPath();
        ctx.moveTo(points[0].x, baseY);
        ctx.lineTo(points[0].x, points[0].y);
        for (let i = 1; i < points.length; i++) {
          const prev = points[i - 1];
          const curr = points[i];
          const cx = (prev.x + curr.x) / 2;
          const cy = (prev.y + curr.y) / 2;
          ctx.quadraticCurveTo(prev.x, prev.y, cx, cy);
        }
        const last = points[points.length - 1];
        ctx.lineTo(last.x, last.y);
        ctx.lineTo(last.x, baseY);
        ctx.closePath();
        ctx.setFillStyle(areaGradient);
        ctx.fill();
        ctx.beginPath();
        ctx.moveTo(points[0].x, points[0].y);
        for (let i = 1; i < points.length; i++) {
          const prev = points[i - 1];
          const curr = points[i];
          const cx = (prev.x + curr.x) / 2;
          const cy = (prev.y + curr.y) / 2;
          ctx.quadraticCurveTo(prev.x, prev.y, cx, cy);
        }
        const lineGradient = ctx.createLinearGradient(0, 0, width, 0);
        lineGradient.addColorStop(0, "#9E8CD6");
        lineGradient.addColorStop(1, "#8F77D0");
        ctx.setStrokeStyle(lineGradient);
        ctx.setLineWidth(rpxToPx(4));
        ctx.setLineCap("round");
        ctx.setLineJoin("round");
        ctx.stroke();
      }
      const focusIndex = Math.min(selectedAreaIndex.value, points.length - 1);
      if (focusIndex >= 0 && points[focusIndex]) {
        const p = points[focusIndex];
        ctx.beginPath();
        ctx.arc(p.x, p.y, rpxToPx(7), 0, 2 * Math.PI);
        ctx.setFillStyle("#8F77D0");
        ctx.fill();
        ctx.beginPath();
        ctx.arc(p.x, p.y, rpxToPx(11), 0, 2 * Math.PI);
        ctx.setStrokeStyle("rgba(143, 119, 208, 0.28)");
        ctx.setLineWidth(rpxToPx(3));
        ctx.stroke();
      }
      ctx.draw();
    };
    const setRange = async (range) => {
      if (selectedRange.value === range)
        return;
      selectedRange.value = range;
      const dayRange = range === "30days" ? 30 : 7;
      await fetchCostTrend(dayRange);
      if (range === "30days") {
        await common_vendor.nextTick$1();
        queryAreaCanvasRect();
        draw30DayAreaChart();
      }
    };
    const onAreaCanvasTouch = (e) => {
      var _a, _b;
      if (!monthlyCostTrend.value.length || !areaCanvasRect.value)
        return;
      const touchX = (_b = (_a = e == null ? void 0 : e.changedTouches) == null ? void 0 : _a[0]) == null ? void 0 : _b.x;
      if (typeof touchX !== "number")
        return;
      const ratio = Math.min(1, Math.max(0, (touchX - areaCanvasRect.value.left) / areaCanvasRect.value.width));
      const index = Math.round(ratio * (monthlyCostTrend.value.length - 1));
      selectedAreaIndex.value = index;
      draw30DayAreaChart();
    };
    const fetchCostTrend = async (range = 7) => {
      var _a;
      try {
        const res = await common_vendor.index.request({
          url: `http://127.0.0.1:8081/analysis/cost/trend?range=${range}`,
          method: "GET",
          header: { "authentication": common_vendor.index.getStorageSync("token") }
        });
        const data = (_a = res.data) == null ? void 0 : _a.data;
        if (!Array.isArray(data) || data.length === 0) {
          if (range === 7)
            weeklyCostTrend.value = [];
          if (range === 30)
            monthlyCostTrend.value = [];
          return;
        }
        const maxValue = Math.max(...data.map((t) => parseFloat(t.amount || t.value || 0))) || 1;
        const MAX_BAR_RPX = 380;
        const MIN_BAR_RPX = 20;
        const points = data.map((t, index) => {
          const amount = parseFloat(t.amount || t.value || 0);
          const ratio = maxValue > 0 ? amount / maxValue : 0;
          const barHeight = amount > 0 ? Math.max(MIN_BAR_RPX, Math.round(ratio * MAX_BAR_RPX)) : MIN_BAR_RPX;
          const date = t.date || "";
          const showTick = index === 0 || index === data.length - 1 || index % 5 === 0;
          return {
            day: getDayName(date),
            date,
            value: amount,
            barHeight,
            isToday: isToday(date),
            displayLabel: range === 30 ? showTick ? formatMonthDay(date) : "" : getDayName(date)
          };
        });
        if (range === 7) {
          weeklyCostTrend.value = points;
        } else {
          monthlyCostTrend.value = points;
          const todayIndex = points.findIndex((point) => point.isToday);
          selectedAreaIndex.value = todayIndex >= 0 ? todayIndex : points.length - 1;
        }
      } catch (e) {
        console.error(`获取${range}天餐费趋势失败:`, e);
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
        fetchCostSummary(),
        fetchCostTrend(7)
      ]);
    };
    common_vendor.watch(selectedRange, async (range) => {
      if (range !== "30days" || !monthlyCostTrend.value.length)
        return;
      await common_vendor.nextTick$1();
      queryAreaCanvasRect();
      draw30DayAreaChart();
    });
    common_vendor.watch(selectedAreaIndex, () => {
      if (selectedRange.value === "30days") {
        draw30DayAreaChart();
      }
    });
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
        q: common_vendor.unref(common_assets.healthAnalysisIcon),
        r: hasNutritionData.value
      }, hasNutritionData.value ? {
        s: common_vendor.s(donutStyle.value),
        t: common_vendor.t(todayIntake.value)
      } : {}, {
        v: common_vendor.t(macros.value.proteinPct),
        w: common_vendor.t(macros.value.proteinG),
        x: common_vendor.t(macros.value.carbPct),
        y: common_vendor.t(macros.value.carbG),
        z: common_vendor.t(macros.value.fatPct),
        A: common_vendor.t(macros.value.fatG),
        B: nutritionSuggestion.value
      }, nutritionSuggestion.value ? {
        C: common_vendor.unref(common_assets.promptIcon),
        D: common_vendor.t(nutritionSuggestion.value)
      } : {}, {
        E: common_vendor.o(goToRecommend),
        F: trendChange.value
      }, trendChange.value ? {
        G: common_vendor.t(trendChange.value),
        H: common_vendor.unref(common_assets.healthAnalysisIcon)
      } : {}, {
        I: weeklyHealthTrend.value.length
      }, weeklyHealthTrend.value.length ? {
        J: common_vendor.f(weeklyHealthTrend.value, (point, index, i0) => {
          return {
            a: index,
            b: point.heightPct + "%",
            c: index * 14.28 + "%"
          };
        }),
        K: common_vendor.f(weekDays, (day, k0, i0) => {
          return {
            a: common_vendor.t(day),
            b: day
          };
        })
      } : {}) : activeTab.value === "cost" ? common_vendor.e({
        M: isOverspending.value
      }, isOverspending.value ? {} : {}, {
        N: common_vendor.t(formatMoney(monthSpent.value)),
        O: spendingProgress.value + "%",
        P: common_vendor.t(formatInt(predictedTotal.value)),
        Q: selectedRange.value === "7days" ? 1 : "",
        R: common_vendor.o(($event) => setRange("7days")),
        S: selectedRange.value === "30days" ? 1 : "",
        T: common_vendor.o(($event) => setRange("30days")),
        U: selectedRange.value === "7days" && weeklyCostTrend.value && weeklyCostTrend.value.length > 0
      }, selectedRange.value === "7days" && weeklyCostTrend.value && weeklyCostTrend.value.length > 0 ? {
        V: common_vendor.f(weeklyCostTrend.value, (item, index, i0) => {
          return {
            a: common_vendor.t(item.value),
            b: item.barHeight + "rpx",
            c: common_vendor.t(item.day),
            d: index,
            e: item.isToday ? 1 : ""
          };
        })
      } : selectedRange.value === "30days" && monthlyCostTrend.value.length > 0 ? {
        X: common_vendor.t(areaTooltipText.value),
        Y: common_vendor.o(onAreaCanvasTouch),
        Z: common_vendor.f(monthlyCostTrend.value, (item, index, i0) => {
          return {
            a: common_vendor.t(item.displayLabel),
            b: `x-${index}`,
            c: item.isToday ? 1 : ""
          };
        })
      } : {}, {
        W: selectedRange.value === "30days" && monthlyCostTrend.value.length > 0,
        aa: categoryBreakdown.value && categoryBreakdown.value.length > 0
      }, categoryBreakdown.value && categoryBreakdown.value.length > 0 ? {
        ab: common_vendor.s(compositionDonutStyle.value),
        ac: common_vendor.t(topCategory.value),
        ad: common_vendor.f(categoryBreakdown.value, (cat, k0, i0) => {
          return {
            a: cat.color,
            b: common_vendor.t(cat.name),
            c: common_vendor.t(formatMoney(cat.amount)),
            d: cat.name
          };
        })
      } : {}, {
        ae: costTip.value
      }, costTip.value ? {
        af: common_vendor.t(costTip.value)
      } : {}) : {}, {
        g: pageState.value === "noData",
        i: activeTab.value === "health",
        L: activeTab.value === "cost"
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-85d0b700"], ["__file", "C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/smart-canteen-app/src/pages/health-analysis/health-analysis.vue"]]);
wx.createPage(MiniProgramPage);
