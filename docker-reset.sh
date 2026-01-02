#!/bin/bash

echo "警告：此操作将删除所有数据库和缓存数据！"
read -p "确认要重置吗？(yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "操作已取消"
    exit 0
fi

echo "停止所有容器..."
docker compose down

echo "删除数据卷..."
docker volume rm bs_mysql_data bs_redis_data
