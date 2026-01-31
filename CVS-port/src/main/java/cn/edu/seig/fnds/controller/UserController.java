package cn.edu.seig.fnds.controller;

import cn.edu.seig.fnds.entity.User;
import cn.edu.seig.fnds.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 * 
 * @author lazzydu
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    private final UserService userService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public UserController(UserService userService,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 验证必填字段
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "用户名不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "密码不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            // 处理空字符串字段，设为null以避免违反唯一性约束
            if (user.getEmail() != null && user.getEmail().trim().isEmpty()) {
                user.setEmail(null);
            }
            if (user.getPhone() != null && user.getPhone().trim().isEmpty()) {
                user.setPhone(null);
            }
            if (user.getNickname() != null && user.getNickname().trim().isEmpty()) {
                user.setNickname(null); // 或者设置默认昵称
            }

            boolean success = userService.register(user);
            if (success) {
                result.put("success", true);
                result.put("message", "注册成功");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "注册失败");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (RuntimeException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> result = new HashMap<>();

        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            if (username == null || username.trim().isEmpty() ||
                    password == null || password.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "用户名和密码不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            User user = userService.login(username, password);
            String token = userService.generateToken(user);

            result.put("success", true);
            result.put("message", "登录成功");
            result.put("user", user);
            result.put("token", token);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            // 清除敏感信息
            user.setPassword(null);
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody User user) {
        Map<String, Object> result = new HashMap<>();

        try {
            user.setId(id);
            boolean success = userService.updateUserInfo(user);
            if (success) {
                // 返回更新后的用户信息
                User updatedUser = userService.getById(id);
                updatedUser.setPassword(null);
                result.put("success", true);
                result.put("message", "更新成功");
                result.put("user", updatedUser);
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "用户不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取用户列表（管理员功能）
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestAttribute("currentUser") User admin) {

        Map<String, Object> result = new HashMap<>();

        // 检查是否为管理员
        if (admin.getRole() != 1) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        try {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                queryWrapper.like("username", keyword)
                        .or().like("nickname", keyword);
            }
            queryWrapper.orderByDesc("create_time");

            IPage<User> page = userService.page(new Page<>(current, size), queryWrapper);

            // 清除密码信息
            page.getRecords().forEach(u -> u.setPassword(null));

            result.put("success", true);
            result.put("data", page.getRecords());
            result.put("total", page.getTotal());
            result.put("current", page.getCurrent());
            result.put("pages", page.getPages());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取用户列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 切换用户状态（管理员功能）
     */
    @PostMapping("/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(
            @PathVariable Long id,
            @RequestAttribute("currentUser") User admin) {

        Map<String, Object> result = new HashMap<>();

        // 检查是否为管理员
        if (admin.getRole() != 1) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        try {
            User user = userService.getById(id);
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return ResponseEntity.notFound().build();
            }

            // 不能禁用自己
            if (user.getId().equals(admin.getId())) {
                result.put("success", false);
                result.put("message", "不能禁用自己");
                return ResponseEntity.badRequest().body(result);
            }

            // 切换状态
            user.setStatus(user.getStatus() == 0 ? 1 : 0);
            boolean success = userService.updateById(user);

            if (success) {
                result.put("success", true);
                result.put("message", user.getStatus() == 0 ? "用户已启用" : "用户已禁用");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "操作失败");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 修改密码（需验证原密码）
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestBody Map<String, String> passwordRequest,
            @RequestAttribute("currentUser") User currentUser) {

        Map<String, Object> result = new HashMap<>();

        try {
            String oldPassword = passwordRequest.get("oldPassword");
            String newPassword = passwordRequest.get("newPassword");

            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "原密码不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "新密码不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            // 新密码不能与原密码相同
            if (newPassword.equals(oldPassword)) {
                result.put("success", false);
                result.put("message", "新密码不能与原密码相同");
                return ResponseEntity.badRequest().body(result);
            }
            if (newPassword.length() < 6) {
                result.put("success", false);
                result.put("message", "新密码长度不能少于6位");
                return ResponseEntity.badRequest().body(result);
            }

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

            if (success) {
                result.put("success", true);
                result.put("message", "密码修改成功");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "密码修改失败");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "密码修改失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 管理员查看用户密码（已禁用 - 安全考虑）
     * 密码已使用BCrypt加密，无法查看明文
     */
    @GetMapping("/{id}/password")
    public ResponseEntity<Map<String, Object>> getUserPassword(
            @PathVariable Long id,
            @RequestAttribute("currentUser") User admin) {

        Map<String, Object> result = new HashMap<>();

        // 检查是否为管理员
        if (admin.getRole() != 1) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        result.put("success", false);
        result.put("message", "密码已加密存储，无法查看明文。如需重置密码，请使用修改密码功能。");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    }

    /**
     * 管理员修改用户密码（无需原密码）
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<Map<String, Object>> adminChangePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> passwordRequest,
            @RequestAttribute("currentUser") User admin) {

        Map<String, Object> result = new HashMap<>();

        // 检查是否为管理员
        if (admin.getRole() != 1) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        try {
            String newPassword = passwordRequest.get("newPassword");

            if (newPassword == null || newPassword.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "新密码不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            if (newPassword.length() < 6) {
                result.put("success", false);
                result.put("message", "新密码长度不能少于6位");
                return ResponseEntity.badRequest().body(result);
            }

            User user = userService.getById(id);
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return ResponseEntity.notFound().build();
            }

            // 加密新密码
            user.setPassword(passwordEncoder.encode(newPassword));
            boolean success = userService.updateById(user);

            if (success) {
                result.put("success", true);
                result.put("message", "密码修改成功");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "密码修改失败");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "密码修改失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
