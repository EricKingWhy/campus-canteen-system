-- 第一步：确保表结构有这俩字段 (如果之前没加的话，防呆设计)
ALTER TABLE dish ADD COLUMN IF NOT EXISTS main_ingredients VARCHAR(255) DEFAULT NULL COMMENT '主要成分';
ALTER TABLE dish ADD COLUMN IF NOT EXISTS allergen_tags VARCHAR(255) DEFAULT NULL COMMENT '忌口/过敏标签';

-- 第二步：批量更新 1-50号 + 2001号 菜品的成分与忌口数据
UPDATE dish SET main_ingredients = '猪肉, 皮蛋, 大米', allergen_tags = '蛋类' WHERE id = 1;
UPDATE dish SET main_ingredients = '鸡肉, 香菇, 大米', allergen_tags = NULL WHERE id = 2;
UPDATE dish SET main_ingredients = '南瓜, 小米', allergen_tags = NULL WHERE id = 3;
UPDATE dish SET main_ingredients = '黄豆, 木耳, 黄花菜', allergen_tags = '豆制品' WHERE id = 4;
UPDATE dish SET main_ingredients = '面粉, 食用油', allergen_tags = '麸质' WHERE id = 5;
UPDATE dish SET main_ingredients = '猪肉, 面粉, 葱', allergen_tags = '麸质' WHERE id = 6;
UPDATE dish SET main_ingredients = '全麦面粉', allergen_tags = '麸质' WHERE id = 7;
UPDATE dish SET main_ingredients = '黄豆, 纯净水', allergen_tags = '豆制品' WHERE id = 8;
UPDATE dish SET main_ingredients = '鸡蛋, 茶叶, 卤料', allergen_tags = '蛋类' WHERE id = 9;
UPDATE dish SET main_ingredients = '面粉, 生菜, 酱料', allergen_tags = '麸质' WHERE id = 10;
UPDATE dish SET main_ingredients = '紫薯, 玉米', allergen_tags = NULL WHERE id = 11;
UPDATE dish SET main_ingredients = '牛肉, 面条, 辣椒', allergen_tags = '麸质, 辛辣' WHERE id = 12;
UPDATE dish SET main_ingredients = '面条, 葱, 酱油', allergen_tags = '麸质' WHERE id = 13;
UPDATE dish SET main_ingredients = '牛肉, 意大利面, 黑胡椒', allergen_tags = '麸质' WHERE id = 14;
UPDATE dish SET main_ingredients = '大米, 虾仁, 鸡蛋, 火腿', allergen_tags = '海鲜, 蛋类' WHERE id = 15;
UPDATE dish SET main_ingredients = '荞麦面, 海苔, 酱油', allergen_tags = '麸质' WHERE id = 16;
UPDATE dish SET main_ingredients = '糙米, 黑米, 时蔬', allergen_tags = NULL WHERE id = 17;
UPDATE dish SET main_ingredients = '猪肉, 淀粉, 马蹄', allergen_tags = NULL WHERE id = 18;
UPDATE dish SET main_ingredients = '鸡肉, 花生, 大米, 葱', allergen_tags = '花生, 辛辣' WHERE id = 19;
UPDATE dish SET main_ingredients = '豆腐, 猪肉末, 辣椒', allergen_tags = '豆制品, 辛辣' WHERE id = 20;
UPDATE dish SET main_ingredients = '猪肉, 木耳, 胡萝卜', allergen_tags = NULL WHERE id = 21;
UPDATE dish SET main_ingredients = '猪肉, 梅干菜', allergen_tags = NULL WHERE id = 22;
UPDATE dish SET main_ingredients = '猪排骨, 糖, 醋', allergen_tags = NULL WHERE id = 23;
UPDATE dish SET main_ingredients = '茄子, 土豆, 青椒', allergen_tags = NULL WHERE id = 24;
UPDATE dish SET main_ingredients = '鸡肉, 香菇, 青椒, 大米', allergen_tags = NULL WHERE id = 25;
UPDATE dish SET main_ingredients = '牛肉, 土豆, 胡萝卜, 咖喱', allergen_tags = NULL WHERE id = 26;
UPDATE dish SET main_ingredients = '黑鱼, 酸菜, 辣椒', allergen_tags = '海鲜, 辛辣' WHERE id = 27;

-- 新增 28-50 及 2001 的详尽数据
UPDATE dish SET main_ingredients = '四季豆, 猪肉末, 辣椒', allergen_tags = '辛辣' WHERE id = 28;
UPDATE dish SET main_ingredients = '猪肉, 青椒, 蒜苗', allergen_tags = '辛辣' WHERE id = 29;
UPDATE dish SET main_ingredients = '鸡胸肉, 生菜, 番茄', allergen_tags = NULL WHERE id = 30;
UPDATE dish SET main_ingredients = '西蓝花, 虾仁', allergen_tags = '海鲜' WHERE id = 31;
UPDATE dish SET main_ingredients = '菜心, 蒜, 酱油', allergen_tags = NULL WHERE id = 32;
UPDATE dish SET main_ingredients = '排骨, 冬瓜', allergen_tags = NULL WHERE id = 33;
UPDATE dish SET main_ingredients = '藜麦, 牛油果, 鸡蛋', allergen_tags = '蛋类' WHERE id = 34;
UPDATE dish SET main_ingredients = '全麦面包, 鸡蛋, 生菜', allergen_tags = '麸质, 蛋类' WHERE id = 35;
UPDATE dish SET main_ingredients = '鲈鱼, 葱, 姜', allergen_tags = '海鲜' WHERE id = 36;
UPDATE dish SET main_ingredients = '番茄, 豆腐', allergen_tags = '豆制品' WHERE id = 37;
UPDATE dish SET main_ingredients = '魔芋丝, 黄瓜, 醋', allergen_tags = NULL WHERE id = 38;
UPDATE dish SET main_ingredients = '蔬菜, 米卷皮', allergen_tags = NULL WHERE id = 39;
UPDATE dish SET main_ingredients = '柠檬, 红茶', allergen_tags = NULL WHERE id = 40;
UPDATE dish SET main_ingredients = '橙子', allergen_tags = NULL WHERE id = 41;
UPDATE dish SET main_ingredients = '纯牛奶', allergen_tags = '乳制品' WHERE id = 42;
UPDATE dish SET main_ingredients = '绿豆, 冰糖', allergen_tags = NULL WHERE id = 43;
UPDATE dish SET main_ingredients = '咖啡豆, 冰块', allergen_tags = NULL WHERE id = 44;
UPDATE dish SET main_ingredients = '芒果, 西柚, 椰奶', allergen_tags = '乳制品' WHERE id = 45;
UPDATE dish SET main_ingredients = '乌梅, 山楂, 冰糖', allergen_tags = NULL WHERE id = 46;
UPDATE dish SET main_ingredients = '牛奶, 益生菌', allergen_tags = '乳制品' WHERE id = 47;
UPDATE dish SET main_ingredients = '红茶, 牛奶, 珍珠', allergen_tags = '乳制品' WHERE id = 48;
UPDATE dish SET main_ingredients = '时令新鲜水果', allergen_tags = NULL WHERE id = 49;
UPDATE dish SET main_ingredients = '碳酸饮料', allergen_tags = NULL WHERE id = 50;

-- 销冠大菜
UPDATE dish SET main_ingredients = '精选五花肉, 冰糖, 老抽, 八角', allergen_tags = NULL WHERE id = 2001;
