package com.xuecheng.framework.domain.cms.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;



/**
 * @author Mr.JK
 * @create 2020-08-06  21:13
 */
@Data
public class QueryPageRequest {
    //接收页面查询的查询条件
    //站点id
    @ApiModelProperty("站点id")
    private String siteId;
    //页面id
    @ApiModelProperty("页面id")
    private String pageId;
    //页面名称
    @ApiModelProperty("页面名称")
    private String pageName;
    //别名
    @ApiModelProperty("别名")
    private String pageAliase;
    //模板id
    @ApiModelProperty("模板id")
    private String templateId;
    //...


}
