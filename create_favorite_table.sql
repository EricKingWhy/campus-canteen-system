-- 创建收藏表
CREATE TABLE IF NOT EXISTS `favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `dish_id` bigint NOT NULL COMMENT '菜品ID',
  `create_time` datetime DEFAULT NULL COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_dish` (`user_id`, `dish_id`) COMMENT '防止重复收藏'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品收藏表';
