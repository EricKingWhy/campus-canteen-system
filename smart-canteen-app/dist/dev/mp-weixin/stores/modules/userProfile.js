"use strict";
const common_vendor = require("../../common/vendor.js");
const api_user = require("../../api/user.js");
const useUserProfileStore = common_vendor.defineStore("userProfile", () => {
  const profile = common_vendor.ref({});
  const loading = common_vendor.ref(false);
  const isLoaded = common_vendor.ref(false);
  const defaultAvatarPath = "/static/images/default_avatar.jpg";
  const legacyDefaultAvatarKeys = ["photo-1599566150163-29194dcaad36", "images.unsplash.com"];
  const normalizeAvatar = (avatar) => {
    const avatarValue = (avatar || "").trim();
    if (!avatarValue)
      return defaultAvatarPath;
    if (legacyDefaultAvatarKeys.some((key) => avatarValue.includes(key)))
      return defaultAvatarPath;
    if (avatarValue.startsWith("http") && !avatarValue.includes("121.41.59.61") && !avatarValue.includes("localhost"))
      return defaultAvatarPath;
    return avatarValue;
  };
  const activityMultiplier = common_vendor.computed(() => {
    const multipliers = {
      1: 1.2,
      // 久坐
      2: 1.375,
      // 轻度
      3: 1.55,
      // 中度
      4: 1.725
      // 重度
    };
    return multipliers[profile.value.activityLevel || 1] || 1.2;
  });
  const calculatedBMI = common_vendor.computed(() => {
    const { weight, height } = profile.value;
    if (!weight || !height || height <= 0)
      return null;
    const bmi = weight / Math.pow(height / 100, 2);
    return Math.round(bmi * 10) / 10;
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
  const calculatedBMR = common_vendor.computed(() => {
    const { weight, height, age, gender } = profile.value;
    if (!weight || !height || !age)
      return null;
    let bmr = 10 * weight + 6.25 * height - 5 * age;
    if (gender === 2) {
      bmr -= 161;
    } else {
      bmr += 5;
    }
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
    const goal = profile.value.healthGoal;
    let adjustment = 0;
    if (goal === 1)
      adjustment = -500;
    if (goal === 2)
      adjustment = 300;
    return tdee + adjustment;
  });
  const displayName = common_vendor.computed(() => {
    return profile.value.nickname || "未设置昵称";
  });
  const displayAvatar = common_vendor.computed(() => {
    return normalizeAvatar(profile.value.avatar);
  });
  const bodyStats = common_vendor.computed(() => {
    const h = profile.value.height;
    const w = profile.value.weight;
    if (!h && !w)
      return "未设置";
    return `${h || "--"}cm / ${w || "--"}kg`;
  });
  async function fetchProfile() {
    loading.value = true;
    try {
      const res = await api_user.getUserProfileAPI();
      if (res.data) {
        profile.value = res.data;
        isLoaded.value = true;
      }
    } catch (error) {
      console.error("获取用户画像失败:", error);
    } finally {
      loading.value = false;
    }
  }
  async function saveProfile(data) {
    loading.value = true;
    try {
      const res = await api_user.updateUserProfileAPI(data);
      if (res.data) {
        profile.value = res.data;
        return { success: true, data: res.data };
      }
      return { success: false, message: "保存失败" };
    } catch (error) {
      console.error("保存用户画像失败:", error);
      return { success: false, message: error.message || "保存失败" };
    } finally {
      loading.value = false;
    }
  }
  function updateLocal(data) {
    profile.value = { ...profile.value, ...data };
  }
  function reset() {
    profile.value = {};
    isLoaded.value = false;
  }
  return {
    // State
    profile,
    loading,
    isLoaded,
    // Getters
    calculatedBMI,
    bmiCategory,
    calculatedBMR,
    calculatedTDEE,
    suggestIntake,
    activityMultiplier,
    displayName,
    displayAvatar,
    bodyStats,
    // Actions
    fetchProfile,
    saveProfile,
    updateLocal,
    reset
  };
});
exports.useUserProfileStore = useUserProfileStore;
