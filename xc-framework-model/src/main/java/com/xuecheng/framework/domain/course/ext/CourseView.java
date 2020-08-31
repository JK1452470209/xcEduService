package com.xuecheng.framework.domain.course.ext;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.CoursePic;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author Mr.JK
 * @create 2020-08-16  15:55
 */
@Data
@ToString
@NoArgsConstructor
public class CourseView implements Serializable {
    private CourseBase courseBase;//基础信息
    private CoursePic coursePic;//课程图片
    private CourseMarket courseMarket;//课程营销
    private TeachplanNode teachplanNode;//教学计划
}
