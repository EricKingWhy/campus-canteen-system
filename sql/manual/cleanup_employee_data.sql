-- ==========================================
-- 员工表数据清理脚本 (Employee Cleanup Script)
-- 执行时机: 重启后端前执行此脚本
-- ==========================================

-- 1. 删除负数ID的脏数据 (由Integer溢出导致)
DELETE FROM employee WHERE id = -1729273854;

-- 2. 删除所有负数ID的员工 (以防万一还有其他)
DELETE FROM employee WHERE id < 0;

-- 3. 验证清理结果
SELECT id, name, username, status FROM employee ORDER BY id;

-- 4. (可选) 如果ID列仍是INT, 可升级为BIGINT以支持Snowflake ID
-- ALTER TABLE employee MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键';

-- ==========================================
-- 执行说明:
-- 在 MySQL Workbench 或命令行中执行:
-- mysql -u root -p smart_canteen < cleanup_employee_data.sql
-- ==========================================
