package cn.edu.seig.fnds.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 视频实体类
 * 
 * @author lazzydu
 */
@Data
@TableName("video")
public class Video {
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String title;
    private String description;
    private String city;
    private LocalDate concertDate;
    private String videoUrl;
    private String thumbnailUrl;
    private Integer duration; // 视频时长(秒)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long uploaderId;
    private Integer status; // 0:待审核 1:审核通过 2:审核拒绝
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reviewerId; // 审核人ID
    private LocalDateTime reviewTime; // 审核时间
    private String reviewComment; // 审核意见
    private Integer viewCount; // 观看次数
    private Integer likeCount; // 点赞次数
    private Integer collectCount; // 收藏次数
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer offlineStatus; // 下架状态 0:正常 1:用户下架 2:管理员下架
    private String offlineReason; // 下架理由
    private LocalDateTime offlineTime; // 下架时间

    /**
     * 上传者昵称（非数据库字段）
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String uploaderName;

    /**
     * 上传者头像（非数据库字段）
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String uploaderAvatar;
}
