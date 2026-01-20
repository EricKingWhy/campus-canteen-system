-- 1. 删除旧表
DROP TABLE IF EXISTS employee;

-- 2. 重建表结构 (包含 username 字段)
CREATE TABLE `employee` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(32) NOT NULL,
  `username` varchar(32) NOT NULL COMMENT '关键字段',
  `password` varchar(64) NOT NULL,
  `phone` varchar(11) NOT NULL,
  `sex` varchar(2) NOT NULL,
  `id_number` varchar(18) NOT NULL,
  `status` int NOT NULL DEFAULT '1',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `create_user` bigint DEFAULT NULL,
  `update_user` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 插入管理员账号 (admin / 123456)
INSERT INTO employee (id, name, username, password, phone, sex, id_number, status) VALUES 
(1, '管理员', 'admin', 'e10adc3949ba59abbe56e057f20f883e', '13812345678', '1', '110101199001010001', 1);

SELECT 'SUCCESS: Employee table fixed' as msg;
