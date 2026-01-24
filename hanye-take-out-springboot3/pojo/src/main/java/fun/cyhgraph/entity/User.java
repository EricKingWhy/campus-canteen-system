package fun.cyhgraph.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    // ===== 基础信息 =====
    private String openid; // 微信 openid (可为空，兼容账号密码登录)
    private String username; // 用户名/学号
    private String password; // 密码
    private String email; // 邮箱
    private String name; // 原昵称字段(兼容)
    private String nickname; // 新昵称字段
    private String phone;
    private String idNumber;
    private String pic; // 头像(前端叫 avatar，复用此字段)
    private LocalDateTime createTime;

    // ===== 健康画像 =====
    private String sex; // 原性别字段(字符串)
    private Integer gender; // 性别: 1男 2女
    private Integer age; // 年龄
    private Double height; // 身高 cm
    private Double weight; // 体重 kg
    private Double activityFactor; // 活动系数(1.2/1.375/1.55/1.725) - 保留原字段
    private Integer healthGoal; // 健康目标: 1减脂 2增肌 3维持

    // ===== 后端计算字段 =====
    private Double bmi; // BMI 体质指数
    private Integer bmr; // BMR 基础代谢率
    private Integer tdee; // TDEE 总能量消耗

    // ===== 饮食偏好 =====
    // allergies 字段已移除，统一使用 avoidTags 存储忌口/过敏信息
    private String tasteTags; // 口味偏好 JSON ["辣","咸"]
    private String avoidTags; // 忌口/过敏 JSON ["花生","麸质"]
    private String nutritionPref; // 营养偏好: 高蛋白/低碳/生酮/均衡
    private BigDecimal mealBudget; // 每餐预算
}
