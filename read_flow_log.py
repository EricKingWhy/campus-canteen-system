import sys
try:
    with open('flow_result.log', 'r', encoding='utf-8', errors='ignore') as f:
        print(f.read())
except Exception as e:
    print(f"Error reading log: {e}")
