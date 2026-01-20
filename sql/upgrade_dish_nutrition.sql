-- =====================================================
-- 阶段一：菜品详情字段升级
-- 用途：为菜品表添加营养成分和销量字段
-- 执行方式：在 MySQL 中运行此脚本
-- =====================================================

USE hanye_take_out;

-- 1. 添加销量字段 (如果不存在)
ALTER TABLE dish ADD COLUMN IF NOT EXISTS sold INT DEFAULT 400 COMMENT '销量';

-- 2. 确保营养成分字段存在 (calories, protein, fat, carbohydrates 已在之前添加)
-- 如果列不存在则添加，如果存在则忽略
-- MySQL 8.0+ 支持 IF NOT EXISTS

-- 备用方案：逐个检查添加 (用于兼容性)
-- 检查并添加 calories
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'hanye_take_out' AND TABLE_NAME = 'dish' AND COLUMN_NAME = 'calories');
SET @sqlstmt := IF(@exist = 0, 'ALTER TABLE dish ADD COLUMN calories INT DEFAULT NULL COMMENT ''热量(kcal)''', 'SELECT ''Column calories exists''');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 protein
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'hanye_take_out' AND TABLE_NAME = 'dish' AND COLUMN_NAME = 'protein');
SET @sqlstmt := IF(@exist = 0, 'ALTER TABLE dish ADD COLUMN protein INT DEFAULT NULL COMMENT ''蛋白质(g)''', 'SELECT ''Column protein exists''');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 fat
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'hanye_take_out' AND TABLE_NAME = 'dish' AND COLUMN_NAME = 'fat');
SET @sqlstmt := IF(@exist = 0, 'ALTER TABLE dish ADD COLUMN fat INT DEFAULT NULL COMMENT ''脂肪(g)''', 'SELECT ''Column fat exists''');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 carbohydrates (carbs)
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'hanye_take_out' AND TABLE_NAME = 'dish' AND COLUMN_NAME = 'carbohydrates');
SET @sqlstmt := IF(@exist = 0, 'ALTER TABLE dish ADD COLUMN carbohydrates INT DEFAULT NULL COMMENT ''碳水化合物(g)''', 'SELECT ''Column carbohydrates exists''');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 sold
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'hanye_take_out' AND TABLE_NAME = 'dish' AND COLUMN_NAME = 'sold');
SET @sqlstmt := IF(@exist = 0, 'ALTER TABLE dish ADD COLUMN sold INT DEFAULT 400 COMMENT ''月销量''', 'SELECT ''Column sold exists''');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 验证表结构
DESCRIBE dish;

-- 4. 将所有现有菜品的 sold 初始化为 400 (如果为 NULL)
UPDATE dish SET sold = 400 WHERE sold IS NULL;

SELECT '✅ 数据库架构升级完成！' AS result;
