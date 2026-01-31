# BCrypt密码加密改造任务清单

## 后端改造

### 配置层
- [x] 创建 `PasswordConfig.java` 配置类
- [x] 添加 `PasswordEncoder` Bean注入
- [x] 添加 Spring Security 依赖到 pom.xml

### Service层
- [x] 修改 `UserService.java`
  - [x] 注入 `PasswordEncoder`
  - [x] 修改 `register()` 方法 - 加密密码后存储
  - [x] 修改 `login()` 方法 - 使用BCrypt验证密码

### Controller层
- [x] 修改 `UserController.java`
  - [x] 注入 `PasswordEncoder`
  - [x] 修改 `changePassword()` 方法 - 验证旧密码和加密新密码
  - [x] 修改 `adminChangePassword()` 方法 - 加密新密码
  - [x] 删除或修改 `getUserPassword()` 方法

### 数据迁移
- [x] 创建 `PasswordMigrationController.java` 临时迁移接口
- [x] 实现密码迁移逻辑
- [x] 测试迁移功能
- [x] **执行数据库备份**
- [x] 执行密码迁移
- [x] 验证迁移结果
- [x] 删除迁移接口代码

## 测试验证
- [x] 测试新用户注册（密码加密）
- [x] 测试用户登录（BCrypt验证）
- [x] 测试修改密码功能
- [x] 测试管理员修改密码功能
- [x] 测试密码验证失败场景

## 文档更新
- [x] 更新API文档
- [x] 创建walkthrough文档记录改造过程

## ✅ 全部完成！
