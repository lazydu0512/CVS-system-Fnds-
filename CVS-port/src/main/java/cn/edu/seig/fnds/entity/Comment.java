package cn.edu.seig.fnds.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论实体类
 * 
 * @author lazzydu
 */
@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long videoId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String content;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId; // 父评论ID，null表示顶级评论
    private Integer status; // 0:正常 1:隐藏
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 评论者用户信息（非数据库字段）
     */
    @TableField(exist = false)
    private User user;

    /**
     * 评论回复列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<Comment> replies;

    /**
     * 评论所属视频信息（非数据库字段）
     */
    @TableField(exist = false)
    private Video video;

    /**
     * 被回复用户的ID（非数据库字段，用于消息通知）
     */
    @TableField(exist = false)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long replyToUserId;
}