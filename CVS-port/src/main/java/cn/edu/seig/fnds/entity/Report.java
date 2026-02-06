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
 * 举报记录实体类
 * 
 * @author lazzydu
 */
@Data
@TableName("report")
public class Report {
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 被举报目标ID(视频ID或评论ID)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetId;

    /**
     * 目标类型(0:视频, 1:评论)
     */
    private Integer targetType;

    /**
     * 举报人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reporterId;

    /**
     * 被举报人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reportedUserId;

    /**
     * 举报分类
     */
    private String reasonCategory;

    /**
     * 具体描述
     */
    private String description;

    /**
     * 处理状态(0:待审核, 1:举报成功, 2:举报驳回)
     */
    private Integer status;

    /**
     * 目标所属城市
     */
    private String cityCode;

    /**
     * 审核人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reviewerId;

    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 审核意见
     */
    private String reviewComment;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 举报者用户信息（非数据库字段）
     */
    @TableField(exist = false)
    private User reporter;

    /**
     * 被举报者用户信息（非数据库字段）
     */
    @TableField(exist = false)
    private User reportedUser;

    /**
     * 审核人用户信息（非数据库字段）
     */
    @TableField(exist = false)
    private User reviewer;

    /**
     * 被举报的视频信息（非数据库字段）
     */
    @TableField(exist = false)
    private Video targetVideo;

    /**
     * 被举报的评论信息（非数据库字段）
     */
    @TableField(exist = false)
    private Comment targetComment;
}
