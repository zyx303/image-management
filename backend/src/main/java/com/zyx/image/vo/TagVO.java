package com.zyx.image.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签VO
 */
@Data
public class TagVO {
    private Long id;
    private String tagName;
    private Integer tagType;
    private Integer useCount;
    private LocalDateTime createTime;
}

