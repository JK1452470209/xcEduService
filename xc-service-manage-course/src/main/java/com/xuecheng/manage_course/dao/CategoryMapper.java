package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.ext.CategoryNode;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Mr.JK
 * @create 2020-08-13  13:56
 */
@Mapper
public interface CategoryMapper {
    /**
     *  查询分类
     * @return
     */
    public CategoryNode selectList();
}
