# Docker 部署指南

本项目使用 Docker Compose 进行容器化部署，包含以下服务：
- MySQL 8.0 数据库
- Redis 7 缓存
- Spring Boot 后端 API
- Vue.js 前端应用

## 前置要求

- Docker 20.10+
- Docker Compose 2.0+

## 快速启动

### 1. 首次部署

```bash
# 给脚本添加执行权限
chmod +x docker-start.sh docker-stop.sh docker-logs.sh

# 启动所有服务
./docker-start.sh
```

### 2. 访问应用
请确认以下端口无占用，否则请修改 `docker compose.yml` 中的端口映射。
- 前端页面: http://localhost:80
- 后端 API: http://localhost:8080/api
- MySQL: localhost:3307
- Redis: localhost:6379

### 3. 查看日志

```bash
# 查看所有服务日志
./docker-logs.sh

# 或查看特定服务日志
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f mysql
```

### 4. 停止服务

```bash
./docker-stop.sh
```

## 常用命令

### 重新构建服务

```bash
# 重新构建所有服务
docker compose build

# 重新构建特定服务
docker compose build backend
docker compose build frontend
```

### 启动/停止单个服务

```bash
# 启动
docker compose up -d backend
docker compose up -d frontend

# 停止
docker compose stop backend
docker compose stop frontend
```

### 进入容器

```bash
# 进入后端容器
docker compose exec backend bash

# 进入数据库容器
docker compose exec mysql bash

# 执行 SQL
docker compose exec mysql mysql -uroot -proot123456 image
```

### 查看容器状态

```bash
docker compose ps
```

### 完全清理（删除数据卷）

```bash
# 警告：这将删除所有数据！
docker compose down -v
```

## 服务说明

### MySQL
- 端口: 3307 (映射到容器的 3306)
- 数据库: image
- Root 密码: root123456
- 用户: imageuser / imagepass123
- 数据持久化: mysql_data 卷

### Redis
- 端口: 6379
- 密码: redis123456
- 数据持久化: redis_data 卷

### Backend
- 端口: 8080
- 上传文件存储: ./uploads (映射到容器 /app/uploads)
- 健康检查: http://localhost:8080/api/actuator/health

### Frontend
- 端口: 80
- Nginx 服务器
- 自动代理 /api/* 请求到后端

## 环境变量配置

可以创建 `.env` 文件来自定义环境变量：

```env
# MySQL
MYSQL_ROOT_PASSWORD=your_password
MYSQL_DATABASE=image
MYSQL_USER=imageuser
MYSQL_PASSWORD=your_user_password

# Redis
REDIS_PASSWORD=your_redis_password

# Backend
SPRING_PROFILES_ACTIVE=prod
```