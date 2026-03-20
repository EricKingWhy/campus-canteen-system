-- ============================================================
-- 最终全量重置已修复脚本 (Reset & Import Final)
-- ============================================================

-- 1. 关闭外键检查 (防止删除报错)
SET foreign_key_checks = 0;

-- 2. 【危险操作】清空旧数据 (彻底删除老菜品、老分类、老收藏)
TRUNCATE TABLE dish;
TRUNCATE TABLE category;
TRUNCATE TABLE setmeal; -- 清空套餐，防止关联报错
TRUNCATE TABLE favorite; -- 清空收藏，防止ID错乱

-- 3. 重建 5 大核心分类 (ID固定)
INSERT INTO category (id, type, name, sort, status, create_time, update_time, create_user, update_user) VALUES 
(11, 1, '营养早餐', 1, 1, NOW(), NOW(), 1, 1),
(12, 1, '丰盛午餐', 2, 1, NOW(), NOW(), 1, 1),
(13, 1, '轻食晚餐', 3, 1, NOW(), NOW(), 1, 1),
(14, 1, '饮品甜点', 4, 1, NOW(), NOW(), 1, 1),
(15, 1, '主食面点', 5, 1, NOW(), NOW(), 1, 1);

-- 4. 重新插入 50 道新菜品 (注意字段：pic, detail, category_id)
-- 自动修复：已移除 Markdown 格式链接，保留纯 URL
INSERT INTO dish (name, price, pic, detail, status, category_id, calories, protein, create_time, update_time, create_user, update_user) VALUES

-- === 🥣 早餐 (ID: 11) ===
('皮蛋瘦肉粥', 6.00, 'https://images.unsplash.com/photo-1511690656952-34342d5c2899?w=400&q=80', '优质松花蛋配瘦肉', 1, 11, 220, 12, NOW(), NOW(), 1, 1),
('香菇滑鸡粥', 8.00, 'https://images.unsplash.com/photo-1596797038530-2c107229654b?w=400&q=80', '鲜嫩鸡肉，营养滋补', 1, 11, 280, 18, NOW(), NOW(), 1, 1),
('南瓜小米粥', 4.00, 'https://images.unsplash.com/photo-1478145046317-39f10e56b5e9?w=400&q=80', '金黄软糯，养胃佳品', 1, 11, 180, 5, NOW(), NOW(), 1, 1),
('老北京豆腐脑', 4.50, 'https://images.unsplash.com/photo-1626806812497-59a415053040?w=400&q=80', '咸香卤汁，入口即化', 1, 11, 150, 8, NOW(), NOW(), 1, 1),
('安心油条', 2.00, 'https://images.unsplash.com/photo-1626202266838-51f789574462?w=400&q=80', '无矾工艺，金黄酥脆', 1, 11, 350, 4, NOW(), NOW(), 1, 1),
('鲜肉大包', 3.00, 'https://images.unsplash.com/photo-1605332766028-1111005e8357?w=400&q=80', '皮薄馅大，汁水丰盈', 1, 11, 280, 10, NOW(), NOW(), 1, 1),
('全麦馒头', 1.50, 'https://images.unsplash.com/photo-1621255765793-6b7315570051?w=400&q=80', '粗粮制作，低糖健康', 1, 11, 180, 5, NOW(), NOW(), 1, 1),
('豆浆(无糖)', 2.50, 'https://images.unsplash.com/photo-1600093463592-8e36ae95ef56?w=400&q=80', '现磨豆浆，富含植物蛋白', 1, 11, 80, 6, NOW(), NOW(), 1, 1),
('茶叶蛋', 1.50, 'https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=400&q=80', '入味五香，补充蛋白质', 1, 11, 70, 7, NOW(), NOW(), 1, 1),
('手抓饼', 7.00, 'https://images.unsplash.com/photo-1627308595229-7830a5c91f9f?w=400&q=80', '层层酥脆，搭配生菜', 1, 11, 420, 6, NOW(), NOW(), 1, 1),
('紫薯玉米棒', 5.00, 'https://images.unsplash.com/photo-1551754655-cd27e38d2076?w=400&q=80', '粗粮膳食纤维，减脂早餐', 1, 11, 160, 4, NOW(), NOW(), 1, 1),

-- === 🍜 主食面点 (ID: 15) ===
('香辣牛肉面', 16.00, 'https://images.unsplash.com/photo-1552611052-33e04de081de?w=400&q=80', '大块牛肉，汤浓面劲', 1, 15, 650, 30, NOW(), NOW(), 1, 1),
('葱油拌面', 9.00, 'https://images.unsplash.com/photo-1617320436940-0697a48858e7?w=400&q=80', '上海风味，葱香浓郁', 1, 15, 450, 8, NOW(), NOW(), 1, 1),
('黑椒牛柳意面', 22.00, 'https://images.unsplash.com/photo-1612929633738-8fe44f7ec841?w=400&q=80', '西式风味，黑椒浓郁', 1, 15, 600, 28, NOW(), NOW(), 1, 1),
('扬州炒饭', 13.00, 'https://images.unsplash.com/photo-1603133872878-684f10842796?w=400&q=80', '粒粒分明，配料丰富', 1, 15, 550, 12, NOW(), NOW(), 1, 1),
('日式荞麦面', 16.00, 'https://images.unsplash.com/photo-1552611052-05bc1230d32e?w=400&q=80', '低GI主食，凉爽开胃', 1, 15, 300, 10, NOW(), NOW(), 1, 1),
('杂粮饭套餐', 15.00, 'https://images.unsplash.com/photo-1617654228969-a864d2d46e3d?w=400&q=80', '富含膳食纤维', 1, 15, 400, 10, NOW(), NOW(), 1, 1),

-- === 🍱 午餐 (ID: 12) ===
('红烧狮子头', 18.00, 'https://images.unsplash.com/photo-1563245372-f21724e3856d?w=400&q=80', '浓油赤酱，肥而不腻', 1, 12, 720, 25, NOW(), NOW(), 1, 1),
('宫保鸡丁饭', 15.00, 'https://images.unsplash.com/photo-1525351484163-7529414395d8?w=400&q=80', '酸甜微辣，下饭神器', 1, 12, 580, 22, NOW(), NOW(), 1, 1),
('麻婆豆腐', 12.00, 'https://images.unsplash.com/photo-1564834724105-918b73d1b9e0?w=400&q=80', '川味经典，麻辣鲜香', 1, 12, 450, 15, NOW(), NOW(), 1, 1),
('鱼香肉丝', 14.00, 'https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400&q=80', '经典川菜，酸甜开胃', 1, 12, 550, 20, NOW(), NOW(), 1, 1),
('梅菜扣肉', 25.00, 'https://images.unsplash.com/photo-1540189549336-e6e99c3679fe?w=400&q=80', '肥瘦相间，入口即化', 1, 12, 800, 18, NOW(), NOW(), 1, 1),
('糖醋排骨', 20.00, 'https://images.unsplash.com/photo-1544025162-d76694265947?w=400&q=80', '酸甜适口，外酥里嫩', 1, 12, 650, 24, NOW(), NOW(), 1, 1),
('地三鲜', 10.00, 'https://images.unsplash.com/photo-1574484284008-86d47dc6b5d3?w=400&q=80', '东北名菜，鲜香浓郁', 1, 12, 400, 5, NOW(), NOW(), 1, 1),
('黄焖鸡米饭', 17.00, 'https://images.unsplash.com/photo-1588166524941-3bf61a9c41db?w=400&q=80', '汤汁浓郁，鸡肉嫩滑', 1, 12, 620, 28, NOW(), NOW(), 1, 1),
('咖喱牛肉饭', 24.00, 'https://images.unsplash.com/photo-1565557623262-b51c2513a641?w=400&q=80', '浓郁咖喱，异域风情', 1, 12, 680, 26, NOW(), NOW(), 1, 1),
('酸菜鱼片', 26.00, 'https://images.unsplash.com/photo-1511914678378-2906b1f69dcf?w=400&q=80', '酸爽开胃，鱼肉鲜嫩', 1, 12, 500, 35, NOW(), NOW(), 1, 1),
('干煸四季豆', 11.00, 'https://images.unsplash.com/photo-1606622879555-e51c1404e13d?w=400&q=80', '麻辣干香，超级下饭', 1, 12, 280, 6, NOW(), NOW(), 1, 1),
('回锅肉', 21.00, 'https://images.unsplash.com/photo-1625938144755-652e08e359b7?w=400&q=80', '川菜之首，肥而不腻', 1, 12, 750, 20, NOW(), NOW(), 1, 1),

-- === 🥗 晚餐/轻食 (ID: 13) ===
('轻食鸡胸肉沙拉', 18.50, 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80', '低脂高蛋白，减脂首选', 1, 13, 350, 35, NOW(), NOW(), 1, 1),
('西蓝花炒虾仁', 22.00, 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400&q=80', '清淡爽口，营养丰富', 1, 13, 280, 25, NOW(), NOW(), 1, 1),
('白灼菜心', 8.00, 'https://images.unsplash.com/photo-1506084868230-bb9d95c24759?w=400&q=80', '清淡解腻，补充维生素', 1, 13, 90, 3, NOW(), NOW(), 1, 1),
('冬瓜排骨汤', 12.00, 'https://images.unsplash.com/photo-1547592180-85f173990554?w=400&q=80', '清热去火，汤鲜味美', 1, 13, 180, 15, NOW(), NOW(), 1, 1),
('藜麦牛油果碗', 24.00, 'https://images.unsplash.com/photo-1623428187969-5da2dcea5ebf?w=400&q=80', '超级食物，健康脂肪', 1, 13, 420, 12, NOW(), NOW(), 1, 1),
('全麦三明治', 12.00, 'https://images.unsplash.com/photo-1550547660-d9450f859349?w=400&q=80', '方便快捷，营养均衡', 1, 13, 320, 15, NOW(), NOW(), 1, 1),
('清蒸鲈鱼片', 28.00, 'https://images.unsplash.com/photo-1621855661131-073c68383863?w=400&q=80', '鲜嫩多汁，优质蛋白', 1, 13, 250, 30, NOW(), NOW(), 1, 1),
('番茄豆腐汤', 8.00, 'https://images.unsplash.com/photo-1596450514965-0c7f20104194?w=400&q=80', '酸甜可口，低卡暖身', 1, 13, 120, 8, NOW(), NOW(), 1, 1),
('凉拌魔芋丝', 9.00, 'https://images.unsplash.com/photo-1610444315278-83e390632b45?w=400&q=80', '超低热量，饱腹代餐', 1, 13, 50, 0, NOW(), NOW(), 1, 1),
('蔬菜沙拉卷', 14.00, 'https://images.unsplash.com/photo-1592417817098-8fd3d9eb14a5?w=400&q=80', '越南风味，清爽解腻', 1, 13, 180, 5, NOW(), NOW(), 1, 1),

-- === 🥤 饮品 (ID: 14) ===
('柠檬红茶', 5.00, 'https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=400&q=80', '冰爽解腻', 1, 14, 120, 0, NOW(), NOW(), 1, 1),
('鲜榨橙汁', 8.00, 'https://images.unsplash.com/photo-1613478223719-2ab802602423?w=400&q=80', '补充维C，鲜橙现榨', 1, 14, 150, 1, NOW(), NOW(), 1, 1),
('热牛奶', 4.00, 'https://images.unsplash.com/photo-1563636619-e9143da7973b?w=400&q=80', '助眠安神，纯牛奶', 1, 14, 130, 8, NOW(), NOW(), 1, 1),
('绿豆汤', 3.00, 'https://images.unsplash.com/photo-1579893559383-c24749f7e887?w=400&q=80', '清热解暑，夏日必备', 1, 14, 100, 4, NOW(), NOW(), 1, 1),
('冰美式咖啡', 12.00, 'https://images.unsplash.com/photo-1517701604599-bb29b5c7355c?w=400&q=80', '提神醒脑，0脂0糖', 1, 14, 5, 0, NOW(), NOW(), 1, 1),
('杨枝甘露', 15.00, 'https://images.unsplash.com/photo-1595981267035-7b04ca84a82d?w=400&q=80', '芒果西米，香甜浓郁', 1, 14, 320, 2, NOW(), NOW(), 1, 1),
('酸梅汤', 5.00, 'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=400&q=80', '生津止渴，去油解腻', 1, 14, 140, 0, NOW(), NOW(), 1, 1),
('低脂酸奶', 6.00, 'https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400&q=80', '希腊酸奶，肠道健康', 1, 14, 100, 8, NOW(), NOW(), 1, 1),
('珍珠奶茶', 10.00, 'https://images.unsplash.com/photo-1558160074-4d7d8bdf4256?w=400&q=80', '快乐源泉，Q弹珍珠', 1, 14, 450, 2, NOW(), NOW(), 1, 1),
('水果拼盘', 12.00, 'https://images.unsplash.com/photo-1549488344-c7052fb89d98?w=400&q=80', '时令鲜果，补充维生素', 1, 14, 100, 1, NOW(), NOW(), 1, 1),
('可乐(无糖)', 3.00, 'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=400&q=80', '肥宅快乐水，无负担', 1, 14, 0, 0, NOW(), NOW(), 1, 1);

-- 5. 重新开启外键检查
SET foreign_key_checks = 1;
