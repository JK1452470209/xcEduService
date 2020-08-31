package com.xuecheng.test.freemarker.controller;

import com.xuecheng.test.freemarker.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Mr.JK
 * @create 2020-08-10  9:30
 */
@RequestMapping("/freemarker")
@Controller
public class FreemarkerController {

    @Autowired
    RestTemplate restTemplate;


    //课程详情页面测试
    @RequestMapping("/course")
    public String course(Map<String,Object> map){
        ResponseEntity<Map> forEntity =
                restTemplate.getForEntity("http://localhost:31200/course/courseview/4028e581617f945f01617f9dabc40000", Map.class);
        Map body = forEntity.getBody();
        map.put("model",body);
        return "course";
    }

    @RequestMapping("/banner")
    public String index_banner(Map<String,Object> map){
        //使用restTemplate在请求轮播图的模型数据
        ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://localhost:31001/cms/config/getmodel/5a791725dd573c3574ee333f", Map.class);
        Map body = forEntity.getBody();

        //设置模型数据
        map.putAll(body);

        return "index_banner";
    }


    @RequestMapping("/test1")
    public String test1(Map<String,Object> map){
        //map就是freemarker模板所使用的数据
        map.put("name","JK");

        Student stu1 = new Student();
        stu1.setName("小明");
        stu1.setAge(18);
        stu1.setMoney(1888.123f);
        stu1.setBirthday(new Date());
        //学生对象2
        Student stu2 = new Student();
        stu2.setName("小红");
        stu2.setAge(22);
        stu2.setMoney(1888.123f);
        stu2.setBirthday(new Date());
        //创建一个List对象，用于储存上面这两个学生对象
        ArrayList<Object> stus = new ArrayList<>();
        stus.add(stu1);
        stus.add(stu2);
        //向数据模型中放入List
        map.put("stus",stus);

        //准备map数据
        HashMap<Object, Object> stuMap = new HashMap<>();
        stuMap.put("stu1",stu1);
        stuMap.put("stu2",stu2);
         //像数据模型内放数据
        map.put("stu1",stu1);
        //向数据模型放入stuMap
        map.put("stuMap", stuMap);


        //返回freemarker模板的位置，基于resources/templates的位置
        return "test1";
    }
}
