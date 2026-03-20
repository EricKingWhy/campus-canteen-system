-- ============================================================
-- 智能归类修复脚本 (Smart Classification Repair)
-- ============================================================

-- 1. 确保 5 大核心分类存在且启用 (ID 11-15)
-- 使用 ON DUPLICATE KEY UPDATE 确保无论是否存在都能正确启用
INSERT INTO category (id, type, name, sort, status, create_time, update_time, create_user, update_user) VALUES 
(11, 1, '早餐', 1, 1, NOW(), NOW(), 1, 1),
(12, 1, '午餐', 2, 1, NOW(), NOW(), 1, 1),
(13, 1, '晚餐', 3, 1, NOW(), NOW(), 1, 1),
(14, 1, '饮品', 4, 1, NOW(), NOW(), 1, 1),
(15, 1, '主食面点', 5, 1, NOW(), NOW(), 1, 1)
ON DUPLICATE KEY UPDATE 
    name = VALUES(name), 
    status = 1,
    update_time = NOW();

-- 2. 图片大修复 (Image Fix) - 给老菜品穿衣服
-- 将 pic 为空或不合法的记录，统一修复为这张 appetizing 的默认图
UPDATE dish 
SET pic = 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80' 
WHERE pic IS NULL OR pic = '' OR pic NOT LIKE 'http%';

-- 3. 智能归类逻辑 (按优先级执行)
-- A. 先全部重置为 '12 (午餐)' (作为兜底)
UPDATE dish SET category_id = 12;

-- B. 早餐识别 (名称含: 粥、蛋、包、油条、豆浆、饼...)
UPDATE dish SET category_id = 11 
WHERE name LIKE '%粥%' 
   OR name LIKE '%蛋%' 
   OR name LIKE '%包%' 
   OR name LIKE '%油条%' 
   OR name LIKE '%豆浆%' 
   OR name LIKE '%饼%';

-- C. 饮品识别 (名称含: 茶、汁、饮、咖啡、可乐、水...)
UPDATE dish SET category_id = 14 
WHERE name LIKE '%茶%' 
   OR name LIKE '%汁%' 
   OR name LIKE '%饮%' 
   OR name LIKE '%咖啡%' 
   OR name LIKE '%可乐%' 
   OR name LIKE '%水%'
   OR name LIKE '%奶%';

-- D. 主食面点识别 (名称含: 面、粉、馒头、饺) -> 您的牛肉面会去这里
UPDATE dish SET category_id = 15 
WHERE name LIKE '%面%' 
   OR name LIKE '%粉%' 
   OR name LIKE '%馒头%' 
   OR name LIKE '%饺%';

-- E. 轻食晚餐识别 (名称含: 沙拉、轻食) -> 优先级较高，最后执行覆盖
UPDATE dish SET category_id = 13 
WHERE name LIKE '%沙拉%' 
   OR name LIKE '%轻食%';

-- 4. 清理无效分类
-- 将除了这 5 个核心分类之外的其他分类全部停用 (status = 0)
UPDATE category SET status = 0 WHERE id NOT IN (11, 12, 13, 14, 15);

-- 5. 最终确认：启用所有归属于核心分类的菜品
UPDATE dish SET status = 1 WHERE category_id IN (11, 12, 13, 14, 15);
