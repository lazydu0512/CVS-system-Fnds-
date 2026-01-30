# 演唱会视频分享平台 (Concert Video Sharing System)

基于 Spring Boot + Vue3 的演唱会视频分享平台，支持用户注册登录、视频上传分享、评论点赞收藏等功能。

## 📚 文档导航

### 快速开始
- **[项目启动指南](./项目启动指南.md)** ⭐ - 详细的环境配置和启动步骤
- **[常见问题FAQ](./常见问题FAQ.md)** - 常见问题解答和故障排查

### 功能和使用
- **[功能说明文档](./功能说明文档.md)** - 完整的功能介绍和使用说明
- **[测试清单](./测试清单.md)** - 功能测试指南和测试用例

### 开发文档
- **[开发指南](./开发指南.md)** - 开发环境搭建和开发规范
- **[API接口文档](./API接口文档.md)** - 所有API接口的详细说明
- **[数据库设计文档](./数据库设计文档.md)** - 数据库表结构和设计说明

### 部署和维护
- **[部署指南](./部署指南.md)** - 生产环境部署步骤

### 项目管理
- **[项目交付清单](./项目交付清单.md)** ⭐ - 项目交付内容和质量确认
- **[项目检查清单](./项目检查清单.md)** - 项目提交前的检查清单
- **[项目完善总结](./项目完善总结.md)** - 项目完善过程和技术总结
- **[文档索引](./文档索引.md)** - 所有文档的快速导航

## 🚀 快速启动

### 一键启动（Windows）
双击运行 `启动项目.bat` 即可自动启动前后端服务。

### 手动启动
详见 [项目启动指南](./项目启动指南.md)

## 项目特性

### 系统功能
- ✅ 用户注册、登录
- ✅ 视频上传、列表查询、审核
- ✅ 评论功能
- ✅ 点赞功能
- ✅ 收藏功能
- ✅ 城市管理员审核功能

### 技术栈
- **后端**: Spring Boot 3.x + MyBatis Plus + MySQL 8.0
- **前端**: Vue 3 + Vite + Element Plus + Pinia + Axios
- **数据库**: MySQL 8.0

## 项目结构

```
CVS-system/
├── CVS-port/                 # 后端项目
│   ├── src/main/java/cn/edu/seig/fnds/
│   │   ├── controller/       # 控制器层
│   │   ├── service/          # 服务层
│   │   ├── mapper/           # 数据访问层
│   │   ├── entity/           # 实体类
│   │   └── config/           # 配置类
│   ├── src/main/resources/
│   │   └── application.properties  # 配置文件
│   └── init.sql              # 数据库初始化脚本
└── CVS-view/                 # 前端项目
    ├── src/
    │   ├── views/            # 页面组件
    │   ├── stores/           # Pinia状态管理
    │   ├── api/              # API接口
    │   └── router/           # 路由配置
    ├── public/               # 静态资源
    └── package.json          # 项目依赖
```

## 快速开始

### 环境要求
- JDK 17+
- MySQL 8.0+
- Node.js 16+
- Maven 3.6+

### 方式一：一键启动（推荐）
1. 确保MySQL服务已启动
2. 双击运行 `启动项目.bat`
3. 等待服务启动完成
4. 访问 http://localhost:5173

### 方式二：手动启动

#### 1. 数据库配置
```bash
# 登录MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE IF NOT EXISTS cvs_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入数据
USE cvs_db;
source CVS-port/init.sql;
```

#### 2. 修改配置
编辑 `CVS-port/src/main/resources/application.properties`:
```properties
spring.datasource.username=你的MySQL用户名
spring.datasource.password=你的MySQL密码
```

#### 3. 启动后端
```bash
cd CVS-port
mvn spring-boot:run
```
后端服务：http://localhost:8080

#### 4. 启动前端
```bash
cd CVS-view

# 安装依赖（首次运行）
npm install

# 启动服务
npm run dev
```
前端服务：http://localhost:5173

> 💡 详细步骤请参考 [项目启动指南](./项目启动指南.md)

## 系统角色

### 普通用户
- 注册登录
- 浏览视频
- 上传视频（需审核）
- 评论、点赞、收藏视频

### 城市管理员
- 拥有普通用户所有权限
- 审核用户上传的视频
- 管理视频内容

## API 接口

### 用户相关
- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `GET /api/user/{id}` - 获取用户信息
- `PUT /api/user/{id}` - 更新用户信息

### 视频相关
- `GET /api/video/list` - 获取视频列表（分页）
- `POST /api/video/upload` - 上传视频
- `GET /api/video/{id}` - 获取视频详情
- `GET /api/video/user/{userId}` - 获取用户视频
- `GET /api/video/pending` - 获取待审核视频（管理员）
- `POST /api/video/{id}/review` - 审核视频（管理员）

### 评论相关
- `POST /api/comment` - 添加评论
- `GET /api/comment/video/{videoId}` - 获取视频评论
- `DELETE /api/comment/{id}` - 删除评论

### 点赞相关
- `POST /api/video-like/{videoId}` - 点赞/取消点赞
- `GET /api/video-like/{videoId}/status` - 检查点赞状态

### 收藏相关
- `POST /api/video-collect/{videoId}` - 收藏/取消收藏
- `GET /api/video-collect/{videoId}/status` - 检查收藏状态
- `GET /api/video-collect/user` - 获取用户收藏

## 测试账号

| 角色 | 用户名 | 密码 | 权限 |
|------|--------|------|------|
| 管理员 | admin | 123456 | 所有权限 + 视频审核 |
| 普通用户 | user1 | 123456 | 基本功能 |
| 普通用户 | user2 | 123456 | 基本功能 |

> 💡 更多测试用例请参考 [测试清单](./测试清单.md)

## 开发说明

### 后端规范
- 使用 MyBatis Plus BaseMapper
- 实体类使用 Lombok @Data 注解
- Controller 接口符合 REST 风格
- 统一返回格式: `{success: boolean, message: string, data: any}`

### 前端规范
- 使用 Vue 3 Composition API
- 使用 Pinia 进行状态管理
- 使用 Element Plus UI 组件库
- 使用 Axios 进行 HTTP 请求

## 部署说明

### 生产环境构建

1. **后端打包**
```bash
cd CVS-port
mvn clean package -DskipTests
```

2. **前端构建**
```bash
cd CVS-view
npm run build
```

3. **部署**
- 将后端 jar 包部署到服务器
- 将前端 dist 目录部署到 Nginx 或其他 Web 服务器
- 配置反向代理将 `/api` 请求转发到后端服务

## 项目特色

1. **完整的用户权限系统**: 支持普通用户和管理员两种角色
2. **视频审核机制**: 确保内容质量和合规性
3. **丰富的社交功能**: 评论、点赞、收藏等互动功能
4. **现代化的技术栈**: 使用最新的前端和后端技术
5. **响应式设计**: 支持移动端和桌面端访问
6. **RESTful API**: 规范的API设计，易于扩展和维护

## 注意事项

1. 上传的视频文件需要配置合适的存储服务（如阿里云OSS、腾讯云COS等）
2. 生产环境需要配置 HTTPS
3. 数据库密码等敏感信息不要提交到代码仓库
4. 定期备份数据库数据
5. 注意版权问题，平台内容应符合相关法律法规

## 许可证

本项目仅用于学习和毕业设计使用。