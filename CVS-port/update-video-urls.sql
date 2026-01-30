-- 更新视频数据，使用本地静态资源
USE cvs_db;

-- 更新所有视频的URL为本地静态资源
UPDATE `video` SET
    `video_url` = 'http://localhost:8080/static/video/video1.mp4',
    `thumbnail_url` = 'http://localhost:8080/static/cover/cover1.png'
WHERE 1=1;

-- 查看更新后的数据
SELECT id, title, video_url, thumbnail_url, status FROM video;

SELECT '视频URL已更新为本地静态资源！' AS message;

