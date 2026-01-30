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
 * 消息通知实体类
 * 
 * @author lazzydu
 */
@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 接收消息的用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 发送消息的用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromUserId;

    /**
     * 消息类型: 1点赞 2评论 3回复
     */
    private Integer type;

    /**
     * 关联的视频ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long videoId;

    /**
     * 关联的评论ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long commentId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 是否已读: 0未读 1已读
     */
    private Integer isRead;

    private LocalDateTime createTime;

    /**
     * 发送者用户信息（非数据库字段）
     */
    @TableField(exist = false)
    private User fromUser;

    /**
     * 关联的视频信息（非数据库字段）
     */
    @TableField(exist = false)
    private Video video;
}
