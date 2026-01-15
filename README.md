# 商品管理系统

一个基于 Spring Boot + MyBatis + Vue 3 的商品管理系统。

## 技术栈

### 后端
- Spring Boot 4.0.1
- MyBatis 3.0.3
- MySQL 9.5.0
- Java 17

### 前端
- Vue 3
- Vite
- Tailwind CSS

## 快速开始

### 前置要求

- JDK 17 或更高版本
- Maven 3.6+
- Node.js 16+ 和 npm
- MySQL 8.0+

### 1. 数据库配置

#### 创建数据库

执行以下 SQL 脚本创建数据库和表：

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS shop_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE shop_db;

-- 创建商品表
CREATE TABLE IF NOT EXISTS product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    price DECIMAL(10, 2) NOT NULL COMMENT '商品价格',
    description VARCHAR(255) COMMENT '商品描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 插入示例数据
INSERT INTO product (name, price, description) VALUES
('iPhone 15', 5999.00, '苹果最新款手机'),
('MacBook Pro', 12999.00, '苹果笔记本电脑'),
('AirPods Pro', 1899.00, '苹果无线耳机');
```

或者直接执行项目中的 SQL 文件：
```bash
mysql -u root -p < demo/src/main/resources/sql/schema.sql
```

#### 配置数据库连接

编辑 `demo/src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/shop_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
    username: root  # 修改为你的数据库用户名
    password: root  # 修改为你的数据库密码
```

### 2. 启动后端服务

```bash
# 进入后端目录
cd demo

# 使用 Maven 启动（Windows）
mvnw.cmd spring-boot:run

# 或使用 Maven 启动（Linux/Mac）
./mvnw spring-boot:run

# 或使用 IDE 直接运行 DemoApplication.java
```

后端服务将在 `http://localhost:8080` 启动。

### 3. 启动前端服务

```bash
# 进入前端目录
cd shopping-frontend-vue

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev
```

前端服务将在 `http://localhost:3000` 启动。

### 4. 访问应用

打开浏览器访问：`http://localhost:3000`

## 项目结构

```
shop/
├── demo/                          # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/example/demo/
│   │   │   │       ├── config/           # 配置类（CORS等）
│   │   │   │       ├── controller/       # 控制器
│   │   │   │       ├── entity/           # 实体类
│   │   │   │       ├── mapper/           # MyBatis Mapper接口
│   │   │   │       └── service/          # 业务逻辑层
│   │   │   └── resources/
│   │   │       ├── mapper/               # MyBatis XML映射文件
│   │   │       ├── sql/                  # SQL脚本
│   │   │       └── application.yml       # 应用配置
│   └── pom.xml
│
└── shopping-frontend-vue/        # 前端项目
    ├── src/
    │   ├── App.vue               # 主组件
    │   ├── main.js               # 入口文件
    │   └── style.css             # 样式文件
    ├── vite.config.js            # Vite配置
    └── package.json
```

## API 接口

### 商品管理

- `GET /api/product/list` - 获取所有商品
- `GET /api/product/{id}` - 根据ID获取商品
- `POST /api/product` - 创建商品
- `PUT /api/product/{id}` - 更新商品
- `DELETE /api/product/{id}` - 删除商品

## 功能特性

- ✅ 商品列表展示
- ✅ 商品添加
- ✅ 商品查询（按ID）
- ✅ 商品删除
- ✅ 跨域支持（CORS）
- ✅ 前后端代理配置

## 常见问题

### 1. 数据库连接失败

- 检查 MySQL 服务是否启动
- 确认数据库用户名和密码是否正确
- 确认数据库 `shop_db` 是否已创建

### 2. 前端无法连接后端

- 确认后端服务已启动（`http://localhost:8080`）
- 检查浏览器控制台是否有错误信息
- 确认 Vite 代理配置正确

### 3. 端口被占用

- 后端端口：修改 `application.yml` 中的 `server.port`
- 前端端口：修改 `vite.config.js` 中的 `server.port`

## 开发说明

### 后端开发

- 使用 MyBatis 进行数据库操作
- Mapper 接口定义在 `mapper` 包下
- XML 映射文件在 `resources/mapper` 目录下
- 实体类使用普通 POJO，无 JPA 注解

### 前端开发

- 使用 Vue 3 Composition API
- API 调用使用 fetch，通过 Vite 代理转发到后端
- 样式使用 Tailwind CSS

## 许可证

MIT
