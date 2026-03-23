"use strict";
const common_vendor = require("../../common/vendor.js");
const api_order = require("../../api/order.js");
require("../../utils/http.js");
require("../../stores/modules/user.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "weekly-report",
  setup(__props) {
    const dayLabels = ["一", "二", "三", "四", "五", "六", "日"];
    const mealColorMap = {
      早餐: "#77574d",
      午餐: "#f68a2f",
      晚餐: "#ffdbcd"
    };
    const getWeekRange = () => {
      const now = /* @__PURE__ */ new Date();
      const dayOfWeek = now.getDay() || 7;
      const monday = new Date(now);
      monday.setDate(now.getDate() - dayOfWeek + 1);
      monday.setHours(0, 0, 0, 0);
      const sunday = new Date(monday);
      sunday.setDate(monday.getDate() + 6);
      sunday.setHours(23, 59, 59, 999);
      return { monday, sunday };
    };
    const formatDate = (date, separator = "-") => {
      const yyyy = date.getFullYear();
      const mm = String(date.getMonth() + 1).padStart(2, "0");
      const dd = String(date.getDate()).padStart(2, "0");
      return `${yyyy}${separator}${mm}${separator}${dd}`;
    };
    const weekRange = common_vendor.ref(getWeekRange());
    const dateRange = common_vendor.computed(() => {
      return `${formatDate(weekRange.value.monday, ".")} - ${formatDate(weekRange.value.sunday, ".")}`;
    });
    const reportData = common_vendor.ref({
      totalAmount: 0,
      totalOrders: 0,
      orderCount: 0,
      dailyAverage: 0,
      lastWeekAmount: 0,
      diffAmount: 0,
      dailyTrend: [],
      mealPeriodDistribution: []
    });
    const weeklyTotal = common_vendor.computed(() => Number(reportData.value.totalAmount || 0).toFixed(2));
    const dailyAvg = common_vendor.computed(() => Number(reportData.value.dailyAverage || 0).toFixed(2));
    const totalOrders = common_vendor.computed(() => Number(reportData.value.orderCount ?? reportData.value.totalOrders ?? 0));
    const trendDiff = common_vendor.computed(() => Math.abs(Number(reportData.value.diffAmount || 0)).toFixed(2));
    const trendUp = common_vendor.computed(() => Number(reportData.value.diffAmount || 0) >= 0);
    const trendArrow = common_vendor.computed(() => {
      const diff = Number(reportData.value.diffAmount || 0);
      if (diff === 0)
        return "→";
      return diff > 0 ? "↑" : "↓";
    });
    const summaryTrendText = common_vendor.computed(() => {
      const diff = Number(reportData.value.diffAmount || 0);
      if (diff === 0)
        return "持平";
      return diff > 0 ? "多" : "少";
    });
    const trendPercent = common_vendor.computed(() => {
      const current = Number(reportData.value.totalAmount || 0);
      const last = Number(reportData.value.lastWeekAmount || 0);
      if (last <= 0)
        return current > 0 ? 100 : 0;
      return Math.round(Math.abs((current - last) / last * 100));
    });
    const weekDays = common_vendor.ref([]);
    const mealPeriods = common_vendor.ref([
      { name: "午餐", percent: 0, color: mealColorMap["午餐"] },
      { name: "晚餐", percent: 0, color: mealColorMap["晚餐"] },
      { name: "早餐", percent: 0, color: mealColorMap["早餐"] }
    ]);
    const coreMealPeriod = common_vendor.computed(() => {
      if (!mealPeriods.value.length)
        return "暂无";
      const sorted = [...mealPeriods.value].sort((a, b) => b.percent - a.percent);
      return sorted[0].percent > 0 ? sorted[0].name : "暂无";
    });
    const getTodayWeekIndex = () => {
      const jsDay = (/* @__PURE__ */ new Date()).getDay();
      return jsDay === 0 ? 6 : jsDay - 1;
    };
    const buildWeekDays = (trend = []) => {
      const trendMap = /* @__PURE__ */ new Map();
      trend.forEach((item) => {
        if (item.day) {
          trendMap.set(item.day, Number(item.dailyTotal || 0));
        }
      });
      const bars = [];
      const monday = new Date(weekRange.value.monday);
      for (let i = 0; i < 7; i++) {
        const current = new Date(monday);
        current.setDate(monday.getDate() + i);
        const key = formatDate(current, "-");
        const amount = Number(trendMap.get(key) || 0);
        bars.push({
          label: dayLabels[i],
          percent: 0,
          amount: amount.toFixed(2),
          isMax: false
        });
      }
      const maxAmount = Math.max(...bars.map((item) => Number(item.amount)), 0);
      const todayIndex = getTodayWeekIndex();
      weekDays.value = bars.map((item, idx) => {
        const amount = Number(item.amount);
        let percent = 8;
        if (maxAmount > 0) {
          percent = amount > 0 ? Math.max(amount / maxAmount * 100, 15) : 8;
        }
        return {
          ...item,
          percent,
          isMax: idx === todayIndex
        };
      });
    };
    const buildMealPeriods = (distribution = []) => {
      const percentageMap = {};
      distribution.forEach((item) => {
        if (item.mealPeriod) {
          percentageMap[item.mealPeriod] = Number(item.percentage || 0);
        }
      });
      mealPeriods.value = ["午餐", "晚餐", "早餐"].map((name) => ({
        name,
        percent: Number((percentageMap[name] || 0).toFixed(1)),
        color: mealColorMap[name]
      }));
    };
    const analysisData = common_vendor.ref({
      maxAmount: 0,
      maxDishName: "",
      topDishName: "",
      topDishCount: 0,
      avgIntervalHours: 0
    });
    const analysisItems = common_vendor.computed(() => {
      const maxSpendValue = analysisData.value.maxAmount > 0 ? `¥${analysisData.value.maxAmount.toFixed(2)} (${analysisData.value.maxDishName || "暂无数据"})` : "暂无数据";
      const topDishValue = analysisData.value.topDishName ? `${analysisData.value.topDishName} (${analysisData.value.topDishCount || 0}次)` : "暂无数据";
      const avgIntervalValue = analysisData.value.avgIntervalHours > 0 ? `${analysisData.value.avgIntervalHours.toFixed(1)} 小时` : "暂无数据";
      return [
        { label: "最高单笔消费", value: maxSpendValue, emoji: "📊", bgColor: "#ffdcc5" },
        { label: "下单最多菜品", value: topDishValue, emoji: "❤️", bgColor: "#ffdbd0" },
        { label: "平均下单间隔", value: avgIntervalValue, emoji: "⏱️", bgColor: "#ffdbcd" }
      ];
    });
    const fetchWeeklyData = async () => {
      try {
        const res = await api_order.getWeeklyReportAPI({
          start_date: formatDate(weekRange.value.monday, "-"),
          end_date: formatDate(weekRange.value.sunday, "-")
        });
        if (res.code === 0 && res.data) {
          const data = res.data;
          reportData.value = {
            ...reportData.value,
            ...data,
            totalAmount: Number(data.totalAmount || 0),
            totalOrders: Number(data.totalOrders || 0),
            orderCount: Number(data.orderCount ?? data.totalOrders ?? 0),
            dailyAverage: Number(data.dailyAverage || 0),
            lastWeekAmount: Number(data.lastWeekAmount || 0),
            diffAmount: Number(data.diffAmount || 0)
          };
          buildWeekDays(data.dailyTrend || []);
          buildMealPeriods(data.mealPeriodDistribution || []);
        }
      } catch (error) {
        console.error("获取周报数据失败:", error);
      }
    };
    const fetchWeeklyAnalysis = async () => {
      try {
        const res = await api_order.getWeeklyAnalysisAPI();
        if (res.code === 0 && res.data) {
          analysisData.value = {
            maxAmount: Number(res.data.maxAmount || 0),
            maxDishName: res.data.maxDishName || "",
            topDishName: res.data.topDishName || "",
            topDishCount: Number(res.data.topDishCount || 0),
            avgIntervalHours: Number(res.data.avgIntervalHours || 0)
          };
        }
      } catch (error) {
        console.error("获取本周餐点分析失败:", error);
      }
    };
    common_vendor.onLoad(() => {
      weekRange.value = getWeekRange();
      buildWeekDays([]);
      buildMealPeriods([]);
      fetchWeeklyData();
      fetchWeeklyAnalysis();
    });
    return (_ctx, _cache) => {
      return {
        a: common_vendor.t(dateRange.value),
        b: common_vendor.t(weeklyTotal.value),
        c: common_vendor.t(trendArrow.value),
        d: common_vendor.t(trendPercent.value),
        e: common_vendor.n(trendUp.value ? "up" : "down"),
        f: common_vendor.t(dailyAvg.value),
        g: common_vendor.t(totalOrders.value),
        h: common_vendor.t(summaryTrendText.value),
        i: common_vendor.t(trendDiff.value),
        j: common_vendor.f(weekDays.value, (day, idx, i0) => {
          return common_vendor.e({
            a: day.isMax
          }, day.isMax ? {
            b: common_vendor.t(day.amount)
          } : {}, {
            c: day.percent + "%",
            d: day.isMax ? 1 : "",
            e: common_vendor.t(day.label),
            f: day.isMax ? 1 : "",
            g: idx
          });
        }),
        k: common_vendor.t(coreMealPeriod.value),
        l: common_vendor.f(mealPeriods.value, (m, k0, i0) => {
          return {
            a: m.color,
            b: common_vendor.t(m.name),
            c: common_vendor.t(m.percent),
            d: m.name
          };
        }),
        m: common_vendor.f(analysisItems.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.emoji),
            b: item.bgColor,
            c: common_vendor.t(item.label),
            d: common_vendor.t(item.value),
            e: item.label
          };
        })
      };
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-3226081b"], ["__file", "C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/smart-canteen-app/src/pages/user/weekly-report.vue"]]);
wx.createPage(MiniProgramPage);
