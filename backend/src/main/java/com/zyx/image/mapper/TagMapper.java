package com.zyx.image.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zyx.image.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标签Mapper
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
}

