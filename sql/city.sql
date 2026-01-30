-- 城市表
CREATE TABLE IF NOT EXISTS city (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '城市名称',
    sort INT DEFAULT 0 COMMENT '排序值，越大越靠前',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入默认数据
INSERT INTO city (name, sort) VALUES 
('北京', 100),
('上海', 90),
('广州', 80),
('深圳', 70),
('杭州', 60),
('南京', 50),
('成都', 40),
('武汉', 30),
('西安', 20),
('重庆', 10),
('天津', 0),
('苏州', 0),
('青岛', 0),
('大连', 0),
('宁波', 0),
('厦门', 0);
