-- 数据库修复脚本
-- 用于修复现有数据库表结构，使其与最新的init.sql一致

USE cvs_db;

-- 检查并修复user表
-- 如果email列不存在，则添加
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'cvs_db' 
  AND TABLE_NAME = 'user' 
  AND COLUMN_NAME = 'email';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `user` ADD COLUMN `email` VARCHAR(100) COMMENT ''邮箱'' AFTER `nickname`',
    'SELECT ''email column already exists'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加email唯一索引（如果不存在）
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists 
FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = 'cvs_db' 
  AND TABLE_NAME = 'user' 
  AND INDEX_NAME = 'uk_email';

SET @sql = IF(@index_exists = 0,
    'ALTER TABLE `user` ADD UNIQUE KEY `uk_email` (`email`)',
    'SELECT ''uk_email index already exists'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并修复phone列
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'cvs_db' 
  AND TABLE_NAME = 'user' 
  AND COLUMN_NAME = 'phone';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `user` ADD COLUMN `phone` VARCHAR(20) COMMENT ''手机号'' AFTER `email`',
    'SELECT ''phone column already exists'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并修复avatar列
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'cvs_db' 
  AND TABLE_NAME = 'user' 
  AND COLUMN_NAME = 'avatar';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `user` ADD COLUMN `avatar` VARCHAR(500) COMMENT ''头像URL'' AFTER `phone`',
    'SELECT ''avatar column already exists'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 显示修复后的表结构
SHOW CREATE TABLE `user`;

SELECT '数据库修复完成！' AS message;

