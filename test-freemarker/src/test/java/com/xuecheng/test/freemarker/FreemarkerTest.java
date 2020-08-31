package com.xuecheng.test.freemarker;

import com.xuecheng.test.freemarker.model.Student;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mr.JK
 * @create 2020-08-10  10:27
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class FreemarkerTest {

    //测试静态化，基于ftl模板文件生成html文件
    @Test
    public void testGenerateHtml() throws IOException, TemplateException {
        //定义配置类
        Configuration configuration = new Configuration(Configuration.getVersion());
        //定义模板
        //得了classpath的路径
        String classpath = this.getClass().getResource("/").getPath();
        configuration.setDirectoryForTemplateLoading(new File(classpath+"/templates/"));
        //获取模板文件的内容
        Template template = configuration.getTemplate("test1.ftl");

        //定义数据模型
        Map map = getMap();

        //静态化
        String context = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
//        System.out.println(context);
        InputStream inputStream = IOUtils.toInputStream(context);
        FileOutputStream outputStream = new FileOutputStream(new File("d:/test1.html"));
        //输出文件
        IOUtils.copy(inputStream,outputStream);
        inputStream.close();
        outputStream.close();
    }

    //获取数据模型
    public Map getMap(){
        Map map = new HashMap();
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

        return map;
    }
}
