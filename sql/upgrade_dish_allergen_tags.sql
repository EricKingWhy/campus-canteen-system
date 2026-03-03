-- ============================================================
-- 智选6道菜: 补充 dish 表 allergen_tags 字段
-- ============================================================

-- 1. 添加 allergen_tags 字段(如果不存在)
ALTER TABLE dish ADD COLUMN IF NOT EXISTS allergen_tags VARCHAR(255) DEFAULT NULL COMMENT '食材/过敏原标签(逗号分隔)';

-- 2. 为现有菜品补充一些常见的过敏原标签(示例数据)
-- 可根据实际菜品名称扩展
UPDATE dish SET allergen_tags = '花生' WHERE name LIKE '%花生%' AND allergen_tags IS NULL;
UPDATE dish SET allergen_tags = '海鲜' WHERE (name LIKE '%虾%' OR name LIKE '%蟹%' OR name LIKE '%鱼%') AND allergen_tags IS NULL;
UPDATE dish SET allergen_tags = '乳制品' WHERE (name LIKE '%奶%' OR name LIKE '%芝士%' OR name LIKE '%cheese%') AND allergen_tags IS NULL;
UPDATE dish SET allergen_tags = '麸质' WHERE (name LIKE '%面%' OR name LIKE '%饺%' OR name LIKE '%馒头%' OR name LIKE '%包子%') AND allergen_tags IS NULL;
