-- 城市管理员关联表
-- 一个城市管理员可以管理多个城市，但一个城市只能被一个管理员管理

CREATE TABLE city_manager (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '城市管理员用户ID',
    city_id BIGINT NOT NULL COMMENT '管理的城市ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_city (city_id) COMMENT '一个城市只能有一个管理员',
    KEY idx_user (user_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (city_id) REFERENCES city(id) ON DELETE CASCADE
);
