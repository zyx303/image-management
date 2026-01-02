package com.zyx.image.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zyx.image.entity.ApiKey;
import com.zyx.image.mapper.ApiKeyMapper;
import com.zyx.image.vo.ApiKeyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API Key 服务
 */
@Service
@RequiredArgsConstructor
public class ApiKeyService {
    
    private final ApiKeyMapper apiKeyMapper;
    
    /**
     * 生成 API Key
     */
    private String generateApiKey() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return "img_" + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
    
    /**
     * 创建 API Key
     */
    @Transactional
    public ApiKeyVO createApiKey(Long userId, String name) {
        ApiKey apiKey = new ApiKey();
        apiKey.setUserId(userId);
        apiKey.setApiKey(generateApiKey());
        apiKey.setName(name != null ? name : "默认 API Key");
        apiKey.setStatus(1);
        apiKey.setCreateTime(LocalDateTime.now());
        // 默认一年有效期
        apiKey.setExpireTime(LocalDateTime.now().plusYears(1));
        
        apiKeyMapper.insert(apiKey);
        
        return convertToVO(apiKey, true);
    }
    
    /**
     * 获取用户的所有 API Key
     */
    public List<ApiKeyVO> getUserApiKeys(Long userId) {
        LambdaQueryWrapper<ApiKey> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiKey::getUserId, userId)
               .orderByDesc(ApiKey::getCreateTime);
        
        List<ApiKey> apiKeys = apiKeyMapper.selectList(wrapper);
        return apiKeys.stream()
                .map(key -> convertToVO(key, false))
                .collect(Collectors.toList());
    }
    
    /**
     * 验证 API Key 并返回用户ID
     */
    public Long validateApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return null;
        }
        
        LambdaQueryWrapper<ApiKey> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiKey::getApiKey, apiKey)
               .eq(ApiKey::getStatus, 1);
        
        ApiKey key = apiKeyMapper.selectOne(wrapper);
        
        if (key == null) {
            return null;
        }
        
        // 检查是否过期
        if (key.getExpireTime() != null && key.getExpireTime().isBefore(LocalDateTime.now())) {
            return null;
        }
        
        // 更新最后使用时间
        key.setLastUsedTime(LocalDateTime.now());
        apiKeyMapper.updateById(key);
        
        return key.getUserId();
    }
    
    /**
     * 删除 API Key
     */
    @Transactional
    public void deleteApiKey(Long id, Long userId) {
        LambdaQueryWrapper<ApiKey> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiKey::getId, id)
               .eq(ApiKey::getUserId, userId);
        
        ApiKey apiKey = apiKeyMapper.selectOne(wrapper);
        if (apiKey == null) {
            throw new RuntimeException("API Key 不存在");
        }
        
        apiKeyMapper.deleteById(id);
    }
    
    /**
     * 禁用/启用 API Key
     */
    @Transactional
    public ApiKeyVO toggleApiKey(Long id, Long userId) {
        LambdaQueryWrapper<ApiKey> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiKey::getId, id)
               .eq(ApiKey::getUserId, userId);
        
        ApiKey apiKey = apiKeyMapper.selectOne(wrapper);
        if (apiKey == null) {
            throw new RuntimeException("API Key 不存在");
        }
        
        apiKey.setStatus(apiKey.getStatus() == 1 ? 0 : 1);
        apiKeyMapper.updateById(apiKey);
        
        return convertToVO(apiKey, false);
    }
    
    /**
     * 重新生成 API Key
     */
    @Transactional
    public ApiKeyVO regenerateApiKey(Long id, Long userId) {
        LambdaQueryWrapper<ApiKey> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiKey::getId, id)
               .eq(ApiKey::getUserId, userId);
        
        ApiKey apiKey = apiKeyMapper.selectOne(wrapper);
        if (apiKey == null) {
            throw new RuntimeException("API Key 不存在");
        }
        
        apiKey.setApiKey(generateApiKey());
        apiKey.setExpireTime(LocalDateTime.now().plusYears(1));
        apiKeyMapper.updateById(apiKey);
        
        return convertToVO(apiKey, true);
    }
    
    /**
     * 转换为 VO
     */
    private ApiKeyVO convertToVO(ApiKey apiKey, boolean showFullKey) {
        ApiKeyVO vo = new ApiKeyVO();
        BeanUtils.copyProperties(apiKey, vo);
        
        // 如果不显示完整 key，只显示前后几位
        if (!showFullKey && apiKey.getApiKey() != null) {
            String key = apiKey.getApiKey();
            if (key.length() > 12) {
                vo.setApiKey(key.substring(0, 8) + "..." + key.substring(key.length() - 4));
            }
        }
        
        return vo;
    }
}
