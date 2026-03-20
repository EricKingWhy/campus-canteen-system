-- ============================================================
-- 全部菜品图片更新 (本地AI生成)
-- 分两批执行：第一批已生成，第二批待配额恢复后继续
-- ============================================================

-- ========== 🍜 主食面点 (ID: 15) — 已完成 ==========
UPDATE dish SET image = '/static/dish/chongqing_noodles.png' WHERE name LIKE '%重庆小面%';
UPDATE dish SET image = '/static/dish/zhengxian_hele_noodles.png' WHERE name LIKE '%饸饹面%';
UPDATE dish SET image = '/static/dish/chicken_shrimp_noodle_soup.png' WHERE name LIKE '%老母鸡%虾仁面%';
UPDATE dish SET image = '/static/dish/spicy_beef_noodles.png' WHERE name LIKE '%香辣牛肉面%';
UPDATE dish SET image = '/static/dish/scallion_oil_noodles.png' WHERE name LIKE '%葱油拌面%';
UPDATE dish SET image = '/static/dish/black_pepper_beef_pasta.png' WHERE name LIKE '%黑椒牛柳意面%';
UPDATE dish SET image = '/static/dish/yangzhou_fried_rice.png' WHERE name LIKE '%扬州炒饭%';
UPDATE dish SET image = '/static/dish/japanese_soba_noodles.png' WHERE name LIKE '%荞麦面%';
UPDATE dish SET image = '/static/dish/mixed_grain_rice_set.png' WHERE name LIKE '%杂粮饭%';

-- ========== 🥣 营养早餐 (ID: 11) — 已完成 8/11 ==========
UPDATE dish SET image = '/static/dish/century_egg_porridge.png' WHERE name LIKE '%皮蛋瘦肉粥%';
UPDATE dish SET image = '/static/dish/mushroom_chicken_porridge.png' WHERE name LIKE '%香菇滑鸡粥%';
UPDATE dish SET image = '/static/dish/pumpkin_millet_porridge.png' WHERE name LIKE '%南瓜小米粥%';
UPDATE dish SET image = '/static/dish/beijing_doufu_nao.png' WHERE name LIKE '%豆腐脑%';
UPDATE dish SET image = '/static/dish/chinese_youtiao.png' WHERE name LIKE '%油条%';
UPDATE dish SET image = '/static/dish/pork_steamed_bun.png' WHERE name LIKE '%鲜肉大包%';
UPDATE dish SET image = '/static/dish/whole_wheat_mantou.png' WHERE name LIKE '%全麦馒头%';
UPDATE dish SET image = '/static/dish/fresh_soy_milk.png' WHERE name LIKE '%豆浆%';

-- ========== ⏳ 以下待第二批生成后补充 ==========
-- 茶叶蛋、手抓饼、紫薯玉米棒
-- 丰盛午餐(12道)、轻食晚餐(10道)、饮品甜点(11道)

-- 验证
SELECT id, name, image FROM dish WHERE image LIKE '/static/dish/%' ORDER BY category_id, id;

-- 营养早餐（剩余）
UPDATE dish SET image = '/static/dish/tea_egg.png' WHERE name LIKE '%茶叶蛋%';
UPDATE dish SET image = '/static/dish/shouzhua_bing.png' WHERE name LIKE '%手抓饼%';
UPDATE dish SET image = '/static/dish/purple_yam_corn.png' WHERE name LIKE '%紫薯玉米棒%';

-- 丰盛午餐
UPDATE dish SET image = '/static/dish/braised_lion_head.png' WHERE name LIKE '%红烧狮子头%';
UPDATE dish SET image = '/static/dish/kung_pao_chicken_rice.png' WHERE name LIKE '%宫保鸡丁饭%';
UPDATE dish SET image = '/static/dish/mapo_tofu.png' WHERE name LIKE '%麻婆豆腐%';
UPDATE dish SET image = '/static/dish/yuxiang_shredded_pork.png' WHERE name LIKE '%鱼香肉丝%';
UPDATE dish SET image = '/static/dish/mei_cai_kou_rou.png' WHERE name LIKE '%梅菜扣肉%';
UPDATE dish SET image = '/static/dish/sweet_sour_ribs.png' WHERE name LIKE '%糖醋排骨%';
UPDATE dish SET image = '/static/dish/di_san_xian.png' WHERE name LIKE '%地三鲜%';
UPDATE dish SET image = '/static/dish/huangmen_chicken_rice.png' WHERE name LIKE '%黄焖鸡%';
UPDATE dish SET image = '/static/dish/curry_beef_rice.png' WHERE name LIKE '%咖喱牛肉饭%';
UPDATE dish SET image = '/static/dish/sour_cabbage_fish.png' WHERE name LIKE '%酸菜鱼%';
UPDATE dish SET image = '/static/dish/dry_fried_green_beans.png' WHERE name LIKE '%干煸四季豆%';
UPDATE dish SET image = '/static/dish/twice_cooked_pork.png' WHERE name LIKE '%回锅肉%';

-- 轻食晚餐 (Partial)
UPDATE dish SET image = '/static/dish/chicken_breast_salad.png' WHERE name LIKE '%鸡胸肉沙拉%';
UPDATE dish SET image = '/static/dish/broccoli_shrimp.png' WHERE name LIKE '%西蓝花%虾仁%';

