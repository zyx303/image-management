#!/bin/bash

set -e  # 遇到错误立即退出

echo "======================================"
echo "开始构建项目"
echo "======================================"

# 1. 构建后端
echo ""
echo "[1/2] 构建后端 Spring Boot 项目..."
cd backend
./mvnw clean package -DskipTests
echo "✓ 后端构建完成"

# 2. 构建前端
echo ""
echo "[2/2] 构建前端 Vue 项目..."
cd ../front/image-vue

# 配置 npm 镜像源
npm config set registry https://registry.npmmirror.com

# 安装依赖（如果 node_modules 不存在）
if [ ! -d "node_modules" ]; then
    echo "安装前端依赖..."
    npm install
fi

# 构建
npm run build
echo "✓ 前端构建完成"

cd ../..

echo ""
echo "======================================"
echo "构建完成！"
echo "======================================"
echo ""
echo "现在可以运行以下命令启动服务："
echo "  bash ./docker-start.sh"
echo ""
