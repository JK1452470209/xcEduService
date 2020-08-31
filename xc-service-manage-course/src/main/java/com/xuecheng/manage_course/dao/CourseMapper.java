package com.xuecheng.manage_course.dao;

import com.github.pagehelper.Page;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by Administrator.
 */
@Mapper
public interface CourseMapper {
   /**
    * 根据id查询CourseBase
    * @param id
    * @return
    */
   CourseBase findCourseBaseById(String id);

   /**
    * 分页查询CourseBase
    * @return
    */
   Page<CourseBase> findCourseList();

   /**
    * course_base和course_pic表连接后的分页查询
    * @return
    */
   Page<CourseInfo> findCoursePageList(CourseListRequest courseListRequest);
}
