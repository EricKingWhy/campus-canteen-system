import pymysql

def fix_orders_table():
    print("馃殌 Starting Orders Table Fix...")
    
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

    sql_alter = "ALTER TABLE `orders` MODIFY COLUMN `address_book_id` bigint NULL COMMENT '鍦板潃id'"

    try:
        with conn.cursor() as cursor:
            # Check if table exists
            cursor.execute("SHOW TABLES LIKE 'orders'")
            if not cursor.fetchone():
                print("鉂?Table 'orders' does not exist!")
                return
            
            try:
                print("Modifying `address_book_id` to be NULLABLE...")
                cursor.execute(sql_alter)
                print("鉁?`address_book_id` is now NULLABLE.")
            except Exception as e:
                print(f"鉂?Failed to alter table: {e}")
            
            conn.commit()
            print("馃帀 Orders table schema update completed.")

    finally:
        conn.close()

if __name__ == "__main__":
    fix_orders_table()

