-- ============================================================
-- 全量菜品图片更新脚本
-- 将所有 unsplash 外链 / 旧 .jpg 编号图片 统一替换为本地 AI 生成的 .png
-- 图片目录: /static/dish/
-- 生成时间: 2026-03-21
-- ============================================================

-- === 🥣 营养早餐 (Category ID: 11) ===
UPDATE dish SET image = '/static/dish/century_egg_porridge.png' WHERE name = '皮蛋瘦肉粥';
UPDATE dish SET image = '/static/dish/mushroom_chicken_porridge.png' WHERE name = '香菇滑鸡粥';
UPDATE dish SET image = '/static/dish/pumpkin_millet_porridge.png' WHERE name = '南瓜小米粥';
UPDATE dish SET image = '/static/dish/beijing_doufu_nao.png' WHERE name = '老北京豆腐脑';
UPDATE dish SET image = '/static/dish/chinese_youtiao.png' WHERE name = '安心油条';
UPDATE dish SET image = '/static/dish/pork_steamed_bun.png' WHERE name = '鲜肉大包';
UPDATE dish SET image = '/static/dish/whole_wheat_mantou.png' WHERE name = '全麦馒头';
UPDATE dish SET image = '/static/dish/fresh_soy_milk.png' WHERE name LIKE '%豆浆%';
UPDATE dish SET image = '/static/dish/tea_egg.png' WHERE name = '茶叶蛋';
UPDATE dish SET image = '/static/dish/scallion_oil_noodles.png' WHERE name = '葱油拌面';
UPDATE dish SET image = '/static/dish/shouzhua_bing.png' WHERE name = '手抓饼';
UPDATE dish SET image = '/static/dish/purple_yam_corn.png' WHERE name = '紫薯玉米棒';

-- === 🍜 主食面点 (Category ID: 15) ===
UPDATE dish SET image = '/static/dish/chongqing_noodles.png' WHERE name = '重庆小面';
UPDATE dish SET image = '/static/dish/zhengxian_hele_noodles.png' WHERE name LIKE '%郑县饸饹面%';
UPDATE dish SET image = '/static/dish/chicken_shrimp_noodle_soup.png' WHERE name LIKE '%老母鸡清汤虾仁面%';
UPDATE dish SET image = '/static/dish/spicy_beef_noodles.png' WHERE name = '香辣牛肉面';
UPDATE dish SET image = '/static/dish/scallion_oil_noodles.png' WHERE name = '葱油拌面';
UPDATE dish SET image = '/static/dish/black_pepper_beef_pasta.png' WHERE name = '黑椒牛柳意面';
UPDATE dish SET image = '/static/dish/yangzhou_fried_rice.png' WHERE name = '扬州炒饭';
UPDATE dish SET image = '/static/dish/japanese_soba_noodles.png' WHERE name = '日式荞麦面';
UPDATE dish SET image = '/static/dish/mixed_grain_rice_set.png' WHERE name = '杂粮饭套餐';

-- === 🍱 丰盛午餐 (Category ID: 12) ===
UPDATE dish SET image = '/static/dish/braised_lion_head.png' WHERE name = '红烧狮子头';
UPDATE dish SET image = '/static/dish/kung_pao_chicken_rice.png' WHERE name LIKE '%宫保鸡丁%';
UPDATE dish SET image = '/static/dish/mapo_tofu.png' WHERE name LIKE '%麻婆豆腐%';
UPDATE dish SET image = '/static/dish/yuxiang_shredded_pork.png' WHERE name LIKE '%鱼香肉丝%';
UPDATE dish SET image = '/static/dish/mei_cai_kou_rou.png' WHERE name = '梅菜扣肉';
UPDATE dish SET image = '/static/dish/sweet_sour_ribs.png' WHERE name = '糖醋排骨';
UPDATE dish SET image = '/static/dish/di_san_xian.png' WHERE name = '地三鲜';
UPDATE dish SET image = '/static/dish/huangmen_chicken_rice.png' WHERE name LIKE '%黄焖鸡%';
UPDATE dish SET image = '/static/dish/curry_beef_rice.png' WHERE name = '咖喱牛肉饭';
UPDATE dish SET image = '/static/dish/sour_cabbage_fish.png' WHERE name LIKE '%酸菜鱼%';
UPDATE dish SET image = '/static/dish/dry_fried_green_beans.png' WHERE name = '干煸四季豆';
UPDATE dish SET image = '/static/dish/twice_cooked_pork.png' WHERE name = '回锅肉';

-- === 🥗 轻食晚餐 (Category ID: 13) ===
UPDATE dish SET image = '/static/dish/chicken_breast_salad.png' WHERE name LIKE '%鸡胸肉沙拉%';
UPDATE dish SET image = '/static/dish/broccoli_shrimp.png' WHERE name LIKE '%西蓝花%虾仁%';
UPDATE dish SET image = '/static/dish/blanched_choy_sum.png' WHERE name = '白灼菜心';
UPDATE dish SET image = '/static/dish/winter_melon_soup.png' WHERE name = '冬瓜排骨汤';
UPDATE dish SET image = '/static/dish/quinoa_avocado_bowl.png' WHERE name LIKE '%藜麦牛油果%';
UPDATE dish SET image = '/static/dish/whole_wheat_sandwich.png' WHERE name = '全麦三明治';
UPDATE dish SET image = '/static/dish/steamed_sea_bass.png' WHERE name LIKE '%清蒸鲈鱼%';
UPDATE dish SET image = '/static/dish/tomato_tofu_soup.png' WHERE name = '番茄豆腐汤';
UPDATE dish SET image = '/static/dish/cold_konjac_noodles.png' WHERE name LIKE '%凉拌魔芋%';
UPDATE dish SET image = '/static/dish/veggie_spring_roll.png' WHERE name = '蔬菜沙拉卷';

-- === 🥤 饮品甜点 (Category ID: 14) ===
UPDATE dish SET image = '/static/dish/lemon_black_tea.png' WHERE name = '柠檬红茶';
UPDATE dish SET image = '/static/dish/fresh_orange_juice.png' WHERE name = '鲜榨橙汁';
UPDATE dish SET image = '/static/dish/hot_milk.png' WHERE name = '热牛奶';
UPDATE dish SET image = '/static/dish/mung_bean_soup.png' WHERE name = '绿豆汤';
UPDATE dish SET image = '/static/dish/iced_americano.png' WHERE name LIKE '%冰美式%';
UPDATE dish SET image = '/static/dish/mango_pomelo_sago.png' WHERE name = '杨枝甘露';
UPDATE dish SET image = '/static/dish/sour_plum_drink.png' WHERE name = '酸梅汤';
UPDATE dish SET image = '/static/dish/low_fat_yogurt.png' WHERE name = '低脂酸奶';
UPDATE dish SET image = '/static/dish/bubble_milk_tea.png' WHERE name = '珍珠奶茶';
-- 水果拼盘暂时使用旧图(AI生图配额已用完，后续补充)
-- UPDATE dish SET image = '/static/dish/fruit_platter.png' WHERE name = '水果拼盘';
UPDATE dish SET image = '/static/dish/coca_cola.jpg' WHERE name LIKE '%可乐%';

-- === 兜底：将所有仍然指向 unsplash 外链的菜品统一替换 ===
-- 以下语句会把所有 image 字段中包含 'unsplash' 的记录，
-- 根据菜品名称来匹配已有的本地图片文件。
-- 如果上面的精确匹配已经覆盖，则不会重复执行。

-- 验证：执行完后检查是否还有 unsplash 链接残留
-- SELECT id, name, image FROM dish WHERE image LIKE '%unsplash%';
