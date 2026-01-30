# 🎬 演唱会视频分享平台 - 前端项目

## 📦 依赖安装指南

### 🚀 推荐安装方式

1. **使用安装脚本（最简单）**
   ```bash
   # Windows: 双击运行 install-dependencies.bat
   # 或命令行：
   cd CVS-view
   .\install-dependencies.bat
   ```

2. **一键安装所有依赖**
   ```bash
   cd CVS-view
   npm run install:all
   ```

3. **手动逐步安装**
   ```bash
   cd CVS-view
   npm install
   npm install vue-router@^4.2.5
   npm install pinia@^2.1.7
   npm install axios@^1.6.0
   npm install element-plus@^2.4.0
   npm install @element-plus/icons-vue@^2.1.0
   ```

### 🔧 启用完整功能

安装完所有依赖后，将 `src/main.js` 替换为 `src/main-full.js` 的内容：

```bash
# 备份当前文件
cp src/main.js src/main-simple.js

# 使用完整版本
cp src/main-full.js src/main.js
```

## 🎯 项目特性

### 核心功能
- ✅ **用户系统** - 注册、登录、个人资料管理
- ✅ **视频管理** - 上传、浏览、搜索、筛选
- ✅ **社交互动** - 评论、点赞、收藏
- ✅ **管理员后台** - 视频审核、用户管理、统计数据

### 技术栈
- **Vue 3** + Composition API
- **Vue Router 4** - 路由管理
- **Pinia 2** - 状态管理
- **Element Plus** - UI组件库
- **Axios** - HTTP客户端
- **Vite** - 构建工具

## 📁 项目结构

```
CVS-view/
├── public/                    # 静态资源
├── src/
│   ├── api/                   # API 接口封装
│   │   └── video.js          # 视频相关接口
│   ├── stores/               # Pinia 状态管理
│   │   └── user.js           # 用户状态管理
│   ├── views/                # 页面组件
│   │   ├── Home.vue          # 首页
│   │   ├── Login.vue         # 登录页
│   │   ├── Register.vue      # 注册页
│   │   ├── VideoList.vue     # 视频列表
│   │   ├── VideoDetail.vue   # 视频详情
│   │   ├── VideoUpload.vue   # 视频上传
│   │   ├── Profile.vue       # 个人中心
│   │   └── Admin.vue         # 管理员后台
│   ├── App.vue               # 根组件
│   ├── main.js              # 应用入口（简化版）
│   └── main-full.js         # 应用入口（完整版）
├── install-dependencies.bat  # 依赖安装脚本
├── START_GUIDE.md           # 完整启动指南
└── package.json             # 项目配置
```

## 🚀 启动项目

```bash
# 1. 安装依赖
cd CVS-view
.\install-dependencies.bat

# 2. 启用完整功能（替换 main.js）
cp src/main-full.js src/main.js

# 3. 启动开发服务器
npm run dev
```

访问 `http://localhost:5173` 查看项目

## 🐛 问题排查

### 依赖安装失败
```bash
# 清理缓存
npm cache clean --force

# 设置国内镜像
npm config set registry https://registry.npmmirror.com

# 重新安装
npm install
```

### 端口冲突
修改 `vite.config.js`：
```javascript
server: {
  port: 3000, // 改为其他端口
}
```

### 依赖版本问题
确保 Node.js 版本 >= 16，推荐使用 LTS 版本。

## 📱 响应式设计

项目支持多种设备：
- 💻 桌面端 (1200px+)
- 📱 平板端 (768px-1199px)
- 📱 手机端 (<768px)

## 🎨 UI 设计

- 使用 **Element Plus** 组件库
- 现代化渐变背景
- 卡片式布局
- 阴影和圆角效果
- 流畅的过渡动画

## 🔗 API 接口

所有接口都在 `src/api/video.js` 中封装：
- 用户认证接口
- 视频 CRUD 接口
- 评论互动接口
- 点赞收藏接口

## 📊 管理后台

管理员功能包括：
- 📈 数据统计面板
- 🎬 视频审核管理
- 👥 用户账号管理
- 🔍 高级搜索筛选

## 🚀 生产部署

```bash
# 构建生产版本
npm run build

# 预览构建结果
npm run preview
```

将 `dist` 目录部署到 Web 服务器即可。