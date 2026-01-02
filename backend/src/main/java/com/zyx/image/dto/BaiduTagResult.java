package com.zyx.image.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 百度AI图像识别返回结果
 */
@Data
public class BaiduTagResult {
    
    /**
     * 唯一的log id，用于问题定位
     */
    @JsonProperty("log_id")
    private Long logId;
    
    /**
     * 返回结果数目
     */
    @JsonProperty("result_num")
    private Integer resultNum;
    
    /**
     * 标签结果数组
     */
    private List<TagItem> result;
    
    /**
     * 错误码
     */
    @JsonProperty("error_code")
    private Integer errorCode;
    
    /**
     * 错误消息
     */
    @JsonProperty("error_msg")
    private String errorMsg;
    
    /**
     * 标签项
     */
    @Data
    public static class TagItem {
        /**
         * 图片中的物体或场景名称
         */
        private String keyword;
        
        /**
         * 置信度，0-1
         */
        private Double score;
        
        /**
         * 识别结果的上层标签
         */
        private String root;
    }
}
