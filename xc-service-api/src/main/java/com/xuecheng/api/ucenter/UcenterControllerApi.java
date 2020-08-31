package com.xuecheng.api.ucenter;

import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author Mr.JK
 * @create 2020-08-25  10:02
 */
@Api(value = "用户中心",description = "用户中心管理")
public interface UcenterControllerApi {

    @ApiOperation("根据用户账号查询用户信息")
    public XcUserExt getUserext(String username);
}