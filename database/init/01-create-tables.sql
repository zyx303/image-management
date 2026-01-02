-- 图片管理系统数据库初始化脚本
-- Database: image

USE image;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码(加密)',
  `email` VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
  `nickname` VARCHAR(50) COMMENT '昵称',
  `avatar` VARCHAR(255) COMMENT '头像URL',
  `phone` VARCHAR(20) COMMENT '手机号',
  `status` TINYINT DEFAULT 1 COMMENT '状态:1-正常,0-禁用',
  `baidu_api_key` VARCHAR(100) COMMENT '百度智能云API Key',
  `baidu_secret_key` VARCHAR(100) COMMENT '百度智能云Secret Key',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_username (username),
  INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 图片表
CREATE TABLE IF NOT EXISTS `image` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '图片ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(200) COMMENT '图片标题',
  `description` TEXT COMMENT '图片描述',
  `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
  `file_size` BIGINT COMMENT '文件大小(字节)',
  `file_type` VARCHAR(50) COMMENT '文件类型',
  `width` INT COMMENT '图片宽度',
  `height` INT COMMENT '图片高度',
  `thumbnail_path` VARCHAR(500) COMMENT '缩略图路径',
  `md5` VARCHAR(32) COMMENT '文件MD5',
  `upload_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `shoot_time` DATETIME COMMENT '拍摄时间(EXIF)',
  `location` VARCHAR(255) COMMENT '拍摄地点(EXIF)',
  `device` VARCHAR(100) COMMENT '拍摄设备(EXIF)',
  `camera_model` VARCHAR(100) COMMENT '相机型号',
  `focal_length` VARCHAR(50) COMMENT '焦距',
  `aperture` VARCHAR(50) COMMENT '光圈',
  `iso` VARCHAR(50) COMMENT 'ISO',
  `view_count` INT DEFAULT 0 COMMENT '浏览次数',
  `status` TINYINT DEFAULT 1 COMMENT '状态:1-正常,0-删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_user_id (user_id),
  INDEX idx_upload_time (upload_time),
  INDEX idx_shoot_time (shoot_time),
  INDEX idx_md5 (md5),
  INDEX idx_status (status),
  FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片表';

-- 3. 标签表
CREATE TABLE IF NOT EXISTS `tag` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
  `tag_name` VARCHAR(50) UNIQUE NOT NULL COMMENT '标签名称',
  `tag_type` TINYINT COMMENT '标签类型:1-自动,2-自定义,3-AI',
  `use_count` INT DEFAULT 0 COMMENT '使用次数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_tag_name (tag_name),
  INDEX idx_tag_type (tag_type),
  INDEX idx_use_count (use_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- 4. 图片标签关联表
CREATE TABLE IF NOT EXISTS `image_tag` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
  `image_id` BIGINT NOT NULL COMMENT '图片ID',
  `tag_id` BIGINT NOT NULL COMMENT '标签ID',
  `confidence` DECIMAL(5,2) COMMENT 'AI识别置信度(0-100)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_image_tag (image_id, tag_id),
  INDEX idx_image_id (image_id),
  INDEX idx_tag_id (tag_id),
  FOREIGN KEY (image_id) REFERENCES image(id) ON DELETE CASCADE,
  FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片标签关联表';

-- 5. 相册表
CREATE TABLE IF NOT EXISTS `album` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '相册ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `album_name` VARCHAR(100) NOT NULL COMMENT '相册名称',
  `description` TEXT COMMENT '相册描述',
  `cover_image_id` BIGINT COMMENT '封面图片ID',
  `image_count` INT DEFAULT 0 COMMENT '图片数量',
  `is_public` TINYINT DEFAULT 0 COMMENT '是否公开:1-公开,0-私密',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_user_id (user_id),
  INDEX idx_is_public (is_public),
  FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='相册表';

-- 6. 相册图片关联表
CREATE TABLE IF NOT EXISTS `album_image` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
  `album_id` BIGINT NOT NULL COMMENT '相册ID',
  `image_id` BIGINT NOT NULL COMMENT '图片ID',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_album_image (album_id, image_id),
  INDEX idx_album_id (album_id),
  INDEX idx_image_id (image_id),
  FOREIGN KEY (album_id) REFERENCES album(id) ON DELETE CASCADE,
  FOREIGN KEY (image_id) REFERENCES image(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='相册图片关联表';

-- 7. API Key 表 (用于 MCP 接口认证)
CREATE TABLE IF NOT EXISTS `api_key` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'API Key ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `api_key` VARCHAR(100) UNIQUE NOT NULL COMMENT 'API Key',
  `name` VARCHAR(100) COMMENT 'API Key 名称',
  `enable` TINYINT DEFAULT 1 COMMENT '状态:1-启用,0-禁用',
  `last_used_time` DATETIME COMMENT '最后使用时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `expire_time` DATETIME COMMENT '过期时间',
  INDEX idx_user_id (user_id),
  INDEX idx_api_key (api_key),
  INDEX idx_enable (enable),
  FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API Key表';

