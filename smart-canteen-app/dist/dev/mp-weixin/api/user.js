"use strict";
const utils_http = require("../utils/http.js");
const getUserInfoAPI = (id) => {
  return utils_http.http({
    url: `/user/user/${id}`,
    method: "GET"
  });
};
const updateUserAPI = (params) => {
  return utils_http.http({
    url: "/user/user",
    method: "PUT",
    data: params
  });
};
const getUserProfileAPI = () => {
  return utils_http.http({
    url: "/user/user/profile",
    method: "GET"
  });
};
const updateUserProfileAPI = (data) => {
  return utils_http.http({
    url: "/user/user/profile",
    method: "PUT",
    data
  });
};
exports.getUserInfoAPI = getUserInfoAPI;
exports.getUserProfileAPI = getUserProfileAPI;
exports.updateUserAPI = updateUserAPI;
exports.updateUserProfileAPI = updateUserProfileAPI;
