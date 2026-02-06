-- 创建视频浏览日志表
-- 用于准确记录每次视频浏览的时间戳

DROP TABLE IF EXISTS `view_log`;
CREATE TABLE `view_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    `video_id` BIGINT NOT NULL COMMENT '视频ID',
    `user_id` BIGINT NULL COMMENT '用户ID，未登录为NULL',
    `view_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
    `ip_address` VARCHAR(50) NULL COMMENT 'IP地址（可选）',
    INDEX `idx_video_id` (`video_id`),
    INDEX `idx_view_time` (`view_time`),
    INDEX `idx_video_time` (`video_id`, `view_time`),
    CONSTRAINT `fk_viewlog_video` FOREIGN KEY (`video_id`) REFERENCES `video`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频浏览日志表';
