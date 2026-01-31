# BCrypt密码加密改造实施计划

## 问题描述

当前系统存在严重的安全隐患：用户密码以明文形式存储在数据库中。这违反了基本的安全准则，需要立即改造为BCrypt加密存储。

## 改造目标

1. 使用BCrypt算法加密所有用户密码
2. 修改注册逻辑，存储加密后的密码
3. 修改登录逻辑，使用BCrypt验证密码
4. 修改密码修改逻辑，使用BCrypt验证旧密码
5. 迁移现有数据库中的明文密码为加密密文

## 用户需要知道的重要信息

> [!WARNING]
> **破坏性变更**：实施此改造后，所有现有用户需要重置密码，或者我们需要编写一个一次性脚本来迁移现有的明文密码。

> [!IMPORTANT]
> **安全提升**：改造完成后，即使数据库泄露，攻击者也无法直接获取用户的真实密码，大大提升系统安全性。

## 技术方案

### 使用的加密算法：BCrypt

**BCrypt的优点：**
- 自带随机盐值（salt），同样的密码每次加密结果都不同
- 慢哈希算法，有效抵御暴力破解和彩虹表攻击
- Spring Security官方推荐，工业级标准

**加密示例：**
```
明文密码: "123456"
BCrypt密文: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
```

## 修改的文件

### 后端改造

#### [NEW] [PasswordConfig.java](file:///d:/BaiduSyncdisk/university/毕业/毕业设计/CVS-system/CVS-port/src/main/java/cn/edu/seig/fnds/config/PasswordConfig.java)

创建密码加密器配置类，提供`PasswordEncoder` Bean：

```java
@Configuration
public class PasswordConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();

    }
}
```

---

#### [MODIFY] [UserService.java](file:///d:/BaiduSyncdisk/university/毕业/毕业设计/CVS-system/CVS-port/src/main/java/cn/edu/seig/fnds/service/UserService.java)

**修改内容：**

1. **注入PasswordEncoder**
   ```java
   @Autowired
   private PasswordEncoder passwordEncoder;
   ```

2. **修改 `register()` 方法**（第46-63行）
   ```java
   public boolean register(User user) {
       // 检查用户名是否已存在
       if (isUsernameExists(user.getUsername())) {
           throw new RuntimeException("用户名已存在");
       }
       
       // 加密密码
       user.setPassword(passwordEncoder.encode(user.getPassword()));
       
       // 设置默认值...
       return save(user);
   }
   ```

3. **修改 `login()` 方法**（第69-80行）
   ```java
   public User login(String username, String password) {
       // 先根据用户名查询用户
       User user = getOne(Wrappers.<User>lambdaQuery()
               .eq(User::getUsername, username)
               .eq(User::getStatus, 0));
       
       if (user == null) {
           throw new RuntimeException("用户名或密码错误");
       }
       
       // 使用BCrypt验证密码
       if (!passwordEncoder.matches(password, user.getPassword())) {
           throw new RuntimeException("用户名或密码错误");
       }
       
       return user;
   }
   ```

---

#### [MODIFY] [UserController.java](file:///d:/BaiduSyncdisk/university/毕业/毕业设计/CVS-system/CVS-port/src/main/java/cn/edu/seig/fnds/controller/UserController.java)

**修改内容：**

1. **注入PasswordEncoder**
   ```java
   @Autowired
   private PasswordEncoder passwordEncoder;
   ```

2. **修改 `changePassword()` 方法**（第256-316行）
   ```java
   // 验证原密码
   User user = userService.getById(currentUser.getId());
   if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
       result.put("success", false);
       result.put("message", "原密码错误");
       return ResponseEntity.badRequest().body(result);
   }
   
   // 加密新密码
   user.setPassword(passwordEncoder.encode(newPassword));
   boolean success = userService.updateById(user);
   ```

3. **修改 `adminChangePassword()` 方法**（第356-410行）
   ```java
   // 加密新密码
   user.setPassword(passwordEncoder.encode(newPassword));
   boolean success = userService.updateById(user);
   ```

4. **移除或修改 `getUserPassword()` 方法**（第321-351行）
   - 选项1：直接删除此接口（推荐）
   - 选项2：返回提示信息"密码已加密，无法查看"

---

#### [NEW] [PasswordMigrationController.java](file:///d:/BaiduSyncdisk/university/毕业/毕业设计/CVS-system/CVS-port/src/main/java/cn/edu/seig/fnds/controller/PasswordMigrationController.java)

**临时迁移接口**（迁移完成后需要删除）：

```java
@RestController
@RequestMapping("/api/admin/migration")
public class PasswordMigrationController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 迁移所有明文密码为BCrypt加密
     * 注意：此接口仅在迁移时使用一次，之后需要删除
     */
    @PostMapping("/encrypt-passwords")
    public ResponseEntity<?> encryptPasswords(@RequestAttribute("currentUser") User admin) {
        if (admin.getRole() != 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("权限不足");
        }
        
        List<User> allUsers = userService.list();
        int count = 0;
        
        for (User user : allUsers) {
            String rawPassword = user.getPassword();
            // 检查是否已经是BCrypt格式
            if (!rawPassword.startsWith("$2a$")) {
                user.setPassword(passwordEncoder.encode(rawPassword));
                userService.updateById(user);
                count++;
            }
        }
        
        return ResponseEntity.ok("成功加密 " + count + " 个用户的密码");
    }
}
```

## 验证计划

### 1. 测试新用户注册
- 注册一个新用户
- 检查数据库，确认密码是BCrypt密文（以`$2a$`开头，长度60字符）
- 使用注册的用户名和密码登录，确认成功

### 2. 测试密码修改
- 登录后修改密码
- 检查数据库，确认新密码是BCrypt密文
- 使用新密码重新登录，确认成功

### 3. 测试管理员修改密码
- 管理员为某个用户重置密码
- 检查数据库，确认密码是BCrypt密文
- 使用新密码登录该用户，确认成功

### 4. 数据库迁移测试
- 调用迁移接口
- 检查所有用户密码是否都已加密
- 随机选择几个用户尝试登录，确认成功

## 迁移步骤

1. ✅ **备份数据库**（必须！）
2. 部署新代码到服务器
3. 调用迁移接口 `POST /api/admin/migration/encrypt-passwords`
4. 验证所有用户密码已加密
5. 测试登录功能
6. 删除迁移接口代码
7. 重新部署

## 风险提示

> [!CAUTION]
> - **数据不可逆**：密码加密后无法还原为明文
> - **迁移前必须备份**：如果迁移失败，需要从备份恢复
> - **迁移接口安全**：迁移完成后必须删除迁移接口，防止被滥用
