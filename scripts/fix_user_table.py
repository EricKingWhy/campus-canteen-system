import pymysql
import sys

def fix_user_table():
    print("🚀 Starting User Table Fix...")
    
    config = {
        'host': 'localhost',
        'port': 3306,
        'user': 'root',
        'password': '123456',
        'database': 'hanye_take_out',
        'charset': 'utf8mb4',
        'cursorclass': pymysql.cursors.DictCursor
    }

    try:
        conn = pymysql.connect(**config)
        print("✅ Database connected.")
    except Exception as e:
        print(f"❌ Connection failed: {e}")
        return

    alternations = [
        ("sex", "ALTER TABLE `user` ADD COLUMN `sex` varchar(2) DEFAULT '1' COMMENT '性别'"),
        ("id_number", "ALTER TABLE `user` ADD COLUMN `id_number` varchar(18) DEFAULT '' COMMENT '身份证号'"),
        ("avatar", "ALTER TABLE `user` ADD COLUMN `avatar` varchar(500) DEFAULT '' COMMENT '头像'")
    ]

    try:
        with conn.cursor() as cursor:
            # Check if table exists
            cursor.execute("SHOW TABLES LIKE 'user'")
            if not cursor.fetchone():
                print("❌ Table 'user' does not exist!")
                return
            
            for col, sql in alternations:
                try:
                    print(f"Adding column '{col}'...")
                    cursor.execute(sql)
                    print(f"✅ Column '{col}' added successfully.")
                except pymysql.err.OperationalError as e:
                    if e.args[0] == 1060: # Duplicate column name
                        print(f"⚠️ Column '{col}' already exists, skipping.")
                    else:
                        print(f"❌ Failed to add '{col}': {e}")
                except Exception as e:
                    print(f"❌ Error adding '{col}': {e}")
            
            conn.commit()
            print("🎉 User table schema update completed.")

    finally:
        conn.close()

if __name__ == "__main__":
    fix_user_table()
