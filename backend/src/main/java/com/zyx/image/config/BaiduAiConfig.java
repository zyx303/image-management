package com.zyx.image.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 百度智能云AI配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "baidu.ai")
public class BaiduAiConfig {
    
    /**
     * API Key
     */
    private String apiKey;
    
    /**
     * Secret Key
     */
    private String secretKey;
    
    /**
     * Access Token URL
     */
    private String tokenUrl = "https://aip.baidubce.com/oauth/2.0/token";
    
    /**
     * 通用物体和场景识别 URL
     */
    private String advancedGeneralUrl = "https://aip.baidubce.com/rest/2.0/image-classify/v2/advanced_general";
}
