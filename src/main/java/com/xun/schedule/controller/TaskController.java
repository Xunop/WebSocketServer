package com.xun.schedule.controller;

import com.xun.schedule.entity.Task;
import com.xun.schedule.service.TaskService;
import com.xun.schedule.service.DynamicTaskService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author xun
 * @create 2022/10/29 14:45
 */
@RestController
public class TaskController {
    @Resource
    TaskService taskService;
    @Resource
    DynamicTaskService dynamicTaskService;


    @GetMapping("/createTask")
    public String start(Task task) {
        dynamicTaskService.add(task);
        return "动态任务" + task.getId() + "已开启";
    }


    @GetMapping("/getTask")
    public List<String> getTask() {
        return dynamicTaskService.getTaskList();
    }

    @GetMapping("/stopTask")
    public boolean stopTask(Integer id) {
        return dynamicTaskService.stop(id);
    }

    @GetMapping("/getState")
    public String getState(Integer id) {
        return dynamicTaskService.getState(id);
    }

}
