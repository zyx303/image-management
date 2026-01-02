package com.zyx.image.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * API Key 实体类
 * 用于 MCP 接口认证
 */
@Data
@TableName("api_key")
public class ApiKey {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String apiKey;
    
    private String name;
    private Integer enable; // 1-启用, 0-禁用
    
    private LocalDateTime lastUsedTime;
    
    private LocalDateTime createTime;
    
    private LocalDateTime expireTime;
}
