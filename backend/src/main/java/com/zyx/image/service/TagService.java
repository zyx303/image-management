package com.zyx.image.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zyx.image.entity.ImageTag;
import com.zyx.image.entity.Tag;
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
    private final ImageTagMapper imageTagMapper;
    private final ImageService imageService;
    
    /**
     * 获取所有标签
     */
    public List<TagVO> getAllTags() {
        List<Tag> tags = tagMapper.selectList(null);
        return tags.stream()
                .map(tag -> {
                    TagVO tagVO = new TagVO();
                    BeanUtils.copyProperties(tag, tagVO);
                    return tagVO;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户标签（用户使用过的标签）
     */
    public List<TagVO> getUserTags(Long userId) {
        // 查询用户图片的标签
        LambdaQueryWrapper<ImageTag> wrapper = new LambdaQueryWrapper<>();
        // 需要通过图片表关联查询，这里简化处理，返回所有标签
        return getAllTags();
    }
    
    /**
     * 创建标签
     */
    @Transactional
    public TagVO createTag(String tagName) {
        // 检查标签是否已存在
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getTagName, tagName);
        Tag existingTag = tagMapper.selectOne(wrapper);
        
        if (existingTag != null) {
            TagVO tagVO = new TagVO();
            BeanUtils.copyProperties(existingTag, tagVO);
            return tagVO;
        }
        
        // 创建新标签
        Tag tag = new Tag();
        tag.setTagName(tagName);
        tag.setTagType(2); // 自定义标签
        tag.setUseCount(0);
        
        tagMapper.insert(tag);
        
        TagVO tagVO = new TagVO();
        BeanUtils.copyProperties(tag, tagVO);
        return tagVO;
    }
    
    /**
     * 更新标签
     */
    @Transactional
    public TagVO updateTag(Long id, String tagName) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new RuntimeException("标签不存在");
        }
        
        tag.setTagName(tagName);
        tagMapper.updateById(tag);
        
        TagVO tagVO = new TagVO();
        BeanUtils.copyProperties(tag, tagVO);
        return tagVO;
    }
    
    /**
     * 删除标签
     */
    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new RuntimeException("标签不存在");
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
        
        // 这里简化处理，实际应该查询图片列表
        // 由于需要分页，这里返回空结果，实际应该实现分页查询
        return PageResult.of(List.of(), (long) imageIds.size(), current, size);
    }
    
    /**
     * 搜索标签
     */
    public List<TagVO> searchTags(String keyword) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Tag::getTagName, keyword);
        }
        
        List<Tag> tags = tagMapper.selectList(wrapper);
        return tags.stream()
                .map(tag -> {
                    TagVO tagVO = new TagVO();
                    BeanUtils.copyProperties(tag, tagVO);
                    return tagVO;
                })
                .collect(Collectors.toList());
    }
}

