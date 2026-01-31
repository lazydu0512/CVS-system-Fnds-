package cn.edu.seig.fnds.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密配置类
 * 
 * @author lazzydu
 */
@Configuration
public class PasswordConfig {

    /**
     * 密码加密器Bean
     * 使用BCrypt强哈希算法，自动加盐，抗暴力破解
     * 
     * @return BCryptPasswordEncoder实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
