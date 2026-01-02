#!/bin/bash

# 停止并删除所有容器
echo "停止所有容器..."
docker compose down

# 构建并启动所有服务
echo "构建并启动所有服务..."
docker compose up --build -d

# 显示日志
echo ""
echo "服务已启动，查看日志..."
docker compose logs -f
