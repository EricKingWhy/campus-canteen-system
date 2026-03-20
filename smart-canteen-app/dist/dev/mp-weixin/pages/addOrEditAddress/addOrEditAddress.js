"use strict";
const common_vendor = require("../../common/vendor.js");
const api_address = require("../../api/address.js");
require("../../utils/http.js");
require("../../stores/modules/user.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "addOrEditAddress",
  setup(__props) {
    const pickerChange = (ev) => {
      console.log(ev);
      address.value = ev.detail.value.join(" ");
      console.log(address.value);
      fullLocationCode = ev.detail.code;
      console.log(fullLocationCode);
      form.cityCode = fullLocationCode[1];
      form.districtCode = fullLocationCode[2];
    };
    const platform = common_vendor.ref("ios");
    const showDel = common_vendor.ref(false);
    const items = [
      {
        value: 1,
        name: "男士"
      },
      {
        value: 0,
        name: "女士"
      }
    ];
    const options = [
      {
        name: "公司"
      },
      {
        name: "家"
      },
      {
        name: "学校"
      }
    ];
    const form = common_vendor.reactive({
      id: 0,
      consignee: "",
      phone: "",
      label: "",
      gender: 1,
      provinceCode: "110000",
      provinceName: "",
      cityCode: "110100",
      cityName: "",
      districtCode: "110102",
      districtName: "",
      detail: ""
    });
    common_vendor.ref("");
    common_vendor.onLoad(async (options2) => {
      init();
      if (options2 && options2.type === "编辑") {
        delId.value = -1;
        showDel.value = true;
        common_vendor.index.setNavigationBarTitle({
          title: "编辑收货地点点址"
        });
        delId.value = options2.id;
      } else {
        showDel.value = false;
      }
    });
    common_vendor.onUnload(() => {
      common_vendor.index.removeStorage({
        key: "edit"
      });
    });
    const statusBarHeight = () => {
      return common_vendor.index.getSystemInfoSync().statusBarHeight + "px";
    };
    const init = () => {
      const res = common_vendor.index.getSystemInfoSync();
      platform.value = res.platform;
    };
    const getTextOption = (item) => {
      console.log("点击了标签", item);
      form.label = item.name;
    };
    const sexChangeHandle = (val) => {
      form.gender = val;
      console.log(form.gender);
    };
    const addAddress = async () => {
      if (form.consignee === "") {
        return common_vendor.index.showToast({
          title: "联系人不能为",
          duration: 1e3,
          icon: "none"
        });
      } else if (form.phone === "") {
        return common_vendor.index.showToast({
          title: "手机号不能为",
          duration: 1e3,
          icon: "none"
        });
      } else if (form.label === "") {
        return common_vendor.index.showToast({
          title: "所属标签不能为",
          duration: 1e3,
          icon: "none"
        });
      } else if (address.value === "") {
        return common_vendor.index.showToast({
          title: "所在地点点不能为",
          duration: 1e3,
          icon: "none"
        });
      }
      if (form.phone) {
        const reg = /^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/;
        if (!reg.test(form.phone)) {
          return common_vendor.index.showToast({
            title: "手机号输入有误",
            duration: 1e3,
            icon: "none"
          });
        }
      }
      const params = {
        ...form,
        provinceName: address.value.split(" ")[0],
        cityName: address.value.split(" ")[1],
        districtName: address.value.split(" ")[2]
      };
      if (showDel.value) {
        console.log("update params !!!", params);
        const res = await api_address.updateAddressAPI(params);
        if (res.code === 0) {
          common_vendor.index.redirectTo({
            url: "/pages/address/address"
          });
        }
      } else {
        delete params.id;
        console.log("add params with label!", params);
        const res = await api_address.addAddressAPI(params);
        if (res.code === 0) {
          common_vendor.index.redirectTo({
            url: "/pages/address/address"
          });
        }
      }
    };
    const deleteAddress = async () => {
      if (delId.value === -1 || !delId.value) {
        return common_vendor.index.showToast({
          title: "删除失败",
          duration: 1e3,
          icon: "none"
        });
      }
      const res = await api_address.deleteAddressAPI(delId.value);
      if (res.code === 0) {
        common_vendor.index.redirectTo({
          url: "/pages/address/address"
        });
        common_vendor.index.showToast({
          title: "地点点址删除成功",
          duration: 1e3,
          icon: "none"
        });
        form.consignee = "";
        form.phone = "";
        form.label = "";
        form.provinceCode = "110000";
        form.cityCode = "110100";
        form.districtCode = "110102";
      }
    };
    return (_ctx, _cache) => {
      var _a;
      return common_vendor.e({
        a: form.consignee,
        b: common_vendor.o(($event) => form.consignee = $event.detail.value),
        c: common_vendor.f(items, (item, index, i0) => {
          return common_vendor.e({
            a: item.value != form.gender
          }, item.value != form.gender ? {} : {}, {
            b: common_vendor.t(item.name),
            c: index,
            d: common_vendor.o(($event) => sexChangeHandle(item.value), index)
          });
        }),
        d: form.phone,
        e: common_vendor.o(($event) => form.phone = $event.detail.value),
        f: _ctx.address
      }, _ctx.address ? {
        g: common_vendor.t(_ctx.address)
      } : {}, {
        h: common_vendor.o(pickerChange),
        i: (_a = _ctx.address) == null ? void 0 : _a.split(" "),
        j: platform.value == "ios" ? 1 : "",
        k: form.detail,
        l: common_vendor.o(($event) => form.detail = $event.detail.value),
        m: common_vendor.f(options, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.name),
            b: form.label === item.name ? 1 : "",
            c: item.name,
            d: common_vendor.o(($event) => getTextOption(item), item.name)
          };
        }),
        n: common_vendor.o(($event) => addAddress()),
        o: showDel.value
      }, showDel.value ? {
        p: common_vendor.o(($event) => deleteAddress())
      } : {}, {
        q: `calc(100% - ${statusBarHeight} - 44px)`
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-06210029"], ["__file", "C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/smart-canteen-app/src/pages/addOrEditAddress/addOrEditAddress.vue"]]);
wx.createPage(MiniProgramPage);
