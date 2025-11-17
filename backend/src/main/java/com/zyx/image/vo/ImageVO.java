package com.zyx.image.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 图片VO
 */
@Data
public class ImageVO {
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
    private List<TagVO> tags;
}

