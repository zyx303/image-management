package com.zyx.image.dto;

import lombok.Data;

/**
 * AI配置DTO
 */
@Data
public class AiConfigDTO {
    /**
     * 百度智能云 API Key
     */
    private String apiKey;
    
    /**
     * 百度智能云 Secret Key
     */
    private String secretKey;
}
