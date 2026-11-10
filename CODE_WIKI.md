# 旅游管理系统 - Code Wiki

## 1. 项目概述

### 1.1 项目简介
这是一个基于 **Spring Boot** 的旅游管理系统，分为**前台用户系统**和**后台管理系统**两个部分。前台系统主要面向普通用户，提供旅游产品浏览、搜索、收藏等功能；后台系统主要面向管理员，提供用户管理、产品管理、权限管理等功能。

### 1.2 核心功能
- **前台系统**：
  - 用户注册/登录（邮箱激活）
  - 旅游产品浏览和搜索
  - 产品详情查看
  - 产品收藏功能
- **后台系统**：
  - 管理员认证和授权（基于Spring Security）
  - 旅游产品管理（增删改查）
  - 产品分类管理
  - 管理员管理
  - 角色权限管理
  - 操作日志记录（AOP实现）

---

## 2. 技术栈

### 2.1 后端技术
| 技术/框架 | 版本 | 用途 |
|---------|------|------|
| Spring Boot | 2.7.0 | 核心框架 |
| Spring Security | - | 安全认证与授权 |
| MyBatis Plus | 3.5.0 | ORM框架 |
| Thymeleaf | - | 模板引擎 |
| MySQL | - | 数据库 |
| Lombok | - | 简化代码 |
| Spring AOP | - | 切面编程（日志记录） |
| Spring Mail | - | 邮件发送 |

### 2.2 前端技术
- Bootstrap
- jQuery
- WangEditor（富文本编辑器）
- Chart.js（图表库）
- AdminLTE（后台UI框架）

### 2.3 构建工具
- Maven
- Docker（支持Docker容器化部署）

---

## 3. 项目结构

```
travel
├── src
│   ├── main
│   │   ├── java/com/itbaizhan/travel
│   │   │   ├── aop                  # 切面模块
│   │   │   ├── bean                 # 业务Bean
│   │   │   ├── controller           # 控制器
│   │   │   │   ├── backstage        # 后台控制器
│   │   │   │   └── frontdesk        # 前台控制器
│   │   │   ├── mapper               # 数据访问层
│   │   │   ├── pojo                 # 实体类
│   │   │   ├── security             # 安全模块
│   │   │   ├── service              # 业务逻辑层
│   │   │   ├── util                 # 工具类
│   │   │   └── TravelApplication.java  # 启动类
│   │   └── resources
│   │       ├── com/itbaizhan/travel/mapper  # MyBatis映射文件
│   │       ├── static               # 静态资源
│   │       │   ├── backstage        # 后台静态资源
│   │       │   └── frontdesk        # 前台静态资源
│   │       ├── templates            # 模板文件
│   │       │   ├── backstage        # 后台模板
│   │       │   └── frontdesk        # 前台模板
│   │       ├── application.yml      # 配置文件
│   │       └── logback.xml          # 日志配置
```

---

## 4. 核心入口

### 4.1 启动类
**文件路径**：[TravelApplication.java](file:///d:/JAVAProject/travel%20-%20副本/src/main/java/com/itbaizhan/travel/TravelApplication.java)

```java
@SpringBootApplication
@MapperScan("com.itbaizhan.travel.mapper")
@ServletComponentScan
public class TravelApplication {
    public static void main(String[] args) {
        SpringApplication.run(TravelApplication.class, args);
    }
    
    // 注册MyBatis Plus分页插件
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

**功能说明**：
- Spring Boot应用入口
- 配置Mapper扫描路径
- 配置Servlet组件扫描
- 注册MyBatis Plus分页插件

### 4.2 配置文件
**文件路径**：[application.yml](file:///d:/JAVAProject/travel%20-%20副本/src/main/resources/application.yml)

**关键配置**：
- 服务端口：8080
- 数据库：MySQL
- 文件上传：最大10MB
- Thymeleaf模板缓存：关闭（开发环境）

---

## 5. 系统架构

### 5.1 整体架构
系统采用**分层架构**设计，遵循MVC模式：

```
┌─────────────────────────────────────────┐
│           前端页面（Thymeleaf）           │
├─────────────────────────────────────────┤
│         Controller层（控制器）           │
├─────────────────────────────────────────┤
│         Service层（业务逻辑）            │
├─────────────────────────────────────────┤
│         Mapper层（数据访问）             │
├─────────────────────────────────────────┤
│           MySQL数据库                    │
└─────────────────────────────────────────┘
```

### 5.2 模块职责

| 模块 | 主要职责 |
|-----|---------|
| Controller层 | 接收请求，调用Service，返回视图或数据 |
| Service层 | 业务逻辑处理，事务控制 |
| Mapper层 | 数据库操作，基于MyBatis Plus |
| Security模块 | 认证、授权、权限控制 |
| AOP模块 | 操作日志记录 |

---

## 6. 核心实体类

### 6.1 Admin（管理员）
**文件路径**：[Admin.java](file:///d:/JAVAProject/travel%20-%20副本/src/main/java/com/itbaizhan/travel/pojo/Admin.java)

| 字段 | 类型 | 说明 |
|-----|------|------|
| aid | Integer | 管理员ID（主键） |
| username | String | 用户名 |
| password | String | 密码 |
| phoneNum | String | 手机号 |
| email | String | 邮箱 |
| status | boolean | 状态（true可用，false禁用） |
| roles | List&lt;Role&gt; | 角色集合（非数据库字段） |

### 6.2 Product（旅游产品）
**文件路径**：[Product.java](file:///d:/JAVAProject/travel%20-%20副本/src/main/java/com/itbaizhan/travel/pojo/Product.java)

| 字段 | 类型 | 说明 |
|-----|------|------|
| pid | Integer | 产品ID（主键） |
| productName | String | 产品名称 |
| price | BigDecimal | 价格 |
| hotline | String | 热线电话 |
| status | Boolean | 产品状态 |
| productDesc | String | 产品详情 |
| pImage | String | 产品图片 |
| cid | Integer | 产品分类ID |
| category | Category | 产品分类对象（非数据库字段） |

### 6.3 Member（会员）
**文件路径**：[Member.java](file:///d:/JAVAProject/travel%20-%20副本/src/main/java/com/itbaizhan/travel/pojo/Member.java)

| 字段 | 类型 | 说明 |
|-----|------|------|
| mid | Integer | 会员ID（主键） |
| username | String | 用户名 |
| password | String | 密码 |
| email | String | 邮箱 |
| phoneNum | String | 手机号 |
| sex | String | 性别 |
| birthday | Date | 生日 |
| active | boolean | 是否激活 |
| activeCode | String | 激活码 |

### 6.4 Category（产品分类）
**文件路径**：[Category.java](file:///d:/JAVAProject/travel%20-%20副本/src/main/java/com/itbaizhan/travel/pojo/Category.java)

| 字段 | 类型 | 说明 |
|-----|------|------|
| cid | Integer | 分类ID（主键） |
| cname | String | 分类名称 |

---

## 7. 核心控制器

### 7.1 后台产品控制器
**文件路径**：[ProductController.java](file:///d:/JAVAProject/travel%20-%20副本/src/main/java/com/itbaizhan/travel/controller/backstage/ProductController.java)

**主要接口**：
- `GET /backstage/product/all` - 分页查询产品列表
- `GET /backstage/product/addPage` - 跳转到添加产品页面
- `POST /backstage/product/add` - 添加产品
- `POST /backstage/product/upload` - 上传产品图片
- `GET /backstage/product/edit` - 跳转到编辑页面
- `POST /backstage/product/update` - 更新产品
- `GET /backstage/product/updateStatus` - 更新产品状态

### 7.2 页面控制器
**文件路径**：[PageController.java](file:///d:/JAVAProject/travel%20-%20副本/src/main/java/com/itbaizhan/travel/controller/PageController.java)

**主要接口**：
- `GET /backstage/{page}` - 访问后台页面
- `GET /frontdesk/{page}` - 访问前台页面

---

## 8. 核心服务层

### 8.1 ProductService（产品服务）
**文件路径**：[ProductService.java](file:///d:/JAVAProject/travel%20-%20副本/src/main/java/com/itbaizhan/travel/service/ProductService.java)

**主要方法**：
- `findPage(int page, int size)` - 分页查询产品
- `add(Product product)` - 添加产品
- `findOne(int pid)` - 查询单个产品
- `update(Product product)` - 更新产品
- `updateStatus(Integer pid)` - 更新产品状态
- `findProduct(Integer cid, String productName, int page, int size)` - 条件查询产品

---

## 9. 安全认证模块

### 9.1 SecurityConfig（安全配置）
**文件路径**：[SecurityConfig.java](file:///d:/JAVAProject/travel%20-%20副本/src/main/java/com/itbaizhan/travel/security/SecurityConfig.java)

**功能说明**：
- 配置自定义登录页面
- 配置权限拦截规则
- 配置退出登录
- 配置异常处理
- 配置密码编码器（BCrypt）

### 9.2 MyUserDetailService（用户详情服务）
**文件路径**：[MyUserDetailService.java](file:///d:/JAVAProject/travel%20-%20副本/src/main/java/com/itbaizhan/travel/security/MyUserDetailService.java)

**功能说明**：
- 实现Spring Security的UserDetailsService接口
- 自定义认证逻辑
- 自定义授权逻辑（加载用户权限）

---

## 10. AOP日志模块

### 10.1 LogAop（日志切面）
**文件路径**：[LogAop.java](file:///d:/JAVAProject/travel%20-%20副本/src/main/java/com/itbaizhan/travel/aop/LogAop.java)

**功能说明**：
- 记录后台所有Controller操作日志
- 记录访问时间、执行时长、IP、URL、用户名
- 记录异常信息

---

## 11. 数据访问层

### 11.1 ProductMapper
**文件路径**：[ProductMapper.java](file:///d:/JAVAProject/travel%20-%20副本/src/main/java/com/itbaizhan/travel/mapper/ProductMapper.java)

**映射文件**：[ProductMapper.xml](file:///d:/JAVAProject/travel%20-%20副本/src/main/resources/com/itbaizhan/travel/mapper/ProductMapper.xml)

**主要方法**：
- `findProductPage(Page<Product> page)` - 分页查询产品（关联分类）
- `findOne(int pid)` - 查询单个产品（关联分类）
- `findFavoriteByPidAndMid(Integer pid, Integer mid)` - 查询收藏关系
- `addFavorite(Integer pid, Integer mid)` - 添加收藏
- `delFavorite(Integer pid, Integer mid)` - 删除收藏
- `findMemberFavorite(Page<Product> page, Integer mid)` - 查询会员收藏

---

## 12. 依赖关系

### 12.1 主要依赖树
```
TravelApplication (启动类)
├── Controller层
│   ├── 依赖Service层
│   └── 依赖Spring Security
├── Service层
│   ├── 依赖Mapper层
│   └── 事务控制
├── Mapper层
│   └── 依赖MyBatis Plus
└── Security模块
    ├── 依赖Service层
    └── 依赖Spring Security
```

### 12.2 Maven依赖（核心）
- spring-boot-starter-web
- spring-boot-starter-thymeleaf
- spring-boot-starter-security
- spring-boot-starter-aop
- spring-boot-starter-mail
- mybatis-plus-boot-starter
- mysql-connector-java
- lombok

---

## 13. 项目运行方式

### 13.1 环境要求
- JDK 11+
- Maven 3.6+
- MySQL 5.7+

### 13.2 数据库配置
1. 创建数据库：`travel`
2. 修改配置文件中的数据库连接信息
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://your-host:3306/travel?serverTimezone=UTC
       username: your-username
       password: your-password
   ```

### 13.3 运行项目
**方式一：使用Maven**
```bash
mvn spring-boot:run
```

**方式二：打包运行**
```bash
mvn clean package
java -jar target/travel-0.0.1-SNAPSHOT.jar
```

**方式三：Docker运行**
```bash
mvn clean package docker:build
docker run -p 8080:8080 travel:0.0.1-SNAPSHOT
```

### 13.4 访问地址
- **前台系统**：http://localhost:8080/frontdesk/index
- **后台系统**：http://localhost:8080/backstage/admin_login

---

## 14. 关键业务流程

### 14.1 后台登录流程
```
1. 用户访问登录页面
2. 输入用户名密码提交
3. MyUserDetailService认证
4. Security授权（加载权限）
5. 登录成功跳转到首页
```

### 14.2 产品管理流程
```
1. 管理员登录
2. 访问产品列表（分页）
3. 可添加/编辑/删除产品
4. 可上传产品图片
5. 可启用/禁用产品
```

### 14.3 前台用户注册流程
```
1. 用户填写注册信息
2. 系统发送激活邮件
3. 用户点击激活链接
4. 账户激活成功
5. 用户可以登录
```

---

## 15. 注意事项

1. **数据库密码**：配置文件中包含明文密码，生产环境需加密
2. **邮箱配置**：邮件功能需配置正确的SMTP服务器信息
3. **文件上传**：上传文件存储在classpath下，生产环境建议使用独立存储
4. **Security权限**：方法级权限控制使用`@PreAuthorize`注解
5. **日志**：操作日志仅记录在日志文件中，未持久化到数据库

---

## 16. 扩展建议

1. 引入Redis缓存热点数据
2. 实现产品搜索功能（Elasticsearch）
3. 添加订单管理模块
4. 完善日志系统（持久化到数据库）
5. 实现前后端分离（使用Vue/React）
6. 添加接口文档（Swagger）

