package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Mr.JK
 * @create 2020-08-12  21:29
 */
@Mapper
public interface TeachplanMapper {

    //课程计划查询
    public TeachplanNode selectList(String courseId);

}
