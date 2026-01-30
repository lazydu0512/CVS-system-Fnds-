-- 演唱会视频分享平台数据库初始化脚本
-- 数据库名：cvs_db

-- 创建数据库
CREATE DATABASE IF NOT EXISTS cvs_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cvs_db;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(100) NOT NULL COMMENT '昵称',
    `email` VARCHAR(100) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `role` INT NOT NULL DEFAULT 0 COMMENT '角色：0-普通用户 1-城市管理员',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态：0-正常 1-禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 视频表
CREATE TABLE IF NOT EXISTS `video` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '视频ID',
    `title` VARCHAR(200) NOT NULL COMMENT '视频标题',
    `description` TEXT COMMENT '视频描述',
    `city` VARCHAR(100) NOT NULL COMMENT '城市',
    `concert_date` DATE NOT NULL COMMENT '演唱会日期',
    `video_url` VARCHAR(1000) NOT NULL COMMENT '视频URL',
    `thumbnail_url` VARCHAR(1000) COMMENT '缩略图URL',
    `duration` INT COMMENT '视频时长(秒)',
    `uploader_id` BIGINT NOT NULL COMMENT '上传者ID',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态：0-待审核 1-审核通过 2-审核拒绝',
    `reviewer_id` BIGINT COMMENT '审核人ID',
    `review_time` DATETIME COMMENT '审核时间',
    `review_comment` TEXT COMMENT '审核意见',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '观看次数',
    `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞次数',
    `collect_count` INT NOT NULL DEFAULT 0 COMMENT '收藏次数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_uploader_id` (`uploader_id`),
    KEY `idx_status` (`status`),
    KEY `idx_city` (`city`),
    KEY `idx_create_time` (`create_time`),
    KEY `fk_uploader` (`uploader_id`),
    KEY `fk_reviewer` (`reviewer_id`),
    CONSTRAINT `fk_video_uploader` FOREIGN KEY (`uploader_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_video_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频表';

-- 评论表
CREATE TABLE IF NOT EXISTS `comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `video_id` BIGINT NOT NULL COMMENT '视频ID',
    `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `parent_id` BIGINT COMMENT '父评论ID，null表示顶级评论',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态：0-正常 1-隐藏',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_video_id` (`video_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`),
    KEY `fk_comment_video` (`video_id`),
    KEY `fk_comment_user` (`user_id`),
    KEY `fk_comment_parent` (`parent_id`),
    CONSTRAINT `fk_comment_video` FOREIGN KEY (`video_id`) REFERENCES `video` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `comment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 视频点赞表
CREATE TABLE IF NOT EXISTS `video_like` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
    `video_id` BIGINT NOT NULL COMMENT '视频ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_video_user` (`video_id`, `user_id`),
    KEY `idx_video_id` (`video_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `fk_like_video` (`video_id`),
    KEY `fk_like_user` (`user_id`),
    CONSTRAINT `fk_like_video` FOREIGN KEY (`video_id`) REFERENCES `video` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频点赞表';

-- 视频收藏表
CREATE TABLE IF NOT EXISTS `video_collect` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `video_id` BIGINT NOT NULL COMMENT '视频ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_collect_video_user` (`video_id`, `user_id`),
    KEY `idx_video_id` (`video_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `fk_collect_video` (`video_id`),
    KEY `fk_collect_user` (`user_id`),
    CONSTRAINT `fk_collect_video` FOREIGN KEY (`video_id`) REFERENCES `video` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_collect_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频收藏表';

-- 消息通知表
CREATE TABLE IF NOT EXISTS `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `user_id` BIGINT NOT NULL COMMENT '接收消息的用户ID',
    `from_user_id` BIGINT NOT NULL COMMENT '发送消息的用户ID',
    `type` INT NOT NULL COMMENT '消息类型: 1点赞 2评论 3回复 4系统通知',
    `video_id` BIGINT COMMENT '关联的视频ID',
    `comment_id` BIGINT COMMENT '关联的评论ID',
    `content` VARCHAR(500) COMMENT '消息内容',
    `is_read` INT NOT NULL DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_read` (`is_read`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_message_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_message_from_user` FOREIGN KEY (`from_user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_message_video` FOREIGN KEY (`video_id`) REFERENCES `video` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- 插入测试数据
-- 管理员用户
INSERT INTO `user` (`username`, `password`, `nickname`, `email`, `role`, `status`) VALUES
('admin', '123456', '系统管理员', 'admin@cvs.com', 1, 0);

-- 普通用户
INSERT INTO `user` (`username`, `password`, `nickname`, `email`, `phone`, `role`, `status`) VALUES
('user1', '123456', '音乐爱好者', 'user1@example.com', '13800138001', 0, 0),
('user2', '123456', '演唱会粉丝', 'user2@example.com', '13800138002', 0, 0);

-- 测试视频数据
INSERT INTO `video` (`title`, `description`, `city`, `concert_date`, `video_url`, `thumbnail_url`, `duration`, `uploader_id`, `status`, `view_count`, `like_count`, `collect_count`) VALUES
('周杰伦演唱会精彩片段', '周杰伦魔天伦世界巡回演唱会北京站精彩表演', '北京', '2023-08-15', 'https://example.com/video1.mp4', 'https://example.com/thumb1.jpg', 300, 2, 1, 1250, 89, 45),
('五月天演唱会完整版', '五月天人生无限公司巡演上海站', '上海', '2023-07-20', 'https://example.com/video2.mp4', 'https://example.com/thumb2.jpg', 480, 3, 1, 2100, 156, 78),
('张学友经典演唱会', '张学友60+巡回演唱会广州站', '广州', '2023-06-10', 'https://example.com/video3.mp4', 'https://example.com/thumb3.jpg', 360, 2, 1, 980, 67, 32),
('邓紫棋Queen of Hearts巡演', '邓紫棋Queen of Hearts世界巡回演唱会深圳站', '深圳', '2023-09-05', 'https://example.com/video4.mp4', 'https://example.com/thumb4.jpg', 420, 3, 1, 1876, 134, 89),
('林俊杰JJ20世界巡回演唱会', '林俊杰JJ20世界巡回演唱会杭州站', '杭州', '2023-05-28', 'https://example.com/video5.mp4', 'https://example.com/thumb5.jpg', 390, 2, 0, 0, 0, 0);

-- 测试评论数据
INSERT INTO `comment` (`video_id`, `user_id`, `content`, `status`) VALUES
(1, 3, '太精彩了！周杰伦的现场太棒了！', 0),
(1, 2, '每次看都觉得不过瘾，希望能多发一些片段', 0),
(2, 2, '五月天的现场永远这么燃！', 0),
(3, 3, '张学友的歌声真是经典中的经典', 0);

-- 测试点赞数据
INSERT INTO `video_like` (`video_id`, `user_id`) VALUES
(1, 2), (1, 3), (2, 2), (2, 3), (3, 2), (4, 3);

-- 测试收藏数据
INSERT INTO `video_collect` (`video_id`, `user_id`) VALUES
(1, 2), (2, 3), (3, 2), (4, 3);