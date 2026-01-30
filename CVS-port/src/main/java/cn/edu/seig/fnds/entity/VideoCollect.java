package cn.edu.seig.fnds.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 视频收藏实体类
 * 
 * @author lazzydu
 */
@Data
@TableName("video_collect")
public class VideoCollect {
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long videoId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private LocalDateTime createTime;

    /**
     * 关联的视频信息（非数据库字段）
     */
    @TableField(exist = false)
    private Video video;
}