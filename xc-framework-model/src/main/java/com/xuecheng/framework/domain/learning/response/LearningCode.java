package com.xuecheng.framework.domain.learning.response;

import com.xuecheng.framework.model.response.ResultCode;
import lombok.ToString;

/**
 * Created by mrt on 2018/3/5.
 */
@ToString
public enum LearningCode implements ResultCode {
    CLEARNING_GET_MEDIA_ERROR(false,23001,"学习中心获取媒资信息错误！"),
    CHOOSECOURSE_COURSEID_ISNULL(false,23002,"课程id为空！"),
    CHOOSECOURSE_USERID_ISNULL(false,23003,"用户id为空！"),
    CHOOSECOURSE_TASKID_ISNULL(false,23004,"课程任务为空！");
    //操作代码
    boolean success;
    //操作代码
    int code;
    //提示信息
    String message;
    private LearningCode(boolean success, int code, String message){
        this.success = success;
        this.code = code;
        this.message = message;
    }

    @Override
    public boolean success() {
        return success;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
