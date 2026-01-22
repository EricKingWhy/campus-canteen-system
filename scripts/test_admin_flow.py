import re
from playwright.sync_api import sync_playwright, expect
import time
import os

def run():
    print("🚀 Starting Admin Order Flow Test...")
    with sync_playwright() as p:
        # Launch browser (Headless=False to see the action)
        browser = p.chromium.launch(headless=False, slow_mo=1000)
        context = browser.new_context(viewport={'width': 1280, 'height': 720})
        page = context.new_page()

        try:
            # 1. Login
            print("📍 Navigating to Login Page (History Mode)...")
            # Try history mode URL first
            page.goto("http://localhost:5173/login")
            
            # Wait for load - check for input or if we are already logged in (redirected)
            try:
                page.wait_for_selector("input[type='text']", timeout=3000)
                # Fill credentials
                print("🔑 Logging in as admin...")
                page.fill("input[type='text']", "admin")
                page.fill("input[type='password']", "123456")
                page.click("button[type='button']") # Assuming submit button
                
                # Wait for dashboard
                page.wait_for_url("**/dashboard", timeout=5000)
                print("✅ Login Successful!")
            except Exception as e:
                print(f"ℹ️ Login step info: {e}. Checking if already logged in or different URL.")
            
            # 2. Go to Order List
            print("📂 Navigating to Order Management...")
            # Direct navigation to avoid menu issues
            page.goto("http://localhost:5173/order")
            time.sleep(2) # Wait for Vue transition

            # 3. Inspect Orders
            print("🔍 Inspecting Order List...")
            # Wait for table rows
            try:
                page.wait_for_selector(".el-table__row", timeout=5000)
                rows = page.locator(".el-table__row").all()
                print(f"📊 Found {len(rows)} orders on the first page.")

                if len(rows) > 0:
                    first_row = rows[0]
                    # Extract all text
                    row_text = first_row.inner_text()
                    print(f"📝 First Order Row Data: {row_text}")
                    
                    # Regex to find 19-digit snowflake ID
                    match = re.search(r'\b\d{19}\b', row_text)
                    if match:
                        order_id = match.group(0)
                        print(f"✅ CRITICAL CHECK PASSED: Found 19-digit Snowflake ID: {order_id}")
                    else:
                        print(f"⚠️ WARNING: 19-digit ID NOT found in row text. Content: {row_text[:100]}...")

                    # 4. Check details (Simulate flow)
                    print("👉 Clicking '查看' (Details) to verify ID in details view...")
                    # Look for a button with text "查看" inside this row
                    details_btn = first_row.get_by_text("查看")
                    if details_btn.count() > 0:
                        details_btn.click()
                        print("✅ Opened Order Details.")
                        time.sleep(2)
                        # Optionally take a screenshot of details
                        page.screenshot(path="order_details.png")
                        print("📸 Saved order_details.png")
                    else:
                        print("⚠️ Access/Details button not found.")

                else:
                    print("⚠️ Order list loaded but is empty. Cannot verify ID format.")

            except Exception as e:
                print(f"❌ Error getting orders: {e}")
                page.screenshot(path="error_state.png")
                print("📸 Saved error_state.png")

        except Exception as main_e:
            print(f"❌ Critical Error: {main_e}")
            page.screenshot(path="critical_error.png")
        
        finally:
            print("🏁 Test Completed.")
            time.sleep(2)
            browser.close()

if __name__ == "__main__":
    run()
