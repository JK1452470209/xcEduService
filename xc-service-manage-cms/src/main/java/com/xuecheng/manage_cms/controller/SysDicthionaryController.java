package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.SysDicthionaryControllerApi;
import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_cms.service.SysDicthinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mr.JK
 * @create 2020-08-13  14:27
 */
@RestController
@RequestMapping("/sys")
public class SysDicthionaryController implements SysDicthionaryControllerApi {

    @Autowired
    SysDicthinaryService sysDicthinaryService;

    @Override
    @GetMapping("/dictionary/get/{dType}")
    public SysDictionary getByType(@PathVariable("dType") String type) {
        return sysDicthinaryService.getByType(type);
    }
}
