import sys
try:
    with open('test_result.log', 'r', encoding='utf-8', errors='ignore') as f:
        content = f.read()
        lines = content.splitlines()
        
        errors_found = False
        for line in lines:
            if "AssertionError" in line or "Traceback" in line:
                print(line)
                errors_found = True
        
        if not errors_found:
            tail = "\n".join(lines[-5:])
            # Standard python unittest prints "OK" at the very end
            if "OK" in tail or "Ran" in tail: 
                 print("✅ TEST PASSED")
                 print("Tail info:")
                 print(tail)
            else:
                 print("LOG ANALYSIS: No obvious errors, but standard SUCCESS marker not found. Tail:")
                 print(tail)
        else:
            print("❌ ERRORS FOUND")

except Exception as e:
    print(f"Analysis Error: {e}")
