package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.system.SysDictionary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Mr.JK
 * @create 2020-08-13  14:30
 */

public interface SysDicthionaryRepository extends MongoRepository<SysDictionary,String> {

    public SysDictionary findAllByDType(String type);
}
