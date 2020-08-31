package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.manage_course.dao.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mr.JK
 * @create 2020-08-13  14:14
 */
@Service
public class CategoryService {

    @Autowired
    CategoryMapper categoryMapper;

    @Transactional
    public CategoryNode findCategoryList(){
        return categoryMapper.selectList();
    }
}
