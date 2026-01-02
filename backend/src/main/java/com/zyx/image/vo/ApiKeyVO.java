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
    private Integer status;
    private LocalDateTime lastUsedTime;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
}
