package com.zyx.image.controller;

import com.zyx.image.service.ApiKeyService;
import com.zyx.image.util.JwtUtil;
import com.zyx.image.vo.ApiKeyVO;
import com.zyx.image.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API Key 控制器
 */
@RestController
@RequestMapping("/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {
    
    private final ApiKeyService apiKeyService;
    private final JwtUtil jwtUtil;
    
    /**
     * 创建 API Key
     */
    @PostMapping
    public Result<ApiKeyVO> createApiKey(@RequestBody(required = false) Map<String, String> data,
                                         HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            String name = data != null ? data.get("name") : null;
            ApiKeyVO apiKey = apiKeyService.createApiKey(userId, name);
            return Result.success("创建成功", apiKey);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户的所有 API Key
     */
    @GetMapping
    public Result<List<ApiKeyVO>> getUserApiKeys(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            List<ApiKeyVO> apiKeys = apiKeyService.getUserApiKeys(userId);
            return Result.success(apiKeys);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除 API Key
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteApiKey(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            apiKeyService.deleteApiKey(id, userId);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 禁用/启用 API Key
     */
    @PutMapping("/{id}/toggle")
    public Result<ApiKeyVO> toggleApiKey(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            ApiKeyVO apiKey = apiKeyService.toggleApiKey(id, userId);
            return Result.success("操作成功", apiKey);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 重新生成 API Key
     */
    @PutMapping("/{id}/regenerate")
    public Result<ApiKeyVO> regenerateApiKey(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            ApiKeyVO apiKey = apiKeyService.regenerateApiKey(id, userId);
            return Result.success("重新生成成功", apiKey);
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
