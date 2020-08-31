package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Mr.JK
 * @create 2020-08-10  13:40
 */
public interface CmsConfigRepository extends MongoRepository<CmsConfig,String> {
}
