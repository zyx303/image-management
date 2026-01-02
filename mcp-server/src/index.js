#!/usr/bin/env node

import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import {
  CallToolRequestSchema,
  ListToolsRequestSchema,
} from "@modelcontextprotocol/sdk/types.js";

// 配置 - 可通过环境变量覆盖
const config = {
  apiBaseUrl: process.env.IMAGE_API_URL || "http://localhost:8080/api",
  apiKey: process.env.IMAGE_API_KEY || "",
};

/**
 * 发起 HTTP 请求到后端 API
 */
async function apiRequest(endpoint, options = {}) {
  const url = `${config.apiBaseUrl}${endpoint}`;
  const headers = {
    "Content-Type": "application/json",
    ...(config.apiKey && { "X-API-Key": config.apiKey }),
    ...options.headers,
  };

  try {
    const response = await fetch(url, {
      ...options,
      headers,
    });

    if (!response.ok) {
      throw new Error(`API request failed: ${response.status} ${response.statusText}`);
    }

    return await response.json();
  } catch (error) {
    throw new Error(`API request error: ${error.message}`);
  }
}

/**
 * 搜索图片
 */
async function searchImages(keyword, page = 1, pageSize = 10) {
  const params = new URLSearchParams({
    current: page.toString(),
    size: pageSize.toString(),
  });
  
  if (keyword) {
    params.append("keyword", keyword);
  }

  const result = await apiRequest(`/mcp/images/search?${params.toString()}`);
  return result;
}

/**
 * 获取图片列表
 */
async function listImages(page = 1, pageSize = 10) {
  const params = new URLSearchParams({
    current: page.toString(),
    size: pageSize.toString(),
  });

  const result = await apiRequest(`/mcp/images?${params.toString()}`);
  return result;
}

/**
 * 获取图片详情
 */
async function getImageDetail(imageId) {
  const result = await apiRequest(`/mcp/images/${imageId}`);
  return result;
}

/**
 * 获取标签列表
 */
async function listTags() {
  const result = await apiRequest("/mcp/tags");
  return result;
}

/**
 * 根据标签搜索图片
 */
async function searchImagesByTag(tagId, page = 1, pageSize = 10) {
  const params = new URLSearchParams({
    current: page.toString(),
    size: pageSize.toString(),
  });

  const result = await apiRequest(`/mcp/tags/${tagId}/images?${params.toString()}`);
  return result;
}

/**
 * 搜索标签
 */
async function searchTags(keyword) {
  const params = new URLSearchParams();
  if (keyword) {
    params.append("keyword", keyword);
  }
  const result = await apiRequest(`/mcp/tags/search?${params.toString()}`);
  return result;
}

/**
 * 获取系统统计
 */
async function getStats() {
  const result = await apiRequest("/mcp/stats");
  return result;
}

/**
 * 格式化图片信息为可读文本
 */
function formatImageInfo(image) {
  const baseUrl = config.apiBaseUrl.replace("/api", "");
  const lines = [
    `📷 **${image.title || image.fileName || "未命名图片"}**`,
    `- ID: ${image.id}`,
    `- 文件名: ${image.fileName}`,
  ];
  lines.push('api key: ' + config.apiKey);
  if (image.description) {
    lines.push(`- 描述: ${image.description}`);
  }

  if (image.width && image.height) {
    lines.push(`- 尺寸: ${image.width} x ${image.height}`);
  }

  if (image.fileSize) {
    const sizeMB = (image.fileSize / 1024 / 1024).toFixed(2);
    lines.push(`- 文件大小: ${sizeMB} MB`);
  }

  if (image.uploadTime) {
    lines.push(`- 上传时间: ${image.uploadTime}`);
  }

  if (image.shootTime) {
    lines.push(`- 拍摄时间: ${image.shootTime}`);
  }

  if (image.device) {
    lines.push(`- 拍摄设备: ${image.device}`);
  }

  if (image.cameraModel) {
    lines.push(`- 相机型号: ${image.cameraModel}`);
  }

  if (image.location) {
    lines.push(`- 拍摄地点: ${image.location}`);
  }

  if (image.tags && image.tags.length > 0) {
    const tagNames = image.tags.map((t) => t.tagName).join(", ");
    lines.push(`- 标签: ${tagNames}`);
  }

  if (image.viewCount !== undefined) {
    lines.push(`- 浏览次数: ${image.viewCount}`);
  }

  // 添加访问链接（带 API Key）
  const apiKeyParam = config.apiKey ? `?api_key=${encodeURIComponent(config.apiKey)}` : "";
  if (image.thumbnailPath) {
    lines.push(`- 缩略图: ${baseUrl}/api/files/thumbnails/${image.thumbnailPath}${apiKeyParam}`);
  }
  if (image.filePath) {
    lines.push(`- 原图: ${baseUrl}/api/files/${image.filePath}${apiKeyParam}`);
  }

  return lines.join("\n");
}

/**
 * 格式化图片列表
 */
function formatImageList(images, total, page, pageSize) {
  if (!images || images.length === 0) {
    return "没有找到符合条件的图片。";
  }

  const totalPages = Math.ceil(total / pageSize);
  const header = `找到 ${total} 张图片 (第 ${page}/${totalPages} 页):\n\n`;
  const imageList = images.map((img, index) => {
    const num = (page - 1) * pageSize + index + 1;
    const title = img.title || img.fileName || "未命名";
    const tags = img.tags ? img.tags.map((t) => t.tagName).join(", ") : "";
    return `${num}. **${title}** (ID: ${img.id})${tags ? ` [${tags}]` : ""}`;
  });

  return header + imageList.join("\n");
}

/**
 * 格式化标签列表
 */
function formatTagList(tags) {
  if (!tags || tags.length === 0) {
    return "没有可用的标签。";
  }

  const header = `共有 ${tags.length} 个标签:\n\n`;
  const tagList = tags.map((tag) => {
    const typeLabel = {
      1: "自动",
      2: "自定义",
      3: "AI",
    }[tag.tagType] || "未知";
    return `- **${tag.tagName}** (ID: ${tag.id}, 类型: ${typeLabel}, 使用次数: ${tag.useCount || 0})`;
  });

  return header + tagList.join("\n");
}

// 创建 MCP Server
const server = new Server(
  {
    name: "image-mcp-server",
    version: "1.0.0",
  },
  {
    capabilities: {
      tools: {},
    },
  }
);

// 定义可用工具
const tools = [
  {
    name: "search_images",
    description:
      "搜索图片。可以根据关键词搜索图片标题、描述或文件名。返回匹配的图片列表。",
    inputSchema: {
      type: "object",
      properties: {
        keyword: {
          type: "string",
          description: "搜索关键词，可以是图片标题、描述或文件名的一部分",
        },
        page: {
          type: "number",
          description: "页码，从 1 开始，默认为 1",
          default: 1,
        },
        pageSize: {
          type: "number",
          description: "每页数量，默认为 10，最大 50",
          default: 10,
        },
      },
      required: [],
    },
  },
  {
    name: "list_images",
    description: "获取图片列表。返回系统中的所有图片，按上传时间倒序排列。",
    inputSchema: {
      type: "object",
      properties: {
        page: {
          type: "number",
          description: "页码，从 1 开始，默认为 1",
          default: 1,
        },
        pageSize: {
          type: "number",
          description: "每页数量，默认为 10，最大 50",
          default: 10,
        },
      },
      required: [],
    },
  },
  {
    name: "get_image_detail",
    description:
      "获取指定图片的详细信息，包括标题、描述、尺寸、EXIF 信息、标签等。",
    inputSchema: {
      type: "object",
      properties: {
        imageId: {
          type: "number",
          description: "图片 ID",
        },
      },
      required: ["imageId"],
    },
  },
  {
    name: "list_tags",
    description: "获取所有可用的图片标签列表。可用于了解系统中有哪些分类。",
    inputSchema: {
      type: "object",
      properties: {},
      required: [],
    },
  },
  {
    name: "search_images_by_tag",
    description: "根据标签 ID 搜索图片。先使用 list_tags 获取标签列表，然后用标签 ID 搜索。",
    inputSchema: {
      type: "object",
      properties: {
        tagId: {
          type: "number",
          description: "标签 ID",
        },
        page: {
          type: "number",
          description: "页码，从 1 开始，默认为 1",
          default: 1,
        },
        pageSize: {
          type: "number",
          description: "每页数量，默认为 10，最大 50",
          default: 10,
        },
      },
      required: ["tagId"],
    },
  },
  {
    name: "search_tags",
    description: "搜索标签。根据关键词搜索标签名称。",
    inputSchema: {
      type: "object",
      properties: {
        keyword: {
          type: "string",
          description: "标签名称关键词",
        },
      },
      required: [],
    },
  },
  {
    name: "get_stats",
    description: "获取系统统计信息，包括图片总数和标签总数。",
    inputSchema: {
      type: "object",
      properties: {},
      required: [],
    },
  },
];

// 处理列出工具请求
server.setRequestHandler(ListToolsRequestSchema, async () => {
  return { tools };
});

// 处理工具调用请求
server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;

  try {
    switch (name) {
      case "search_images": {
        const keyword = args?.keyword || "";
        const page = Math.max(1, args?.page || 1);
        const pageSize = Math.min(50, Math.max(1, args?.pageSize || 10));

        const result = await searchImages(keyword, page, pageSize);

        if (result.code !== 200) {
          return {
            content: [
              {
                type: "text",
                text: `搜索失败: ${result.message || "未知错误"}`,
              },
            ],
          };
        }

        const { records, total } = result.data;
        const formattedResult = formatImageList(records, total, page, pageSize);

        return {
          content: [
            {
              type: "text",
              text: formattedResult,
            },
          ],
        };
      }

      case "list_images": {
        const page = Math.max(1, args?.page || 1);
        const pageSize = Math.min(50, Math.max(1, args?.pageSize || 10));

        const result = await listImages(page, pageSize);

        if (result.code !== 200) {
          return {
            content: [
              {
                type: "text",
                text: `获取图片列表失败: ${result.message || "未知错误"}`,
              },
            ],
          };
        }

        const { records, total } = result.data;
        const formattedResult = formatImageList(records, total, page, pageSize);

        return {
          content: [
            {
              type: "text",
              text: formattedResult,
            },
          ],
        };
      }

      case "get_image_detail": {
        const imageId = args?.imageId;
        if (!imageId) {
          return {
            content: [
              {
                type: "text",
                text: "错误: 请提供图片 ID",
              },
            ],
          };
        }

        const result = await getImageDetail(imageId);

        if (result.code !== 200) {
          return {
            content: [
              {
                type: "text",
                text: `获取图片详情失败: ${result.message || "未知错误"}`,
              },
            ],
          };
        }

        const formattedResult = formatImageInfo(result.data);

        return {
          content: [
            {
              type: "text",
              text: formattedResult,
            },
          ],
        };
      }

      case "list_tags": {
        const result = await listTags();

        if (result.code !== 200) {
          return {
            content: [
              {
                type: "text",
                text: `获取标签列表失败: ${result.message || "未知错误"}`,
              },
            ],
          };
        }

        const formattedResult = formatTagList(result.data);

        return {
          content: [
            {
              type: "text",
              text: formattedResult,
            },
          ],
        };
      }

      case "search_images_by_tag": {
        const tagId = args?.tagId;
        if (!tagId) {
          return {
            content: [
              {
                type: "text",
                text: "错误: 请提供标签 ID",
              },
            ],
          };
        }

        const page = Math.max(1, args?.page || 1);
        const pageSize = Math.min(50, Math.max(1, args?.pageSize || 10));

        const result = await searchImagesByTag(tagId, page, pageSize);

        if (result.code !== 200) {
          return {
            content: [
              {
                type: "text",
                text: `根据标签搜索失败: ${result.message || "未知错误"}`,
              },
            ],
          };
        }

        const { records, total } = result.data;
        const formattedResult = formatImageList(records, total, page, pageSize);

        return {
          content: [
            {
              type: "text",
              text: formattedResult,
            },
          ],
        };
      }

      case "search_tags": {
        const keyword = args?.keyword || "";
        const result = await searchTags(keyword);

        if (result.code !== 200) {
          return {
            content: [
              {
                type: "text",
                text: `搜索标签失败: ${result.message || "未知错误"}`,
              },
            ],
          };
        }

        const formattedResult = formatTagList(result.data);

        return {
          content: [
            {
              type: "text",
              text: formattedResult,
            },
          ],
        };
      }

      case "get_stats": {
        const result = await getStats();

        if (result.code !== 200) {
          return {
            content: [
              {
                type: "text",
                text: `获取统计失败: ${result.message || "未知错误"}`,
              },
            ],
          };
        }

        const stats = result.data;
        const formattedResult = `📊 **系统统计**\n- 图片总数: ${stats.totalImages}\n- 标签总数: ${stats.totalTags}`;

        return {
          content: [
            {
              type: "text",
              text: formattedResult,
            },
          ],
        };
      }

      default:
        return {
          content: [
            {
              type: "text",
              text: `未知工具: ${name}`,
            },
          ],
        };
    }
  } catch (error) {
    return {
      content: [
        {
          type: "text",
          text: `执行错误: ${error.message}`,
        },
      ],
    };
  }
});

// 启动服务器
async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error("Image MCP Server started");
}

main().catch((error) => {
  console.error("Server error:", error);
  process.exit(1);
});
