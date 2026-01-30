package cn.edu.seig.fnds.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 城市实体类
 * 
 * @author lazzydu
 */
@Data
@TableName("city")
public class City {
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 城市名称
     */
    private String name;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
