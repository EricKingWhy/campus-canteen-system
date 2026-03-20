-- ================================================================
-- 快速修复脚本：添加 allergies 列（一行搞定）
-- 直接复制到 Navicat/MySQL Workbench 执行
-- ================================================================
USE smart_canteen;

-- 添加 allergies 字段（幂等）
ALTER TABLE user ADD COLUMN IF NOT EXISTS allergies VARCHAR(500) NULL COMMENT '过敏/忌口(兼容旧字段)';

-- 验证
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'smart_canteen' AND TABLE_NAME = 'user' AND COLUMN_NAME = 'allergies';
