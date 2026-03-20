-- ============================================================
-- 清除旧编号 .jpg 图片的菜品记录
-- 这些菜品引用的是不存在的 /static/dish/52.jpg ~ 71.jpg 文件
-- 执行前请确认这些旧菜品不再需要
-- ============================================================

-- 先查看将要删除的菜品（预览，不执行删除）
-- SELECT id, name, image, description FROM dish WHERE image REGEXP '/static/dish/[0-9]+\\.jpg';

-- 删除所有引用编号 .jpg 图片的旧菜品
DELETE FROM dish WHERE image REGEXP '/static/dish/[0-9]+\\.jpg';

-- 同时清理关联的口味数据（如果有的话）
DELETE FROM dish_flavor WHERE dish_id NOT IN (SELECT id FROM dish);
