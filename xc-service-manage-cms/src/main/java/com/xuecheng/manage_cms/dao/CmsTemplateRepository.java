package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Mr.JK
 * @create 2020-08-10  20:46
 */
public interface CmsTemplateRepository extends MongoRepository<CmsTemplate,String> {
}
