# 使用本地构建的 jar 文件
FROM eclipse-temurin:21-jre
WORKDIR /app

# 创建上传目录
RUN mkdir -p /app/uploads/thumbnails

# 从本地复制已构建的 jar 文件
COPY target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 运行应用
ENTRYPOINT ["java", "-jar", "app.jar"]
