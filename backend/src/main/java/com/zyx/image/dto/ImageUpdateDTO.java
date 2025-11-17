package com.zyx.image.dto;

import lombok.Data;

import java.util.List;

/**
 * 图片更新DTO
 */
@Data
public class ImageUpdateDTO {
    private String title;
    
    private String description;
    
    private List<Long> tagIds;
}

