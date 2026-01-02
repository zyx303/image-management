package com.zyx.image.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zyx.image.config.BaiduAiConfig;
import com.zyx.image.dto.BaiduTagResult;
import com.zyx.image.entity.User;
import com.zyx.image.mapper.UserMapper;
import com.zyx.image.vo.AiTagVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 百度AI图像识别服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BaiduAiService {
    
    private final BaiduAiConfig baiduAiConfig;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;
    
    private static final String ACCESS_TOKEN_KEY_PREFIX = "baidu:ai:access_token:user:";
    private static final long TOKEN_EXPIRE_SECONDS = 29 * 24 * 60 * 60; // 29天
    
    /**
     * 获取用户的百度AI配置
     */
    private String[] getUserAiConfig(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userMapper.selectById(userId);
        if (user == null || user.getBaiduApiKey() == null || user.getBaiduSecretKey() == null 
                || user.getBaiduApiKey().isEmpty() || user.getBaiduSecretKey().isEmpty()) {
            return null;
        }
        return new String[]{user.getBaiduApiKey(), user.getBaiduSecretKey()};
    }
    
    /**
     * 获取Access Token
     * 优先从Redis缓存获取，缓存不存在则请求新的token
     */
    public String getAccessToken(Long userId) {
        String[] config = getUserAiConfig(userId);
        if (config == null) {
            throw new RuntimeException("请先配置百度智能云 API Key 和 Secret Key");
        }
        
        String cacheKey = ACCESS_TOKEN_KEY_PREFIX + userId;
        // 从Redis获取缓存的token
        String cachedToken = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedToken != null && !cachedToken.isEmpty()) {
            log.debug("从缓存获取百度AI Access Token，用户ID: {}", userId);
            return cachedToken;
        }
        
        // 请求新的token
        log.info("请求新的百度AI Access Token，用户ID: {}", userId);
        return refreshAccessToken(userId, config[0], config[1]);
    }
    
    /**
     * 刷新Access Token
     */
    public String refreshAccessToken(Long userId, String apiKey, String secretKey) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            String url = String.format("%s?grant_type=client_credentials&client_id=%s&client_secret=%s",
                    baiduAiConfig.getTokenUrl(),
                    apiKey,
                    secretKey);
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                
                if (jsonNode.has("error")) {
                    String error = jsonNode.get("error").asText();
                    String errorDescription = jsonNode.has("error_description") 
                            ? jsonNode.get("error_description").asText() 
                            : "未知错误";
                    log.error("获取百度AI Access Token失败: {} - {}", error, errorDescription);
                    throw new RuntimeException("获取Access Token失败: " + errorDescription);
                }
                
                String accessToken = jsonNode.get("access_token").asText();
                long expiresIn = jsonNode.get("expires_in").asLong();
                
                // 缓存到Redis，设置过期时间比实际过期时间短一点
                String cacheKey = ACCESS_TOKEN_KEY_PREFIX + userId;
                long cacheExpire = Math.min(expiresIn - 3600, TOKEN_EXPIRE_SECONDS);
                stringRedisTemplate.opsForValue().set(cacheKey, accessToken, cacheExpire, TimeUnit.SECONDS);
                
                log.info("成功获取百度AI Access Token，用户ID: {}，有效期: {}秒", userId, expiresIn);
                return accessToken;
            }
            
            throw new RuntimeException("获取Access Token失败: 响应异常");
        } catch (Exception e) {
            log.error("获取百度AI Access Token异常", e);
            throw new RuntimeException("获取Access Token失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试百度AI配置是否正确
     */
    public boolean testConfig(String apiKey, String secretKey) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            String url = String.format("%s?grant_type=client_credentials&client_id=%s&client_secret=%s",
                    baiduAiConfig.getTokenUrl(),
                    apiKey,
                    secretKey);
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                return !jsonNode.has("error") && jsonNode.has("access_token");
            }
            return false;
        } catch (Exception e) {
            log.error("测试百度AI配置异常", e);
            return false;
        }
    }
    
    /**
     * 通用物体和场景识别
     * @param imagePath 图片文件路径
     * @param userId 用户ID
     * @return 识别结果列表
     */
    public List<AiTagVO> analyzeImage(String imagePath, Long userId) throws IOException {
        log.info("开始AI分析图片: {}, 用户ID: {}", imagePath, userId);
        
        // 读取图片文件并转换为Base64
        Path path = Paths.get(imagePath);
        if (!Files.exists(path)) {
            log.error("图片文件不存在: {}", imagePath);
            throw new IOException("图片文件不存在: " + imagePath);
        }
        
        byte[] imageBytes = Files.readAllBytes(path);
        long fileSize = imageBytes.length;
        log.info("读取图片文件成功，大小: {} bytes ({} KB)", fileSize, fileSize / 1024);
        
        // 检查文件大小（不超过8M）
        if (fileSize > 8 * 1024 * 1024) {
            throw new IOException("图片文件过大，请使用小于8MB的图片");
        }
        
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        log.debug("Base64编码完成，长度: {}", base64Image.length());
        
        return analyzeImageByBase64(base64Image, userId);
    }
    
    /**
     * 通用物体和场景识别（Base64图片）
     * @param base64Image Base64编码的图片数据（可以包含或不包含data:image/xxx;base64,前缀）
     * @param userId 用户ID
     * @return 识别结果列表
     */
    public List<AiTagVO> analyzeImageByBase64(String base64Image, Long userId) {
        log.info("开始AI分析Base64图片，用户ID: {}", userId);
        
        try {
            // 移除可能存在的data URI前缀
            String cleanBase64 = base64Image;
            if (base64Image.contains(",")) {
                // 如果包含逗号，说明可能是 data:image/xxx;base64,xxxxx 格式
                String[] parts = base64Image.split(",", 2);
                if (parts.length == 2 && parts[0].startsWith("data:")) {
                    cleanBase64 = parts[1];
                    log.debug("移除data URI前缀: {}", parts[0]);
                }
            }
            
            // 检查Base64大小（百度要求base64编码后不超过8M）
            int base64Length = cleanBase64.length();
            // Base64编码后的大小约为原文件的4/3倍，这里估算原始大小
            long estimatedSize = (long) (base64Length * 0.75);
            log.info("Base64长度: {}, 估算原始大小: {} bytes", base64Length, estimatedSize);
            
            if (base64Length > 11 * 1024 * 1024) { // 约8M的Base64
                throw new RuntimeException("图片太大，请使用小于6MB的图片");
            }
            
            String accessToken = getAccessToken(userId);
            
            RestTemplate restTemplate = new RestTemplate();
            
            // 构建请求URL
            String url = baiduAiConfig.getAdvancedGeneralUrl() + "?access_token=" + accessToken;
            
            // 按照官方示例：先对Base64进行URLEncode，然后构建参数字符串
            String imgParam = URLEncoder.encode(cleanBase64, StandardCharsets.UTF_8.toString());
            String param = "image=" + imgParam;
            
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // 使用字符串作为请求体（而不是MultiValueMap）
            HttpEntity<String> request = new HttpEntity<>(param, headers);
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.debug("百度AI响应: {}", response.getBody());
                
                BaiduTagResult result = objectMapper.readValue(response.getBody(), BaiduTagResult.class);
                
                // 检查错误
                if (result.getErrorCode() != null) {
                    log.error("百度AI识别失败 - 错误码: {}, 错误信息: {}, 完整响应: {}", 
                            result.getErrorCode(), result.getErrorMsg(), response.getBody());
                    throw new RuntimeException("AI识别失败 [" + result.getErrorCode() + "]: " + result.getErrorMsg());
                }
                
                // 转换结果
                List<AiTagVO> tagList = new ArrayList<>();
                if (result.getResult() != null) {
                    for (BaiduTagResult.TagItem item : result.getResult()) {
                        AiTagVO tag = AiTagVO.builder()
                                .keyword(item.getKeyword())
                                .score(item.getScore())
                                .category(item.getRoot())
                                .build();
                        
                        tagList.add(tag);
                    }
                }
                
                log.info("AI分析完成，识别出 {} 个标签", tagList.size());
                return tagList;
            }
            
            throw new RuntimeException("图像识别失败: 响应异常");
        } catch (Exception e) {
            log.error("百度AI图像识别异常", e);
            throw new RuntimeException("图像识别失败: " + e.getMessage());
        }
    }
    
    /**
     * 通用物体和场景识别（URL方式）
     * @param imageUrl 图片URL
     * @param userId 用户ID
     * @return 识别结果列表
     */
    public List<AiTagVO> analyzeImageByUrl(String imageUrl, Long userId) {
        log.info("开始AI分析图片URL: {}, 用户ID: {}", imageUrl, userId);
        
        try {
            String accessToken = getAccessToken(userId);
            
            RestTemplate restTemplate = new RestTemplate();
            
            // 构建请求URL
            String url = baiduAiConfig.getAdvancedGeneralUrl() + "?access_token=" + accessToken;
            
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // 构建请求体 - 按照官方示例格式
            String param = "url=" + URLEncoder.encode(imageUrl, StandardCharsets.UTF_8.toString());
            
            HttpEntity<String> request = new HttpEntity<>(param, headers);
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                BaiduTagResult result = objectMapper.readValue(response.getBody(), BaiduTagResult.class);
                
                // 检查错误
                if (result.getErrorCode() != null) {
                    log.error("百度AI识别失败 - 错误码: {}, 错误信息: {}, 完整响应: {}", 
                            result.getErrorCode(), result.getErrorMsg(), response.getBody());
                    throw new RuntimeException("AI识别失败 [" + result.getErrorCode() + "]: " + result.getErrorMsg());
                }
                
                // 转换结果
                List<AiTagVO> tagList = new ArrayList<>();
                if (result.getResult() != null) {
                    for (BaiduTagResult.TagItem item : result.getResult()) {
                        AiTagVO tag = AiTagVO.builder()
                                .keyword(item.getKeyword())
                                .score(item.getScore())
                                .category(item.getRoot())
                                .build();
                        
                        tagList.add(tag);
                    }
                }
                
                log.info("AI分析完成，识别出 {} 个标签", tagList.size());
                return tagList;
            }
            
            throw new RuntimeException("图像识别失败: 响应异常");
        } catch (Exception e) {
            log.error("百度AI图像识别异常", e);
            throw new RuntimeException("图像识别失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查服务是否可用（用户配置是否正确）
     */
    public boolean isServiceAvailable(Long userId) {
        String[] config = getUserAiConfig(userId);
        return config != null;
    }
}
