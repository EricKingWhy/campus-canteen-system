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
exports.getOrderPageAPI = getOrderPageAPI;
exports.reOrderAPI = reOrderAPI;
