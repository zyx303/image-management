package com.zyx.image.controller;

import com.zyx.image.entity.Image;
import com.zyx.image.entity.ImageTag;
import com.zyx.image.entity.Tag;
import com.zyx.image.mapper.ImageTagMapper;
import com.zyx.image.mapper.TagMapper;
import com.zyx.image.service.BaiduAiService;
import com.zyx.image.service.ImageService;
import com.zyx.image.util.FileUtil;
import com.zyx.image.vo.AiTagVO;
import com.zyx.image.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * AI标签识别控制器
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiTagController {
    
    private final BaiduAiService baiduAiService;
    private final ImageService imageService;
    private final TagMapper tagMapper;
    private final ImageTagMapper imageTagMapper;
    private final FileUtil fileUtil;
    
    /**
     * 检查AI服务是否可用
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> checkStatus() {
        boolean available = baiduAiService.isServiceAvailable();
        return Result.success(Map.of(
                "available", available,
                "message", available ? "AI服务已配置" : "AI服务未配置，请配置百度智能云API Key和Secret Key"
        ));
    }
    
    /**
     * 分析上传的图片
     * @param file 图片文件
     * @return AI识别的标签列表
     */
    @PostMapping("/analyze")
    public Result<List<AiTagVO>> analyzeUploadedImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("请上传图片文件");
            }
            
            // 将图片转换为Base64
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
            
            // 调用AI分析
            List<AiTagVO> tags = baiduAiService.analyzeImageByBase64(base64Image);
            
            return Result.success("分析成功", tags);
        } catch (IOException e) {
            log.error("读取图片文件失败", e);
            return Result.error("读取图片文件失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("AI分析失败", e);
            return Result.error("AI分析失败: " + e.getMessage());
        }
    }
    
    /**
     * 分析已存在的图片
     * @param imageId 图片ID
     * @return AI识别的标签列表
     */
    @GetMapping("/analyze/{imageId}")
    public Result<List<AiTagVO>> analyzeExistingImage(@PathVariable Long imageId) {
        try {
            // 获取图片信息
            Image image = imageService.getImageById(imageId);
            if (image == null) {
                return Result.error("图片不存在");
            }
            
            // 获取图片完整路径
            String fullPath = fileUtil.getFullPath(image.getFilePath());
            
            // 调用AI分析
            List<AiTagVO> tags = baiduAiService.analyzeImage(fullPath);
            
            return Result.success("分析成功", tags);
        } catch (Exception e) {
            log.error("AI分析失败", e);
            return Result.error("AI分析失败: " + e.getMessage());
        }
    }
    
    /**
     * 分析图片并自动添加标签
     * @param imageId 图片ID
     * @param minScore 最低置信度阈值（0-1），默认0.5
     * @return AI识别的标签列表及保存状态
     */
    @PostMapping("/analyze-and-tag/{imageId}")
    public Result<Map<String, Object>> analyzeAndAddTags(
            @PathVariable Long imageId,
            @RequestParam(defaultValue = "0.5") Double minScore,
            HttpServletRequest request) {
        try {
            // 获取图片信息
            Image image = imageService.getImageById(imageId);
            if (image == null) {
                return Result.error("图片不存在");
            }
            
            // 获取图片完整路径
            String fullPath = fileUtil.getFullPath(image.getFilePath());
            
            // 调用AI分析
            List<AiTagVO> allTags = baiduAiService.analyzeImage(fullPath);
            
            // 过滤低置信度标签
            List<AiTagVO> filteredTags = allTags.stream()
                    .filter(tag -> tag.getScore() >= minScore)
                    .toList();
            
            // 保存标签到数据库（从category提取大类）
            int addedCount = 0;
            Set<String> addedTagNames = new HashSet<>(); // 用于去重
            
            for (AiTagVO aiTag : filteredTags) {
                // 从category中提取大类（破折号前的部分）
                if (aiTag.getCategory() == null || aiTag.getCategory().isEmpty()) {
                    continue;
                }
                
                String tagName = aiTag.getCategory().split("-")[0].trim();
                if (tagName.isEmpty() || addedTagNames.contains(tagName)) {
                    continue;
                }
                
                addedTagNames.add(tagName);
                
                // 查找或创建标签
                Tag tag = tagMapper.selectByName(tagName);
                if (tag == null) {
                    tag = new Tag();
                    tag.setTagName(tagName);
                    tag.setTagType(3); // 3-AI标签
                    tag.setUseCount(0);
                    tagMapper.insert(tag);
                }
                
                // 检查是否已关联
                ImageTag existing = imageTagMapper.selectByImageIdAndTagId(imageId, tag.getId());
                if (existing == null) {
                    // 添加关联
                    ImageTag imageTag = new ImageTag();
                    imageTag.setImageId(imageId);
                    imageTag.setTagId(tag.getId());
                    imageTagMapper.insert(imageTag);
                    
                    // 更新使用次数
                    tag.setUseCount(tag.getUseCount() + 1);
                    tagMapper.updateById(tag);
                    
                    addedCount++;
                }
            }
            
            return Result.success("分析并添加标签成功", Map.of(
                    "allTags", allTags,
                    "filteredTags", filteredTags,
                    "addedCount", addedCount,
                    "minScore", minScore
            ));
        } catch (Exception e) {
            log.error("AI分析并添加标签失败", e);
            return Result.error("AI分析失败: " + e.getMessage());
        }
    }
    
    /**
     * 通过URL分析图片
     * @param imageUrl 图片URL
     * @return AI识别的标签列表
     */
    @PostMapping("/analyze-url")
    public Result<List<AiTagVO>> analyzeImageByUrl(@RequestBody Map<String, String> request) {
        try {
            String imageUrl = request.get("url");
            if (imageUrl == null || imageUrl.isEmpty()) {
                return Result.error("请提供图片URL");
            }
            
            List<AiTagVO> tags = baiduAiService.analyzeImageByUrl(imageUrl);
            
            return Result.success("分析成功", tags);
        } catch (Exception e) {
            log.error("AI分析失败", e);
            return Result.error("AI分析失败: " + e.getMessage());
        }
    }
}
