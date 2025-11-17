package com.zyx.image.controller;

import com.zyx.image.service.ImageService;
import com.zyx.image.util.JwtUtil;
import com.zyx.image.util.FileUtil;
import com.zyx.image.vo.ImageVO;
import com.zyx.image.vo.PageResult;
import com.zyx.image.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * 图片控制器
 */
@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {
    
    private final ImageService imageService;
    private final JwtUtil jwtUtil;
    
    /**
     * 上传图片
     */
    @PostMapping("/upload")
    public Result<ImageVO> uploadImage(@RequestParam("file") MultipartFile file,
                                       HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            ImageVO imageVO = imageService.uploadImage(file, userId);
            return Result.success("上传成功", imageVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量上传图片
     */
    @PostMapping("/batch-upload")
    public Result<List<ImageVO>> batchUploadImages(@RequestParam("files") MultipartFile[] files,
                                                    HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            List<ImageVO> imageVOs = imageService.batchUploadImages(files, userId);
            return Result.success("上传成功", imageVOs);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取图片列表
     */
    @GetMapping
    public Result<PageResult<ImageVO>> getImageList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            PageResult<ImageVO> result = imageService.getImageList(current, size, userId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户图片列表
     */
    @GetMapping("/user")
    public Result<PageResult<ImageVO>> getUserImages(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            PageResult<ImageVO> result = imageService.getImageList(current, size, userId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取图片详情
     */
    @GetMapping("/{id}")
    public Result<ImageVO> getImageDetail(@PathVariable Long id) {
        try {
            ImageVO imageVO = imageService.getImageDetail(id);
            return Result.success(imageVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新图片信息
     */
    @PutMapping("/{id}")
    public Result<ImageVO> updateImage(@PathVariable Long id,
                                       @RequestBody com.zyx.image.dto.ImageUpdateDTO updateDTO,
                                       HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            ImageVO imageVO = imageService.updateImage(id, userId, updateDTO);
            return Result.success("更新成功", imageVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除图片
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteImage(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            imageService.deleteImage(id, userId);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量删除图片
     */
    @DeleteMapping("/batch-delete")
    public Result<Void> batchDeleteImages(@RequestBody Map<String, List<Long>> data, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            List<Long> ids = data.get("ids");
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的图片");
            }
            imageService.batchDeleteImages(ids, userId);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 搜索图片
     */
    @GetMapping("/search")
    public Result<PageResult<ImageVO>> searchImages(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            PageResult<ImageVO> result = imageService.searchImages(keyword, current, size, userId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 添加图片标签
     */
    @PostMapping("/{imageId}/tags")
    public Result<Void> addImageTag(@PathVariable Long imageId,
                                    @RequestBody Map<String, Long> data) {
        try {
            Long tagId = data.get("tagId");
            if (tagId == null) {
                return Result.error("标签ID不能为空");
            }
            imageService.addImageTag(imageId, tagId);
            return Result.success("添加成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 移除图片标签
     */
    @DeleteMapping("/{imageId}/tags/{tagId}")
    public Result<Void> removeImageTag(@PathVariable Long imageId,
                                       @PathVariable Long tagId) {
        try {
            imageService.removeImageTag(imageId, tagId);
            return Result.success("移除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 下载图片
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadImage(@PathVariable Long id) {
        try {
            ImageVO imageVO = imageService.getImageDetail(id);
            java.nio.file.Path filePath = java.nio.file.Paths.get(imageVO.getFilePath());
            Resource resource = new FileSystemResource(filePath.toFile());
            
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imageVO.getFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new RuntimeException("未找到Token");
    }
}

/**
 * 文件访问控制器
 * 专门用于提供图片文件访问服务，仅允许用户访问自己的图片
 */
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
class FileAccessController {
    
    private final FileUtil fileUtil;
    private final ImageService imageService;
    private final JwtUtil jwtUtil;
    
    /**
     * 获取图片文件
     * 支持访问原图和缩略图
     * 路径格式：/files/{relativePath}
     * 例如：/files/2025/11/17/xxx.png 或 /files/thumbnails/2025/11/17/xxx.png
     * 需要JWT认证，仅能访问自己的图片
     */
    @GetMapping("/**")
    public ResponseEntity<Resource> getFile(HttpServletRequest request) {
        try {
            // 获取并验证用户身份
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // 获取完整的请求路径
            String requestPath = request.getRequestURI();
            // 移除 /api/files/ 前缀
            String contextPath = request.getContextPath();
            String relativePath = requestPath.substring((contextPath + "/files/").length());
            
            // 安全检查：防止路径遍历攻击
            if (relativePath.contains("..") || relativePath.startsWith("/")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // 判断是缩略图还是原图，并获取对应的图片信息
            com.zyx.image.entity.Image image;
            boolean isThumbnail = false;
            
            if (relativePath.startsWith("thumbnails/")) {
                isThumbnail = true;
                String thumbnailRelativePath = relativePath.substring("thumbnails/".length());
                image = imageService.getImageByThumbnailPath(thumbnailRelativePath);
            } else {
                image = imageService.getImageByPath(relativePath);
            }
            
            // 检查图片是否存在
            if (image == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 权限检查：验证图片是否属于当前用户
            if (!image.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // 获取实际文件路径
            Path filePath;
            if (isThumbnail) {
                filePath = Paths.get(fileUtil.getThumbnailFullPath(image.getThumbnailPath()));
            } else {
                filePath = Paths.get(fileUtil.getFullPath(image.getFilePath()));
            }
            
            // 检查文件是否存在
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            // 创建资源
            Resource resource = new FileSystemResource(filePath.toFile());
            
            // 确定Content-Type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                // 如果无法自动检测，根据文件扩展名设置
                String filename = filePath.getFileName().toString().toLowerCase();
                if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (filename.endsWith(".png")) {
                    contentType = "image/png";
                } else if (filename.endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (filename.endsWith(".webp")) {
                    contentType = "image/webp";
                } else {
                    contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                }
            }
            
            // 返回图片资源，设置为inline显示
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=31536000") // 缓存1年
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 从请求中获取用户ID
     * 支持从Authorization header或查询参数中获取token
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        try {
            String token = null;
            
            // 首先尝试从 Authorization header 中获取
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                token = bearerToken.substring(7);
            }
            
            // 如果 header 中没有，尝试从查询参数中获取（用于 img 标签等无法设置 header 的场景）
            if (token == null) {
                token = request.getParameter("token");
            }
            
            if (token != null) {
                return jwtUtil.getUserIdFromToken(token);
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}

