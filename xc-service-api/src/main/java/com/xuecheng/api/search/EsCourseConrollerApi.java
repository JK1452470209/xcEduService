package com.xuecheng.api.search;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.Map;

/**
 * @author Mr.JK
 * @create 2020-08-18  21:58
 */
@Api(value = "课程搜索", description = "基于ES构建的课程搜索API",tags = {"课程搜索"})
public interface EsCourseConrollerApi {

    @ApiOperation("课程详细搜索")
    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam);

    @ApiOperation("根据id搜索课程发布信息")
    public Map<String,CoursePub> getdetail(String id);

    @ApiOperation("根据课程计划id查询课程媒资信息")
    public TeachplanMediaPub getmedia(String id);

}
