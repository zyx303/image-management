#!/bin/bash

# 图片管理系统 - 一键部署脚本
# 使用 Docker 多阶段构建，无需本地安装 Node.js 和 Maven

set -e

echo "======================================"
echo "   图片管理系统 - 一键部署脚本"
echo "======================================"
echo ""

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    echo "❌ 错误: 未检测到 Docker，请先安装 Docker"
    echo "   访问 https://docs.docker.com/get-docker/ 下载安装"
    exit 1
fi

# 检查 Docker Compose 是否可用
if ! docker compose version &> /dev/null; then
    echo "❌ 错误: Docker Compose 不可用"
    echo "   请确保安装了 Docker Compose v2"
    exit 1
fi

echo "✅ Docker 环境检查通过"
echo ""

# 解析命令行参数
COMMAND=${1:-start}

case $COMMAND in
    start)
        echo "🚀 开始构建并启动服务..."
        echo ""
        
        # 停止并删除旧容器
        echo "🧹 清理旧容器..."
        docker compose down 2>/dev/null || true
        
        echo ""
        echo "🔨 开始构建镜像（显示实时进度）..."
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo ""
        
        # 使用 --progress=plain 显示详细构建过程
        docker compose build --progress=plain
        
        echo ""
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo "✅ 镜像构建完成，正在启动容器..."
        echo ""
        
        # 启动服务
        docker compose up -d
        
        echo ""
        echo "⏳ 等待服务完全启动..."
        
        # 等待后端服务健康检查通过
        echo -n "   检查后端服务"
        for i in {1..30}; do
            if docker compose ps | grep -q "image-management-backend.*healthy"; then
                echo " ✅"
                break
            fi
            echo -n "."
            sleep 2
        done
        
        echo -n "   检查前端服务"
        for i in {1..15}; do
            if docker compose ps | grep -q "image-management-frontend.*Up"; then
                echo " ✅"
                break
            fi
            echo -n "."
            sleep 1
        done
        
        # 检查服务状态
        echo ""
        echo "📊 服务状态:"
        docker compose ps
        
        echo ""
        echo "✅ 部署完成！"
        echo ""
        echo "📝 访问地址:"
        echo "   前端: http://localhost"
        echo "   后端: http://localhost:8080/api"
        echo "   MySQL: localhost:3307"
        echo "   Redis: localhost:6379"
        echo ""
        echo "💡 提示:"
        echo "   - 查看日志: ./deploy.sh logs"
        echo "   - 停止服务: ./deploy.sh stop"
        echo "   - 重启服务: ./deploy.sh restart"
        echo "   - 完全清理: ./deploy.sh clean"
        ;;
        
    stop)
        echo "🛑 停止服务..."
        docker compose stop
        echo "✅ 服务已停止"
        ;;
        
    restart)
        echo "🔄 重启服务..."
        docker compose restart
        echo "✅ 服务已重启"
        ;;
        
    logs)
        echo "📋 显示服务日志 (Ctrl+C 退出)..."
        docker compose logs -f
        ;;
        
    status)
        echo "📊 服务状态:"
        docker compose ps
        ;;
        
    clean)
        echo "🗑️  完全清理（包括数据卷）..."
        read -p "⚠️  警告: 这将删除所有数据，确认继续？(y/N) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            docker compose down -v
            echo "✅ 清理完成"
        else
            echo "❌ 已取消"
        fi
        ;;
        
    rebuild)
        echo "🔨 重新构建服务..."
        docker compose down
        docker compose build --no-cache
        docker compose up -d
        echo "✅ 重新构建完成"
        ;;
        
    *)
        echo "用法: $0 {start|stop|restart|logs|status|clean|rebuild}"
        echo ""
        echo "命令说明:"
        echo "  start   - 构建并启动所有服务（默认）"
        echo "  stop    - 停止所有服务"
        echo "  restart - 重启所有服务"
        echo "  logs    - 查看服务日志"
        echo "  status  - 查看服务状态"
        echo "  clean   - 完全清理（包括数据）"
        echo "  rebuild - 强制重新构建"
        exit 1
        ;;
esac

