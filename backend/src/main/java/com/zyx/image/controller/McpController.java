package com.zyx.image.controller;

import com.zyx.image.service.ApiKeyService;
import com.zyx.image.service.ImageService;
import com.zyx.image.service.TagService;
import com.zyx.image.vo.ImageVO;
import com.zyx.image.vo.PageResult;
import com.zyx.image.vo.Result;
import com.zyx.image.vo.TagVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * MCP (Model Context Protocol) 公开接口控制器
 * 提供给大模型检索图片的接口，通过 API Key 认证
 */
@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
public class McpController {
    
    private final ImageService imageService;
    private final TagService tagService;
    private final ApiKeyService apiKeyService;
    
    /**
     * 从请求中获取用户ID（通过 API Key）
     */
    private Long getUserIdFromApiKey(HttpServletRequest request) {
        // 从 Header 获取 API Key
        String apiKey = request.getHeader("X-API-Key");
        
        // 如果 Header 中没有，从查询参数获取
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = request.getParameter("api_key");
        }
        
        if (apiKey == null || apiKey.isEmpty()) {
            return null;
        }
        
        return apiKeyService.validateApiKey(apiKey);
    }
    
    /**
     * 搜索图片（需要 API Key 认证）
     * 根据关键词搜索图片标题、描述和文件名
     */
    @GetMapping("/images/search")
    public Result<PageResult<ImageVO>> searchImages(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromApiKey(request);
            if (userId == null) {
                return Result.error(401, "无效的 API Key");
            }
            // 搜索限制为当前用户的图片
            PageResult<ImageVO> result = imageService.searchImages(keyword, current, size, userId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取图片列表（需要 API Key 认证）
     */
    @GetMapping("/images")
    public Result<PageResult<ImageVO>> getImages(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromApiKey(request);
            if (userId == null) {
                return Result.error(401, "无效的 API Key");
            }
            // 获取限制为当前用户的图片
            PageResult<ImageVO> result = imageService.getImageList(current, size, userId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取图片详情（需要 API Key 认证）
     */
    @GetMapping("/images/{id}")
    public Result<ImageVO> getImageDetail(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromApiKey(request);
            if (userId == null) {
                return Result.error(401, "无效的 API Key");
            }
            ImageVO imageVO = imageService.getImageDetail(id);
            // 验证图片归属
            if (!imageVO.getUserId().equals(userId)) {
                return Result.error(403, "无权访问此图片");
            }
            return Result.success(imageVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取所有标签（需要 API Key 认证）
     */
    @GetMapping("/tags")
    public Result<List<TagVO>> getAllTags(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromApiKey(request);
            if (userId == null) {
                return Result.error(401, "无效的 API Key");
            }
            List<TagVO> tags = tagService.getAllTags();
            return Result.success(tags);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据标签获取图片（需要 API Key 认证）
     */
    @GetMapping("/tags/{tagId}/images")
    public Result<PageResult<ImageVO>> getImagesByTag(
            @PathVariable Long tagId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromApiKey(request);
            if (userId == null) {
                return Result.error(401, "无效的 API Key");
            }
            // 获取用户在该标签下的图片
            PageResult<ImageVO> result = tagService.getImagesByTagAndUser(tagId, userId, current, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 搜索标签（需要 API Key 认证）
     */
    @GetMapping("/tags/search")
    public Result<List<TagVO>> searchTags(@RequestParam(required = false) String keyword,
                                          HttpServletRequest request) {
        try {
            Long userId = getUserIdFromApiKey(request);
            if (userId == null) {
                return Result.error(401, "无效的 API Key");
            }
            List<TagVO> tags = tagService.searchTags(keyword);
            return Result.success(tags);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取系统统计信息（需要 API Key 认证）
     */
    @GetMapping("/stats")
    public Result<McpStats> getStats(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromApiKey(request);
            if (userId == null) {
                return Result.error(401, "无效的 API Key");
            }
            PageResult<ImageVO> images = imageService.getImageList(1L, 1L, userId);
            List<TagVO> tags = tagService.getAllTags();
            
            McpStats stats = new McpStats();
            stats.setTotalImages(images.getTotal());
            stats.setTotalTags((long) tags.size());
            
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * MCP 统计信息
     */
    @lombok.Data
    public static class McpStats {
        private Long totalImages;
        private Long totalTags;
    }
}
