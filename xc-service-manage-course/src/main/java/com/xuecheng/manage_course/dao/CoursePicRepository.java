package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CoursePic;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Mr.JK
 * @create 2020-08-15  22:40
 */
public interface CoursePicRepository extends JpaRepository<CoursePic,String> {

    //当返回值大于0 删除成功
    long deleteByCourseid(String courseId);
}
