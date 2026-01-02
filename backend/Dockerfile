# 多阶段构建 - 第一阶段：使用 Maven 构建
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# 先复制 pom.xml 并下载依赖（利用 Docker 缓存）
COPY pom.xml .
RUN echo "📦 [后端] 开始下载 Maven 依赖..." && \
    mvn dependency:go-offline -B && \
    echo "✅ [后端] Maven 依赖下载完成"

# 复制源代码并构建
COPY src ./src
RUN echo "🔨 [后端] 开始编译 Spring Boot 项目..." && \
    mvn clean package -DskipTests -B && \
    echo "✅ [后端] 编译完成"

# 第二阶段：运行时环境
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 安装 curl（用于健康检查）
RUN apk add --no-cache curl

# 创建上传目录
RUN mkdir -p /app/uploads/thumbnails

# 从构建阶段复制 jar 文件
COPY --from=builder /build/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 运行应用
ENTRYPOINT ["java", "-jar", "app.jar"]
