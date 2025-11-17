package com.zyx.image.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zyx.image.entity.ImageTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 图片标签关联Mapper
 */
@Mapper
public interface ImageTagMapper extends BaseMapper<ImageTag> {
}

