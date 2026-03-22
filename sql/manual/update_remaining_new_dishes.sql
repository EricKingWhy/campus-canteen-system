-- ============================================================
-- 更新新增的 13 道菜品(第二批) & 之前遗留的 1 道水果拼盘
-- 目录: /static/dish/
-- ============================================================

UPDATE dish SET image = '/static/dish/purple_yam_bun.png' WHERE name = '全麦紫薯芋泥包';
UPDATE dish SET image = '/static/dish/tomato_beef_brisket_rice.png' WHERE name = '经典番茄牛腩盖饭';
UPDATE dish SET image = '/static/dish/sizzling_black_pepper_chicken_rice.png' WHERE name = '铁板黑椒鸡扒饭';
UPDATE dish SET image = '/static/dish/golden_broth_sour_cabbage_fish_rice.png' WHERE name = '金汤酸菜鱼片饭';
UPDATE dish SET image = '/static/dish/curry_potato_beef_stew.png' WHERE name = '咖喱土豆炖牛肉';
UPDATE dish SET image = '/static/dish/osmanthus_fermented_rice_tangyuan.png' WHERE name = '桂花酒酿小圆子';
UPDATE dish SET image = '/static/dish/matcha_red_bean_mochi.png' WHERE name = '抹茶蜜豆大福';
UPDATE dish SET image = '/static/dish/taro_paste_meat_floss_mochi.png' WHERE name = '芋泥肉松麻薯';
UPDATE dish SET image = '/static/dish/coconut_mango_pudding.png' WHERE name = '椰香芒果布丁';
UPDATE dish SET image = '/static/dish/caramel_creme_brulee.png' WHERE name = '焦糖烤脆皮布蕾';
UPDATE dish SET image = '/static/dish/black_sesame_tangyuan.png' WHERE name = '黑芝麻糊汤圆(干捞)';
UPDATE dish SET image = '/static/dish/super_pork_trotter_rice.png' WHERE name = '超级猪脚饭';
UPDATE dish SET image = '/static/dish/super_duck_leg_rice.png' WHERE name = '超级鸭腿饭';

-- ============ 之前遗留配额不足的 1 道老菜品 ============
UPDATE dish SET image = '/static/dish/fruit_platter.png' WHERE name = '水果拼盘';
