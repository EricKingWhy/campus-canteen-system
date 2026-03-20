-- 1. 确保至少有一个“热销推荐”分类 (Type=1)
INSERT IGNORE INTO category (id, type, name, sort, status, create_time, update_time, create_user, update_user) 
VALUES (1001, 1, '热销推荐', 1, 1, NOW(), NOW(), 1, 1);

-- 2. 确保至少有一道菜品 (关联到上述分类)
INSERT IGNORE INTO dish (id, name, category_id, price, image, description, status, create_time, update_time, create_user, update_user)
VALUES (2001, '招牌红烧肉', 1001, 38.00, 'https://replicate.delivery/pbxt/J1Yq5X8Xj5X8Xj5X8Xj5X8Xj5X8Xj5X8/out-0.png', '肥而不腻，入口即化', 1, NOW(), NOW(), 1, 1);

-- 3. 强制启用所有数据 (再次确认)
UPDATE category SET status = 1;
UPDATE dish SET status = 1;

SELECT 'SUCCESS: Data seeded and enabled' as msg;
