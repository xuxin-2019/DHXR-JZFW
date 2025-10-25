# 家政服务平台后端

## 项目介绍
这是一个家政服务平台的后端系统，采用Spring Boot + MyBatis-Plus + MySQL技术栈开发，提供了用户、护工、订单、服务类型、评价、通知等模块的功能接口。

## 技术栈
- 框架：Spring Boot 2.x
- ORM框架：MyBatis-Plus
- 数据库：MySQL 5.7+
- API文档：Swagger 2
- 构建工具：Maven

## 项目结构
```
src/main/java/com/homemaker/
├── common/             # 公共类，如统一返回结果
├── config/             # 配置类，如Swagger、跨域等配置
├── controller/         # 控制器层，处理HTTP请求
├── entity/             # 实体类，对应数据库表
├── exception/          # 异常处理相关类
├── mapper/             # Mapper接口，数据访问层
├── service/            # 服务层接口
│   └── impl/           # 服务层实现类
├── utils/              # 工具类
└── HomeMakerApplication.java  # 应用启动类
```

## 功能模块

### 1. 用户模块
- 用户注册
- 用户登录
- 获取和更新用户信息

### 2. 护工模块
- 护工注册
- 护工登录
- 获取和更新护工信息
- 查询空闲护工
- 更新护工状态

### 3. 管理员模块
- 管理员登录
- 获取和更新管理员信息

### 4. 服务类型模块
- 获取所有服务类型
- 根据ID获取服务类型
- 添加、更新、删除服务类型

### 5. 订单模块
- 创建订单
- 查询用户订单
- 查询护工订单
- 查询所有订单（管理员）
- 更新订单状态
- 分配订单给护工

### 6. 评价模块
- 创建评价
- 根据订单ID查询评价

### 7. 通知模块
- 创建通知
- 查询用户通知
- 查询护工通知
- 标记通知为已读

## 环境要求
- JDK 1.8+
- MySQL 5.7+
- Maven 3.6+

## 数据库配置
在 `src/main/resources/application.yml` 文件中配置数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/home_make_db?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
```

## 运行项目
1. 确保MySQL数据库已启动，并且创建了名为`home_make_db`的数据库
2. 执行 `mvn clean install` 构建项目
3. 执行 `mvn spring-boot:run` 启动项目
4. 访问 `http://localhost:8080/homemaker/swagger-ui.html` 查看API文档

## 接口文档
项目使用Swagger生成API文档，启动项目后可通过以下地址访问：
`http://localhost:8080/homemaker/swagger-ui.html`

## 跨域配置
项目已配置跨域支持，允许所有域名的请求访问API接口。

## 异常处理
项目实现了全局异常处理机制，统一返回格式为：
```json
{
  "code": 响应码,
  "message": 响应消息,
  "data": 响应数据
}
```
其中：
- 响应码200表示成功
- 响应码500表示失败

## 注意事项
1. 密码采用MD5加密存储
2. 所有接口返回统一格式的JSON数据
3. 项目使用MyBatis-Plus简化数据库操作
4. 请确保数据库中已创建所需的表结构