"use strict";
const common_vendor = require("../../common/vendor.js");
const baseUrl = "http://127.0.0.1:8081";
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "weekly-report",
  setup(__props) {
    const weeklyTotal = common_vendor.ref("138.50");
    const dailyAvg = common_vendor.ref("19.79");
    const totalOrders = common_vendor.ref(20);
    const trendUp = common_vendor.ref(true);
    const trendPercent = common_vendor.ref(8);
    const trendDiff = common_vendor.ref("10.30");
    const dateRange = common_vendor.computed(() => {
      const now = /* @__PURE__ */ new Date();
      const dayOfWeek = now.getDay() || 7;
      const monday = new Date(now);
      monday.setDate(now.getDate() - dayOfWeek + 1);
      const sunday = new Date(monday);
      sunday.setDate(monday.getDate() + 6);
      const fmt = (d) => `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, "0")}.${String(d.getDate()).padStart(2, "0")}`;
      return `${fmt(monday)} - ${fmt(sunday)}`;
    });
    const weekDays = common_vendor.ref([
      { label: "一", percent: 40, amount: "15.50", isMax: false },
      { label: "二", percent: 55, amount: "21.00", isMax: false },
      { label: "三", percent: 90, amount: "28.50", isMax: true },
      { label: "四", percent: 45, amount: "17.00", isMax: false },
      { label: "五", percent: 65, amount: "24.00", isMax: false },
      { label: "六", percent: 30, amount: "18.50", isMax: false },
      { label: "日", percent: 25, amount: "14.00", isMax: false }
    ]);
    const mealPeriods = common_vendor.ref([
      { name: "午餐", percent: 45, color: "#f68a2f" },
      { name: "晚餐", percent: 35, color: "#ffdbcd" },
      { name: "早餐", percent: 20, color: "#77574d" }
    ]);
    const analysisItems = common_vendor.ref([
      { label: "最高单笔消费", value: "¥18.00 (牛肉板面 - 餐点, 周三)", emoji: "📊", bgColor: "#ffdcc5" },
      { label: "下单最多菜品", value: "排骨瓦罐汤 (4次)", emoji: "❤️", bgColor: "#ffdbd0" },
      { label: "平均下单间隔", value: "4.2 小时", emoji: "⏱️", bgColor: "#ffdbcd" }
    ]);
    const fetchWeeklyData = () => {
      const token = common_vendor.index.getStorageSync("token");
      common_vendor.index.request({
        url: baseUrl + "/analysis/cost/summary",
        method: "GET",
        header: { "authentication": token },
        success: (res) => {
          console.log("Weekly cost summary:", res.data);
          if (res.data && res.data.code === 0 && res.data.data) {
            const data = res.data.data;
            if (data.weekSpent)
              weeklyTotal.value = data.weekSpent.toFixed(2);
            if (data.totalOrders)
              totalOrders.value = data.totalOrders;
          }
        },
        fail: (err) => {
          console.error("获取周报数据失败:", err);
        }
      });
      common_vendor.index.request({
        url: baseUrl + "/analysis/health/trend?range=7",
        method: "GET",
        header: { "authentication": token },
        success: (res) => {
          console.log("Weekly health trend:", res.data);
        },
        fail: (err) => {
          console.error("获取健康趋势失败:", err);
        }
      });
    };
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    common_vendor.onLoad(() => {
      fetchWeeklyData();
    });
    return (_ctx, _cache) => {
      return {
        a: common_vendor.o(goBack),
        b: common_vendor.t(dateRange.value),
        c: common_vendor.t(weeklyTotal.value),
        d: common_vendor.t(trendUp.value ? "↑" : "↓"),
        e: common_vendor.t(trendPercent.value),
        f: common_vendor.n(trendUp.value ? "up" : "down"),
        g: common_vendor.t(dailyAvg.value),
        h: common_vendor.t(totalOrders.value),
        i: common_vendor.t(trendUp.value ? "多" : "少"),
        j: common_vendor.t(trendDiff.value),
        k: common_vendor.f(weekDays.value, (day, idx, i0) => {
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
