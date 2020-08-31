package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.dao.CourseMarketRepository;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author Mr.JK
 * @create 2020-08-13  20:46
 */
@Service
public class CourseMarketService {

    @Autowired
    CourseMarketRepository courseMarketRepository;

    @Transactional
    public CourseMarket getCourseMarketById(String courseMarketId) {
        if (StringUtils.isEmpty(courseMarketId)){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseMarketId);
        if (optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    @Transactional
    public ResponseResult updateCourseMarket(String id, CourseMarket courseMarket) {
        if (StringUtils.isEmpty(id)){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        if (courseMarket == null){
            return null;
        }
        CourseMarket courseMarketOld = this.getCourseMarketById(id);
        if (courseMarketOld == null){
            courseMarketRepository.save(courseMarket);
        }else {
            BeanUtils.copyProperties(courseMarket,courseMarketOld);
            courseMarketRepository.save(courseMarketOld);

        }

        return ResponseResult.SUCCESS();


    }
}
