package cn.edu.seig.fnds.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 城市管理员关联实体类
 * 一个城市管理员可以管理多个城市，但一个城市只能被一个管理员管理
 * 
 * @author lazzydu
 */
@Data
@TableName("city_manager")
public class CityManager {
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 城市管理员用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 管理的城市ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long cityId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 城市名称（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private String cityName;

    /**
     * 管理员昵称（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private String managerName;
}
