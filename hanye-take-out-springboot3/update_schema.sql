-- ================================
-- 智慧食堂 - 用户认证字段升级 SQL
-- 执行此脚本前，请备份数据库！
-- ================================

-- 1. 添加用户名、密码、邮箱字段
ALTER TABLE `user` ADD COLUMN `username` VARCHAR(50) NULL AFTER `openid`;
ALTER TABLE `user` ADD COLUMN `password` VARCHAR(100) NULL AFTER `username`;
ALTER TABLE `user` ADD COLUMN `email` VARCHAR(100) NULL AFTER `password`;

-- 2. 添加用户名唯一索引 (用户名不能重复)
ALTER TABLE `user` ADD UNIQUE INDEX `idx_username` (`username`);

-- 3. (可选) 如果需要测试数据，可以执行以下语句
-- INSERT INTO `user` (`username`, `password`, `name`, `create_time`) VALUES ('test', '123456', '测试用户', NOW());
-- INSERT INTO `user` (`username`, `password`, `name`, `create_time`) VALUES ('admin', 'admin123', '管理员', NOW());

-- 执行完成后，请重启后端服务！
