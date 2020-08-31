package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Mr.JK
 * @create 2020-08-25  10:11
 */
public interface XcCompanyUserRepository extends JpaRepository<XcCompanyUser,String> {

    //根据用户id查询改用户所属的公司id
    XcCompanyUser findByUserId(String userId);
}
