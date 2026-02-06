package cn.edu.seig.fnds.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频浏览日志实体类
 * 
 * @author lazzydu
 */
@Data
@TableName("view_log")
public class ViewLog {
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long videoId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    private LocalDateTime viewTime;

    private String ipAddress;
}
