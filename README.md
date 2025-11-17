# 图片管理系统

一个功能完善的Web图片管理系统，支持用户注册登录、图片上传存储、智能分类、检索查询、在线编辑等功能。

## 技术栈

### 前端
- Vue 3.5.22
- Vue Router 4.6.3
- Pinia 3.0.3
- Vite 7.1.11

### 后端
- Spring Boot 3.5.7
- MyBatis Plus 3.5.5
- MySQL 8.0
- Redis 7
- JWT

## 快速开始

### 1. 启动数据库

使用 Docker Compose 启动 MySQL 和 Redis：

```bash
docker compose up -d
```

这将启动：
- MySQL 8.0 (端口 3306)
- Redis 7 (端口 6379)

数据库会自动初始化，创建所需的表和测试数据。

#### 数据库连接信息

**MySQL:**
- 主机: localhost:3306
- 数据库: image
- 用户名: imageuser
- 密码: imagepass123
- Root密码: root123456

**Redis:**
- 主机: localhost:6379
- 密码: redis123456

#### 测试账号
- 用户名: testuser
- 密码: 123456

### 2. 启动后端

确保 JDK 21 已安装：

```bash
cd backend
mvn spring-boot:run
```

后端将在 http://localhost:8080 启动

### 3. 启动前端

确保 Node.js 20+ 已安装：

```bash
cd front/image-vue
npm install
npm run dev
```

前端将在 http://localhost:5173 启动

### 4. 使用启动脚本

或者使用提供的启动脚本（需要修改为并行启动）：

```bash
chmod +x start.sh
./start.sh
```

## 项目结构

```
bs/
├── backend/                    # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/zyx/image/
│   │   │   │   ├── ImageApplication.java
│   │   │   │   ├── config/          # 配置类
│   │   │   │   ├── controller/      # 控制器
│   │   │   │   ├── service/         # 服务层
│   │   │   │   ├── entity/          # 实体类
│   │   │   │   ├── mapper/          # Mapper
│   │   │   │   └── utils/           # 工具类
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       ├── application-dev.properties
│   │   │       └── application-prod.properties
│   │   └── test/
│   └── pom.xml
├── front/                      # 前端项目
│   └── image-vue/
│       ├── src/
│       │   ├── views/          # 页面
│       │   ├── components/     # 组件
│       │   ├── router/         # 路由
│       │   ├── stores/         # 状态管理
│       │   └── api/            # API接口
│       └── package.json
├── database/                   # 数据库脚本
│   ├── init/                   # 初始化脚本
│   │   ├── 01-create-tables.sql
│   │   └── 02-insert-test-data.sql
│   └── conf/                   # MySQL配置
│       └── my.cnf
├── uploads/                    # 文件上传目录（自动创建）
├── docker-compose.yml          # Docker配置
├── start.sh                    # 启动脚本
└── README.md                   # 项目说明
```

## 数据库表结构

系统包含以下数据表：

1. **user** - 用户表
2. **image** - 图片表
3. **tag** - 标签表
4. **image_tag** - 图片标签关联表
5. **album** - 相册表
6. **album_image** - 相册图片关联表

详细的表结构请查看 `database/init/01-create-tables.sql`

## 核心功能

### 已实现功能
- [x] 项目初始化
- [x] 数据库设计
- [x] Docker环境配置

### 待实现功能
- [ ] 用户注册登录
- [ ] 图片上传（单张/批量）
- [ ] EXIF信息解析
- [ ] 缩略图生成
- [ ] 标签管理（自动/自定义）
- [ ] 图片检索
- [ ] 图片展示（网格/瀑布流/轮播）
- [ ] 图片编辑（裁剪/滤镜）
- [ ] 移动端适配
- [ ] AI图片分析（增强功能）
- [ ] MCP接口（增强功能）

## 开发环境

### 环境要求
- JDK 21+
- Node.js 20+
- Maven 3.6+
- Docker & Docker Compose

### IDE推荐
- 后端: IntelliJ IDEA
- 前端: VS Code / Cursor

## 常用命令

### Docker 命令
```bash
# 启动服务
docker compose up -d

# 停止服务
docker compose down

# 查看日志
docker compose logs -f mysql

# 重启服务
docker compose restart

# 查看服务状态
docker compose ps
```

### 后端命令
```bash
# 编译
mvn clean compile

# 打包
mvn clean package

# 运行
mvn spring-boot:run

# 跳过测试打包
mvn clean package -DskipTests
```

### 前端命令
```bash
# 安装依赖
npm install

# 开发模式
npm run dev

# 生产构建
npm run build

# 代码检查
npm run lint

# 代码格式化
npm run format
```

## 数据库管理

### 连接MySQL
```bash
# 使用Docker容器连接
docker exec -it image-management-mysql mysql -u imageuser -p

# 或使用root用户
docker exec -it image-management-mysql mysql -u root -p
```

### 手动创建数据库（如需要）
```sql
CREATE DATABASE IF NOT EXISTS image 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;
```

### 导入初始化脚本（如需要）
```bash
docker exec -i image-management-mysql mysql -u root -proot123456 image < database/init/01-create-tables.sql
docker exec -i image-management-mysql mysql -u root -proot123456 image < database/init/02-insert-test-data.sql
```

## API接口文档

### 认证接口
- POST `/api/auth/register` - 用户注册
- POST `/api/auth/login` - 用户登录
- POST `/api/auth/logout` - 用户登出

### 图片接口
- GET `/api/images` - 获取图片列表
- GET `/api/images/{id}` - 获取图片详情
- POST `/api/images/upload` - 上传图片
- PUT `/api/images/{id}` - 更新图片信息
- DELETE `/api/images/{id}` - 删除图片

详细API文档将在开发过程中补充。

## 配置说明

### 修改数据库密码

编辑 `docker-compose.yml`:
```yaml
environment:
  MYSQL_ROOT_PASSWORD: 你的root密码
  MYSQL_PASSWORD: 你的用户密码
```

同时修改 `backend/src/main/resources/application.properties`:
```properties
spring.datasource.password=你的用户密码
```

### 修改上传路径

编辑 `backend/src/main/resources/application.properties`:
```properties
file.upload.path=你的上传路径
```

## 故障排查

### 数据库连接失败
1. 确认 Docker 容器正在运行: `docker compose ps`
2. 检查数据库密码是否正确
3. 查看数据库日志: `docker compose logs mysql`

### 端口冲突
如果端口被占用，修改 `docker-compose.yml` 中的端口映射：
```yaml
ports:
  - "3307:3306"  # 将MySQL映射到3307端口
```

### 上传目录权限
```bash
mkdir -p uploads/thumbnails
chmod -R 755 uploads
```

## 贡献

本项目为课程设计项目。

## 许可

MIT License

## 联系方式

如有问题，请联系开发者。

