import pymysql
import sys

def fix_user_table():
    print("馃殌 Starting User Table Fix...")
    
    config = {
        'host': 'localhost',
        'port': 3306,
        'user': 'root',
        'password': '123456',
        'database': 'smart_canteen',
        'charset': 'utf8mb4',
        'cursorclass': pymysql.cursors.DictCursor
    }

    try:
        conn = pymysql.connect(**config)
        print("鉁?Database connected.")
    except Exception as e:
        print(f"鉂?Connection failed: {e}")
        return

    alternations = [
        ("sex", "ALTER TABLE `user` ADD COLUMN `sex` varchar(2) DEFAULT '1' COMMENT '鎬у埆'"),
        ("id_number", "ALTER TABLE `user` ADD COLUMN `id_number` varchar(18) DEFAULT '' COMMENT '韬唤璇佸彿'"),
        ("avatar", "ALTER TABLE `user` ADD COLUMN `avatar` varchar(500) DEFAULT '' COMMENT '澶村儚'")
    ]

    try:
        with conn.cursor() as cursor:
            # Check if table exists
            cursor.execute("SHOW TABLES LIKE 'user'")
            if not cursor.fetchone():
                print("鉂?Table 'user' does not exist!")
                return
            
            for col, sql in alternations:
                try:
                    print(f"Adding column '{col}'...")
                    cursor.execute(sql)
                    print(f"鉁?Column '{col}' added successfully.")
                except pymysql.err.OperationalError as e:
                    if e.args[0] == 1060: # Duplicate column name
                        print(f"鈿狅笍 Column '{col}' already exists, skipping.")
                    else:
                        print(f"鉂?Failed to add '{col}': {e}")
                except Exception as e:
                    print(f"鉂?Error adding '{col}': {e}")
            
            conn.commit()
            print("馃帀 User table schema update completed.")

    finally:
        conn.close()

if __name__ == "__main__":
    fix_user_table()

