#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
阶段二：AI 智能数据填充
根据菜名智能推断营养成分并生成诱人描述
"""

import pymysql
import random

# 数据库配置
DB_CONFIG = {
    'host': '127.0.0.1',
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'hanye_take_out',
    'charset': 'utf8mb4'
}

# 菜品关键词 → 营养特征映射
NUTRITION_RULES = {
    # 高蛋白类
    '鸡': {'calories': (250, 400), 'protein': (25, 40), 'fat': (5, 15), 'carbs': (10, 30), 'tags': '低脂高蛋白'},
    '鸡胸': {'calories': (200, 300), 'protein': (30, 45), 'fat': (3, 8), 'carbs': (5, 20), 'tags': '增肌首选'},
    '牛': {'calories': (300, 500), 'protein': (25, 40), 'fat': (15, 30), 'carbs': (10, 25), 'tags': '高蛋白'},
    '虾': {'calories': (150, 250), 'protein': (20, 35), 'fat': (3, 10), 'carbs': (5, 15), 'tags': '低脂海鲜'},
    '鱼': {'calories': (180, 300), 'protein': (22, 38), 'fat': (5, 15), 'carbs': (5, 15), 'tags': '优质蛋白'},
    '蛋': {'calories': (200, 350), 'protein': (15, 25), 'fat': (10, 20), 'carbs': (5, 15), 'tags': '营养均衡'},
    
    # 高碳水类
    '饭': {'calories': (350, 550), 'protein': (8, 15), 'fat': (5, 15), 'carbs': (50, 80), 'tags': '主食推荐'},
    '面': {'calories': (400, 600), 'protein': (12, 20), 'fat': (8, 20), 'carbs': (55, 85), 'tags': '面食'},
    '粥': {'calories': (150, 300), 'protein': (5, 12), 'fat': (2, 8), 'carbs': (25, 50), 'tags': '养胃暖心'},
    '饺子': {'calories': (300, 450), 'protein': (12, 20), 'fat': (10, 20), 'carbs': (35, 55), 'tags': '经典面食'},
    '包子': {'calories': (250, 400), 'protein': (8, 15), 'fat': (8, 18), 'carbs': (35, 50), 'tags': '早餐首选'},
    '馒头': {'calories': (200, 300), 'protein': (6, 10), 'fat': (2, 5), 'carbs': (40, 60), 'tags': '低脂主食'},
    
    # 肉类
    '红烧': {'calories': (350, 550), 'protein': (20, 35), 'fat': (15, 30), 'carbs': (15, 30), 'tags': '浓香入味'},
    '肉': {'calories': (300, 500), 'protein': (18, 30), 'fat': (15, 35), 'carbs': (10, 25), 'tags': '肉香四溢'},
    '排骨': {'calories': (350, 550), 'protein': (20, 35), 'fat': (20, 40), 'carbs': (10, 20), 'tags': '骨香浓郁'},
    '猪': {'calories': (320, 500), 'protein': (18, 28), 'fat': (20, 40), 'carbs': (10, 20), 'tags': '经典美味'},
    
    # 蔬菜类
    '菜': {'calories': (80, 200), 'protein': (3, 8), 'fat': (2, 8), 'carbs': (8, 20), 'tags': '清爽健康'},
    '青菜': {'calories': (50, 120), 'protein': (2, 5), 'fat': (1, 5), 'carbs': (5, 12), 'tags': '低卡蔬菜'},
    '沙拉': {'calories': (100, 250), 'protein': (5, 15), 'fat': (5, 15), 'carbs': (10, 25), 'tags': '轻食首选'},
    '蔬菜': {'calories': (60, 150), 'protein': (3, 8), 'fat': (2, 8), 'carbs': (8, 18), 'tags': '健康轻食'},
    '豆腐': {'calories': (100, 200), 'protein': (10, 18), 'fat': (5, 12), 'carbs': (5, 15), 'tags': '植物蛋白'},
    
    # 汤类
    '汤': {'calories': (80, 200), 'protein': (5, 15), 'fat': (3, 10), 'carbs': (5, 15), 'tags': '暖胃滋补'},
    
    # 炒菜类
    '炒': {'calories': (200, 400), 'protein': (10, 25), 'fat': (10, 25), 'carbs': (15, 35), 'tags': '家常风味'},
    '烧': {'calories': (250, 450), 'protein': (15, 30), 'fat': (12, 28), 'carbs': (15, 30), 'tags': '酱香浓郁'},
    '蒸': {'calories': (150, 350), 'protein': (10, 25), 'fat': (5, 15), 'carbs': (15, 35), 'tags': '清淡健康'},
    '煎': {'calories': (250, 450), 'protein': (15, 30), 'fat': (15, 30), 'carbs': (10, 25), 'tags': '外酥里嫩'},
}

# 菜品描述模板
DESCRIP_TEMPLATES = {
    '鸡': ['精选鲜嫩鸡肉，口感鲜美', '低脂高蛋白，健身必备', '鲜嫩多汁，营养丰富'],
    '牛': ['精选优质牛肉，鲜嫩可口', '高蛋白营养，肉质鲜美', '牛肉滑嫩，回味无穷'],
    '虾': ['新鲜海虾，鲜甜弹牙', '低脂海鲜，营养美味', '虾肉饱满，口感Q弹'],
    '鱼': ['鲜嫩鱼肉，营养丰富', '优质蛋白，细嫩爽滑', '鱼肉鲜美，入口即化'],
    '肉': ['精选优质肉类，肉香四溢', '肥瘦相间，口感丰富', '肉质鲜嫩，浓香入味'],
    '红烧': ['肥而不腻，入口即化', '浓油赤酱，经典味道', '酱香浓郁，回味悠长'],
    '排骨': ['骨香浓郁，肉质酥烂', '精选排骨，肉香四溢', '排骨软糯，酱香入味'],
    '饭': ['米饭软糯，搭配丰富', '营养均衡，饱腹感强', '经典套餐，美味实惠'],
    '面': ['面条劲道，汤底醇厚', '经典面食，回味无穷', '面香四溢，暖胃暖心'],
    '粥': ['软糯可口，养胃暖心', '熬制入味，营养易吸收', '粥品细腻，口感顺滑'],
    '蔬菜': ['新鲜时蔬，清爽健康', '低卡轻食，营养丰富', '翠绿爽口，清淡健康'],
    '沙拉': ['新鲜蔬果，低卡健康', '轻食首选，营养均衡', '清爽可口，减脂必备'],
    '豆腐': ['豆香浓郁，嫩滑可口', '植物蛋白，健康美味', '入口即化，营养丰富'],
    '汤': ['汤汁醇厚，暖胃滋补', '精心熬制，营养丰富', '鲜香美味，回味悠长'],
    '炒': ['大火快炒，锁住鲜味', '家常风味，美味可口', '火候恰到，口感丰富'],
    '蒸': ['蒸制入味，原汁原味', '清淡健康，营养保留', '鲜嫩可口，回味无穷'],
    'default': ['精心烹制，美味可口', '新鲜食材，营养丰富', '经典美味，回味无穷']
}


def get_nutrition_for_dish(dish_name):
    """根据菜名推断营养成分"""
    # 默认值
    nutrition = {
        'calories': random.randint(250, 450),
        'protein': random.randint(10, 25),
        'fat': random.randint(8, 20),
        'carbs': random.randint(20, 50)
    }
    
    # 根据关键词匹配
    for keyword, rules in NUTRITION_RULES.items():
        if keyword in dish_name:
            nutrition['calories'] = random.randint(rules['calories'][0], rules['calories'][1])
            nutrition['protein'] = random.randint(rules['protein'][0], rules['protein'][1])
            nutrition['fat'] = random.randint(rules['fat'][0], rules['fat'][1])
            nutrition['carbs'] = random.randint(rules['carbs'][0], rules['carbs'][1])
            break
    
    return nutrition


def get_description_for_dish(dish_name):
    """根据菜名生成诱人描述"""
    for keyword, templates in DESCRIP_TEMPLATES.items():
        if keyword in dish_name:
            return random.choice(templates)
    return random.choice(DESCRIP_TEMPLATES['default'])


def main():
    print("=" * 50)
    print("🍽️  开始 AI 智能数据填充")
    print("=" * 50)
    
    try:
        # 连接数据库
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # 首先确保 sold 列存在
        try:
            cursor.execute("ALTER TABLE dish ADD COLUMN sold INT DEFAULT 400")
            print("✅ 添加 sold 列成功")
        except Exception as e:
            if 'Duplicate column' in str(e):
                print("ℹ️  sold 列已存在")
            else:
                print(f"⚠️  添加 sold 列: {e}")
        
        # 获取所有菜品
        cursor.execute("SELECT id, name FROM dish")
        dishes = cursor.fetchall()
        
        print(f"\n📊 共找到 {len(dishes)} 道菜品待处理\n")
        
        updated_count = 0
        for dish_id, dish_name in dishes:
            # 获取营养数据
            nutrition = get_nutrition_for_dish(dish_name)
            description = get_description_for_dish(dish_name)
            sold = random.randint(300, 800)  # 模拟初始销量
            
            # 更新数据库
            sql = """
                UPDATE dish 
                SET calories = %s, 
                    protein = %s, 
                    fat = %s, 
                    carbohydrates = %s,
                    description = %s,
                    sold = %s
                WHERE id = %s
            """
            cursor.execute(sql, (
                nutrition['calories'],
                nutrition['protein'],
                nutrition['fat'],
                nutrition['carbs'],
                description,
                sold,
                dish_id
            ))
            
            print(f"  ✓ [{dish_id}] {dish_name}")
            print(f"      热量: {nutrition['calories']}kcal | 蛋白质: {nutrition['protein']}g | 脂肪: {nutrition['fat']}g | 碳水: {nutrition['carbs']}g")
            print(f"      描述: {description} | 销量: {sold}")
            
            updated_count += 1
        
        conn.commit()
        
        print("\n" + "=" * 50)
        print(f"✅ AI 智能数据填充完成！共更新 {updated_count} 道菜品")
        print("=" * 50)
        
    except Exception as e:
        print(f"❌ 错误: {e}")
        import traceback
        traceback.print_exc()
    finally:
        if 'cursor' in dir():
            cursor.close()
        if 'conn' in dir():
            conn.close()


if __name__ == '__main__':
    main()
