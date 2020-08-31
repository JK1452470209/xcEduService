package com.xuecheng.manage_cms.dao;

import com.xuecheng.manage_cms.service.PageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Mr.JK
 * @create 2020-08-10  21:03
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class PageServiceTest {

    @Autowired
    PageService pageService;

    @Test
    public void testGetPageHtml(){
//        String pageHtml = pageService.getPageHtml("5e034cf73cf44b42441592ba");
        String pageHtml = pageService.getPageHtml("5e034cf73cf44b42441592ba");
        System.out.println(pageHtml);
    }

}

















