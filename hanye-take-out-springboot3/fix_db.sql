-- ================================
-- 智慧食堂 - 完整数据库修复补丁 v3
-- 修复 dish 表缺失的字段
-- 
-- 【重要】执行时如果字段已存在会报错，忽略即可！
-- ================================

-- 1. 修复 dish 表
-- 【核心修复】添加 image 字段（最关键的）
ALTER TABLE dish ADD COLUMN image VARCHAR(255) COMMENT '菜品图片路径';

-- 注意：如果上面的命令报 "Duplicate column name 'image'" 错误，说明字段已存在，可继续执行

ALTER TABLE dish ADD COLUMN description VARCHAR(500) COMMENT '菜品描述';
ALTER TABLE dish ADD COLUMN sort INT DEFAULT 0 COMMENT '排序值';
ALTER TABLE dish ADD COLUMN create_time DATETIME COMMENT '创建时间';
ALTER TABLE dish ADD COLUMN update_time DATETIME COMMENT '更新时间';
ALTER TABLE dish ADD COLUMN create_user BIGINT COMMENT '创建人';
ALTER TABLE dish ADD COLUMN update_user BIGINT COMMENT '更新人';
ALTER TABLE dish ADD COLUMN calories DOUBLE COMMENT '卡路里';
ALTER TABLE dish ADD COLUMN protein DOUBLE COMMENT '蛋白质';
ALTER TABLE dish ADD COLUMN fat DOUBLE COMMENT '脂肪';
ALTER TABLE dish ADD COLUMN carbohydrates DOUBLE COMMENT '碳水化合物';

-- 2. 修复 user 表
ALTER TABLE user MODIFY COLUMN openid VARCHAR(45) NULL DEFAULT NULL;
ALTER TABLE user ADD COLUMN username VARCHAR(50) COMMENT '用户名';
ALTER TABLE user ADD COLUMN password VARCHAR(64) COMMENT '密码';
ALTER TABLE user ADD COLUMN email VARCHAR(100) COMMENT '邮箱';

-- 3. 验证
SHOW COLUMNS FROM dish;
