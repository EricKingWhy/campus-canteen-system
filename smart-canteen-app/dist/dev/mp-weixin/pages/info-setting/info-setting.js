"use strict";
const common_vendor = require("../../common/vendor.js");
const stores_modules_userProfile = require("../../stores/modules/userProfile.js");
const common_assets = require("../../common/assets.js");
require("../../api/user.js");
require("../../utils/http.js");
require("../../stores/modules/user.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "info-setting",
  setup(__props) {
    const profileStore = stores_modules_userProfile.useUserProfileStore();
    const currentStep = common_vendor.ref(1);
    const loading = common_vendor.ref(false);
    const customAvoid = common_vendor.ref("");
    const goalIconMap = {
      1: common_assets.goalIconJianzhi,
      2: common_assets.goalIconZengji,
      3: common_assets.goalIconWeichi
    };
    const formData = common_vendor.ref({
      nickname: "",
      phone: "",
      avatar: "",
      gender: 1,
      age: 22,
      height: 170,
      weight: 65,
      activityLevel: 2,
      healthGoal: 3,
      tasteTags: [],
      avoidTags: [],
      nutritionPref: "",
      mealBudget: 15
    });
    const activityOptions = [
      { value: 1, name: "久坐", desc: "极少运动", icon: "🪑" },
      { value: 2, name: "轻度", desc: "每周1-3天", icon: "🚶" },
      { value: 3, name: "中度", desc: "每周3-5天", icon: "🏃" },
      { value: 4, name: "重度", desc: "每周6-7天", icon: "💪" }
    ];
    const goalOptions = [
      { value: 1, name: "减脂", icon: "📉" },
      { value: 2, name: "增肌", icon: "💪" },
      { value: 3, name: "维持", icon: "⚖️" }
    ];
    const tasteOptions = ["清淡", "麻辣", "咸鲜", "酸甜"];
    const avoidOptions = ["花生", "麸质", "乳制品", "海鲜", "蛋类"];
    const nutritionOptions = ["高蛋白", "低碳", "生酮", "均衡"];
    const activityMultiplier = common_vendor.computed(() => {
      const multipliers = { 1: 1.2, 2: 1.375, 3: 1.55, 4: 1.725 };
      return multipliers[formData.value.activityLevel || 1] || 1.2;
    });
    const calculatedBMI = common_vendor.computed(() => {
      const { weight, height } = formData.value;
      if (!weight || !height || height <= 0)
        return null;
      return Math.round(weight / Math.pow(height / 100, 2) * 10) / 10;
    });
    const bmiCategory = common_vendor.computed(() => {
      const bmi = calculatedBMI.value;
      if (bmi === null)
        return "未知";
      if (bmi < 18.5)
        return "偏瘦";
      if (bmi < 24)
        return "正常";
      if (bmi < 28)
        return "超重";
      return "肥胖";
    });
    const bmiIndicatorPosition = common_vendor.computed(() => {
      const bmi = calculatedBMI.value;
      if (bmi === null)
        return "0%";
      const percent = Math.min(100, Math.max(0, (bmi - 15) / 20 * 100));
      return `${percent}%`;
    });
    const calculatedBMR = common_vendor.computed(() => {
      const { weight, height, age, gender } = formData.value;
      if (!weight || !height || !age)
        return null;
      let bmr = 10 * weight + 6.25 * height - 5 * age;
      if (gender === 2)
        bmr -= 161;
      else
        bmr += 5;
      return Math.round(bmr);
    });
    const calculatedTDEE = common_vendor.computed(() => {
      const bmr = calculatedBMR.value;
      if (bmr === null)
        return null;
      return Math.round(bmr * activityMultiplier.value);
    });
    const suggestIntake = common_vendor.computed(() => {
      const tdee = calculatedTDEE.value;
      if (tdee === null)
        return null;
      const goal = formData.value.healthGoal;
      let adjustment = 0;
      if (goal === 1)
        adjustment = -500;
      if (goal === 2)
        adjustment = 300;
      return tdee + adjustment;
    });
    const onHeightInput = (e) => {
      formData.value.height = Number(e.detail.value) || 170;
    };
    const onWeightInput = (e) => {
      formData.value.weight = Number(e.detail.value) || 65;
    };
    const onHeightSliderChange = (e) => {
      formData.value.height = e.detail.value;
    };
    const onWeightSliderChange = (e) => {
      formData.value.weight = e.detail.value;
    };
    const onBudgetSliderChange = (e) => {
      formData.value.mealBudget = e.detail.value;
    };
    const toggleTag = (field, tag) => {
      const tags = formData.value[field] || [];
      const index = tags.indexOf(tag);
      if (index > -1) {
        tags.splice(index, 1);
      } else {
        tags.push(tag);
      }
      formData.value[field] = [...tags];
    };
    const addCustomAvoid = () => {
      if (customAvoid.value.trim()) {
        const tags = formData.value.avoidTags || [];
        if (!tags.includes(customAvoid.value.trim())) {
          tags.push(customAvoid.value.trim());
          formData.value.avoidTags = [...tags];
        }
        customAvoid.value = "";
      }
    };
    const nextStep = () => {
      currentStep.value = 2;
    };
    const handleBack = () => {
      if (currentStep.value === 2) {
        currentStep.value = 1;
      } else {
        common_vendor.index.navigateBack();
      }
    };
    const handleSave = async () => {
      loading.value = true;
      try {
        const result = await profileStore.saveProfile(formData.value);
        if (result.success) {
          common_vendor.index.showToast({ title: "保存成功", icon: "success" });
          setTimeout(() => common_vendor.index.navigateBack(), 1e3);
        } else {
          common_vendor.index.showToast({ title: result.message || "保存失败", icon: "none" });
        }
      } catch (error) {
        common_vendor.index.showToast({ title: error.message || "保存失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    };
    const handleSkip = () => {
      common_vendor.index.navigateBack();
    };
    const chooseAvatar = () => {
      common_vendor.index.chooseMedia({
        count: 1,
        mediaType: ["image"],
        sourceType: ["album", "camera"],
        success: (res) => {
          const tempFilePath = res.tempFiles[0].tempFilePath;
          common_vendor.wx$1.getFileSystemManager().readFile({
            filePath: tempFilePath,
            encoding: "base64",
            success: (fileRes) => {
              formData.value.avatar = "data:image/png;base64," + fileRes.data;
            },
            fail: () => {
              formData.value.avatar = tempFilePath;
            }
          });
        }
      });
    };
    common_vendor.onMounted(async () => {
      await profileStore.fetchProfile();
      if (profileStore.isLoaded && profileStore.profile) {
        const p = profileStore.profile;
        formData.value = {
          nickname: p.nickname || "",
          phone: p.phone || "",
          avatar: p.avatar || "",
          gender: p.gender || 1,
          age: p.age || 22,
          height: p.height || 170,
          weight: p.weight || 65,
          activityLevel: p.activityLevel || 2,
          healthGoal: p.healthGoal || 3,
          tasteTags: p.tasteTags || [],
          avoidTags: p.avoidTags || [],
          nutritionPref: p.nutritionPref || "",
          mealBudget: p.mealBudget || 15
        };
      }
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(handleBack),
        b: currentStep.value === 2
      }, currentStep.value === 2 ? {
        c: common_vendor.o(handleSave)
      } : {}, {
        d: common_vendor.t(currentStep.value),
        e: common_vendor.t(currentStep.value === 1 ? "基本信息" : "饮食偏好"),
        f: currentStep.value === 1 ? "50%" : "100%",
        g: currentStep.value === 1
      }, currentStep.value === 1 ? common_vendor.e({
        h: !formData.value.avatar || formData.value.avatar.includes("photo-1599566150163-29194dcaad36") || formData.value.avatar.includes("images.unsplash.com") || formData.value.avatar.startsWith("http") && !formData.value.avatar.includes("121.41.59.61") && !formData.value.avatar.includes("localhost") ? "/static/images/default_avatar.jpg" : formData.value.avatar,
        i: common_vendor.o(chooseAvatar),
        j: formData.value.nickname,
        k: common_vendor.o(($event) => formData.value.nickname = $event.detail.value),
        l: formData.value.phone,
        m: common_vendor.o(($event) => formData.value.phone = $event.detail.value),
        n: formData.value.gender === 1 ? 1 : "",
        o: common_vendor.o(($event) => formData.value.gender = 1),
        p: formData.value.gender === 2 ? 1 : "",
        q: common_vendor.o(($event) => formData.value.gender = 2),
        r: common_vendor.o(($event) => formData.value.age = Math.max(10, (formData.value.age || 20) - 1)),
        s: common_vendor.t(formData.value.age || 20),
        t: common_vendor.o(($event) => formData.value.age = Math.min(80, (formData.value.age || 20) + 1)),
        v: common_vendor.o([($event) => formData.value.height = $event.detail.value, onHeightInput]),
        w: formData.value.height,
        x: formData.value.height || 170,
        y: common_vendor.o(onHeightSliderChange),
        z: common_vendor.o([($event) => formData.value.weight = $event.detail.value, onWeightInput]),
        A: formData.value.weight,
        B: formData.value.weight || 65,
        C: common_vendor.o(onWeightSliderChange),
        D: common_vendor.f(activityOptions, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.icon),
            b: common_vendor.t(item.name),
            c: common_vendor.t(item.desc),
            d: item.value,
            e: formData.value.activityLevel === item.value ? 1 : "",
            f: common_vendor.o(($event) => formData.value.activityLevel = item.value, item.value)
          };
        }),
        E: common_vendor.f(goalOptions, (item, k0, i0) => {
          return {
            a: goalIconMap[item.value],
            b: common_vendor.t(item.name),
            c: item.value,
            d: formData.value.healthGoal === item.value ? 1 : "",
            e: common_vendor.o(($event) => formData.value.healthGoal = item.value, item.value)
          };
        }),
        F: common_vendor.t(calculatedBMI.value || "--"),
        G: common_vendor.t(bmiCategory.value),
        H: calculatedBMI.value
      }, calculatedBMI.value ? {
        I: bmiIndicatorPosition.value
      } : {}, {
        J: common_vendor.t(calculatedBMR.value || "--"),
        K: common_vendor.t(calculatedTDEE.value || "--"),
        L: common_vendor.t(suggestIntake.value || "--")
      }) : {
        M: common_vendor.f(tasteOptions, (tag, k0, i0) => {
          var _a, _b, _c;
          return common_vendor.e({
            a: common_vendor.t(tag),
            b: (_a = formData.value.tasteTags) == null ? void 0 : _a.includes(tag)
          }, ((_b = formData.value.tasteTags) == null ? void 0 : _b.includes(tag)) ? {} : {}, {
            c: tag,
            d: ((_c = formData.value.tasteTags) == null ? void 0 : _c.includes(tag)) ? 1 : "",
            e: common_vendor.o(($event) => toggleTag("tasteTags", tag), tag)
          });
        }),
        N: common_vendor.f(avoidOptions, (tag, k0, i0) => {
          var _a, _b, _c;
          return common_vendor.e({
            a: common_vendor.t(tag),
            b: (_a = formData.value.avoidTags) == null ? void 0 : _a.includes(tag)
          }, ((_b = formData.value.avoidTags) == null ? void 0 : _b.includes(tag)) ? {} : {}, {
            c: tag,
            d: ((_c = formData.value.avoidTags) == null ? void 0 : _c.includes(tag)) ? 1 : "",
            e: common_vendor.o(($event) => toggleTag("avoidTags", tag), tag)
          });
        }),
        O: common_vendor.o(addCustomAvoid),
        P: customAvoid.value,
        Q: common_vendor.o(($event) => customAvoid.value = $event.detail.value),
        R: common_vendor.f(nutritionOptions, (item, k0, i0) => {
          return common_vendor.e({
            a: common_vendor.t(item),
            b: formData.value.nutritionPref === item
          }, formData.value.nutritionPref === item ? {} : {}, {
            c: item,
            d: formData.value.nutritionPref === item ? 1 : "",
            e: common_vendor.o(($event) => formData.value.nutritionPref = item, item)
          });
        }),
        S: common_vendor.t(formData.value.mealBudget || 15),
        T: formData.value.mealBudget || 15,
        U: common_vendor.o(onBudgetSliderChange)
      }, {
        V: currentStep.value === 1
      }, currentStep.value === 1 ? {
        W: common_vendor.o(nextStep)
      } : {
        X: common_vendor.o(handleSave),
        Y: loading.value,
        Z: common_vendor.o(handleSkip)
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-84cc0418"], ["__file", "C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/smart-canteen-app/src/pages/info-setting/info-setting.vue"]]);
wx.createPage(MiniProgramPage);
