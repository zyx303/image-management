# 多阶段构建 - 第一阶段：使用 Node.js 构建
FROM node:20-alpine AS builder
WORKDIR /build

# 复制 package.json 和 package-lock.json（利用 Docker 缓存）
COPY package*.json ./
RUN echo "📦 [前端] 开始安装 npm 依赖..." && \
    npm ci --only=production=false && \
    echo "✅ [前端] npm 依赖安装完成"

# 复制源代码并构建
COPY . .
RUN echo "🔨 [前端] 开始构建 Vue 项目..." && \
    npm run build && \
    echo "✅ [前端] 构建完成"

# 第二阶段：运行时环境
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# 删除默认的 nginx 内容
RUN rm -rf ./*

# 从构建阶段复制构建结果
COPY --from=builder /build/dist .

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
