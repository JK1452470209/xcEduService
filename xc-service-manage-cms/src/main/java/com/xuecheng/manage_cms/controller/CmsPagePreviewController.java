package com.xuecheng.manage_cms.controller;

import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_cms.service.PageService;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletOutputStream;
import java.io.IOException;

/**
 * @author Mr.JK
 * @create 2020-08-10  23:07
 */
@Controller
public class CmsPagePreviewController extends BaseController {

    @Autowired
    PageService pageService;

    //页面预览
    @RequestMapping(value = "/cms/preview/{pageId}", method = RequestMethod.GET)
    public void preView(@PathVariable("pageId") String pageId) throws IOException, TemplateException {
        //执行静态化
        String pageHtml = pageService.getPageHtml(pageId);
        if(StringUtils.isNotEmpty(pageHtml)){
            try {
                //通过response对象将页面内容输出
                ServletOutputStream outputStream = response.getOutputStream();
                response.setHeader("Content-type","text/html;charset=utf-8");
                outputStream.write(pageHtml.getBytes("utf-8"));
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
