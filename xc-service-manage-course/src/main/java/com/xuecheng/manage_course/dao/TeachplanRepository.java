package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.Teachplan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


/**
 * @author Mr.JK
 * @create 2020-08-12  22:34
 */
public interface TeachplanRepository extends JpaRepository<Teachplan,String> {

    //根据课程id和parentId查询teachplan
    public List<Teachplan> findByCourseidAndParentid(String courseId,String parentId);


}
