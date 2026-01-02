package com.zyx.image.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI识别标签VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiTagVO {
    
    /**
     * 标签名称（物体或场景名称）
     */
    private String keyword;
    
    /**
     * 置信度（0-1）
     */
    private Double score;
    
    /**
     * 分类（上层标签，如"公众人物"、"风景"、"动物"等）
     */
    private String category;
}
