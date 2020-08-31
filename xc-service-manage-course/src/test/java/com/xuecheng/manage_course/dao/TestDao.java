package com.xuecheng.manage_course.dao;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sound.midi.Soundbank;
import java.util.Optional;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestDao {
    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Autowired
    CourseMapper courseMapper;
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    CategoryMapper categoryMapper;
    @Test
    public void testCourseBaseRepository(){
        Optional<CourseBase> optional = courseBaseRepository.findById("402885816240d276016240f7e5000002");
        if(optional.isPresent()){
            CourseBase courseBase = optional.get();
            System.out.println(courseBase);
        }

    }

    @Test
    public void testCourseMapper(){
        CourseBase courseBase = courseMapper.findCourseBaseById("402885816240d276016240f7e5000002");
        System.out.println(courseBase);

    }

    @Test
    public void testFindTeachplan(){
        TeachplanNode teachplanNode = teachplanMapper.selectList("4028e581617f01617f9dabc0000");
        System.out.println(teachplanNode);
    }

    @Test
    public void testPageHelper(){
        PageHelper.startPage(1,2);
        Page<CourseBase> courseList = courseMapper.findCourseList();
        System.out.println(courseList.getResult());
    }

    @Test
    public void testfindCoursePageList(){
        PageHelper.startPage(1,3);
        Page<CourseInfo> coursePageList = courseMapper.findCoursePageList(new CourseListRequest("1"));
        System.out.println(coursePageList);
    }

    @Test
    public void testselectList(){
        CategoryNode categoryNode = categoryMapper.selectList();
        System.out.println(categoryNode);
    }
}
