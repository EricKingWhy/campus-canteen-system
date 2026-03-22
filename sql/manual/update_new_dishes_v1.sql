-- ============================================================
-- 更新第一批新增菜品的 AI 图片 (每个菜品生成了 v1 和 v2 供使用)
-- 目录: /static/dish/
-- 生成时间: 2026-03-22
-- ============================================================

-- 默认选取 v1 作为主图，如需更换可手动改为 v2.png
UPDATE dish SET image = '/static/dish/shrimp_quinoa_bowl_v1.png' WHERE name = '鲜虾藜麦轻食碗';
UPDATE dish SET image = '/static/dish/pan_fried_black_pepper_chicken_v1.png' WHERE name = '慢煎黑椒鸡胸肉';
UPDATE dish SET image = '/static/dish/broccoli_beef_slices_v1.png' WHERE name = '白灼西蓝花牛肉片';
UPDATE dish SET image = '/static/dish/stir_fried_konjac_noodles_v1.png' WHERE name = '清炒时蔬魔芋丝';
UPDATE dish SET image = '/static/dish/tuna_sandwich_v1.png' WHERE name = '金枪鱼全麦三明治';
UPDATE dish SET image = '/static/dish/garlic_steamed_chicken_v1.png' WHERE name = '蒜蓉蒸无骨鸡腿肉';
UPDATE dish SET image = '/static/dish/mushroom_chicken_brown_rice_porridge_v1.png' WHERE name = '香菇滑鸡糙米粥';
UPDATE dish SET image = '/static/dish/spinach_tamagoyaki_v1.png' WHERE name = '田园菠菜厚蛋烧';
UPDATE dish SET image = '/static/dish/pork_wonton_v1.png' WHERE name = '鲜肉小馄饨(清汤)';
