# Travel 旅游管理系统

前后端分离的旅游线路管理平台：后端提供 REST API，前台面向会员浏览、收藏线路，后台面向管理员维护线路、分类、账号与 RBAC 权限。

## 功能特性

### 前台会员端
- 首页轮播与热门线路推荐
- 线路列表：分类筛选、关键词搜索、分页
- 线路详情：富文本图文详情、收藏 / 取消收藏
- 会员注册：图形验证码 + 邮箱激活链接
- 会员登录：支持用户名、手机号、邮箱登录
- 我的收藏、退出登录

### 后台管理端
- 统一登录入口（管理员 / 会员身份切换）
- 数据概览：当前账号与权限一览
- 线路管理：新增、编辑、上下架、图片上传、富文本描述
- 分类管理：新增、编辑、删除、查询
- 管理员管理：新增、编辑、启用 / 禁用、分配角色
- 角色管理：增删改查、分配权限
- 权限管理：增删改查
- 操作日志：AOP 记录后台接口的访问时间、耗时、IP、URL、用户与异常

### 平台能力
- JWT 无状态认证：access + refresh 双令牌、刷新轮换、登出黑名单
- 双 Redis 实例：可淘汰缓存与不可淘汰状态分离
- 公开目录缓存：分类、线路列表、线路详情，含空值缓存与分布式锁重建
- 固定窗口限流：管理员登录、会员登录、注册
- 图形验证码、BCrypt 密码存储、方法级权限校验
- 统一响应体、全局异常处理、TraceId 链路追踪
- OpenAPI 文档与 Actuator 健康检查

## 技术栈

### 后端
| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 11 | 运行环境 |
| Spring Boot | 2.7.0 | 核心框架 |
| Spring Security | 5.x | JWT 认证与权限控制 |
| MyBatis-Plus | 3.5.0 | ORM、分页 |
| MySQL | 8.0 | 业务数据库 |
| Redis | 7 | 缓存与登录态（Lettuce + 连接池） |
| JJWT | 0.11.5 | JWT 签发与校验 |
| Spring Mail | - | 注册激活邮件 |
| Spring AOP | - | 后台操作日志 |
| springdoc-openapi | 1.6.15 | Swagger / OpenAPI |
| Lombok | - | 简化实体 |
| Maven | 3.6+ | 构建 |

### 前端
| 应用 | 技术 | 开发端口 |
|------|------|----------|
| 前台 `apps/front` | Vue 3.5、Vite 5、Pinia、Vue Router、axios | 5173 |
| 后台 `apps/admin` | Vue 3.5、Element Plus、Vite 5、Pinia、Vue Router、axios | 5174 |

包管理使用 **pnpm 9.15.9** 工作区。

## 系统架构

```
浏览器
  ├── 前台 SPA（Vue 3）  ── /api/v1/public、/api/v1/auth、/api/v1/member
  └── 后台 SPA（Vue 3）  ── /api/v1/auth、/api/v1/admin
                    │
                 Nginx（可选统一入口）
                    │  /api/v1/ → Spring Boot
                    │  /upload/ → Spring Boot（运行期上传目录 / 内置图片）
                    ↓
          Spring Boot（Controller → Service → Mapper）
             ├── MySQL（业务数据）
             ├── Redis cache（目录缓存，可淘汰）
             └── Redis state（JWT 状态/验证码/限流/锁）
```

## 项目结构

```
travel
├── src/main/java/com/itbaizhan/travel
│   ├── api/                      # REST 接口（/api/v1）
│   │   ├── AuthController        # 登录、注册、激活、刷新、登出
│   │   ├── PublicCatalogController
│   │   ├── MemberFavoriteController
│   │   ├── AdminProfileController
│   │   ├── admin/                # 后台：线路/分类/用户/角色/权限
│   │   └── dto/                  # 请求与响应 DTO
│   ├── aop/                      # 后台操作日志切面
│   ├── bean/                     # Result、日志、权限状态视图对象
│   ├── common/                   # 统一响应、分页、错误码、异常、TraceId
│   ├── config/                   # Redis、JWT、OpenAPI、上传资源映射
│   ├── infrastructure/
│   │   ├── cache/                # 缓存服务与目录缓存失效
│   │   ├── captcha/              # 图形验证码
│   │   ├── limit/                # 固定窗口限流
│   │   └── lock/                 # Redis 分布式锁
│   ├── mapper/                   # MyBatis-Plus Mapper
│   ├── pojo/                     # Admin、Member、Product、Category、Role、Permission
│   ├── security/                 # JWT 过滤链与令牌过滤器
│   │   └── jwt/
│   ├── service/                  # 业务逻辑
│   ├── util/                     # 邮件工具
│   └── TravelApplication.java
├── src/main/resources
│   ├── com/itbaizhan/travel/mapper/  # Mapper XML
│   ├── static/upload/img/product/    # 内置商品示例图片（运行期上传目录为空时回退）
│   ├── application.yml
│   └── logback.xml
├── src/test/java                 # 服务层单测 + 激活流程集成测试
├── travel-web/                   # 前端 pnpm 工作区
│   ├── apps/front                # 前台 SPA（含 public 静态资源）
│   ├── apps/admin                # 后台 SPA
│   └── package.json              # dev:front / build:front / dev:admin / build:admin
├── docker/
│   ├── nginx/                    # 统一入口（宿主机 8088）
│   └── redis/                    # cache(6379) / state(6380)
├── db/migration/                 # 数据库增量脚本
├── docs/                         # 历史改造方案与执行日志（归档）
├── backup/                       # 数据库备份
└── upload/                       # 运行期上传目录
```

## 快速开始

### 1. 环境要求

- JDK 11+
- Maven 3.6+（或使用仓库自带 `mvnw` / `mvnw.cmd`）
- MySQL 8.0+
- Redis 7（双实例：6379 缓存、6380 状态）
- Node.js 18+、pnpm 9.15.9（前端）

### 2. 初始化数据库

1. 创建数据库 `travel`，导入基础表与初始数据（可使用 `backup/travel_20261110.sql`）。
2. 执行增量脚本：

```bash
mysql -u root -p travel < db/migration/phase0_additive_ddl.sql
```

### 3. 配置环境变量

在项目根目录创建 `.env`（已在 `.gitignore` 中忽略），`application.yml` 会自动导入：

```properties
JWT_SECRET=<至少 32 字节的随机字符串>
```

常用变量：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `SERVER_PORT` | `8080` | 后端端口 |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | `localhost` / `3306` / `travel` | MySQL 连接 |
| `DB_USERNAME` / `DB_PASSWORD` | `root` / `123456` | MySQL 账号 |
| `REDIS_CACHE_HOST` / `REDIS_CACHE_PORT` | `127.0.0.1` / `6379` | 缓存实例 |
| `REDIS_STATE_HOST` / `REDIS_STATE_PORT` | `127.0.0.1` / `6380` | 状态实例 |
| `JWT_SECRET` | 占位值 | JWT 签名密钥，必须覆盖 |
| `MAIL_USER` / `MAIL_PASSWORD` | 示例 QQ 邮箱 | 发件账号与 SMTP 授权码 |
| `PROJECT_PATH` | `http://localhost:5173` | 激活邮件与登录引导的链接前缀，必须指向前台 SPA 地址（Nginx 部署改为 `http://localhost:8088` 或生产域名），不能填后端端口 |
| `UPLOAD_PATH` | `${user.dir}/upload` | 图片上传目录 |

### 4. 启动 Redis

```bash
docker compose -f docker/redis/docker-compose.yml up -d
```

### 5. 启动后端

```bash
# Windows
mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

### 6. 启动前端

```bash
cd travel-web
pnpm install
pnpm dev:front   # 前台 http://127.0.0.1:5173
pnpm dev:admin   # 后台 http://127.0.0.1:5174/admin/
```

> 两个 Vite 应用的开发代理都把 `/api` 与 `/upload` 转发到 `http://127.0.0.1:8081`。若后端使用默认 8080，请以 `SERVER_PORT=8081` 启动后端，或修改 `apps/front/vite.config.ts`、`apps/admin/vite.config.ts` 的代理目标。

### 7. 生产构建与 Nginx 统一入口

```bash
cd travel-web
pnpm build:front
pnpm build:admin

docker compose -f docker/nginx/docker-compose.yml up -d
```

Nginx 将前台挂载到 `/`、后台挂载到 `/admin/`，并把 `/api/v1/` 与 `/upload/` 都转发到后端 `8081`；后端需以 `SERVER_PORT=8081` 启动。

## 访问地址

| 入口 | 地址 |
|------|------|
| 前台开发 | http://127.0.0.1:5173 |
| 后台开发 | http://127.0.0.1:5174/admin/ |
| Nginx 统一入口 | http://127.0.0.1:8088（前台 `/`，后台 `/admin/`） |
| OpenAPI 文档 | http://localhost:8080/swagger-ui/index.html |
| 健康检查 | http://localhost:8080/actuator/health |

## API 概览（`/api/v1`）

统一响应：

```json
{
  "code": 0,
  "message": "OK",
  "data": {},
  "traceId": "3f2a9c1b7d4e8a06",
  "timestamp": 1762732800000
}
```

`code`：`0` 成功、`400` 参数错误、`401` 未登录、`403` 无权限、`404` 不存在、`409` 冲突、`429` 限流、`500` 服务异常。

### 认证 `/api/v1/auth`（公开）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/admin/login` | 管理员登录 |
| POST | `/member/login` | 会员登录 |
| POST | `/member/register` | 会员注册（需验证码） |
| GET | `/member/activate?code=` | 邮箱激活 |
| GET | `/captcha` | 获取图形验证码（Base64） |
| POST | `/refresh` | 刷新令牌（旧 refresh 立即失效） |
| POST | `/logout` | 登出（access 入黑名单、refresh 失效） |

### 公开目录 `/api/v1/public`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/categories` | 全部分类（缓存 30 分钟） |
| GET | `/products?cid=&productName=&page=1&size=10` | 线路分页（缓存 2 分钟） |
| GET | `/products/{pid}` | 线路详情（缓存 10 分钟，空值缓存 2 分钟） |

### 会员 `/api/v1/member`（`ROLE_MEMBER`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/favorites?page=1&size=10` | 我的收藏分页 |
| GET | `/favorites/{pid}/status` | 是否已收藏 |
| POST | `/favorites/{pid}` | 添加收藏 |
| DELETE | `/favorites/{pid}` | 取消收藏 |

### 管理端 `/api/v1/admin`（`ROLE_ADMIN`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/me` | 当前管理员信息与权限 |
| GET/POST | `/users` | 管理员分页 / 新增 |
| GET/PUT | `/users/{aid}` | 管理员详情 / 更新 |
| PATCH | `/users/{aid}/status` | 启用或禁用 |
| GET/PUT | `/users/{aid}/roles` | 查询 / 更新管理员角色 |
| GET/POST | `/roles` | 角色分页 / 新增 |
| GET/PUT/DELETE | `/roles/{rid}` | 角色详情 / 更新 / 删除 |
| GET/PUT | `/roles/{rid}/permissions` | 查询 / 更新角色权限 |
| GET/POST | `/permissions` | 权限分页 / 新增 |
| GET/PUT/DELETE | `/permissions/{pid}` | 权限详情 / 更新 / 删除 |
| GET/POST | `/categories` | 分类分页 / 新增 |
| GET | `/categories/all` | 全部分类 |
| GET/PUT/DELETE | `/categories/{cid}` | 分类详情 / 更新 / 删除 |
| GET/POST | `/products` | 线路分页 / 新增 |
| GET/PUT | `/products/{pid}` | 线路详情 / 更新 |
| PATCH | `/products/{pid}/status` | 上架或下架 |
| POST | `/products/upload` | 上传线路图片 |

> 分类与线路写操作会通过 `CatalogCacheInvalidator` 清理公开目录缓存。

## 认证与安全

- 仅 `/api/v1/**` 受 Spring Security 保护，会话策略为 `STATELESS`，禁用表单登录与 HTTP Basic。
- `/auth/**`、`/public/**` 放行；`/admin/**` 需要 `ROLE_ADMIN`；`/member/**` 需要 `ROLE_MEMBER`。
- `/admin/**`、`/role/**`、`/permission/**` 分别通过 `@PreAuthorize` 校验权限串（如 `/admin/all`）。
- JWT 使用 HS256，claims 含 `uid`、`typ`、`perms`；管理员 access 默认 2 小时、会员 7 天、refresh 14 天。
- refresh token 的哈希保存在 Redis `state` 实例，刷新后轮换；登出后 access 的 `jti` 进入黑名单。
- 管理员登录 5 次/分钟、会员登录 10 次/分钟、注册 5 次/10 分钟。
- 密码使用 BCrypt 存储；注册后必须通过邮件激活才能登录。
- 富文本详情在渲染前经 `sanitizeHtml` 白名单清洗，移除脚本、事件属性与危险协议。

## Redis 与缓存

| 实例 | 默认端口 | 用途 |
|------|----------|------|
| `cache` | 6379 | 分类、线路列表、线路详情缓存与空值缓存 |
| `state` | 6380 | JWT 黑名单、refresh 记录、验证码、限流计数、分布式锁 |

- `CacheService`：所有缓存强制 TTL，`__NULL__` 哨兵防穿透
- `RedisLock`：`SET NX PX` + Lua 释放，用于线路详情缓存互斥重建
- `RateLimiter`：固定窗口计数
- `CaptchaService`：4 位图形验证码，5 分钟有效，校验后删除
- `TraceIdFilter`：生成或透传 `X-Trace-Id`，写入 MDC 与响应头

## 构建与测试

```bash
# 后端编译
mvnw.cmd -DskipTests compile

# 服务层单元测试（无需外部依赖）
mvnw.cmd -Dtest=FixVerificationTest test

# 激活流程集成测试（需本机 MySQL 与 Redis）
mvnw.cmd -Dtest=ActivationFlowIntegrationTest test

# 前端生产构建
cd travel-web
pnpm build:front
pnpm build:admin
```

## 注意事项

1. `.env` 中的 `JWT_SECRET`、数据库密码、邮箱授权码不要提交到仓库。
2. 上传图片保存到 `UPLOAD_PATH/img/product`，通过 `/upload/**` 访问。`WebMvcConfig` 优先读取运行期上传目录，找不到时回退到内置的 `classpath:/static/upload/`（随包发布的商品示例图片），因此 Nginx 的 `/upload/` 应代理到后端而非直接挂载目录；多实例部署需挂共享存储或对象存储。
3. 生产环境建议关闭 MyBatis 控制台 SQL 输出（`application.yml` 中 `log-impl`）。
4. Nginx 配置默认后端端口为 `8081`，与后端默认 `8080` 不同，部署时请统一。
5. `backup/travel_20261110.sql` 为数据库备份，导入前请确认表结构与当前代码匹配。

## 质量与迁移记录

- [新栈迁移与质量检测报告](docs/新栈迁移与质量检测报告.md)：旧页面迁移核查、清理清单、构建/接口/安全检测结果与遗留风险

## 历史资料

`docs/` 下保留了前后端分离与 Redis 改造的技术方案和实施日志，用于追溯迁移过程；其中涉及的旧 Thymeleaf 入口、Session 登录等内容已不再适用。当前运行方式以本 README 为准。
