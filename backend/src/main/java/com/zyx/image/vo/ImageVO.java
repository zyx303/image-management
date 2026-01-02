package com.zyx.image.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    /**
     * 获取EXIF信息对象（前端需要的格式）
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Map<String, String> getExifInfo() {
        Map<String, String> exif = new HashMap<>();
        
        // 拍摄时间
        if (shootTime != null) {
            exif.put("DateTime", shootTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        
        // 设备信息
        if (device != null && !device.isEmpty()) {
            exif.put("Make", device);
        }
        
        // 相机型号
        if (cameraModel != null && !cameraModel.isEmpty()) {
            exif.put("Model", cameraModel);
        }
        
        // 焦距
        if (focalLength != null && !focalLength.isEmpty()) {
            exif.put("FocalLength", focalLength);
        }
        
        // 光圈
        if (aperture != null && !aperture.isEmpty()) {
            exif.put("Aperture", aperture);
        }
        
        // ISO
        if (iso != null && !iso.isEmpty()) {
            exif.put("ISO", iso);
        }
        
        // 位置信息（如果有的话，需要解析）
        if (location != null && !location.isEmpty()) {
            // 假设location格式为 "lat,lon"
            String[] parts = location.split(",");
            if (parts.length == 2) {
                exif.put("GPSLatitude", parts[0].trim());
                exif.put("GPSLongitude", parts[1].trim());
            }
        }
        
        return exif.isEmpty() ? null : exif;
    }
}

