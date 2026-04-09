# 桃资源管理系统后端

## 环境要求

- JDK 17
- Maven 3.8+
- MySQL 8

## 启动步骤

1. 在 MySQL 中执行 [src/main/resources/sql/peach.sql](src/main/resources/sql/peach.sql)
2. 根据实际环境修改 [src/main/resources/application.yml](src/main/resources/application.yml) 的数据库账号密码、本地文件目录、微信小程序 `app-id` 和 `app-secret`
3. 执行 `mvn spring-boot:run`，或直接运行 `com.example.peach.PeachApplication`

## 默认账号

- 管理员：`admin / 123456`
- 小程序测试账号：`miniapp01 / 123456`

## 目录说明

- `common`：通用配置、异常、返回体、安全组件
- `modules/auth`：登录认证、当前用户、修改密码
- `modules/category`：种属分类管理
- `modules/user`：账号管理
- `modules/variety`：品种管理
- `modules/file`：本地图片上传
- `modules/qrcode`：二维码生成、查询、导出

## 在线文档

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 接口说明

- 认证：`/api/auth/login`、`/api/auth/miniapp/login`、`/api/auth/logout`、`/api/auth/info`、`/api/auth/password`
- 种属：`/api/categories/tree`、`/api/categories`
- 用户：`/api/users/page`、`/api/users/{id}`、`/api/users`、`/api/users/resetPassword`、`/api/users/status`
- 品种：`/api/varieties/page`、`/api/varieties/{id}`、`/api/varieties`、`/api/varieties/export`
- 文件：`/api/files/upload`
- 二维码：`/api/qrcode/generate/{id}`、`/api/qrcode/{id}`、`/api/qrcode/export`、`/api/qrcode/scan/{id}`、`/api/qrcode/records/page`

## 小程序登录说明

- 小程序前端先调用 `wx.login()` 获取 `loginCode`
- 再调用 `getPhoneNumber` 获取 `phoneCode`
- 后端调用微信 `code2Session` 和 `getuserphonenumber` 官方接口，拿到 `openid` 与手机号
- 若本地不存在该小程序用户，系统自动创建 `MINIAPP` 账号
- 登录返回包含 `userType`、`identity`、`admin`，前端可直接区分管理员与普通用户

## 上传目录

- 支持 `local` 和 `rustfs` 两种存储方式
- 当前默认配置为 RustFS：`http://localhost:9000`
- 模板图 `moban.png` 仍从 `D:/project/taoyuan/data` 本地读取
- 二维码卡片模板默认读取 `D:/project/taoyuan/data/moban.png`
- 新增品种后会自动生成二维码卡片，修改品种时会同步刷新二维码卡片
