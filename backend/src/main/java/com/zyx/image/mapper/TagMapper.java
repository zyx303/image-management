package com.zyx.image.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zyx.image.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 标签Mapper
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    
    /**
     * 根据标签名查找标签
     */
    @Select("SELECT * FROM tag WHERE tag_name = #{tagName} LIMIT 1")
    Tag selectByName(String tagName);
}

