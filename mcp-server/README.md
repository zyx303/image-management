# Image MCP Server

一个用于图片检索的 MCP (Model Context Protocol) 服务器，允许 AI 助手（如 Claude、ChatGPT 等）检索您的图片库。

## 功能

- **search_images**: 根据关键词搜索图片（标题、描述、文件名）
- **list_images**: 获取图片列表
- **get_image_detail**: 获取图片详细信息（包括 EXIF 数据）
- **list_tags**: 获取所有标签
- **search_images_by_tag**: 根据标签搜索图片
- **search_tags**: 搜索标签
- **get_stats**: 获取统计信息

## 安装

```bash
cd mcp-server
npm install
```

## 配置

### 环境变量

| 变量名 | 描述 | 默认值 |
|--------|------|--------|
| `IMAGE_API_URL` | 后端 API 地址 | `http://localhost:8080/api` |
| `IMAGE_API_KEY` | API Key（在系统设置中生成） | - |

### Claude Desktop 配置

编辑 `~/.config/claude/claude_desktop_config.json`（Linux）或 `%APPDATA%\Claude\claude_desktop_config.json`（Windows）：

```json
{
  "mcpServers": {
    "image-search": {
      "command": "node",
      "args": ["/path/to/mcp-server/src/index.js"],
      "env": {
        "IMAGE_API_URL": "http://localhost:8080/api",
        "IMAGE_API_KEY": "your_api_key_here"
      }
    }
  }
}
```

### Cursor 配置

编辑 `~/.cursor/mcp.json`：

```json
{
  "mcpServers": {
    "image-search": {
      "command": "node",
      "args": ["/path/to/mcp-server/src/index.js"],
      "env": {
        "IMAGE_API_URL": "http://localhost:8080/api",
        "IMAGE_API_KEY": "your_api_key_here"
      }
    }
  }
}
```

## 获取 API Key

1. 登录图片管理系统
2. 进入「设置」->「MCP 接口」
3. 点击「创建 API Key」
4. 复制生成的 API Key

## 安全说明

- 每个 API Key 只能访问对应用户的图片
- API Key 可以随时禁用或重新生成
- 请妥善保管 API Key，不要泄露给他人

## 使用示例

配置完成后，您可以在 AI 助手中进行如下对话：

- "搜索我的图片库中包含'风景'的图片"
- "列出我最近上传的 10 张图片"
- "查看图片 ID 为 5 的详细信息"
- "我有哪些图片标签？"
- "查找标签为'旅行'的所有图片"

## 开发

```bash
# 启动开发模式
npm run dev

# 正常启动
npm start
```

## 许可证

MIT
