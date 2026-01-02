package com.zyx.image.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String password;
    
    private String email;
    
    private String nickname;
    
    private String avatar;
    
    private String phone;
    
    private Integer status; // 1-正常, 0-禁用
    
    /**
     * 百度智能云 API Key
     */
    private String baiduApiKey;
    
    /**
     * 百度智能云 Secret Key
     */
    private String baiduSecretKey;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}

