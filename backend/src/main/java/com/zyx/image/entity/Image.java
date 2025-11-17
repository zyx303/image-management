package com.zyx.image.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图片实体类
 */
@Data
@TableName("image")
public class Image {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String title;
    
    private String description;
    
    private String fileName;
    
    private String filePath;
    
    private Long fileSize;
    
    private String fileType;
    
    private Integer width;
    
    private Integer height;
    
    private String thumbnailPath;
    
    private String md5;
    
    private LocalDateTime uploadTime;
    
    private LocalDateTime shootTime;
    
    private String location;
    
    private String device;
    
    private String cameraModel;
    
    private String focalLength;
    
    private String aperture;
    
    private String iso;
    
    private Integer viewCount;
    
    private Integer status; // 1-正常, 0-删除
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}

