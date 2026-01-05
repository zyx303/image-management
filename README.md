# 图片管理系统

## 项目简介

基于 Spring Boot + Vue 3 的图片管理系统，支持图片上传、管理、标签分类、AI 识别和 EXIF 信息提取,mcp 接口等功能。

### 主要功能

- 📸 图片上传、下载、删除、裁剪和色调调整
- 🏷️ 标签管理（手动标签、自动标签、AI 识别标签）
- 📊 EXIF 信息提取（拍摄时间、相机信息、GPS 位置等）
- 🤖 AI 图像识别（基于百度智能云）
- 👤 用户注册登录、个人资料管理
- 🔍 MCP 接口

## 依赖


- Docker 20.10+
- Docker Compose 2.0+


## 部署方式

### 一键部署

```bash
# 给脚本添加执行权限
chmod +x deploy.sh

# 启动所有服务（构建镜像并启动容器）
./deploy.sh start
```

### 部署命令

```bash
./deploy.sh start      # 构建并启动服务
./deploy.sh stop       # 停止服务
./deploy.sh restart    # 重启服务
./deploy.sh logs       # 查看日志
./deploy.sh status     # 查看服务状态
./deploy.sh clean      # 完全清理（包括数据）
./deploy.sh rebuild    # 强制重新构建
```

### 访问地址

- **前端页面**: http://服务器IP:8083
- **后端 API**: http://服务器IP:8085/api
- **MySQL**: localhost:3308 (仅容器内访问，外部访问需使用此端口)
- **Redis**: localhost:6380 (仅容器内访问，外部访问需使用此端口)

如需修改端口，请编辑 `docker-compose.yml` 文件中的 `ports` 配置。

### 默认配置

- **MySQL**: root/root123456, 数据库: image
- **Redis**: 密码: redis123456
- **上传文件**: 存储在 `./uploads` 目录


