# 在线购物平台 MVP

> **课程**: 软件质量与测试课 2025-2026-2 学期
> **技术栈**: Spring Boot 3 + Thymeleaf + H2 + Bootstrap 5 + JUnit 5 + Selenium + JMeter

---

## 一、项目概述

本系统是一个完整的在线购物平台最小可行产品（MVP），涵盖从用户注册登录、商品浏览搜索、购物车管理到订单结算的核心业务流程。系统包含两类角色：普通用户（买家）和系统管理员。

### 核心功能模块

| 模块 | 功能描述 |
|------|----------|
| 用户注册/登录 | 密码复杂度校验、用户名唯一性、连续5次失败锁定15分钟 |
| 商品浏览/搜索 | 分页列表、关键字模糊搜索、商品详情查看 |
| 购物车管理 | 添加商品、修改数量、删除商品、总金额实时计算 |
| 订单结算 | 收货信息校验、库存扣减、订单生成 |
| 订单管理 | 查看订单列表、模拟付款、取消订单、确认收货 |
| 管理员 | 商品上架/编辑/下架、订单确认/发货 |

### 订单状态流转

```
用户提交订单 → 待付款 → 管理员确认 → 用户付款 → 已付款 → 管理员发货 → 已发货 → 用户确认收货 → 已完成
                  ↘ 用户取消 / 3天超时 → 已取消（库存恢复）
```

---

## 二、技术架构

```
┌────────────────────────────────────────────┐
│              浏览器 (Browser)               │
├────────────────────────────────────────────┤
│    Thymeleaf + Bootstrap 5 (前端渲染)       │
├────────────────────────────────────────────┤
│         Spring Boot 3 (后端框架)             │
│  ┌──────────┬──────────┬──────────────┐    │
│  │Controller│ Service  │  Repository  │    │
│  └──────────┴──────────┴──────────────┘    │
├────────────────────────────────────────────┤
│        Spring Data JPA (数据访问层)          │
├────────────────────────────────────────────┤
│           H2 Database (嵌入式数据库)         │
└────────────────────────────────────────────┘
```

- **后端**: Java 17 + Spring Boot 3.2.5 + Spring Data JPA
- **数据库**: H2 (文件模式存储，无需安装外部数据库)
- **前端**: Thymeleaf + Bootstrap 5 + Bootstrap Icons
- **构建工具**: Maven

---

## 三、快速开始

### 3.1 环境要求

- JDK 17+
- Maven 3.6+
- IDE: Eclipse / IntelliJ IDEA

### 3.2 启动项目

#### 方式一：双击脚本（推荐）

进入项目目录，双击 **`run.bat`** 即可启动系统并自动打开浏览器。

#### 方式二：命令行

```bash
# 1. 进入项目目录
cd online-shopping-platform

# 2. 编译并启动
mvnw.cmd spring-boot:run

# 3. 访问系统
# 前台首页: http://localhost:9090/product/list
# H2控制台: http://localhost:9090/h2-console
```

### 3.3 预置测试账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | `admin` | `Admin@123` | 可管理商品和订单 |
| 普通用户 | `testuser` | `Test@123` | 可浏览、购买、下单 |

### 3.4 注册规则

**用户名**: 6-20个字符，以字母开头，仅允许英文字母、数字和下划线
**密码**: 8-16个字符，必须同时包含大写字母、小写字母、数字和特殊字符(@#$%^&*)

---

## 四、测试指南

### 4.1 JUnit 单元测试

双击 **`test.bat`** 运行所有单元测试，或在命令行执行：

```bash
# 运行所有单元测试
mvnw.cmd test

# 运行指定测试类
mvnw.cmd test -Dtest=UserServiceTest
```

测试覆盖:
- **UserServiceTest**: 注册/登录功能，包含边界值测试（用户名5/6/20/21字符、密码7/8/16/17字符）、等价类划分、锁定机制测试
- 测试涵盖了第4次失败(未锁定)、第5次失败(触发锁定)等边界场景

### 4.2 Selenium UI 自动化测试

```bash
# 前置条件：
# 1. 系统已启动在 http://localhost:9090
# 2. Chrome 浏览器已安装
# 3. chromedriver 已配置在系统 PATH 中

双击 uitest.bat 运行，或执行：

mvnw.cmd test -Dtest=SeleniumUITest
```

测试场景覆盖:
| 用例ID | 测试场景 | 检查点 |
|--------|----------|--------|
| UI-01 | 登录页加载 | 页脚课程标识 |
| UI-02 | 用户登录 | 登录成功跳转 |
| UI-03 | 首页课程标识 | 页脚课程标识 |
| UI-04 | 商品搜索 | 搜索结果正确 |
| UI-05 | 空关键字搜索 | 错误提示 |
| UI-06 | 加入购物车 | Alert提示、数量更新 |
| UI-07 | 购物车页 | 课程标识 |
| UI-08 | 结算页 | 课程标识 |
| UI-09 | 端到端流程 | 登录→搜索→加购→结算→提交 |
| UI-10 | 我的订单页 | 课程标识、订单记录 |
| UI-11 | 个人信息页 | 课程标识 |
| UI-12 | 登录失败 | 错误提示正确 |

### 4.3 JMeter 性能测试

```bash
# 使用 JMeter GUI 打开测试计划
jmeter -t jmeter/jmeter-test-plan.jmx
```

测试计划包含:
- **日常负载**: 50并发用户，持续请求（商品浏览、搜索、购物车）
- **峰值负载**: 200并发用户，持续5分钟（集中登录场景）

性能指标要求:

| 事务类型 | 90%响应时间 | 平均响应时间 |
|----------|------------|-------------|
| 静态资源加载 | ≤ 1.5秒 | ≤ 1.0秒 |
| 登录/注册接口 | ≤ 2.0秒 | ≤ 1.5秒 |
| 搜索接口 | ≤ 3.0秒 | ≤ 2.0秒 |
| 添加购物车接口 | ≤ 2.0秒 | ≤ 1.5秒 |
| 提交订单接口 | ≤ 3.5秒 | ≤ 2.5秒 |

其他要求:
- 30分钟50并发测试下，事务失败率 < 1%
- 服务器CPU使用率不持续超过80%

---

## 五、项目结构

```
online-shopping-platform/
├── pom.xml                              # Maven 配置
├── README.md                            # 项目说明
├── jmeter/
│   └── jmeter-test-plan.jmx             # JMeter 性能测试计划
├── data/                                # H2 数据库文件（运行时自动生成）
└── src/
    ├── main/
    │   ├── java/com/shop/
    │   │   ├── ShopApplication.java      # 启动类
    │   │   ├── config/
    │   │   │   ├── WebConfig.java        # Web MVC 配置
    │   │   │   ├── LoginInterceptor.java # 登录拦截器
    │   │   │   ├── DataInitializer.java  # 初始数据加载
    │   │   │   └── OrderScheduler.java   # 超时订单自动取消
    │   │   ├── controller/
    │   │   │   ├── UserController.java   # 用户控制器
    │   │   │   ├── ProductController.java# 商品控制器
    │   │   │   ├── CartController.java   # 购物车控制器
    │   │   │   ├── OrderController.java  # 订单控制器
    │   │   │   └── AdminController.java  # 管理员控制器
    │   │   ├── model/
    │   │   │   ├── User.java             # 用户实体
    │   │   │   ├── Product.java          # 商品实体
    │   │   │   ├── CartItem.java         # 购物车项实体
    │   │   │   ├── Order.java            # 订单实体
    │   │   │   └── OrderItem.java        # 订单项实体
    │   │   ├── repository/               # 数据访问层
    │   │   │   ├── UserRepository.java
    │   │   │   ├── ProductRepository.java
    │   │   │   ├── CartItemRepository.java
    │   │   │   ├── OrderRepository.java
    │   │   │   └── OrderItemRepository.java
    │   │   └── service/                  # 业务逻辑层
    │   │       ├── UserService.java      # 用户服务（注册/登录/个人信息）
    │   │       ├── ProductService.java   # 商品服务（CRUD/搜索）
    │   │       ├── CartService.java      # 购物车服务
    │   │       └── OrderService.java     # 订单服务（结算/支付/状态流转）
    │   └── resources/
    │       ├── application.properties    # 配置文件
    │       ├── static/css/style.css      # 自定义样式
    │       └── templates/
    │           ├── layout.html           # 公共布局（导航栏/页脚含课程标识）
    │           ├── login.html            # 登录页
    │           ├── register.html         # 注册页
    │           ├── index.html            # 首页（商品列表+搜索）
    │           ├── product-detail.html   # 商品详情
    │           ├── cart.html             # 购物车
    │           ├── checkout.html         # 订单结算
    │           ├── order-success.html    # 下单成功
    │           ├── my-orders.html        # 我的订单
    │           ├── order-detail.html     # 订单详情
    │           ├── user/profile.html     # 个人信息
    │           └── admin/
    │               ├── products.html     # 商品管理
    │               ├── product-form.html # 商品表单
    │               ├── orders.html       # 订单管理
    │               └── order-detail.html # 订单详情
    └── test/java/com/shop/
        ├── UserServiceTest.java          # JUnit 单元测试
        └── SeleniumUITest.java           # Selenium UI 自动化测试
```

---

## 六、课程要求对应

| 课设要求 | 实现说明 |
|----------|----------|
| 注册功能 | 完整前端+后端校验，用户名/密码格式验证 |
| 登录功能 | Session会话管理，5次失败锁定15分钟 |
| 用户个人信息 | 手机号/邮箱/地址的修改与展示 |
| 课程标识 | 所有关键页面的页脚包含"软件质量与测试课 2025-2026-2 学期" |
| JUnit测试 | UserServiceTest 覆盖注册/登录边界值测试 |
| Selenium测试 | SeleniumUITest 覆盖端到端购物流程 |
| JMeter测试 | 50并发日常负载 + 200并发5分钟峰值负载 |
| 输入域规格 | 严格按课设第4节要求实现所有字段校验规则 |
| 性能指标 | 按5.2节要求设定响应时间SLA |

---

## 七、注意事项

1. **H2数据库**: 数据文件存储在 `data/` 目录，如需重置数据，删除该目录后重启即可
2. **端口默认9090**: 如端口冲突，修改 `application.properties` 中的 `server.port`
3. **密码明文存储**: 为便于课程测试，密码采用明文存储；实际生产环境应使用BCrypt加密
4. **模拟支付**: 本系统不接入真实支付网关，"付款"操作为一键模拟完成
5. **定时取消**: 系统每小时自动检查并取消超过3天未付款的订单
