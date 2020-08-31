package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Mr.JK
 * @create 2020-08-28  17:07
 */
@Mapper
public interface XcMenuMapper {

    //根据用户id查询用户的权限
    public List<XcMenu> selectPermissionByUserId(String userid);

}
