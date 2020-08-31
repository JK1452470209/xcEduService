package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.TeachplanMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Mr.JK
 * @create 2020-08-21  21:39
 */
public interface TeachplanMediaRepository extends JpaRepository<TeachplanMedia,String> {

    //根据课程id查询列表
    public List<TeachplanMedia> findByCourseId(String courseId);

}
