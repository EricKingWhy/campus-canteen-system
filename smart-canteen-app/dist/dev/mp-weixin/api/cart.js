"use strict";
const utils_http = require("../utils/http.js");
const addToCartAPI = (cartDTO) => {
  return utils_http.http({
    method: "POST",
    url: "/user/shoppingCart/add",
    data: cartDTO
  });
};
const subCartAPI = (cartDTO) => {
  return utils_http.http({
    method: "PUT",
    url: "/user/shoppingCart/sub",
    data: cartDTO
  });
};
const getCartAPI = () => {
  return utils_http.http({
    method: "GET",
    url: "/user/shoppingCart/list"
  });
};
const cleanCartAPI = () => {
  return utils_http.http({
    method: "DELETE",
    url: "/user/shoppingCart/clean"
  });
};
exports.addToCartAPI = addToCartAPI;
exports.cleanCartAPI = cleanCartAPI;
exports.getCartAPI = getCartAPI;
exports.subCartAPI = subCartAPI;
