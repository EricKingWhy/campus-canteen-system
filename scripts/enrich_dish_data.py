#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
闃舵浜岋細AI 鏅鸿兘鏁版嵁濉厖
鏍规嵁鑿滃悕鏅鸿兘鎺ㄦ柇钀ュ吇鎴愬垎骞剁敓鎴愯浜烘弿杩?
"""

import pymysql
import random

# 鏁版嵁搴撻厤缃?
DB_CONFIG = {
    'host': '127.0.0.1',
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'smart_canteen',
    'charset': 'utf8mb4'
}

# 鑿滃搧鍏抽敭璇?鈫?钀ュ吇鐗瑰緛鏄犲皠
NUTRITION_RULES = {
    # 楂樿泲鐧界被
    '楦?: {'calories': (250, 400), 'protein': (25, 40), 'fat': (5, 15), 'carbs': (10, 30), 'tags': '浣庤剛楂樿泲鐧?},
    '楦¤兏': {'calories': (200, 300), 'protein': (30, 45), 'fat': (3, 8), 'carbs': (5, 20), 'tags': '澧炶倢棣栭€?},
    '鐗?: {'calories': (300, 500), 'protein': (25, 40), 'fat': (15, 30), 'carbs': (10, 25), 'tags': '楂樿泲鐧?},
    '铏?: {'calories': (150, 250), 'protein': (20, 35), 'fat': (3, 10), 'carbs': (5, 15), 'tags': '浣庤剛娴烽矞'},
    '楸?: {'calories': (180, 300), 'protein': (22, 38), 'fat': (5, 15), 'carbs': (5, 15), 'tags': '浼樿川铔嬬櫧'},
    '铔?: {'calories': (200, 350), 'protein': (15, 25), 'fat': (10, 20), 'carbs': (5, 15), 'tags': '钀ュ吇鍧囪　'},
    
    # 楂樼⒊姘寸被
    '楗?: {'calories': (350, 550), 'protein': (8, 15), 'fat': (5, 15), 'carbs': (50, 80), 'tags': '涓婚鎺ㄨ崘'},
    '闈?: {'calories': (400, 600), 'protein': (12, 20), 'fat': (8, 20), 'carbs': (55, 85), 'tags': '闈㈤'},
    '绮?: {'calories': (150, 300), 'protein': (5, 12), 'fat': (2, 8), 'carbs': (25, 50), 'tags': '鍏昏儍鏆栧績'},
    '楗哄瓙': {'calories': (300, 450), 'protein': (12, 20), 'fat': (10, 20), 'carbs': (35, 55), 'tags': '缁忓吀闈㈤'},
    '鍖呭瓙': {'calories': (250, 400), 'protein': (8, 15), 'fat': (8, 18), 'carbs': (35, 50), 'tags': '鏃╅棣栭€?},
    '棣掑ご': {'calories': (200, 300), 'protein': (6, 10), 'fat': (2, 5), 'carbs': (40, 60), 'tags': '浣庤剛涓婚'},
    
    # 鑲夌被
    '绾㈢儳': {'calories': (350, 550), 'protein': (20, 35), 'fat': (15, 30), 'carbs': (15, 30), 'tags': '娴撻鍏ュ懗'},
    '鑲?: {'calories': (300, 500), 'protein': (18, 30), 'fat': (15, 35), 'carbs': (10, 25), 'tags': '鑲夐鍥涙孩'},
    '鎺掗': {'calories': (350, 550), 'protein': (20, 35), 'fat': (20, 40), 'carbs': (10, 20), 'tags': '楠ㄩ娴撻儊'},
    '鐚?: {'calories': (320, 500), 'protein': (18, 28), 'fat': (20, 40), 'carbs': (10, 20), 'tags': '缁忓吀缇庡懗'},
    
    # 钄彍绫?
    '鑿?: {'calories': (80, 200), 'protein': (3, 8), 'fat': (2, 8), 'carbs': (8, 20), 'tags': '娓呯埥鍋ュ悍'},
    '闈掕彍': {'calories': (50, 120), 'protein': (2, 5), 'fat': (1, 5), 'carbs': (5, 12), 'tags': '浣庡崱钄彍'},
    '娌欐媺': {'calories': (100, 250), 'protein': (5, 15), 'fat': (5, 15), 'carbs': (10, 25), 'tags': '杞婚棣栭€?},
    '钄彍': {'calories': (60, 150), 'protein': (3, 8), 'fat': (2, 8), 'carbs': (8, 18), 'tags': '鍋ュ悍杞婚'},
    '璞嗚厫': {'calories': (100, 200), 'protein': (10, 18), 'fat': (5, 12), 'carbs': (5, 15), 'tags': '妞嶇墿铔嬬櫧'},
    
    # 姹ょ被
    '姹?: {'calories': (80, 200), 'protein': (5, 15), 'fat': (3, 10), 'carbs': (5, 15), 'tags': '鏆栬儍婊嬭ˉ'},
    
    # 鐐掕彍绫?
    '鐐?: {'calories': (200, 400), 'protein': (10, 25), 'fat': (10, 25), 'carbs': (15, 35), 'tags': '瀹跺父椋庡懗'},
    '鐑?: {'calories': (250, 450), 'protein': (15, 30), 'fat': (12, 28), 'carbs': (15, 30), 'tags': '閰遍娴撻儊'},
    '钂?: {'calories': (150, 350), 'protein': (10, 25), 'fat': (5, 15), 'carbs': (15, 35), 'tags': '娓呮贰鍋ュ悍'},
    '鐓?: {'calories': (250, 450), 'protein': (15, 30), 'fat': (15, 30), 'carbs': (10, 25), 'tags': '澶栭叆閲屽'},
}

# 鑿滃搧鎻忚堪妯℃澘
DESCRIP_TEMPLATES = {
    '楦?: ['绮鹃€夐矞瀚╅浮鑲夛紝鍙ｆ劅椴滅編', '浣庤剛楂樿泲鐧斤紝鍋ヨ韩蹇呭', '椴滃澶氭眮锛岃惀鍏讳赴瀵?],
    '鐗?: ['绮鹃€変紭璐ㄧ墰鑲夛紝椴滃鍙彛', '楂樿泲鐧借惀鍏伙紝鑲夎川椴滅編', '鐗涜倝婊戝锛屽洖鍛虫棤绌?],
    '铏?: ['鏂伴矞娴疯櫨锛岄矞鐢滃脊鐗?, '浣庤剛娴烽矞锛岃惀鍏荤編鍛?, '铏捐倝楗辨弧锛屽彛鎰烸寮?],
    '楸?: ['椴滃楸艰倝锛岃惀鍏讳赴瀵?, '浼樿川铔嬬櫧锛岀粏瀚╃埥婊?, '楸艰倝椴滅編锛屽叆鍙ｅ嵆鍖?],
    '鑲?: ['绮鹃€変紭璐ㄨ倝绫伙紝鑲夐鍥涙孩', '鑲ョ槮鐩搁棿锛屽彛鎰熶赴瀵?, '鑲夎川椴滃锛屾祿棣欏叆鍛?],
    '绾㈢儳': ['鑲ヨ€屼笉鑵伙紝鍏ュ彛鍗冲寲', '娴撴补璧ら叡锛岀粡鍏稿懗閬?, '閰遍娴撻儊锛屽洖鍛虫偁闀?],
    '鎺掗': ['楠ㄩ娴撻儊锛岃倝璐ㄩ叆鐑?, '绮鹃€夋帓楠紝鑲夐鍥涙孩', '鎺掗杞朝锛岄叡棣欏叆鍛?],
    '楗?: ['绫抽キ杞朝锛屾惌閰嶄赴瀵?, '钀ュ吇鍧囪　锛岄ケ鑵规劅寮?, '缁忓吀濂楅锛岀編鍛冲疄鎯?],
    '闈?: ['闈㈡潯鍔查亾锛屾堡搴曢唶鍘?, '缁忓吀闈㈤锛屽洖鍛虫棤绌?, '闈㈤鍥涙孩锛屾殩鑳冩殩蹇?],
    '绮?: ['杞朝鍙彛锛屽吇鑳冩殩蹇?, '鐔埗鍏ュ懗锛岃惀鍏绘槗鍚告敹', '绮ュ搧缁嗚吇锛屽彛鎰熼『婊?],
    '钄彍': ['鏂伴矞鏃惰敩锛屾竻鐖藉仴搴?, '浣庡崱杞婚锛岃惀鍏讳赴瀵?, '缈犵豢鐖藉彛锛屾竻娣″仴搴?],
    '娌欐媺': ['鏂伴矞钄灉锛屼綆鍗″仴搴?, '杞婚棣栭€夛紝钀ュ吇鍧囪　', '娓呯埥鍙彛锛屽噺鑴傚繀澶?],
    '璞嗚厫': ['璞嗛娴撻儊锛屽婊戝彲鍙?, '妞嶇墿铔嬬櫧锛屽仴搴风編鍛?, '鍏ュ彛鍗冲寲锛岃惀鍏讳赴瀵?],
    '姹?: ['姹ゆ眮閱囧帤锛屾殩鑳冩粙琛?, '绮惧績鐔埗锛岃惀鍏讳赴瀵?, '椴滈缇庡懗锛屽洖鍛虫偁闀?],
    '鐐?: ['澶х伀蹇倰锛岄攣浣忛矞鍛?, '瀹跺父椋庡懗锛岀編鍛冲彲鍙?, '鐏€欐伆鍒帮紝鍙ｆ劅涓板瘜'],
    '钂?: ['钂稿埗鍏ュ懗锛屽師姹佸師鍛?, '娓呮贰鍋ュ悍锛岃惀鍏讳繚鐣?, '椴滃鍙彛锛屽洖鍛虫棤绌?],
    'default': ['绮惧績鐑瑰埗锛岀編鍛冲彲鍙?, '鏂伴矞椋熸潗锛岃惀鍏讳赴瀵?, '缁忓吀缇庡懗锛屽洖鍛虫棤绌?]
}


def get_nutrition_for_dish(dish_name):
    """鏍规嵁鑿滃悕鎺ㄦ柇钀ュ吇鎴愬垎"""
    # 榛樿鍊?
    nutrition = {
        'calories': random.randint(250, 450),
        'protein': random.randint(10, 25),
        'fat': random.randint(8, 20),
        'carbs': random.randint(20, 50)
    }
    
    # 鏍规嵁鍏抽敭璇嶅尮閰?
    for keyword, rules in NUTRITION_RULES.items():
        if keyword in dish_name:
            nutrition['calories'] = random.randint(rules['calories'][0], rules['calories'][1])
            nutrition['protein'] = random.randint(rules['protein'][0], rules['protein'][1])
            nutrition['fat'] = random.randint(rules['fat'][0], rules['fat'][1])
            nutrition['carbs'] = random.randint(rules['carbs'][0], rules['carbs'][1])
            break
    
    return nutrition


def get_description_for_dish(dish_name):
    """鏍规嵁鑿滃悕鐢熸垚璇变汉鎻忚堪"""
    for keyword, templates in DESCRIP_TEMPLATES.items():
        if keyword in dish_name:
            return random.choice(templates)
    return random.choice(DESCRIP_TEMPLATES['default'])


def main():
    print("=" * 50)
    print("馃嵔锔? 寮€濮?AI 鏅鸿兘鏁版嵁濉厖")
    print("=" * 50)
    
    try:
        # 杩炴帴鏁版嵁搴?
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # 棣栧厛纭繚 sold 鍒楀瓨鍦?
        try:
            cursor.execute("ALTER TABLE dish ADD COLUMN sold INT DEFAULT 400")
            print("鉁?娣诲姞 sold 鍒楁垚鍔?)
        except Exception as e:
            if 'Duplicate column' in str(e):
                print("鈩癸笍  sold 鍒楀凡瀛樺湪")
            else:
                print(f"鈿狅笍  娣诲姞 sold 鍒? {e}")
        
        # 鑾峰彇鎵€鏈夎彍鍝?
        cursor.execute("SELECT id, name FROM dish")
        dishes = cursor.fetchall()
        
        print(f"\n馃搳 鍏辨壘鍒?{len(dishes)} 閬撹彍鍝佸緟澶勭悊\n")
        
        updated_count = 0
        for dish_id, dish_name in dishes:
            # 鑾峰彇钀ュ吇鏁版嵁
            nutrition = get_nutrition_for_dish(dish_name)
            description = get_description_for_dish(dish_name)
            sold = random.randint(300, 800)  # 妯℃嫙鍒濆閿€閲?
            
            # 鏇存柊鏁版嵁搴?
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
            
            print(f"  鉁?[{dish_id}] {dish_name}")
            print(f"      鐑噺: {nutrition['calories']}kcal | 铔嬬櫧璐? {nutrition['protein']}g | 鑴傝偑: {nutrition['fat']}g | 纰虫按: {nutrition['carbs']}g")
            print(f"      鎻忚堪: {description} | 閿€閲? {sold}")
            
            updated_count += 1
        
        conn.commit()
        
        print("\n" + "=" * 50)
        print(f"鉁?AI 鏅鸿兘鏁版嵁濉厖瀹屾垚锛佸叡鏇存柊 {updated_count} 閬撹彍鍝?)
        print("=" * 50)
        
    except Exception as e:
        print(f"鉂?閿欒: {e}")
        import traceback
        traceback.print_exc()
    finally:
        if 'cursor' in dir():
            cursor.close()
        if 'conn' in dir():
            conn.close()


if __name__ == '__main__':
    main()

