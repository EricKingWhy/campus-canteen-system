-- 1. 启用所有菜品分类
UPDATE category SET status = 1;

-- 2. 启用所有菜品
UPDATE dish SET status = 1;

-- 3. 启用所有套餐
UPDATE setmeal SET status = 1;

SELECT 'SUCCESS: All data enabled (Status 1)' as msg;
