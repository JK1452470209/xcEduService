package com.xuecheng.order.mq;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.TaskService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author Mr.JK
 * @create 2020-08-30  11:06
 */
@Component
public class ChooseCourseTask {


    private static final Logger LOGGER = LoggerFactory.getLogger(ChooseCourseTask.class);


    @Autowired
    TaskService taskService;

    /**
     * 接收选课响应结果
     */
    @RabbitListener(queues = {RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE})
    public void receiveFinishChoosecourseTask(XcTask task){
        if (task != null && StringUtils.isNotEmpty(task.getId())){
            LOGGER.info("receiveChoosecourseTask...{}",task.getId());
            //接收到 的消息id
            String id = task.getId();
            //删除任务，添加历史任务
            taskService.finishTask(id);

        }
    }

    //每隔1分钟扫描消息表，向mq发送消息
//    @Scheduled(cron = "0/60 * * * * *")
    @Scheduled(cron="0/3 * * * * *")
    public void sendChoosecourseTask(){
        //取出当前时间1分钟之前的时间
        Calendar calendar =new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(GregorianCalendar.MINUTE,-1);
        Date time = calendar.getTime();
        List<XcTask> taskList = taskService.findXcTaskList(time,10);
        System.out.println(taskList);
        //调用service发布消息，将添加选课的任务发送给mq
        for (XcTask xcTask : taskList){
            //取任务
            if (taskService.getTask(xcTask.getId(),xcTask.getVersion()) > 0){
                String ex = xcTask.getMqExchange();//要发送的交换机
                String routingkey = xcTask.getMqRoutingkey();//发送消息要带routingKey
                taskService.publish(xcTask,ex,routingkey);
            }
        }
    }


    // @Scheduled(fixedRate = 5000) //上次执行开始时间后5秒执行
    // @Scheduled(fixedDelay = 5000) //上次执行完毕后的5秒执行
    // @Scheduled(initialDelay=3000, fixedRate=5000) //第一次延迟3秒，以后每隔5秒执行一次
    //@Scheduled(cron="0/3 * * * * *")//每隔3秒执行一次
    public void task1(){
        LOGGER.info("===============测试定时任务1开始===============");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("===============测试定时任务1结束===============");
    }
}
