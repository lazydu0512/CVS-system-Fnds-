# BCrypt密码加密改造完成报告

本文档记录了将系统从明文密码存储升级到BCrypt加密存储的完整改造过程。

## 改造概述

✅ **已完成所有代码修改**

系统现已使用工业级BCrypt算法加密存储用户密码，显著提升安全性。即使数据库泄露，攻击者也无法直接获取用户的真实密码。

---

## 修改的文件

### 1. 新增文件

#### [PasswordConfig.java](file:///d:/BaiduSyncdisk/university/毕业/毕业设计/CVS-system/CVS-port/src/main/java/cn/edu/seig/fnds/config/PasswordConfig.java)

**作用：** 提供PasswordEncoder Bean

```java
@Configuration
public class PasswordConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

#### [PasswordMigrationController.java](file:///d:/BaiduSyncdisk/university/毕业/毕业设计/CVS-system/CVS-port/src/main/java/cn/edu/seig/fnds/controller/PasswordMigrationController.java)

**作用：** 临时迁移接口，将现有明文密码加密

**提供的API：**
- `POST /api/admin/migration/encrypt-passwords` - 执行密码迁移
- `GET /api/admin/migration/check-encryption-status` - 检查加密状态

> [!CAUTION]
> **迁移完成后必须删除此文件！**

---

### 2. 修改的文件

#### [pom.xml](file:///d:/BaiduSyncdisk/university/毕业/毕业设计/CVS-system/CVS-port/pom.xml)

添加了Spring Security依赖（仅用于密码加密）：

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>
```

---

#### [UserService.java:46-91](file:///d:/BaiduSyncdisk/university/毕业/毕业设计/CVS-system/CVS-port/src/main/java/cn/edu/seig/fnds/service/UserService.java#L46-L91)

**修改内容：**

1. 注入PasswordEncoder
2. 注册时加密密码
3. 登录时使用BCrypt验证

**关键改动：**

```diff
+ @Autowired
+ private PasswordEncoder passwordEncoder;

  public boolean register(User user) {
      // ...
+     // 加密密码
+     user.setPassword(passwordEncoder.encode(user.getPassword()));
      // ...
  }

  public User login(String username, String password) {
-     // 旧：直接比对明文密码
-     User user = getOne(Wrappers.<User>lambdaQuery()
-         .eq(User::getUsername, username)
-         .eq(User::getPassword, password));
      
+     // 新：先查询用户，再验证密码
+     User user = getOne(Wrappers.<User>lambdaQuery()
+         .eq(User::getUsername, username));
+     
+     if (!passwordEncoder.matches(password, user.getPassword())) {
+         throw new RuntimeException("用户名或密码错误");
+     }
  }
```

---

#### [UserController.java](file:///d:/BaiduSyncdisk/university/毕业/毕业设计/CVS-system/CVS-port/src/main/java/cn/edu/seig/fnds/controller/UserController.java)

**修改内容：**

1. 注入PasswordEncoder
2. 修改密码时使用BCrypt验证和加密
3. 禁用查看明文密码功能

**关键改动：**

```diff
  public ResponseEntity changePassword(...) {
-     if (!user.getPassword().equals(oldPassword)) {
+     if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
          // 原密码错误
      }
      
-     user.setPassword(newPassword);
+     user.setPassword(passwordEncoder.encode(newPassword));
  }

  @GetMapping("/{id}/password")
  public ResponseEntity getUserPassword(...) {
-     // 旧：返回明文密码（安全隐患）
-     result.put("password", user.getPassword());
      
+     // 新：拒绝返回，提示已加密
+     result.put("message", "密码已加密存储，无法查看明文");
+     return ResponseEntity.status(FORBIDDEN).body(result);
  }
```

---

## 使用指南

### 第一步：备份数据库（必须！）

```bash
# MySQL备份命令示例
mysqldump -u root -p your_database_name > backup_$(date +%Y%m%d_%H%M%S).sql
```

> [!WARNING]
> **密码加密后不可逆！迁移前必须备份！**

---

### 第二步：启动应用

```bash
cd CVS-port
mvn spring-boot:run
```

等待应用启动完成。

---

### 第三步：检查加密状态

使用管理员账号登录，然后调用检查接口：

```bash
GET http://localhost:8080/api/admin/migration/check-encryption-status
Authorization: Bearer <your_admin_token>
```

**响应示例：**

```json
{
    "success": true,
    "totalUsers": 10,
    "encryptedCount": 0,
    "plaintextCount": 10,
    "allEncrypted": false
}
```

---

### 第四步：执行密码迁移

调用迁移接口：

```bash
POST http://localhost:8080/api/admin/migration/encrypt-passwords
Authorization: Bearer <your_admin_token>
```

**响应示例：**

```json
{
    "success": true,
    "message": "密码迁移完成",
    "totalUsers": 10,
    "encryptedCount": 10,
    "skippedCount": 0,
    "errorCount": 0
}
```

---

### 第五步：验证迁移结果

1. **再次检查加密状态**

```bash
GET /api/admin/migration/check-encryption-status
```

确认 `allEncrypted: true`

2. **测试登录**

使用现有用户账号登录，验证密码验证功能正常。

3. **查看数据库**

```sql
SELECT id, username, password FROM user LIMIT 5;
```

密码应该类似：
```
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
```

---

### 第六步：删除迁移控制器（必须！）

迁移完成并验证无误后，删除迁移相关代码：

```bash
# 删除迁移控制器
rm src/main/java/cn/edu/seig/fnds/controller/PasswordMigrationController.java

# 重新编译部署
mvn clean package
```

> [!CAUTION]
> **必须删除迁移接口，防止安全风险！**

---

## 测试验证

### 测试1：新用户注册

```bash
POST /api/user/register
Content-Type: application/json

{
    "username": "testuser",
    "password": "123456",
    "nickname": "测试用户"
}
```

**验证：** 检查数据库，密码应为BCrypt密文（60字符，以`$2a$`开头）

---

### 测试2：用户登录

```bash
POST /api/user/login
Content-Type: application/json

{
    "username": "testuser",
    "password": "123456"
}
```

**期望：** 登录成功，返回token

---

### 测试3：修改密码

```bash
POST /api/user/change-password
Authorization: Bearer <token>
Content-Type: application/json

{
    "oldPassword": "123456",
    "newPassword": "654321"
}
```

**期望：** 修改成功，可以用新密码登录

---

### 测试4：错误密码登录

```bash
POST /api/user/login
Content-Type: application/json

{
    "username": "testuser",
    "password": "wrongpassword"
}
```

**期望：** 登录失败，提示"用户名或密码错误"

---

## 技术细节

### BCrypt加密原理

**加密示例：**
```
明文：123456
密文：$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
```

**密文结构：**
- `$2a$` - 算法版本
- `10$` - 成本因子（迭代次数 = 2^10）
- 后续 - 盐值 + 哈希值

**特点：**
1. 每次加密结果都不同（随机盐值）
2. 相同密码的密文也完全不同
3. 慢哈希算法，抗暴力破解

---

### 性能影响

BCrypt是慢哈希算法（这是优点），但会影响登录性能：

- **注册/登录时间增加：** 约50-100ms
- **可接受范围：** 对用户体验影响极小
- **安全提升：** 远超过性能损失

---

## 安全提升

| 项目 | 改造前 | 改造后 |
|------|--------|--------|
| 密码存储 | 明文 | BCrypt加密 |
| 数据库泄露风险 | ❌ 密码直接暴露 | ✅ 无法获取明文 |
| 彩虹表攻击 | ❌ 完全无防御 | ✅ 随机盐值防御 |
| 暴力破解 | ❌ 容易破解 | ✅ 慢哈希抵御 |
| 相同密码识别 | ❌ 密文相同 | ✅ 密文完全不同 |
| 管理员查看密码 | ❌ 可查看明文 | ✅ 已禁用 |

---

## 注意事项

> [!IMPORTANT]
> 1. **迁移前必须备份数据库**
> 2. **迁移后必须删除PasswordMigrationController**
> 3. **在生产环境执行前，先在测试环境验证**
> 4. **通知用户系统维护时间窗口**

---

## 总结

✅ **代码改造完成**
- 创建了密码加密配置
- 修改了注册/登录逻辑
- 修改了密码变更逻辑
- 禁用了密码查看功能
- 添加了数据迁移工具

✅ **安全性大幅提升**
- 密码BCrypt加密存储
- 抗暴力破解和彩虹表攻击
- 符合工业安全标准

📋 **后续步骤**
1. 备份数据库
2. 执行密码迁移
3. 验证功能正常
4. 删除迁移控制器
5. 部署到生产环境
