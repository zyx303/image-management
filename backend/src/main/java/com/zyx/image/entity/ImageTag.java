package com.zyx.image.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 图片标签关联实体类
 */
@Data
@TableName("image_tag")
public class ImageTag {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long imageId;
    
    private Long tagId;
    
    private BigDecimal confidence; // AI识别置信度(0-100)
    
    private LocalDateTime createTime;
}

