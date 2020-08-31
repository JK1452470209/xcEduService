package com.xuecheng.learning.dao;

import com.xuecheng.framework.domain.learning.XcLearningCourse;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Mr.JK
 * @create 2020-08-31  10:40
 */
public interface XcLearningCourseRepository extends JpaRepository<XcLearningCourse,String> {

    //根据用户id和课程id查询
    XcLearningCourse findByUserIdAndCourseId(String userId,String courseId);

}