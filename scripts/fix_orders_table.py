import pymysql

def fix_orders_table():
    print("🚀 Starting Orders Table Fix...")
    
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

    sql_alter = "ALTER TABLE `orders` MODIFY COLUMN `address_book_id` bigint NULL COMMENT '地址id'"

    try:
        with conn.cursor() as cursor:
            # Check if table exists
            cursor.execute("SHOW TABLES LIKE 'orders'")
            if not cursor.fetchone():
                print("❌ Table 'orders' does not exist!")
                return
            
            try:
                print("Modifying `address_book_id` to be NULLABLE...")
                cursor.execute(sql_alter)
                print("✅ `address_book_id` is now NULLABLE.")
            except Exception as e:
                print(f"❌ Failed to alter table: {e}")
            
            conn.commit()
            print("🎉 Orders table schema update completed.")

    finally:
        conn.close()

if __name__ == "__main__":
    fix_orders_table()
