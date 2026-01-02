#!/bin/bash

echo "=========================================="
echo "图片管理系统启动脚本"
echo "=========================================="

# 创建上传目录
echo "创建上传目录..."
mkdir -p uploads/thumbnails
chmod -R 755 uploads

# 启动数据库
echo "启动数据库服务..."
docker compose up -d

# 等待数据库就绪
echo "等待数据库就绪..."
MAX_ATTEMPTS=30
ATTEMPT=0

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    # 检查容器是否在运行
    if ! docker ps | grep -q image-management-mysql; then
        echo "✗ MySQL容器未运行"
        exit 1
    fi
    
    # 尝试连接数据库
    if docker exec image-management-mysql mysqladmin ping -h localhost -u root -proot123456 --silent 2>/dev/null; then
        echo "✓ 数据库已就绪"
        break
    fi
    
    ATTEMPT=$((ATTEMPT + 1))
    if [ $ATTEMPT -lt $MAX_ATTEMPTS ]; then
        echo "  等待中... ($ATTEMPT/$MAX_ATTEMPTS)"
        sleep 2
    fi
done

if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
    echo "✗ 数据库未就绪，请检查Docker容器状态和日志"
    echo "  运行 'docker logs image-management-mysql' 查看日志"
    exit 1
fi

# 启动后端（后台运行）
echo "停止后端服务..."
pkill -f "spring-boot:run"
# 或者根据端口停止
if lsof -t -i:8080 > /dev/null 2>&1; then
    kill $(lsof -t -i:8080)
    echo "✓ 后端服务已停止"
fi
echo "启动后端服务..."
cd backend
nohup ./mvnw spring-boot:run > ../backend.log 2>&1 &
BACKEND_PID=$!
echo "后端服务已启动 (PID: $BACKEND_PID)"
cd ..

# 等待后端启动
echo "等待后端服务启动（5s）..."
sleep 5

# 启动前端
echo "启动前端服务..."
cd front/image-vue
npm run dev

# 注意：前端会占用当前终端，按 Ctrl+C 停止
# 如果需要关闭后端，运行: kill $BACKEND_PID 或查找进程 kill $(lsof -t -i:8080)
