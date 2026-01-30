package cn.edu.seig.fnds.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 
 * @author lazzydu
 */
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Integer role; // 0:普通用户 1:系统管理员 2:城市管理员
    private Integer status; // 0:正常 1:禁用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
