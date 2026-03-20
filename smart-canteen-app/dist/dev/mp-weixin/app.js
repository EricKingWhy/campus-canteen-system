"use strict";
Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
const common_vendor = require("./common/vendor.js");
if (!Math) {
  "./pages/login/login.js";
  "./pages/index/index_v2.js";
  "./pages/category/category.js";
  "./pages/my/my.js";
  "./pages/order/order.js";
  "./pages/detail/detail.js";
  "./pages/submit/submit.js";
  "./pages/submit/success.js";
  "./pages/address/address.js";
  "./pages/favorite/favorite.js";
  "./pages/addOrEditAddress/addOrEditAddress.js";
  "./pages/remark/remark.js";
  "./pages/pay/pay.js";
  "./pages/pay/success.js";
  "./pages/orderDetail/orderDetail.js";
  "./pages/history/history.js";
  "./pages/updateMy/updateMy.js";
  "./pages/info-setting/info-setting.js";
  "./pages/health-analysis/health-analysis.js";
  "./pages/user/weekly-report.js";
}
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "App",
  setup(__props) {
    common_vendor.onLaunch(() => {
      console.log("App Launch");
    });
    common_vendor.onShow(() => {
      console.log("App Show");
    });
    common_vendor.onHide(() => {
      console.log("App Hide");
    });
    return () => {
    };
  }
});
const App = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__file", "C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/smart-canteen-app/src/App.vue"]]);
const pinia = common_vendor.createPinia();
pinia.use(common_vendor.src_default);
function createApp() {
  const app = common_vendor.createSSRApp(App);
  app.use(pinia);
  return {
    app
  };
}
createApp().app.mount("#app");
exports.createApp = createApp;
