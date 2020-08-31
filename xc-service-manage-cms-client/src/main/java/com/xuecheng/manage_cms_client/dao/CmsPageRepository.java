package com.xuecheng.manage_cms_client.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Mr.JK
 * @create 2020-08-12  10:02
 */
public interface CmsPageRepository extends MongoRepository<CmsPage,String> {
}
