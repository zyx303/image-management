package com.zyx.image.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zyx.image.dto.ImageUpdateDTO;
import com.zyx.image.entity.Image;
import com.zyx.image.entity.ImageTag;
import com.zyx.image.entity.Tag;
import com.zyx.image.mapper.ImageMapper;
import com.zyx.image.mapper.ImageTagMapper;
import com.zyx.image.mapper.TagMapper;
import com.zyx.image.util.ExifUtil;
import com.zyx.image.util.FileUtil;
import com.zyx.image.vo.ImageVO;
import com.zyx.image.vo.PageResult;
import com.zyx.image.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 图片服务
 */
@Service
@RequiredArgsConstructor
public class ImageService {
    
    private final ImageMapper imageMapper;
    private final TagMapper tagMapper;
    private final ImageTagMapper imageTagMapper;
    private final FileUtil fileUtil;
    private final ExifUtil exifUtil;
    
    /**
     * 上传图片
     */
    @Transactional
    public ImageVO uploadImage(MultipartFile file, Long userId, List<Long> tagIds) throws IOException {
        // 保存文件
        String filePath = fileUtil.saveFile(file);
        
        // 生成缩略图
        String thumbnailPath = fileUtil.generateThumbnail(filePath, 300, 300);
        
        // 计算MD5（从已保存的文件路径计算，避免MultipartFile流被消费的问题）
        String md5 = null;
        try {
            md5 = fileUtil.calculateMD5(filePath);
        } catch (Exception e) {
            throw new RuntimeException("计算MD5失败: " + e.getMessage());
        }
        
        // 提取EXIF信息
        Map<String, Object> exifData = exifUtil.extractExif(file);
        
        // 获取图片尺寸（从已保存的文件路径读取，避免MultipartFile流被消费的问题）
        int width = 0;
        int height = 0;
        try {
            java.nio.file.Path savedFilePath = java.nio.file.Paths.get(fileUtil.getFullPath(filePath));
            java.awt.image.BufferedImage bufferedImage = javax.imageio.ImageIO.read(savedFilePath.toFile());
            if (bufferedImage != null) {
                width = bufferedImage.getWidth();
                height = bufferedImage.getHeight();
            }
        } catch (Exception e) {
            // 如果无法读取尺寸，使用默认值0
        }
        
        // 创建图片记录
        Image image = new Image();
        image.setUserId(userId);
        image.setFileName(file.getOriginalFilename());
        image.setFilePath(filePath);
        image.setFileSize(file.getSize());
        image.setFileType(file.getContentType());
        image.setWidth(width);
        image.setHeight(height);
        image.setThumbnailPath(thumbnailPath);
        image.setMd5(md5);
        image.setStatus(1);
        
        // 设置EXIF信息
        if (exifData.containsKey("shootTime")) {
            image.setShootTime((java.time.LocalDateTime) exifData.get("shootTime"));
        }
        if (exifData.containsKey("device")) {
            image.setDevice((String) exifData.get("device"));
        }
        if (exifData.containsKey("cameraModel")) {
            image.setCameraModel((String) exifData.get("cameraModel"));
        }
        if (exifData.containsKey("focalLength")) {
            image.setFocalLength((String) exifData.get("focalLength"));
        }
        if (exifData.containsKey("aperture")) {
            image.setAperture((String) exifData.get("aperture"));
        }
        if (exifData.containsKey("iso")) {
            image.setIso((String) exifData.get("iso"));
        }
        
        imageMapper.insert(image);

        // 添加标签关联
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                ImageTag imageTag = new ImageTag();
                imageTag.setImageId(image.getId());
                imageTag.setTagId(tagId);
                imageTagMapper.insert(imageTag);
            }
        }
        
        return convertToVO(image);
    }
    
    /**
     * 批量上传图片
     */
    @Transactional
    public List<ImageVO> batchUploadImages(MultipartFile[] files, Long userId) throws IOException {
        return java.util.Arrays.stream(files)
                .map(file -> {
                    try {
                        return uploadImage(file, userId, null);
                    } catch (IOException e) {
                        throw new RuntimeException("上传失败: " + file.getOriginalFilename(), e);
                    }
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 获取图片列表
     */
    public PageResult<ImageVO> getImageList(Long current, Long size, Long userId) {
        return getImageList(current, size, userId, null, null);
    }
    
    /**
     * 获取图片列表（支持排序）
     */
    public PageResult<ImageVO> getImageList(Long current, Long size, Long userId, String sortField, String sortOrder) {
        Page<Image> page = new Page<>(current, size);
        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Image::getStatus, 1);
        if (userId != null) {
            wrapper.eq(Image::getUserId, userId);
        }
        
        // 动态排序
        if (sortField != null && !sortField.isEmpty()) {
            boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
            switch (sortField) {
                case "createTime":
                    wrapper.orderBy(true, isAsc, Image::getUploadTime);
                    break;
                case "fileName":
                    wrapper.orderBy(true, isAsc, Image::getFileName);
                    break;
                case "fileSize":
                    wrapper.orderBy(true, isAsc, Image::getFileSize);
                    break;
                default:
                    wrapper.orderByDesc(Image::getUploadTime);
            }
        } else {
            wrapper.orderByDesc(Image::getUploadTime);
        }
        
        IPage<Image> imagePage = imageMapper.selectPage(page, wrapper);
        
        List<ImageVO> imageVOs = imagePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageResult.of(imageVOs, imagePage.getTotal(), imagePage.getCurrent(), imagePage.getSize());
    }
    
    /**
     * 获取图片详情
     */
    public ImageVO getImageDetail(Long id) {
        Image image = imageMapper.selectById(id);
        if (image == null || image.getStatus() == 0) {
            throw new RuntimeException("图片不存在");
        }
        
        // 增加浏览次数
        image.setViewCount(image.getViewCount() + 1);
        imageMapper.updateById(image);
        
        return convertToVO(image);
    }
    
    /**
     * 根据文件路径获取图片
     */
    public Image getImageByPath(String filePath) {
        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Image::getFilePath, filePath);
        wrapper.eq(Image::getStatus, 1);
        return imageMapper.selectOne(wrapper);
    }
    
    /**
     * 根据缩略图路径获取图片
     */
    public Image getImageByThumbnailPath(String thumbnailPath) {
        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Image::getThumbnailPath, thumbnailPath);
        wrapper.eq(Image::getStatus, 1);
        return imageMapper.selectOne(wrapper);
    }
    
    /**
     * 根据ID获取图片实体
     */
    public Image getImageById(Long id) {
        Image image = imageMapper.selectById(id);
        if (image == null || image.getStatus() == 0) {
            return null;
        }
        return image;
    }
    
    /**
     * 更新图片信息
     */
    @Transactional
    public ImageVO updateImage(Long id, Long userId, ImageUpdateDTO updateDTO) {
        Image image = imageMapper.selectById(id);
        if (image == null || image.getStatus() == 0) {
            throw new RuntimeException("图片不存在");
        }
        
        if (!image.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此图片");
        }
        
        if (updateDTO.getTitle() != null) {
            image.setTitle(updateDTO.getTitle());
        }
        if (updateDTO.getDescription() != null) {
            image.setDescription(updateDTO.getDescription());
        }
        
        imageMapper.updateById(image);
        
        // 更新标签
        if (updateDTO.getTagIds() != null) {
            // 删除旧标签
            LambdaQueryWrapper<ImageTag> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ImageTag::getImageId, id);
            imageTagMapper.delete(wrapper);
            
            // 添加新标签
            updateDTO.getTagIds().forEach(tagId -> {
                ImageTag imageTag = new ImageTag();
                imageTag.setImageId(id);
                imageTag.setTagId(tagId);
                imageTagMapper.insert(imageTag);
            });
        }
        
        return convertToVO(image);
    }
    
    /**
     * 删除图片
     */
    @Transactional
    public void deleteImage(Long id, Long userId) {
        Image image = imageMapper.selectById(id);
        if (image == null || image.getStatus() == 0) {
            throw new RuntimeException("图片不存在");
        }
        
        if (!image.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此图片");
        }
        
        // 删除文件（即使文件不存在也不会抛异常）
        fileUtil.deleteFile(image.getFilePath());
        if (image.getThumbnailPath() != null) {
            fileUtil.deleteThumbnail(image.getThumbnailPath());
        }
        
        // 使用 MyBatis-Plus 的逻辑删除（会自动将 status 设为 0）
        imageMapper.deleteById(id);
        
        // 删除标签关联
        LambdaQueryWrapper<ImageTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImageTag::getImageId, id);
        imageTagMapper.delete(wrapper);
    }
    
    /**
     * 批量删除图片
     */
    @Transactional
    public void batchDeleteImages(List<Long> ids, Long userId) {
        for (Long id : ids) {
            deleteImage(id, userId);
        }
    }
    
    /**
     * 搜索图片
     */
    public PageResult<ImageVO> searchImages(String keyword, Long current, Long size, Long userId) {
        Page<Image> page = new Page<>(current, size);
        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Image::getStatus, 1);
        if (userId != null) {
            wrapper.eq(Image::getUserId, userId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Image::getTitle, keyword)
                    .or().like(Image::getDescription, keyword)
                    .or().like(Image::getFileName, keyword));
        }
        wrapper.orderByDesc(Image::getUploadTime);
        
        IPage<Image> imagePage = imageMapper.selectPage(page, wrapper);
        
        List<ImageVO> imageVOs = imagePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageResult.of(imageVOs, imagePage.getTotal(), imagePage.getCurrent(), imagePage.getSize());
    }
    
    /**
     * 添加图片标签
     */
    @Transactional
    public void addImageTag(Long imageId, Long tagId) {
        ImageTag imageTag = new ImageTag();
        imageTag.setImageId(imageId);
        imageTag.setTagId(tagId);
        imageTagMapper.insert(imageTag);
    }
    
    /**
     * 移除图片标签
     */
    @Transactional
    public void removeImageTag(Long imageId, Long tagId) {
        LambdaQueryWrapper<ImageTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImageTag::getImageId, imageId)
                .eq(ImageTag::getTagId, tagId);
        imageTagMapper.delete(wrapper);
    }
    
    /**
     * 编辑图片（裁剪/调整后保存）
     */
    @Transactional
    public ImageVO editImage(Long id, Long userId, String imageData) throws IOException {
        Image image = imageMapper.selectById(id);
        if (image == null || image.getStatus() == 0) {
            throw new RuntimeException("图片不存在");
        }
        
        if (!image.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此图片");
        }
        
        // 保存旧文件路径用于删除
        String oldFilePath = image.getFilePath();
        String oldThumbnailPath = image.getThumbnailPath();
        
        // 保存编辑后的图片
        String newFilePath = fileUtil.saveEditedImage(imageData, oldFilePath);
        
        // 生成新的缩略图
        String newThumbnailPath = fileUtil.generateThumbnail(newFilePath, 300, 300);
        
        // 获取新图片尺寸
        int width = 0;
        int height = 0;
        try {
            java.nio.file.Path savedFilePath = java.nio.file.Paths.get(fileUtil.getFullPath(newFilePath));
            java.awt.image.BufferedImage bufferedImage = javax.imageio.ImageIO.read(savedFilePath.toFile());
            if (bufferedImage != null) {
                width = bufferedImage.getWidth();
                height = bufferedImage.getHeight();
            }
        } catch (Exception e) {
            // 如果无法读取尺寸，使用默认值0
        }
        
        // 计算新文件大小
        long fileSize = 0;
        try {
            java.nio.file.Path savedFilePath = java.nio.file.Paths.get(fileUtil.getFullPath(newFilePath));
            fileSize = java.nio.file.Files.size(savedFilePath);
        } catch (Exception e) {
            // 忽略
        }
        
        // 计算新MD5
        String md5 = null;
        try {
            md5 = fileUtil.calculateMD5(newFilePath);
        } catch (Exception e) {
            // 忽略
        }
        
        // 更新图片记录
        image.setFilePath(newFilePath);
        image.setThumbnailPath(newThumbnailPath);
        image.setWidth(width);
        image.setHeight(height);
        image.setFileSize(fileSize);
        if (md5 != null) {
            image.setMd5(md5);
        }
        imageMapper.updateById(image);
        
        // 删除旧文件
        try {
            fileUtil.deleteFile(oldFilePath);
            if (oldThumbnailPath != null) {
                fileUtil.deleteThumbnail(oldThumbnailPath);
            }
        } catch (Exception e) {
            // 忽略删除失败
        }
        
        return convertToVO(image);
    }
    
    /**
     * 转换为VO
     */
    private ImageVO convertToVO(Image image) {
        ImageVO imageVO = new ImageVO();
        BeanUtils.copyProperties(image, imageVO);

        // 保持相对路径，前端会通过API访问文件
        // 不再转换为完整路径
        
        // 加载标签
        LambdaQueryWrapper<ImageTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImageTag::getImageId, image.getId());
        List<ImageTag> imageTags = imageTagMapper.selectList(wrapper);
        
        List<TagVO> tags = imageTags.stream()
                .map(it -> {
                    Tag tag = tagMapper.selectById(it.getTagId());
                    if (tag != null) {
                        TagVO tagVO = new TagVO();
                        BeanUtils.copyProperties(tag, tagVO);
                        return tagVO;
                    }
                    return null;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        
        imageVO.setTags(tags);
        return imageVO;
    }
}

