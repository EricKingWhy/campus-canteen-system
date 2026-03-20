
import mysql.connector
import sys

# Database config
config = {
  'user': 'root',
  'password': '123456',
  'host': '127.0.0.1', # Use IPv4 explicitly
  'database': 'smart_canteen',
  'auth_plugin': 'mysql_native_password' # Try forcing native password if supported, or just rely on 127.0.0.1
}

import traceback

def fix_db():
    try:
        print("Connecting to database (127.0.0.1)...")
        # Remove auth_plugin if it causes issues, but 127.0.0.1 is usually safer than localhost
        cnx = mysql.connector.connect(user='root', password='123456', host='127.0.0.1', database='smart_canteen')
        cursor = cnx.cursor()

        print("Dropping old table...")
        cursor.execute("DROP TABLE IF EXISTS employee")

        print("Creating new table...")
        create_table_sql = """
        CREATE TABLE `employee` (
          `id` bigint NOT NULL AUTO_INCREMENT,
          `name` varchar(32) NOT NULL,
          `username` varchar(32) NOT NULL COMMENT '鍏抽敭瀛楁',
          `password` varchar(64) NOT NULL,
          `phone` varchar(11) NOT NULL,
          `sex` varchar(2) NOT NULL,
          `id_number` varchar(18) NOT NULL,
          `status` int NOT NULL DEFAULT '1',
          `create_time` datetime DEFAULT NULL,
          `update_time` datetime DEFAULT NULL,
          `create_user` bigint DEFAULT NULL,
          `update_user` bigint DEFAULT NULL,
          PRIMARY KEY (`id`),
          UNIQUE KEY `idx_username` (`username`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """
        cursor.execute(create_table_sql)

        print("Inserting admin user...")
        # MD5 for '123456' is 'e10adc3949ba59abbe56e057f20f883e'
        insert_sql = "INSERT INTO employee (id, name, username, password, phone, sex, id_number, status) VALUES (1, '绠＄悊鍛?, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '13812345678', '1', '110101199001010001', 1)"
        cursor.execute(insert_sql)

        cnx.commit()
        cursor.close()
        cnx.close()
        print("SUCCESS: Database table 'employee' has been reset and admin user created.")

    except ImportError:
        print("ERROR: mysql-connector-python is required. Run: pip install mysql-connector-python")
        sys.exit(1)
    except Exception as err:
        print(f"ERROR: {err}")
        sys.exit(1)

if __name__ == "__main__":
    fix_db()

