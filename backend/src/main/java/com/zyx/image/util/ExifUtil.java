package com.zyx.image.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * EXIF工具类
 */
@Component
public class ExifUtil {
    
    /**
     * 提取EXIF信息
     */
    public Map<String, Object> extractExif(MultipartFile file) {
        Map<String, Object> exifData = new HashMap<>();
        
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file.getInputStream());
            
            // 拍摄时间
            ExifSubIFDDirectory exifDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (exifDirectory != null && exifDirectory.containsTag(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)) {
                String dateTime = exifDirectory.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                if (dateTime != null) {
                    try {
                        LocalDateTime shootTime = LocalDateTime.parse(dateTime, 
                            DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss"));
                        exifData.put("shootTime", shootTime);
                    } catch (Exception e) {
                        // 忽略解析错误
                    }
                }
            }
            
            // 设备信息
            ExifIFD0Directory ifd0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (ifd0Directory != null) {
                if (ifd0Directory.containsTag(ExifIFD0Directory.TAG_MAKE)) {
                    exifData.put("device", ifd0Directory.getString(ExifIFD0Directory.TAG_MAKE));
                }
                if (ifd0Directory.containsTag(ExifIFD0Directory.TAG_MODEL)) {
                    exifData.put("cameraModel", ifd0Directory.getString(ExifIFD0Directory.TAG_MODEL));
                }
            }
            
            // 焦距、光圈、ISO
            if (exifDirectory != null) {
                if (exifDirectory.containsTag(ExifSubIFDDirectory.TAG_FOCAL_LENGTH)) {
                    exifData.put("focalLength", exifDirectory.getString(ExifSubIFDDirectory.TAG_FOCAL_LENGTH));
                }
                if (exifDirectory.containsTag(ExifSubIFDDirectory.TAG_FNUMBER)) {
                    exifData.put("aperture", "f/" + exifDirectory.getString(ExifSubIFDDirectory.TAG_FNUMBER));
                }
                if (exifDirectory.containsTag(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT)) {
                    exifData.put("iso", exifDirectory.getString(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT));
                }
            }
            
            // GPS 位置信息
            GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gpsDirectory != null) {
                // 获取纬度
                if (gpsDirectory.containsTag(GpsDirectory.TAG_LATITUDE)) {
                    com.drew.lang.GeoLocation geoLocation = gpsDirectory.getGeoLocation();
                    if (geoLocation != null) {
                        double latitude = geoLocation.getLatitude();
                        double longitude = geoLocation.getLongitude();
                        // 存储为逗号分隔的字符串: "纬度,经度"
                        exifData.put("location", String.format("%.6f,%.6f", latitude, longitude));
                    }
                }
            }
            
        } catch (ImageProcessingException | IOException e) {
            // 如果无法读取EXIF信息，返回空Map
        }
        
        return exifData;
    }
}

