package com.zyx.image.util;

import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件工具类
 */
@Component
public class FileUtil {
    
    @Value("${file.upload.path:/home/zyx/work/bs/uploads}")
    private String uploadPath;
    
    @Value("${file.upload.thumbnail-path:/home/zyx/work/bs/uploads/thumbnails}")
    private String thumbnailPath;
    
    /**
     * 保存文件
     */
    public String saveFile(MultipartFile file) throws IOException {
        // 创建目录
        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path dir = Paths.get(uploadPath, dateDir);
        Files.createDirectories(dir);
        
        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : "";
        String filename = UUID.randomUUID().toString() + extension;
        
        // 保存文件
        Path filePath = dir.resolve(filename);
        file.transferTo(filePath.toFile());
        
        // 返回相对路径
        return Paths.get(dateDir, filename).toString().replace("\\", "/");
    }
    
    /**
     * 生成缩略图
     */
    public String generateThumbnail(String filePath, int width, int height) throws IOException {
        Path sourcePath = Paths.get(uploadPath, filePath);
        if (!Files.exists(sourcePath)) {
            throw new IOException("源文件不存在: " + sourcePath);
        }
        
        // 创建缩略图目录
        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path thumbnailDir = Paths.get(thumbnailPath, dateDir);
        Files.createDirectories(thumbnailDir);
        
        // 生成缩略图文件名
        String originalFilename = Paths.get(filePath).getFileName().toString();
        String nameWithoutExt = originalFilename.substring(0, originalFilename.lastIndexOf("."));
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String thumbnailFilename = nameWithoutExt + "_thumb" + extension;
        
        // 生成缩略图
        Path thumbnailFilePath = thumbnailDir.resolve(thumbnailFilename);
        Thumbnails.of(sourcePath.toFile())
                .size(width, height)
                .outputFormat(extension.substring(1))
                .toFile(thumbnailFilePath.toFile());
        
        // 返回相对路径
        return Paths.get(dateDir, thumbnailFilename).toString().replace("\\", "/");
    }
    
    /**
     * 计算文件MD5（从MultipartFile）
     */
    public String calculateMD5(MultipartFile file) throws IOException {
        return DigestUtils.md5Hex(file.getInputStream());
    }
    
    /**
     * 计算文件MD5（从文件路径）
     */
    public String calculateMD5(String filePath) throws IOException {
        Path path = Paths.get(uploadPath, filePath);
        if (!Files.exists(path)) {
            throw new IOException("文件不存在: " + path);
        }
        return DigestUtils.md5Hex(Files.newInputStream(path));
    }
    
    /**
     * 删除文件
     */
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            System.out.println("警告: 文件路径为空，跳过删除");
            return;
        }
        
        Path path = Paths.get(uploadPath, filePath);
        System.out.println("尝试删除文件: " + path.toAbsolutePath());
        
        if (Files.exists(path)) {
            try {
                Files.delete(path);
                System.out.println("文件删除成功: " + path.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("删除文件失败（但继续执行）: " + path.toAbsolutePath() + ", 错误: " + e.getMessage());
                // 不抛异常，认为文件已被删除
            }
        } else {
            System.out.println("文件不存在，视为已删除: " + path.toAbsolutePath());
        }
    }
    
    /**
     * 删除缩略图
     */
    public void deleteThumbnail(String thumbnailPath) {
        if (thumbnailPath == null || thumbnailPath.isEmpty()) {
            System.out.println("警告: 缩略图路径为空，跳过删除");
            return;
        }
        
        Path path = Paths.get(this.thumbnailPath, thumbnailPath);
        System.out.println("尝试删除缩略图: " + path.toAbsolutePath());
        
        if (Files.exists(path)) {
            try {
                Files.delete(path);
                System.out.println("缩略图删除成功: " + path.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("删除缩略图失败（但继续执行）: " + path.toAbsolutePath() + ", 错误: " + e.getMessage());
                // 不抛异常，认为缩略图已被删除
            }
        } else {
            System.out.println("缩略图不存在，视为已删除: " + path.toAbsolutePath());
        }
    }
    
    /**
     * 获取文件完整路径
     */
    public String getFullPath(String relativePath) {
        return Paths.get(uploadPath, relativePath).toString();
    }
    
    /**
     * 获取缩略图完整路径
     */
    public String getThumbnailFullPath(String relativePath) {
        return Paths.get(thumbnailPath, relativePath).toString();
    }
    
    /**
     * 保存编辑后的图片（从Base64）
     */
    public String saveEditedImage(String base64Data, String originalFilePath) throws IOException {
        // 解析Base64数据
        String[] parts = base64Data.split(",");
        String imageData = parts.length > 1 ? parts[1] : parts[0];
        byte[] imageBytes = java.util.Base64.getDecoder().decode(imageData);
        
        // 创建目录
        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path dir = Paths.get(uploadPath, dateDir);
        Files.createDirectories(dir);
        
        // 从原始文件路径获取扩展名
        String extension = ".jpg";
        if (originalFilePath != null && originalFilePath.contains(".")) {
            extension = originalFilePath.substring(originalFilePath.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + extension;
        
        // 保存文件
        Path filePath = dir.resolve(filename);
        Files.write(filePath, imageBytes);
        
        // 返回相对路径
        return Paths.get(dateDir, filename).toString().replace("\\", "/");
    }
    
    /**
     * 获取上传路径
     */
    public String getUploadPath() {
        return uploadPath;
    }
}

