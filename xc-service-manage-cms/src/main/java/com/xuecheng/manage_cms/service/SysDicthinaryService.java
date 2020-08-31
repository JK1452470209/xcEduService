package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.manage_cms.dao.SysDicthionaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Mr.JK
 * @create 2020-08-13  14:29
 */
@Service
public class SysDicthinaryService {

    @Autowired
    SysDicthionaryRepository sysDicthionaryRepository;

    public SysDictionary getByType(String type) {

        if (type == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        return sysDicthionaryRepository.findAllByDType(type);


    }
}
