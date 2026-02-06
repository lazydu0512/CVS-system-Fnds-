-- 举报记录表
-- 用于存储用户对视频和评论的举报信息

CREATE TABLE IF NOT EXISTS `report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `target_id` BIGINT NOT NULL COMMENT '被举报目标ID(视频ID或评论ID)',
  `target_type` INT NOT NULL COMMENT '目标类型(0:视频, 1:评论)',
  `reporter_id` BIGINT NOT NULL COMMENT '举报人ID',
  `reported_user_id` BIGINT NOT NULL COMMENT '被举报人ID',
  `reason_category` VARCHAR(50) NOT NULL COMMENT '举报分类',
  `description` TEXT COMMENT '具体描述',
  `status` INT NOT NULL DEFAULT 0 COMMENT '处理状态(0:待审核, 1:举报成功, 2:举报驳回)',
  `city_code` VARCHAR(100) NOT NULL COMMENT '目标所属城市',
  `reviewer_id` BIGINT COMMENT '审核人ID',
  `review_time` DATETIME COMMENT '审核时间',
  `review_comment` TEXT COMMENT '审核意见',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_target` (`target_id`, `target_type`),
  INDEX `idx_reporter` (`reporter_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_city` (`city_code`),
  FOREIGN KEY (`reporter_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`reported_user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报记录表';
