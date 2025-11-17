package com.zyx.image.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 相册实体类
 */
@Data
@TableName("album")
public class Album {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String albumName;
    
    private String description;
    
    private Long coverImageId;
    
    private Integer imageCount;
    
    private Integer isPublic; // 1-公开, 0-私密
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}

