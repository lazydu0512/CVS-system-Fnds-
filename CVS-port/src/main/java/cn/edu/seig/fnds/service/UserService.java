package cn.edu.seig.fnds.service;

import cn.edu.seig.fnds.entity.User;
import cn.edu.seig.fnds.mapper.UserMapper;
import cn.edu.seig.fnds.util.JwtUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 用户服务类
 * 
 * @author lazzydu
 */
@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    /**
     * 生成登录令牌（使用JWT）
     */
    public String generateToken(User user) {
        return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    /**
     * 根据令牌获取用户（从JWT解析用户ID，然后查询数据库）
     */
    public User getUserByToken(String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId != null) {
            return getById(userId);
        }
        return null;
    }

    /**
     * 用户注册
     */
    public boolean register(User user) {
        // 检查用户名是否已存在
        if (isUsernameExists(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (StringUtils.hasText(user.getEmail()) && isEmailExists(user.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 设置默认值
        user.setRole(0); // 普通用户
        user.setStatus(0); // 正常状态
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        return save(user);
    }

    /**
     * 用户登录
     */
    public User login(String username, String password) {
        // 先根据用户名查询用户
        User user = getOne(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, username)
                .eq(User::getStatus, 0)); // 只允许正常状态用户登录

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 使用BCrypt验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        return user;
    }

    /**
     * 检查用户名是否存在
     */
    private boolean isUsernameExists(String username) {
        return count(Wrappers.<User>lambdaQuery().eq(User::getUsername, username)) > 0;
    }

    /**
     * 检查邮箱是否存在
     */
    private boolean isEmailExists(String email) {
        return count(Wrappers.<User>lambdaQuery().eq(User::getEmail, email)) > 0;
    }

    /**
     * 更新用户信息
     */
    public boolean updateUserInfo(User user) {
        user.setUpdateTime(LocalDateTime.now());
        return updateById(user);
    }
}