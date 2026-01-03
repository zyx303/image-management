package com.zyx.image.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zyx.image.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 标签Mapper
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    
    /**
     * 根据标签名查找标签（系统默认标签）
     */
    @Select("SELECT * FROM tag WHERE tag_name = #{tagName} AND user_id IS NULL LIMIT 1")
    Tag selectByName(String tagName);
    
    /**
     * 根据用户ID和标签名查找标签
     */
    @Select("SELECT * FROM tag WHERE tag_name = #{tagName} AND user_id = #{userId} LIMIT 1")
    Tag selectByUserIdAndName(@Param("userId") Long userId, @Param("tagName") String tagName);
    
    /**
     * 获取用户可见的标签（用户自己的标签 + 系统默认标签）
     */
    @Select("SELECT * FROM tag WHERE user_id = #{userId} OR user_id IS NULL ORDER BY use_count DESC")
    List<Tag> selectUserVisibleTags(@Param("userId") Long userId);
    
    /**
     * 获取系统默认标签
     */
    @Select("SELECT * FROM tag WHERE user_id IS NULL ORDER BY use_count DESC")
    List<Tag> selectDefaultTags();
}

