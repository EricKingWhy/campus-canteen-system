"use strict";
const utils_http = require("../utils/http.js");
const getOrderPageAPI = (params) => {
  console.log("params", params);
  return utils_http.http({
    url: "/user/order/historyOrders",
    method: "GET",
    data: params
  });
};
const reOrderAPI = (id) => {
  return utils_http.http({
    url: `/user/order/reOrder/${id}`,
    method: "POST"
  });
};
const getWeeklyAnalysisAPI = () => {
  return utils_http.http({
    url: "/analysis/weekly-analysis",
    method: "GET"
  });
};
const getWeeklyReportAPI = (params) => {
  return utils_http.http({
    url: "/user/order/weekly-report",
    method: "GET",
    data: params
  });
};
exports.getOrderPageAPI = getOrderPageAPI;
exports.getWeeklyAnalysisAPI = getWeeklyAnalysisAPI;
exports.getWeeklyReportAPI = getWeeklyReportAPI;
exports.reOrderAPI = reOrderAPI;
