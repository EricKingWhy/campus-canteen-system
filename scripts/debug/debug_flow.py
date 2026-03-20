import requests
import sys
import io

# Fix encoding for Windows PowerShell validation
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

# 配置
BASE_URL = "http://localhost:8081"
# Admin & User Token will be fetched dynamically if possible, or use hardcoded for testing if dev env allows
# For this specific verify script, we will try to login to get fresh tokens
ADMIN_LOGIN_URL = f"{BASE_URL}/admin/employee/login"
USER_LOGIN_URL = f"{BASE_URL}/user/user/login"

def get_admin_token():
    try:
        resp = requests.post(ADMIN_LOGIN_URL, json={"username": "admin", "password": "123456"})
        if resp.status_code == 200 and resp.json().get("code") == 1:
            return resp.json()["data"]["token"]
    except:
        pass
    return ""

def get_user_token():
    try:
        # Mock login code
        resp = requests.post(USER_LOGIN_URL, json={"code": "123456"}) 
        if resp.status_code == 200 and resp.json().get("code") == 1:
            return resp.json()["data"]["token"]
    except:
        pass
    return ""

def test_flow():
    ADMIN_TOKEN = get_admin_token()
    USER_TOKEN = get_user_token()
    
    if not ADMIN_TOKEN or not USER_TOKEN:
        print("❌ 无法获取Token，无法继续测试")
        return

    # 目标订单ID - 此ID应该是之前存在问题的ID，或者我们可以创建一个新的来测
    # 根据用户请求，我们验证特定ID: 2013926363226796034 (如果存在)
    # 或者为了稳妥，我们新建一个单子来测全流程
    
    print("--- 启动全链路验证 (含 Long ID 截断修复验证) ---")
    
    # 1. 新建订单 (保证有一个确定存在的ID)
    create_headers = {"authentication": USER_TOKEN}
    submit_data = {
        "remark": "FlowDebugOrder",
        "payMethod": 1,
        "addressBookId": 1, 
        "amount": 100,
        "packAmount": 0,
        "tablewareNumber": 1,
        "estimatedDeliveryTime": "2026-01-22T12:00:00"
    }
    # 先清空购物车防止报错 (Optional)
    try: requests.delete(f"{BASE_URL}/user/shoppingCart/clean", headers=create_headers)
    except: pass
    
    # 加菜进购物车
    # 需先获知一个 Dish ID. 简便起见，我们直接尝试查询一个现有的 Dish
    # 若无法获取，仅打印警告
    
    print("⚠️  跳过创建新订单步骤，直接尝试操作指定ID: 2013926363226796034")
    order_id = "2013926363226796034" 

    admin_headers = {"token": ADMIN_TOKEN}
    user_headers = {"authentication": USER_TOKEN}

    # 1. Admin Delivery (3 -> 4)
    print(f"--- [STEP 1] 管理员点击制作完成 (Delivery) ID: {order_id} ---")
    resp = requests.put(
        f"{BASE_URL}/admin/order/delivery/{order_id}",
        headers=admin_headers
    )
    print(f"API响应: {resp.status_code} - {resp.text}")

    # 2. Check Status
    check_resp = requests.get(
        f"{BASE_URL}/admin/order/details/{order_id}",
        headers=admin_headers
    )
    if check_resp.status_code != 200:
        print(f"❌ 无法查询订单详情: {check_resp.text}")
        return

    order_data = check_resp.json().get('data', {})
    status = order_data.get('status')
    delivery_time = order_data.get('deliveryTime')
    print(f"当前数据库状态: {status} (期望: 4)")
    print(f"DeliveryTime: {delivery_time} (期望: 非空)")
    
    if status != 4:
        print("❌ 失败！状态未变为 4！Fix无效！")
        return
    else:
        print("✅ 状态已变更为 4 (待取餐)！")

    # 3. User Complete (4 -> 5)
    print(f"--- [STEP 2] 用户点击确认取餐 (Complete) ---")
    user_resp = requests.put(
        f"{BASE_URL}/user/order/complete/{order_id}",
        headers=user_headers
    )
    print(f"API响应: {user_resp.status_code} - {user_resp.text}")
    
    # 4. Final Check
    final_resp = requests.get(
        f"{BASE_URL}/admin/order/details/{order_id}",
        headers=admin_headers
    )
    final_data = final_resp.json().get('data', {})
    final_status = final_data.get('status')
    print(f"最终数据库状态: {final_status} (期望: 5)")
    
    if final_status == 5:
        print("✅ 全链路打通！修复成功！")
    else:
        print("❌ 用户确认失败。")

if __name__ == "__main__":
    test_flow()
