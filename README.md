# 演唱会视频分享平台 (Concert Video Sharing System)

基于 Spring Boot + Vue3 的演唱会视频分享平台，支持用户注册登录、视频上传分享、评论点赞收藏等功能。

## 🎉 最新更新

### 🔒 密码安全加密升级 (2026-02-01)
- ✅ 所有用户密码现已使用 **BCrypt** 工业级加密算法存储
- ✅ 抗暴力破解和彩虹表攻击
- ✅ 符合现代安全标准
- 📖 详细说明：[密码加密改造文档](./docs/security/walkthrough.md)

---

## 📚 文档导航

### 🚀 快速开始
- **[项目启动指南](./项目启动指南.md)** ⭐ - 详细的环境配置和启动步骤
- **[快速启动指南](./快速启动指南.md)** - 快速上手指南
- **[常见问题FAQ](./常见问题FAQ.md)** - 常见问题解答和故障排查

### 📖 功能和使用
- **[功能说明文档](./功能说明文档.md)** - 完整的功能介绍和使用说明
- **[测试清单](./测试清单.md)** - 功能测试指南和测试用例

### 💻 开发文档
- **[开发指南](./开发指南.md)** - 开发环境搭建和开发规范
- **[API接口文档](./API接口文档.md)** - 所有API接口的详细说明
- **[数据库设计文档](./数据库设计文档.md)** - 数据库表结构和设计说明

### 🔒 安全文档
- **[密码加密改造文档](./docs/security/walkthrough.md)** 🔐 - BCrypt密码加密实施指南
- **[安全改造实施计划](./docs/security/implementation_plan.md)** - 详细的实施计划

### 🚢 部署和维护
- **[部署指南](./部署指南.md)** - 生产环境部署步骤

### 📋 项目管理
- **[项目交付清单](./项目交付清单.md)** ⭐ - 项目交付内容和质量确认
- **[项目完善总结](./项目完善总结.md)** - 项目完善过程和技术总结
- **[文档索引](./文档索引.md)** - 所有文档的快速导航

---

## 🚀 快速启动

### 一键启动（Windows）
```bash
# 双击运行
启动项目.bat
```

### 手动启动
详见 [项目启动指南](./项目启动指南.md)

---

## ⭐ 项目特性

### 核心功能
- ✅ 用户注册、登录（**BCrypt密码加密**）
- ✅ 头像上传裁剪（支持在线裁剪调整）
- ✅ 视频上传、列表查询、审核
- ✅ 评论功能
- ✅ 点赞功能
- ✅ 收藏功能
- ✅ 城市管理员审核功能
- ✅ 系统管理员权限管理

### 安全特性  
- 🔒 **BCrypt密码加密** - 工业级密码存储
- 🔐 **JWT身份认证** - 安全的用户令牌
- 🛡️ **权限控制** - 细粒度的角色权限管理
- 📝 **SQL注入防护** - MyBatis Plus参数化查询

### 技术特色
- 🎨 **现代化UI** - B站风格界面设计
- 📱 **响应式布局** - 支持移动端和桌面端
- ☁️ **OSS云存储** - 阿里云OSS视频和图片存储
- 🚀 **前后端分离** - RESTful API架构

---

## 🛠️ 技术栈

### 后端
- **框架**: Spring Boot 3.2.1
- **ORM**: MyBatis Plus 3.5.7
- **数据库**: MySQL 8.0
- **认证**: JWT (jjwt 0.11.5)
- **安全**: Spring Security Crypto (BCrypt)
- **云存储**: 阿里云OSS SDK

### 前端
- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite 5
- **UI组件**: Element Plus
- **状态管理**: Pinia
- **HTTP客户端**: Axios
- **图片裁剪**: vue-cropper

---

## 📁 项目结构

```
CVS-system/
├── docs/                      # 📚 项目文档
│   ├── security/              # 🔒 安全相关文档
│   │   ├── walkthrough.md      # 密码加密改造指南
│   │   ├── implementation_plan.md  # 实施计划
│   │   └── task.md             # 任务清单
│   └── development/           # 💻 开发文档
├── CVS-port/                  # 后端项目 (Spring Boot)
│   ├── src/main/java/cn/edu/seig/fnds/
│   │   ├── controller/        # 🎮 控制器层
│   │   ├── service/           # 🔧 服务层
│   │   ├── mapper/            # 💾 数据访问层
│   │   ├── entity/            # 📦 实体类
│   │   └── config/            # ⚙️ 配置类
│   │       ├── WebConfig.java         # Web配置
│   │       └── PasswordConfig.java    # 🔐 密码加密配置
│   └── src/main/resources/
│       ├── application.properties     # 应用配置
│       └── mapper/                    # MyBatis XML映射
├── CVS-view/                  # 前端项目 (Vue 3 + Vite)
│   ├── src/
│   │   ├── views/             # 📄 页面组件
│   │   ├── components/        # 🧩 公共组件
│   │   ├── stores/            # 📦 Pinia状态管理
│   │   ├── api/               # 🌐 API接口
│   │   ├── router/            # 🛤️ 路由配置
│   │   └── utils/             # 🔧 工具函数
│   └── public/                # 静态资源
├── sql/                       # 📊 数据库脚本
└── README.md                  # 📖 项目说明
```

---

## 🚀 快速开始

### 环境要求
- ☕ JDK 17+
- 🐬 MySQL 8.0+
- 📦 Node.js 16+
- 🔨 Maven 3.6+

### 方式一：一键启动（推荐）

```bash
# 1. 确保MySQL服务已启动
# 2. 双击运行
启动项目.bat

# 3. 等待服务启动完成
# 4. 访问
http://localhost:5173
```

### 方式二：手动启动

#### 1. 数据库配置
```bash
# 登录MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE IF NOT EXISTS cvs_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入数据
USE cvs_db;
source sql/init.sql;
```

#### 2. 修改配置
编辑 `CVS-port/src/main/resources/application.properties`:
```properties
spring.datasource.username=你的MySQL用户名
spring.datasource.password=你的MySQL密码

# 阿里云OSS配置（可选）
aliyun.oss.endpoint=your-endpoint
aliyun.oss.accessKeyId=your-access-key-id
aliyun.oss.accessKeySecret=your-access-key-secret
aliyun.oss.bucketName=your-bucket-name
```

#### 3. 启动后端
```bash
cd CVS-port
mvn spring-boot:run
```
**后端服务**：http://localhost:8080

#### 4. 启动前端
```bash
cd CVS-view

# 安装依赖（首次运行）
npm install

# 启动服务
npm run dev
```
**前端服务**：http://localhost:5173

> 💡 详细步骤请参考 [项目启动指南](./项目启动指南.md)

---

## 👥 系统角色

### 🟢 普通用户
- 注册登录
- 浏览视频
- 上传视频（需审核）
- 评论、点赞、收藏视频
- 个人中心管理

### 🔵 城市管理员
- 拥有普通用户所有权限
- 审核用户上传的视频
- 管理视频内容

### 🔴 系统管理员
- 拥有所有权限
- 用户管理
- 权限分配
- 系统配置

---

## 🎯 核心API接口

### 用户相关
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/user/register` | 用户注册 | ❌ |
| POST | `/api/user/login` | 用户登录 | ❌ |
| GET | `/api/user/{id}` | 获取用户信息 | ✅ |
| PUT | `/api/user/{id}` | 更新用户信息 | ✅ |
| POST | `/api/user/change-password` | 修改密码 | ✅ |

### 视频相关
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/video/list` | 获取视频列表 | ❌ |
| POST | `/api/video/upload` | 上传视频 | ✅ |
| GET | `/api/video/{id}` | 获取视频详情 | ❌ |
| POST | `/api/video/{id}/review` | 审核视频 | ✅ (管理员) |

### 社交互动
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/comment` | 添加评论 | ✅ |
| POST | `/api/video-like/{videoId}` | 点赞/取消点赞 | ✅ |
| POST | `/api/video-collect/{videoId}` | 收藏/取消收藏 | ✅ |

> 📖 完整API文档：[API接口文档](./API接口文档.md)

---

## 🧪 测试账号

| 角色 | 用户名 | 密码 | 权限 |
|------|--------|------|------|
| 系统管理员 | admin | 123456 | 所有权限 |
| 普通用户 | user1 | 123456 | 基本功能 |
| 普通用户 | user2 | 123456 | 基本功能 |

> ⚠️ **安全提示**：所有密码已使用BCrypt加密存储，无法反向查看明文

> 💡 更多测试用例请参考 [测试清单](./测试清单.md)

---

## 🔐 安全说明

### 密码安全
- ✅ 使用BCrypt加密算法存储密码
- ✅ 每个密码使用独立的随机盐值
- ✅ 密码不可逆，即使数据库泄露也无法获取明文
- ✅ 抗暴力破解（慢哈希算法）

### 数据安全
- ✅ SQL注入防护（MyBatis Plus参数化查询）
- ✅ XSS防护（前端输入验证和转义）
- ✅ CSRF防护（JWT令牌验证）

### 访问控制
- ✅ JWT身份认证
- ✅ 基于角色的权限控制(RBAC)
- ✅ 接口级权限验证

---

## 📦 部署说明

### 生产环境构建

#### 1. 后端打包
```bash
cd CVS-port
mvn clean package -DskipTests
# 生成文件：target/CVS-port-0.0.1-SNAPSHOT.jar
```

#### 2. 前端构建
```bash
cd CVS-view
npm run build
# 生成目录：dist/
```

#### 3. 部署
- 将后端jar包部署到服务器运行
- 将前端dist目录部署到Nginx或其他Web服务器
- 配置反向代理将`/api`请求转发到后端服务

> 📖 详细部署步骤：[部署指南](./部署指南.md)

---

## ⚠️ 注意事项

1. **存储配置**：上传的视频和图片需要配置阿里云OSS或其他对象存储服务
2. **HTTPS配置**：生产环境建议配置HTTPS
3. **敏感信息**：数据库密码、OSS密钥等不要提交到代码仓库
4. **定期备份**：定期备份数据库数据
5. **合规要求**：平台内容应符合相关法律法规

---

## 🤝 贡献指南

本项目主要用于学习和毕业设计。如有问题或建议，欢迎提Issue。

---

## 📄 许可证

本项目仅用于学习和毕业设计使用。

---

## 📞 联系方式

如有问题，请参考 [常见问题FAQ](./常见问题FAQ.md) 或查看相关文档。