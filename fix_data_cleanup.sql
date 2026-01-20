-- ============================================================
-- 数据库修复与清洗脚本 (Database Cleanup & Fix)
-- ============================================================

-- 1. 先停用所有分类 (Soft Delete)
UPDATE category SET status = 0;

-- 2. 启用/插入 5 大核心分类 (确保 ID 11-15 存在且启用)
-- 使用 INSERT ON DUPLICATE KEY UPDATE 确保无论是否存在都能正确启用
INSERT INTO category (id, type, name, sort, status, create_time, update_time, create_user, update_user) VALUES 
(11, 1, '早餐', 1, 1, NOW(), NOW(), 1, 1),
(12, 1, '午餐', 2, 1, NOW(), NOW(), 1, 1),
(13, 1, '晚餐', 3, 1, NOW(), NOW(), 1, 1),
(14, 1, '饮品', 4, 1, NOW(), NOW(), 1, 1),
(15, 1, '主食面点', 5, 1, NOW(), NOW(), 1, 1)
ON DUPLICATE KEY UPDATE 
    status = 1, 
    name = VALUES(name), 
    sort = VALUES(sort),
    update_time = NOW();

-- 3. 修复没有图片的老菜品 (Fix Missing Images)
-- 将没有图片的菜品统一设置为一张通用的美食图
UPDATE dish 
SET pic = 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80' 
WHERE pic IS NULL OR pic = '' OR pic NOT LIKE 'http%';

-- 4. 修复孤儿菜品 (Fix Orphaned Dishes)
-- 将属于已停用分类的菜品，移动到 '午餐' (12) 或 '主食面点' (15)
-- 这里为了通过 SQL 简单实现随机，我们统一移动到 '午餐' (12)，
-- 或者可以使用 CASE WHEN RAND() > 0.5 THEN 12 ELSE 15 END 来随机分配 (MySQL支持)
UPDATE dish 
SET category_id = CASE WHEN RAND() > 0.5 THEN 12 ELSE 15 END,
    update_time = NOW()
WHERE category_id NOT IN (11, 12, 13, 14, 15) AND status = 1;

-- 5. 再次确认所有启用菜品的分类状态也都已启用
UPDATE dish d
JOIN category c ON d.category_id = c.id
SET d.status = 1
WHERE c.status = 1;
