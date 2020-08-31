package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsSite;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Mr.JK
 * @create 2020-08-17  14:07
 */
public interface CmsSiteRepository extends MongoRepository<CmsSite,String> {
}
