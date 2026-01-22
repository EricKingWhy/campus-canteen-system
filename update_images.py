
import mysql.connector
from datetime import datetime

def update_db():
    try:
        conn = mysql.connector.connect(
            host="localhost",
            user="root",
            password="123456",
            database="hanye_take_out"
        )
        cursor = conn.cursor()
        
        # 1. Update specific dishes
        updates = [
            ("/static/dish/kung_pao_chicken.jpg", ["宫保鸡丁", "Kung Pao Chicken"]),
            ("/static/dish/coca_cola.jpg", ["可乐", "可口可乐", "Coca Cola"]),
            ("/static/dish/braised_pork.jpg", ["红烧肉", "Braised Pork"])
        ]
        
        for img_path, names in updates:
            for name in names:
                print(f"Updating {name} to {img_path}...")
                cursor.execute("UPDATE dish SET image = %s WHERE name LIKE %s", (img_path, f"%{name}%"))
        
        # 2. Check update
        cursor.execute("SELECT id, name, image FROM dish WHERE image LIKE '/static/dish/%'")
        results = cursor.fetchall()
        print("\nUpdated dishes:")
        for row in results:
            print(row)
            
        conn.commit()
        cursor.close()
        conn.close()
        print("\nDatabase update completed successfully.")
        
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    update_db()
