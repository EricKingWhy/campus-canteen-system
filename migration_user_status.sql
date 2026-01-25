-- ============================================================
-- 用户管理模块迁移脚本 - 幂等执行
-- 2026-01-25
-- ============================================================

-- 1. 为 user 表添加 status 字段（如不存在）
-- MySQL 不支持 IF NOT EXISTS 语法，使用存储过程检查
DELIMITER //
CREATE PROCEDURE add_user_status_if_not_exists()
BEGIN
    DECLARE col_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO col_exists 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'user' 
      AND COLUMN_NAME = 'status';
    
    IF col_exists = 0 THEN
        ALTER TABLE user ADD COLUMN status TINYINT DEFAULT 1 COMMENT '状态: 1启用 0禁用';
    END IF;
END //
DELIMITER ;

CALL add_user_status_if_not_exists();
DROP PROCEDURE IF EXISTS add_user_status_if_not_exists;

-- 2. 确保所有现有用户状态为启用
UPDATE user SET status = 1 WHERE status IS NULL;
