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
    public ImageVO uploadImage(MultipartFile file, Long userId) throws IOException {
        // 保存文件
        String filePath = fileUtil.saveFile(file);
        
        // 生成缩略图
        String thumbnailPath = fileUtil.generateThumbnail(filePath, 300, 300);
        
        // 计算MD5
        String md5 = fileUtil.calculateMD5(file);
        
        // 提取EXIF信息
        Map<String, Object> exifData = exifUtil.extractExif(file);
        
        // 获取图片尺寸
        int width = 0;
        int height = 0;
        try {
            java.io.InputStream inputStream = file.getInputStream();
            java.awt.image.BufferedImage bufferedImage = javax.imageio.ImageIO.read(inputStream);
            if (bufferedImage != null) {
                width = bufferedImage.getWidth();
                height = bufferedImage.getHeight();
            }
            inputStream.close();
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
                        return uploadImage(file, userId);
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
        Page<Image> page = new Page<>(current, size);
        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Image::getStatus, 1);
        if (userId != null) {
            wrapper.eq(Image::getUserId, userId);
        }
        wrapper.orderByDesc(Image::getUploadTime);
        
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
    public void deleteImage(Long id, Long userId) throws IOException {
        Image image = imageMapper.selectById(id);
        if (image == null || image.getStatus() == 0) {
            throw new RuntimeException("图片不存在");
        }
        
        if (!image.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此图片");
        }
        
        // 删除文件
        fileUtil.deleteFile(image.getFilePath());
        if (image.getThumbnailPath() != null) {
            fileUtil.deleteThumbnail(image.getThumbnailPath());
        }
        
        // 逻辑删除
        image.setStatus(0);
        imageMapper.updateById(image);
        
        // 删除标签关联
        LambdaQueryWrapper<ImageTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImageTag::getImageId, id);
        imageTagMapper.delete(wrapper);
    }
    
    /**
     * 批量删除图片
     */
    @Transactional
    public void batchDeleteImages(List<Long> ids, Long userId) throws IOException {
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
     * 转换为VO
     */
    private ImageVO convertToVO(Image image) {
        ImageVO imageVO = new ImageVO();
        BeanUtils.copyProperties(image, imageVO);
        
        // 设置完整路径
        if (image.getFilePath() != null) {
            imageVO.setFilePath(fileUtil.getFullPath(image.getFilePath()));
        }
        if (image.getThumbnailPath() != null) {
            imageVO.setThumbnailPath(fileUtil.getThumbnailFullPath(image.getThumbnailPath()));
        }
        
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

