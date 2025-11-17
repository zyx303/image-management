package com.zyx.image.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签实体类
 */
@Data
@TableName("tag")
public class Tag {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String tagName;
    
    private Integer tagType; // 1-自动, 2-自定义, 3-AI
    
    private Integer useCount;
    
    private LocalDateTime createTime;
}

