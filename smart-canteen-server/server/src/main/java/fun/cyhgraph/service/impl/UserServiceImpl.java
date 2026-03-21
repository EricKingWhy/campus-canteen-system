package fun.cyhgraph.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fun.cyhgraph.constant.MessageConstant;
import fun.cyhgraph.dto.UserLoginDTO;
import fun.cyhgraph.dto.UserProfileDTO;
import fun.cyhgraph.dto.UserRegisterDTO;
import fun.cyhgraph.entity.User;
import fun.cyhgraph.exception.LoginFailedException;
import fun.cyhgraph.mapper.UserMapper;
import fun.cyhgraph.service.UserService;
import fun.cyhgraph.vo.UserProfileVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ====================== 活动系数映射 ======================
    private static final double[] ACTIVITY_MULTIPLIERS = { 1.0, 1.2, 1.375, 1.55, 1.725 };

    /**
     * activityLevel (1-4) -> activityFactor (乘数)
     */
    private Double activityLevelToFactor(Integer level) {
        if (level == null || level < 1 || level > 4)
            return 1.2; // 默认久坐
        return ACTIVITY_MULTIPLIERS[level];
    }

    /**
     * activityFactor (乘数) -> activityLevel (1-4)
     */
    private Integer activityFactorToLevel(Double factor) {
        if (factor == null)
            return 1;
        if (factor <= 1.2)
            return 1;
        if (factor <= 1.375)
            return 2;
        if (factor <= 1.55)
            return 3;
        return 4;
    }

    // ====================== 健康指标计算 ======================

    /**
     * 计算 BMI = weight / (height/100)^2
     */
    private Double calculateBMI(Double weight, Double height) {
        if (weight == null || height == null || height <= 0)
            return null;
        double bmi = weight / Math.pow(height / 100.0, 2);
        return Math.round(bmi * 10.0) / 10.0; // 保留1位小数
    }

    /**
     * 获取 BMI 分类
     */
    private String getBMICategory(Double bmi) {
        if (bmi == null)
            return "未知";
        if (bmi < 18.5)
            return "偏瘦";
        if (bmi < 24.0)
            return "正常";
        if (bmi < 28.0)
            return "超重";
        return "肥胖";
    }

    /**
     * 计算 BMR (Mifflin-St Jeor 公式)
     * 男：10*weight + 6.25*height - 5*age + 5
     * 女：10*weight + 6.25*height - 5*age - 161
     */
    private Integer calculateBMR(Double weight, Double height, Integer age, Integer gender) {
        if (weight == null || height == null || age == null)
            return null;
        double bmr = 10 * weight + 6.25 * height - 5 * age;
        // gender: 1=男, 2=女
        if (gender != null && gender == 2) {
            bmr -= 161;
        } else {
            bmr += 5; // 默认男性
        }
        return (int) Math.round(bmr);
    }

    /**
     * 计算 TDEE = BMR * activityMultiplier
     */
    private Integer calculateTDEE(Integer bmr, Double activityFactor) {
        if (bmr == null)
            return null;
        double factor = (activityFactor != null) ? activityFactor : 1.2;
        return (int) Math.round(bmr * factor);
    }

    /**
     * 计算建议摄入 = TDEE + 目标调整
     * 减脂(-500), 增肌(+300), 维持(0)
     */
    private Integer calculateSuggestIntake(Integer tdee, Integer healthGoal) {
        if (tdee == null)
            return null;
        int adjustment = 0;
        if (healthGoal != null) {
            switch (healthGoal) {
                case 1:
                    adjustment = -500;
                    break; // 减脂
                case 2:
                    adjustment = 300;
                    break; // 增肌
                case 3:
                    adjustment = 0;
                    break; // 维持
            }
        }
        return tdee + adjustment;
    }

    // ====================== JSON 序列化/反序列化 ======================

    private String tagsToJson(List<String> tags) {
        if (tags == null || tags.isEmpty())
            return null;
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (JsonProcessingException e) {
            log.warn("Tags 序列化失败: {}", e.getMessage());
            return null;
        }
    }

    private List<String> jsonToTags(String json) {
        if (json == null || json.isEmpty())
            return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) {
            log.warn("Tags 反序列化失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    // ====================== 接口实现 ======================

    @Override
    public User register(UserRegisterDTO userRegisterDTO) {
        log.info("用户注册：{}", userRegisterDTO.getUsername());

        String username = userRegisterDTO.getUsername() != null ? userRegisterDTO.getUsername().trim() : null;
        String nickname = (userRegisterDTO.getNickname() != null && !userRegisterDTO.getNickname().trim().isEmpty())
                ? userRegisterDTO.getNickname().trim()
                : username;
        String email = (userRegisterDTO.getEmail() != null && !userRegisterDTO.getEmail().trim().isEmpty())
                ? userRegisterDTO.getEmail().trim()
                : null;

        User existingUser = userMapper.selectByUsername(username);
        if (existingUser != null) {
            throw new LoginFailedException("用户名已存在，请更换一个用户名");
        }

        User newUser = User.builder()
                .username(username)
                .password(userRegisterDTO.getPassword())
                .email(email)
                .name(nickname) // 兼容旧字段
                .nickname(nickname) // 新字段同步保存，确保全链路可回读
                .mealBudget(new BigDecimal("15.00")) // 默认预算
                .createTime(LocalDateTime.now())
                .build();

        userMapper.insert(newUser);
        log.info("注册成功，用户ID: {}", newUser.getId());
        return newUser;
    }

    @Override
    public User login(UserLoginDTO userLoginDTO) {
        log.info("用户登录：{}", userLoginDTO.getUsername());

        User user = userMapper.selectByUsername(userLoginDTO.getUsername());
        if (user == null) {
            throw new LoginFailedException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        if (!userLoginDTO.getPassword().equals(user.getPassword())) {
            throw new LoginFailedException(MessageConstant.PASSWORD_ERROR);
        }

        log.info("登录成功，用户ID: {}", user.getId());
        return user;
    }

    /**
     * 获取用户画像
     */
    @Override
    public UserProfileVO getUserProfile(Long userId) {
        log.info("获取用户画像: userId={}", userId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在: {}", userId);
            return UserProfileVO.builder().build(); // 返回空 VO
        }

        // 转换 activityFactor -> activityLevel
        Integer activityLevel = activityFactorToLevel(user.getActivityFactor());

        // 从数据库读取计算值，或重新计算
        Double bmi = user.getBmi();
        Integer bmr = user.getBmr();
        Integer tdee = user.getTdee();

        // 如果没有计算值，重新计算
        if (bmi == null)
            bmi = calculateBMI(user.getWeight(), user.getHeight());
        if (bmr == null)
            bmr = calculateBMR(user.getWeight(), user.getHeight(), user.getAge(), user.getGender());
        if (tdee == null)
            tdee = calculateTDEE(bmr, user.getActivityFactor());

        Integer suggestIntake = calculateSuggestIntake(tdee, user.getHealthGoal());
        String bmiCategory = getBMICategory(bmi);

        return UserProfileVO.builder()
                .id(user.getId())
                .nickname(user.getNickname() != null ? user.getNickname() : user.getName())
                .avatar(user.getPic())
                .phone(user.getPhone())
                .gender(user.getGender())
                .age(user.getAge())
                .height(user.getHeight())
                .weight(user.getWeight())
                .activityLevel(activityLevel)
                .healthGoal(user.getHealthGoal())
                .bmi(bmi)
                .bmiCategory(bmiCategory)
                .bmr(bmr)
                .tdee(tdee)
                .suggestIntake(suggestIntake)
                .tasteTags(jsonToTags(user.getTasteTags()))
                .avoidTags(jsonToTags(user.getAvoidTags()))
                .nutritionPref(user.getNutritionPref())
                .mealBudget(user.getMealBudget())
                .build();
    }

    /**
     * 更新用户画像 (部分更新 - 只更新非 null 字段)
     */
    @Override
    public UserProfileVO updateProfile(Long userId, UserProfileDTO dto) {
        log.info("更新用户画像: userId={}, dto={}", userId, dto);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // ===== 部分更新：只更新 DTO 中非 null 且非空的字段 =====
        // 注意：空字符串 "" 视为未传入，不覆盖已有值

        // 基础信息（字符串字段需额外判断空串）
        if (dto.getNickname() != null && !dto.getNickname().trim().isEmpty())
            user.setNickname(dto.getNickname().trim());
        if (dto.getAvatar() != null && !dto.getAvatar().trim().isEmpty())
            user.setPic(dto.getAvatar()); // avatar -> pic
        if (dto.getPhone() != null && !dto.getPhone().trim().isEmpty())
            user.setPhone(dto.getPhone().trim());

        // 健康画像
        if (dto.getGender() != null)
            user.setGender(dto.getGender());
        if (dto.getAge() != null) {
            if (dto.getAge() < 10 || dto.getAge() > 80) {
                throw new RuntimeException("年龄必须在 10-80 之间");
            }
            user.setAge(dto.getAge());
        }
        if (dto.getHeight() != null) {
            if (dto.getHeight() < 120 || dto.getHeight() > 220) {
                throw new RuntimeException("身高必须在 120-220 cm 之间");
            }
            user.setHeight(dto.getHeight());
        }
        if (dto.getWeight() != null) {
            if (dto.getWeight() < 30 || dto.getWeight() > 200) {
                throw new RuntimeException("体重必须在 30-200 kg 之间");
            }
            user.setWeight(dto.getWeight());
        }
        if (dto.getActivityLevel() != null) {
            user.setActivityFactor(activityLevelToFactor(dto.getActivityLevel()));
        }
        if (dto.getHealthGoal() != null)
            user.setHealthGoal(dto.getHealthGoal());

        // 饮食偏好
        if (dto.getTasteTags() != null)
            user.setTasteTags(tagsToJson(dto.getTasteTags()));
        if (dto.getAvoidTags() != null)
            user.setAvoidTags(tagsToJson(dto.getAvoidTags()));
        if (dto.getNutritionPref() != null)
            user.setNutritionPref(dto.getNutritionPref());
        if (dto.getMealBudget() != null)
            user.setMealBudget(dto.getMealBudget());

        // ===== 后端二次计算 BMI/BMR/TDEE =====
        Double bmi = calculateBMI(user.getWeight(), user.getHeight());
        Integer bmr = calculateBMR(user.getWeight(), user.getHeight(), user.getAge(), user.getGender());
        Integer tdee = calculateTDEE(bmr, user.getActivityFactor());

        user.setBmi(bmi);
        user.setBmr(bmr);
        user.setTdee(tdee);

        // 更新数据库
        userMapper.updateById(user);
        log.info("用户画像更新成功: userId={}, bmi={}, bmr={}, tdee={}", userId, bmi, bmr, tdee);

        // 返回最新画像
        return getUserProfile(userId);
    }
}
