# 校园二手交易平台（label-3360）

## 项目说明
这是一个基于 **Spring Boot + MyBatis + MySQL + Thymeleaf** 的校园二手交易平台，覆盖登录注册、商品发布交易、订单查看、管理员后台管理等完整流程。

## 技术栈
- Backend: Spring Boot 3, Spring MVC, Spring Security, MyBatis
- Frontend: Thymeleaf + 自定义 CSS
- Database: MySQL 8.0
- Deploy: Docker Compose（frontend + backend + db）

## 服务地址
- Frontend（Nginx 代理入口）: `http://localhost:3000`
- Backend（Spring Boot）: `http://localhost:8000`
- MySQL: `localhost:3306` (`root` / `root`)

## 目录结构
- `label-3360/backend`: 后端主项目（Java + 模板 + SQL 初始化）
- `label-3360/frontend/nginx.conf`: 前端网关与反向代理配置
- `label-3360/docker-compose.yml`: 一键启动编排

## 启动方式
### Docker 一键启动
```bash
cd label-3360
docker compose up --build
```

### IDEA 本地启动后端
1. 打开 `label-3360/backend` Maven 项目
2. 准备本地 MySQL（`campus_trade`，`root/root`）
3. 运行 `com.example.campustrade.CampusTradeApplication`
4. 访问 `http://localhost:8000/items`

## 功能清单
### 认证与角色
- 登录地址：`/login`
- 注册地址：`/register`
- 注册用户默认角色：`USER`
- 管理员账号登录后默认进入：`/admin/items`

### 商品前台（普通用户）
- 商品列表：`/items`
  - 仅展示在售商品 `ACTIVE`
  - 支持分页（`page` / `size`）
- 商品发布：`/items/new`
- 商品详情：`/items/{id}`
- 购买交易：普通用户可购买他人商品，成交后自动变更为 `SOLD`

### 我的发布（普通用户）
- 页面：`/items/mine`
- 展示自己发布的所有商品（在售 + 已成交）
- 展示买家信息：`买家ID + 买家名称`（未成交显示 `-`）
- 已卖出商品不可编辑/删除（前后端双重限制）

### 我的订单（普通用户）
- 页面：`/items/orders`
- 展示自己购买记录
- 展示卖家信息：`卖家ID + 卖家名称`

### 后台管理（管理员）
- 用户管理：`/admin/users`
  - 支持分页
  - 可删除普通用户（会清理其发布商品及相关买家关联）
- 商品管理：`/admin/items`
  - 支持分页
  - 展示卖家、买家：`ID + 名称`
  - 仅允许删除普通用户发布且未卖出的商品
  - `SOLD` 商品不展示删除按钮且后端拦截删除请求
- 管理员在商品前台只读，不可发布/购买/编辑/删除

### 编辑/删除后的回跳行为
- 从商品列表发起编辑/删除：完成后回到 `/items`
- 从“我发布的”发起编辑/删除：完成后回到 `/items/mine`
- 回跳会保留 `page` 和 `size` 分页参数

## 分页覆盖范围
- `/items`
- `/items/mine`
- `/items/orders`
- `/admin/items`
- `/admin/users`

## 初始化数据
- 启动时自动执行 `schema.sql` 与 `data.sql`
- 预置 60+ 条商品（含 `ACTIVE` 与 `SOLD`）用于分页与交易场景验证

## 测试账号
- Admin: `admin / 123456`
- User: `alice / 123456`
- User: `bob / 123456`

## 测试
- 单测框架：JUnit 5 + Spring Boot Test + Mockito
- 已覆盖 `ItemServiceImplTest`、`ItemControllerTest`
- 可执行（Docker test stage）：

```bash
cd /Users/lu/go/src/parttime/3360
docker build --target test -t campus-trade-backend-test:3360 ./label-3360/backend
```
