package com.zyx.image.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zyx.image.entity.ImageTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 图片标签关联Mapper
 */
@Mapper
public interface ImageTagMapper extends BaseMapper<ImageTag> {
    
    /**
     * 根据图片ID和标签ID查找关联
     */
    @Select("SELECT * FROM image_tag WHERE image_id = #{imageId} AND tag_id = #{tagId} LIMIT 1")
    ImageTag selectByImageIdAndTagId(Long imageId, Long tagId);
}

