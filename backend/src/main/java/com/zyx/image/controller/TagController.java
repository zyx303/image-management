package com.zyx.image.controller;

import com.zyx.image.service.TagService;
import com.zyx.image.util.JwtUtil;
import com.zyx.image.vo.ImageVO;
import com.zyx.image.vo.PageResult;
import com.zyx.image.vo.Result;
import com.zyx.image.vo.TagVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 标签控制器
 */
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {
    
    private final TagService tagService;
    private final JwtUtil jwtUtil;
    
    /**
     * 获取所有标签
     */
    @GetMapping
    public Result<List<TagVO>> getAllTags() {
        try {
            List<TagVO> tags = tagService.getAllTags();
            return Result.success(tags);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户标签
     */
    @GetMapping("/user")
    public Result<List<TagVO>> getUserTags(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            List<TagVO> tags = tagService.getUserTags(userId);
            return Result.success(tags);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 创建标签
     */
    @PostMapping
    public Result<TagVO> createTag(@RequestBody Map<String, String> data) {
        try {
            String tagName = data.get("tagName");
            if (tagName == null || tagName.isEmpty()) {
                return Result.error("标签名称不能为空");
            }
            TagVO tagVO = tagService.createTag(tagName);
            return Result.success("创建成功", tagVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新标签
     */
    @PutMapping("/{id}")
    public Result<TagVO> updateTag(@PathVariable Long id, @RequestBody Map<String, String> data) {
        try {
            String tagName = data.get("tagName");
            if (tagName == null || tagName.isEmpty()) {
                return Result.error("标签名称不能为空");
            }
            TagVO tagVO = tagService.updateTag(id, tagName);
            return Result.success("更新成功", tagVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除标签
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        try {
            tagService.deleteTag(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据标签获取图片
     */
    @GetMapping("/{tagId}/images")
    public Result<PageResult<ImageVO>> getImagesByTag(
            @PathVariable Long tagId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size) {
        try {
            PageResult<ImageVO> result = tagService.getImagesByTag(tagId, current, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 搜索标签
     */
    @GetMapping("/search")
    public Result<List<TagVO>> searchTags(@RequestParam(required = false) String keyword) {
        try {
            List<TagVO> tags = tagService.searchTags(keyword);
            return Result.success(tags);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new RuntimeException("未找到Token");
    }
}

