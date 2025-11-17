#!/bin/bash
echo "=========================================="
echo "重置数据库脚本"
echo "=========================================="

# 停止并删除容器和数据卷
echo "停止并删除容器和数据卷..."
docker compose down -v

# 重新创建并启动
echo "重新创建并启动容器..."
docker compose up -d

# 等待数据库就绪
echo "等待数据库就绪..."
MAX_ATTEMPTS=30
ATTEMPT=0

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
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
    echo "✗ 数据库未就绪，请检查日志"
    exit 1
fi

echo "=========================================="
echo "数据库重置完成！"
echo "=========================================="
