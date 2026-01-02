-- 插入测试数据
USE image;

-- 插入测试用户 (密码都是 123456 的BCrypt加密)
-- BCrypt: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH (仅示例，实际需要后端生成)
INSERT INTO `user` (username, password, email, nickname, status) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@example.com', '管理员', 1),
('testuser', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'test@example.com', '测试用户', 1);

-- 插入常用标签
INSERT INTO `tag` (tag_name, tag_type, use_count) VALUES
('风景', 2, 0),
('人物', 2, 0),
('动物', 2, 0),
('建筑', 2, 0),
('美食', 2, 0),
('旅行', 2, 0),
('生活', 2, 0),
('工作', 2, 0),
('家庭', 2, 0),
('朋友', 2, 0);

-- 提示信息
SELECT '数据库初始化完成！' AS message;
SELECT CONCAT('测试账号: testuser / 123456') AS info;

