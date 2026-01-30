# 演唱会视频分享平台 - API接口文档

## 📋 基本信息

- **Base URL**: `http://localhost:8080/api`
- **认证方式**: Bearer Token
- **数据格式**: JSON
- **字符编码**: UTF-8

## 🔐 认证说明

### Token使用
需要认证的接口需要在请求头中携带Token：
```
Authorization: Bearer {token}
```

### 获取Token
通过登录接口获取Token，登录成功后返回的`token`字段即为认证令牌。

## 📝 统一响应格式

### 成功响应
```json
{
  "success": true,
  "message": "操作成功",
  "data": {}
}
```

### 失败响应
```json
{
  "success": false,
  "message": "错误信息"
}
```

## 🔑 用户相关接口

### 1. 用户注册
- **接口**: `POST /api/user/register`
- **认证**: 不需要
- **请求体**:
```json
{
  "username": "testuser",
  "password": "123456",
  "nickname": "测试用户",
  "email": "test@example.com",
  "phone": "13800138000"
}
```
- **响应**:
```json
{
  "success": true,
  "message": "注册成功"
}
```

### 2. 用户登录
- **接口**: `POST /api/user/login`
- **认证**: 不需要
- **请求体**:
```json
{
  "username": "admin",
  "password": "123456"
}
```
- **响应**:
```json
{
  "success": true,
  "message": "登录成功",
  "user": {
    "id": 1,
    "username": "admin",
    "nickname": "系统管理员",
    "email": "admin@cvs.com",
    "role": 1,
    "status": 0
  },
  "token": "uuid-token-string"
}
```

### 3. 获取用户信息
- **接口**: `GET /api/user/{id}`
- **认证**: 不需要
- **响应**: 返回用户对象（不含密码）

### 4. 更新用户信息
- **接口**: `PUT /api/user/{id}`
- **认证**: 需要
- **请求体**:
```json
{
  "nickname": "新昵称",
  "email": "new@example.com",
  "phone": "13900139000"
}
```

## 🎬 视频相关接口

### 1. 获取视频列表
- **接口**: `GET /api/video/list`
- **认证**: 不需要
- **参数**:
  - `current`: 当前页码（默认1）
  - `size`: 每页数量（默认10）
  - `city`: 城市筛选（可选）
  - `title`: 标题搜索（可选）
- **示例**: `/api/video/list?current=1&size=10&city=北京`
- **响应**:
```json
{
  "success": true,
  "data": [...],
  "total": 100,
  "current": 1,
  "pages": 10
}
```

### 2. 获取视频详情
- **接口**: `GET /api/video/{id}`
- **认证**: 不需要
- **响应**: 返回视频对象，同时增加观看次数

### 3. 上传视频
- **接口**: `POST /api/video/upload`
- **认证**: 需要
- **请求体**:
```json
{
  "title": "周杰伦演唱会",
  "description": "精彩片段",
  "city": "北京",
  "concertDate": "2024-01-20",
  "videoUrl": "/uploads/video.mp4",
  "thumbnailUrl": "/uploads/thumb.jpg",
  "duration": 300
}
```

### 4. 获取用户视频
- **接口**: `GET /api/video/user/{userId}`
- **认证**: 不需要
- **响应**: 返回该用户上传的所有视频

### 5. 获取待审核视频
- **接口**: `GET /api/video/pending`
- **认证**: 需要（管理员）
- **响应**: 返回所有待审核视频列表

### 6. 审核视频
- **接口**: `POST /api/video/{id}/review`
- **认证**: 需要（管理员）
- **请求体**:
```json
{
  "status": 1,
  "reviewComment": "审核通过"
}
```
- **说明**: status: 1-通过, 2-拒绝

## 💬 评论相关接口

### 1. 添加评论
- **接口**: `POST /api/comment`
- **认证**: 需要
- **请求体**:
```json
{
  "videoId": 1,
  "content": "太精彩了！",
  "parentId": null
}
```

### 2. 获取视频评论
- **接口**: `GET /api/comment/video/{videoId}`
- **认证**: 不需要
- **响应**: 返回该视频的所有评论

### 3. 删除评论
- **接口**: `DELETE /api/comment/{id}`
- **认证**: 需要
- **权限**: 评论作者或管理员

## 👍 点赞相关接口

### 1. 点赞/取消点赞
- **接口**: `POST /api/video-like/{videoId}`
- **认证**: 需要
- **响应**:
```json
{
  "success": true,
  "message": "点赞成功",
  "liked": true
}
```

### 2. 检查点赞状态
- **接口**: `GET /api/video-like/{videoId}/status`
- **认证**: 需要
- **响应**:
```json
{
  "success": true,
  "liked": true
}
```

## ⭐ 收藏相关接口

### 1. 收藏/取消收藏
- **接口**: `POST /api/video-collect/{videoId}`
- **认证**: 需要
- **响应**:
```json
{
  "success": true,
  "message": "收藏成功",
  "collected": true
}
```

### 2. 检查收藏状态
- **接口**: `GET /api/video-collect/{videoId}/status`
- **认证**: 需要

### 3. 获取用户收藏
- **接口**: `GET /api/video-collect/user`
- **认证**: 需要
- **响应**: 返回当前用户的收藏列表

## 📁 文件上传接口

### 上传文件
- **接口**: `POST /api/file/upload`
- **认证**: 需要
- **Content-Type**: `multipart/form-data`
- **参数**: `file` (文件)
- **响应**:
```json
{
  "success": true,
  "message": "上传成功",
  "url": "/uploads/filename.ext"
}
```
- **说明**: 
  - 支持视频和图片上传
  - 最大文件大小: 500MB
  - 文件保存在服务器的uploads目录

## ⚠️ 错误码说明

| HTTP状态码 | 说明 |
|-----------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未认证或Token无效 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 🧪 测试示例

### 使用curl测试

#### 登录
```bash
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

#### 获取视频列表
```bash
curl http://localhost:8080/api/video/list?current=1&size=10
```

#### 上传视频（需要Token）
```bash
curl -X POST http://localhost:8080/api/video/upload \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {your-token}" \
  -d '{...}'
```

