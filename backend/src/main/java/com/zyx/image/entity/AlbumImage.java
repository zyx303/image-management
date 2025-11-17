package com.zyx.image.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 相册图片关联实体类
 */
@Data
@TableName("album_image")
public class AlbumImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long albumId;
    
    private Long imageId;
    
    private Integer sortOrder;
    
    private LocalDateTime createTime;
}

