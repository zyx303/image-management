package com.zyx.image.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * API Key VO
 */
@Data
public class ApiKeyVO {
    private Long id;
    private Long userId;
    private String apiKey;
    private String name;
    private Integer enable; // 1-启用, 0-禁用
    private LocalDateTime lastUsedTime;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
}
