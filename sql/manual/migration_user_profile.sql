-- ================================================================
-- 用户画像字段迁移脚本 (User Profile Migration Script)
-- 目标：扩展 user 表以支持健康画像和饮食偏好
-- 原则：向后兼容、幂等执行、所有新增字段允许 NULL 或有默认值
-- ================================================================

USE smart_canteen;

-- ----------------------------------------------------------------
-- 1. 健康目标 (health_goal)
-- ----------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'smart_canteen' 
    AND TABLE_NAME = 'user' 
    AND COLUMN_NAME = 'health_goal'
);
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE user ADD COLUMN health_goal TINYINT NULL COMMENT ''健康目标: 1减脂 2增肌 3维持''',
    'SELECT ''health_goal already exists'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------------------------------------------
-- 2. BMI (后端计算)
-- ----------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'smart_canteen' 
    AND TABLE_NAME = 'user' 
    AND COLUMN_NAME = 'bmi'
);
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE user ADD COLUMN bmi DOUBLE NULL COMMENT ''BMI体质指数(后端计算)''',
    'SELECT ''bmi already exists'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------------------------------------------
-- 3. BMR (基础代谢)
-- ----------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'smart_canteen' 
    AND TABLE_NAME = 'user' 
    AND COLUMN_NAME = 'bmr'
);
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE user ADD COLUMN bmr INT NULL COMMENT ''基础代谢率(后端计算)''',
    'SELECT ''bmr already exists'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------------------------------------------
-- 4. TDEE (总能量消耗)
-- ----------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'smart_canteen' 
    AND TABLE_NAME = 'user' 
    AND COLUMN_NAME = 'tdee'
);
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE user ADD COLUMN tdee INT NULL COMMENT ''总能量消耗(后端计算)''',
    'SELECT ''tdee already exists'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------------------------------------------
-- 5. 口味偏好 (taste_tags) - JSON 数组字符串
-- ----------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'smart_canteen' 
    AND TABLE_NAME = 'user' 
    AND COLUMN_NAME = 'taste_tags'
);
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE user ADD COLUMN taste_tags VARCHAR(500) NULL COMMENT ''口味偏好JSON数组''',
    'SELECT ''taste_tags already exists'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------------------------------------------
-- 5.5 过敏/忌口兼容字段 (allergies) - 解决 Unknown column 问题
-- ----------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'smart_canteen' 
    AND TABLE_NAME = 'user' 
    AND COLUMN_NAME = 'allergies'
);
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE user ADD COLUMN allergies VARCHAR(500) NULL COMMENT ''过敏/忌口(兼容旧字段)''',
    'SELECT ''allergies already exists'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------------------------------------------
-- 6. 忌口/过敏 (avoid_tags) - 与 allergies 并存
-- ----------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'smart_canteen' 
    AND TABLE_NAME = 'user' 
    AND COLUMN_NAME = 'avoid_tags'
);
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE user ADD COLUMN avoid_tags VARCHAR(500) NULL COMMENT ''忌口/过敏JSON数组''',
    'SELECT ''avoid_tags already exists'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------------------------------------------
-- 7. 营养偏好 (nutrition_pref)
-- ----------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'smart_canteen' 
    AND TABLE_NAME = 'user' 
    AND COLUMN_NAME = 'nutrition_pref'
);
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE user ADD COLUMN nutrition_pref VARCHAR(200) NULL COMMENT ''营养偏好''',
    'SELECT ''nutrition_pref already exists'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------------------------------------------
-- 8. 每餐预算 (meal_budget)
-- ----------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'smart_canteen' 
    AND TABLE_NAME = 'user' 
    AND COLUMN_NAME = 'meal_budget'
);
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE user ADD COLUMN meal_budget DECIMAL(10,2) NOT NULL DEFAULT 15.00 COMMENT ''每餐预算''',
    'SELECT ''meal_budget already exists'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------------------------------------------
-- 9. 昵称 (nickname)
-- ----------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'smart_canteen' 
    AND TABLE_NAME = 'user' 
    AND COLUMN_NAME = 'nickname'
);
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE user ADD COLUMN nickname VARCHAR(100) NULL COMMENT ''昵称''',
    'SELECT ''nickname already exists'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ================================================================
-- 冒烟测试：验证更新不会触发 NOT NULL 约束
-- ================================================================
-- 注意：我们只更新画像字段，不需要触发 openid/id_number 的插入约束
-- SELECT id, gender, age, height, weight, activity_factor, health_goal, bmi, bmr, tdee FROM user LIMIT 1;

-- ================================================================
-- 验证表结构
-- ================================================================
DESC user;
