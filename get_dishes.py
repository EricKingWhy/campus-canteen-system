import pymysql

conn = pymysql.connect(host='localhost', user='root', password='123456', database='smart_canteen', charset='utf8mb4')
cursor = conn.cursor()

# Get dishes where image is empty, NULL, or doesn't look like a valid local file
cursor.execute('''
    SELECT id, name, description, image 
    FROM dish 
    WHERE image IS NULL 
       OR image = '' 
       OR (image NOT LIKE '%.png' AND image NOT LIKE '%.jpg')
       OR image LIKE '%unsplash%';
''')
rows = cursor.fetchall()

with open('new_dishes.txt', 'w', encoding='utf-8') as f:
    for r in rows:
        f.write(f'{r[0]}|{r[1]}|{r[2]}|{r[3]}\n')

print(f"Found {len(rows)} dishes needing images.")
