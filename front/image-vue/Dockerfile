# Dockerfile - 使用本地构建
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# 删除默认的 nginx 内容
RUN rm -rf ./*

# 从本地复制构建结果（需要先在本地运行 npm run build）
COPY dist .

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
