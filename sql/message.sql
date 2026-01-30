-- 消息通知表
CREATE TABLE IF NOT EXISTS message (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '接收消息的用户ID',
    from_user_id BIGINT NOT NULL COMMENT '发送消息的用户ID',
    type INT NOT NULL COMMENT '消息类型: 1点赞 2评论 3回复',
    video_id BIGINT COMMENT '关联的视频ID',
    comment_id BIGINT COMMENT '关联的评论ID',
    content VARCHAR(500) COMMENT '消息内容',
    is_read INT DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
    create_time DATETIME,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
