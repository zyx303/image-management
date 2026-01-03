package com.zyx.image.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zyx.image.entity.Image;
import com.zyx.image.entity.ImageTag;
import com.zyx.image.entity.Tag;
import com.zyx.image.mapper.ImageMapper;
import com.zyx.image.mapper.ImageTagMapper;
import com.zyx.image.mapper.TagMapper;
import com.zyx.image.vo.ImageVO;
import com.zyx.image.vo.PageResult;
import com.zyx.image.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签服务
 */
@Service
@RequiredArgsConstructor
public class TagService {
    
    private final TagMapper tagMapper;
    private final ImageMapper imageMapper;
    private final ImageTagMapper imageTagMapper;
    private final ImageService imageService;
    
    /**
     * 获取所有标签（仅系统默认标签）
     */
    public List<TagVO> getAllTags() {
        List<Tag> tags = tagMapper.selectDefaultTags();
        return tags.stream()
                .map(this::convertToTagVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户可见的所有标签（用户自己的标签 + 系统默认标签）
     */
    public List<TagVO> getUserVisibleTags(Long userId) {
        List<Tag> tags = tagMapper.selectUserVisibleTags(userId);
        return tags.stream()
                .map(this::convertToTagVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户标签（用户使用过的标签）
     */
    public List<TagVO> getUserTags(Long userId) {
        // 查询该用户的图片ID列表
        LambdaQueryWrapper<Image> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(Image::getUserId, userId)
                   .eq(Image::getStatus, 1);
        List<Image> userImages = imageMapper.selectList(imageWrapper);
        
        if (userImages.isEmpty()) {
            return List.of();
        }
        
        List<Long> imageIds = userImages.stream()
                .map(Image::getId)
                .collect(Collectors.toList());
        
        // 查询这些图片关联的标签ID
        LambdaQueryWrapper<ImageTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.in(ImageTag::getImageId, imageIds);
        List<ImageTag> imageTags = imageTagMapper.selectList(tagWrapper);
        
        // 获取去重后的标签ID
        List<Long> tagIds = imageTags.stream()
                .map(ImageTag::getTagId)
                .distinct()
                .collect(Collectors.toList());
        
        if (tagIds.isEmpty()) {
            return List.of();
        }
        
        // 查询标签详情
        LambdaQueryWrapper<Tag> tagQueryWrapper = new LambdaQueryWrapper<>();
        tagQueryWrapper.in(Tag::getId, tagIds);
        List<Tag> tags = tagMapper.selectList(tagQueryWrapper);
        
        return tags.stream()
                .map(this::convertToTagVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 创建标签（关联到指定用户）
     */
    @Transactional
    public TagVO createTag(String tagName, Long userId) {
        // 检查用户是否已有同名标签
        Tag existingTag = tagMapper.selectByUserIdAndName(userId, tagName);
        if (existingTag != null) {
            return convertToTagVO(existingTag);
        }
        
        // 检查系统默认标签中是否已存在同名标签
        Tag defaultTag = tagMapper.selectByName(tagName);
        if (defaultTag != null) {
            // 系统默认标签已存在，直接返回
            return convertToTagVO(defaultTag);
        }
        
        // 创建新标签（关联到用户）
        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setTagName(tagName);
        tag.setTagType(2); // 自定义标签
        tag.setUseCount(0);
        
        tagMapper.insert(tag);
        
        return convertToTagVO(tag);
    }
    
    /**
     * 创建标签（旧方法，保持兼容性，默认不关联用户）
     */
    @Transactional
    public TagVO createTag(String tagName) {
        return createTag(tagName, null);
    }
    
    /**
     * 更新标签
     */
    @Transactional
    public TagVO updateTag(Long id, String tagName, Long userId) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new RuntimeException("标签不存在");
        }
        
        // 检查权限：只能修改自己的标签，不能修改系统默认标签
        if (tag.getUserId() == null) {
            throw new RuntimeException("系统默认标签不可修改");
        }
        if (!tag.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此标签");
        }
        
        tag.setTagName(tagName);
        tagMapper.updateById(tag);
        
        return convertToTagVO(tag);
    }
    
    /**
     * 删除标签
     */
    @Transactional
    public void deleteTag(Long id, Long userId) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new RuntimeException("标签不存在");
        }
        
        // 检查权限：只能删除自己的标签，不能删除系统默认标签
        if (tag.getUserId() == null) {
            throw new RuntimeException("系统默认标签不可删除");
        }
        if (!tag.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此标签");
        }
        
        // 删除图片标签关联
        LambdaQueryWrapper<ImageTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImageTag::getTagId, id);
        imageTagMapper.delete(wrapper);
        
        // 删除标签
        tagMapper.deleteById(id);
    }
    
    /**
     * 根据标签获取图片
     */
    public PageResult<ImageVO> getImagesByTag(Long tagId, Long current, Long size) {
        return getImagesByTagAndUser(tagId, null, current, size);
    }
    
    /**
     * 根据标签和用户获取图片
     */
    public PageResult<ImageVO> getImagesByTagAndUser(Long tagId, Long userId, Long current, Long size) {
        // 查询标签关联的图片ID
        LambdaQueryWrapper<ImageTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImageTag::getTagId, tagId);
        List<ImageTag> imageTags = imageTagMapper.selectList(wrapper);
        
        List<Long> imageIds = imageTags.stream()
                .map(ImageTag::getImageId)
                .collect(Collectors.toList());
        
        if (imageIds.isEmpty()) {
            return PageResult.of(List.of(), 0L, current, size);
        }
        
        // 分页查询图片列表
        Page<Image> page = new Page<>(current, size);
        LambdaQueryWrapper<Image> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.in(Image::getId, imageIds)
                   .eq(Image::getStatus, 1);
        
        // 如果指定了用户ID，则只查询该用户的图片
        if (userId != null) {
            imageWrapper.eq(Image::getUserId, userId);
        }
        
        imageWrapper.orderByDesc(Image::getUploadTime);

        IPage<Image> imagePage = imageMapper.selectPage(page, imageWrapper);

        List<ImageVO> imageVOs = imagePage.getRecords().stream()
                .map(this::convertToImageVO)
                .collect(Collectors.toList());

        return PageResult.of(imageVOs, imagePage.getTotal(), imagePage.getCurrent(), imagePage.getSize());
    }
    
    /**
     * 搜索标签（用户可见的标签）
     */
    public List<TagVO> searchTags(String keyword, Long userId) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        // 只搜索用户自己的标签和系统默认标签
        wrapper.and(w -> w.eq(Tag::getUserId, userId).or().isNull(Tag::getUserId));
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Tag::getTagName, keyword);
        }
        
        List<Tag> tags = tagMapper.selectList(wrapper);
        return tags.stream()
                .map(this::convertToTagVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 搜索标签（旧方法，保持兼容性）
     */
    public List<TagVO> searchTags(String keyword) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Tag::getTagName, keyword);
        }
        
        List<Tag> tags = tagMapper.selectList(wrapper);
        return tags.stream()
                .map(this::convertToTagVO)
                .collect(Collectors.toList());
    }

    /**
     * 将Tag实体转换为TagVO
     */
    private TagVO convertToTagVO(Tag tag) {
        TagVO tagVO = new TagVO();
        BeanUtils.copyProperties(tag, tagVO);
        return tagVO;
    }

    /**
     * 将Image实体转换为ImageVO
     */
    private ImageVO convertToImageVO(Image image) {
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

