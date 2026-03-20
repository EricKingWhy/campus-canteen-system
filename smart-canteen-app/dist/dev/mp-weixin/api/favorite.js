"use strict";
const utils_http = require("../utils/http.js");
const favoriteAddAPI = (dishId) => {
  return utils_http.http({
    url: `/user/favorite/add?dishId=${dishId}`,
    method: "POST"
  });
};
const favoriteRemoveAPI = (dishId) => {
  return utils_http.http({
    url: `/user/favorite/remove?dishId=${dishId}`,
    method: "POST"
  });
};
const favoriteListAPI = () => {
  return utils_http.http({
    url: `/user/favorite/list`,
    method: "GET"
  });
};
const favoriteCheckAPI = (dishId) => {
  return utils_http.http({
    url: `/user/favorite/check/${dishId}`,
    method: "GET"
  });
};
exports.favoriteAddAPI = favoriteAddAPI;
exports.favoriteCheckAPI = favoriteCheckAPI;
exports.favoriteListAPI = favoriteListAPI;
exports.favoriteRemoveAPI = favoriteRemoveAPI;
