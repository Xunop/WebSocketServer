package com.xun.schedule.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xun.schedule.entity.Task;
import com.xun.schedule.enums.TextBlock;
import com.xun.schedule.mapper.TaskMapper;
import com.xun.schedule.util.RedisUtil;
import com.xun.schedule.webScoket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.convert.ConverterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

/**
 * @Author xun
 * @create 2022/10/29 21:23
 */
@Component
@Slf4j
public class DynamicTaskService {

    @Resource
    TaskMapper taskMapper;

    @Resource
    RedisUtil redisUtil;

    public Map<Integer, ScheduledFuture<?>> taskMap = new ConcurrentHashMap<>();
    public List<Integer> taskList = new CopyOnWriteArrayList<>();
    private final ThreadPoolTaskScheduler syncScheduler;
    private final static String commonFormat = TextBlock.commonFormat;
    private final static String message = TextBlock.message;
    private final static String xmlMessage = TextBlock.xmlMessage;

    public DynamicTaskService(ThreadPoolTaskScheduler syncScheduler) {
        this.syncScheduler = syncScheduler;
    }

    /**
     * 查看已开启但还未执行的动态任务
     *
     * @return
     */
    public List<String> getTaskList() {
        ArrayList<String> result = new ArrayList<>();
        for (Integer id : taskList) {
            if ("SUCCESS".equals(this.getState(id))) {
                result.add("任务" + id + "：SUCCESS");
            } else if ("FAILED".equals(this.getState(id))) {
                log.error("任务{}执行失败", id);
                result.add("任务" + id + "：FAILED");
            } else if ("CANCELLED".equals(this.getState(id))) {
                result.add("任务" + id + "：CANCELLED");
            } else {
                result.add("任务" + id + "：RUNNING");
            }
        }
        return result;
    }


    /**
     * 添加一个动态任务
     *
     * @param task
     * @return
     */
    public boolean add(Task task) {
        LocalDateTime now = LocalDateTime.now();
        // 此处的逻辑是 ，如果本周数已经有任务存在，先删除之前的，再添加现在的。（即重复就覆盖）

        if (null != taskMap.get(task.getId()) || redisUtil.hasKey(task.getId().toString()) || taskMapper.exists(
                new LambdaQueryWrapper<Task>().eq(Task::getId, task.getId()))) {
            stop(task.getId());
        }

        // hutool 工具包下的一个转换类型工具类
        ConverterRegistry converterRegistry = ConverterRegistry.getInstance();
        String day = task.getLectureDayOfWeek();
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        // 获取授课时间与当前时间的差
        // todo 如果过了授课周之后还改过去的文档时会出现一些问题
        int days = switch (day.charAt(1)) {
            case '一' -> Math.abs(dayOfWeek.getValue() - 1);
            case '二' -> Math.abs(dayOfWeek.getValue() - 2);
            case '三' -> Math.abs(dayOfWeek.getValue() - 3);
            case '四' -> Math.abs(dayOfWeek.getValue() - 4);
            case '五' -> Math.abs(dayOfWeek.getValue() - 5);
            case '六' -> Math.abs(dayOfWeek.getValue() - 6);
            case '日' -> Math.abs(dayOfWeek.getValue() - 7);
            default -> 0;
        };
        LocalDateTime lecDate = now.plusDays(days);
        // 构造授课开始时间
        LocalDateTime lecTime = LocalDateTime.of(lecDate.getYear(), lecDate.getMonth(), lecDate.getDayOfMonth(),
                Integer.parseInt(task.getLectureTime().substring(0, 2)),
                Integer.parseInt(task.getLectureTime().substring(3, 5)));
        Date startTime = converterRegistry.convert(Date.class, lecTime);

        // schedule :调度给定的Runnable ，在指定的执行时间调用它。
        // 一旦调度程序关闭或返回的ScheduledFuture被取消，执行将结束。
        // 参数：
        // 任务 – 触发器触发时执行的 Runnable
        // startTime – 任务所需的执行时间（如果这是过去，则任务将立即执行，即尽快执行）
        ScheduledFuture<?> schedule = syncScheduler.schedule(getRunnable(task), startTime);
        task.setStartTime(startTime);
        taskList.add(task.getId());
        // 获取授课时间所在的周数
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.set(lecDate.getYear(), lecDate.getMonth().getValue() - 1, lecDate.getDayOfMonth());
        int weekYear = gregorianCalendar.get(Calendar.WEEK_OF_YEAR);
        // 标记这个任务为未执行
        task.setExecute(false);
        task.setWeekYear(weekYear);
        log.info(commonFormat.formatted(task.getGroup(), task.getTopic(), task.getLecturer(), task.getNumOfPeople(),
                task.getMeetingNumber(), task.getMode(), task.getLectureDayOfWeek(), task.getLectureTime(), now));
        // 存数据库持久化数据
        taskMapper.insert(task);
        taskMap.put(task.getId(), schedule);
        redisUtil.set(task.getId().toString(), task);
        return true;
    }

    /**
     * 运行任务
     *
     * @param task
     * @return
     */
    public Runnable getRunnable(Task task) {
        return () -> {
            log.info("---动态定时任务运行---");
            try {
                String send = message.formatted(task.getGroup(), task.getTopic(), task.getLecturer(),
                        task.getNumOfPeople(),
                        task.getMeetingNumber(), task.getMode(), task.getLectureDayOfWeek(), task.getLectureTime(),
                        LocalDateTime.now());
//                String xml = xmlMessage.formatted(task.getGroup(), task.getTopic(), task.getLecturer(),
//                        task.getNumOfPeople(),
//                        task.getMeetingNumber(), task.getMode(), task.getLectureDayOfWeek(), task.getLectureTime(),
//                        LocalDateTime.now());
//                WebSocketServer.sendInfo(xml, "kakabot");
                // todo 判断会话是否存在
                WebSocketServer.sendInfo(send, "kakabot");
                // 任务执行成功则更新数据库
                task.setExecute(true);
                taskMapper.updateById(task);
                redisUtil.set(task.getId().toString(), task);
                taskList.remove(task.getId());
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.info("---end--------");
        };
    }

    /**
     * 停止任务
     * 包括从数据库中删除这些数据
     *
     * @param id 任务id
     * @return 执行结果
     */
    public boolean stop(Integer id) {
        if (null != taskMap.get(id)) {
            ScheduledFuture<?> scheduledFuture = taskMap.get(id);
            scheduledFuture.cancel(true);
            taskMap.remove(id);
        }
        if (taskList.contains(id)) taskList.remove(id);
        taskMapper.delete(new QueryWrapper<Task>().eq("id", id));
        redisUtil.del(id.toString());
        return true;
    }

    /**
     * 获取任务状态
     *
     * @param id
     * @return
     */
    public String getState(Integer id) {
        if (taskMap.get(id) == null) {
            throw new RuntimeException("任务id不存在");
        }
        ScheduledFuture<?> scheduledFuture = taskMap.get(id);
        System.out.println(taskMap.toString());
        return scheduledFuture.state().name();
    }

    /**
     * 在6点到23点时每半小时执行一次
     * 查找是否有遗漏任务，或者防止服务重启漏任务
     */
    @Bean
    @Scheduled(cron = "0 0/15 6-23 * * ?")
    public void ScheduledQuery() {
        log.info("定时查询遗漏任务");
        // 遗漏任务数量
        int nums = 0;
        QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isExecute", 0);
        // 查找未完成的任务
        List<Task> tasks = taskMapper.selectList(queryWrapper);
        if (tasks.isEmpty() && taskList.isEmpty()) {
            log.info("遗漏任务数：{}", nums);
            return;
        } else if (taskList.isEmpty() || tasks.isEmpty()) {
            if (taskList.isEmpty()) { // 如果taskList为空，但是tasks不为空说明漏任务了，需要加上tasks里面的任务
                for (Task task : tasks) {
                    // 先检查redis中有没有这个键，没有再从MySQL中取
                    Task tarTask;
                    if ((tarTask = (Task) redisUtil.get(task.getId().toString())) != null) {
                        this.add(tarTask);
                        nums++;
                    } else {
                        LambdaQueryWrapper<Task> taskWrapper = new LambdaQueryWrapper<>();
                        taskWrapper.eq(Task::getId, task.getId());
                        Task resTask = taskMapper.selectOne(taskWrapper);
                        // 补加任务
                        this.add(resTask);
                        nums++;
                        taskWrapper.clear();
                    }
                }
            } else {
                log.error("数据库数据异常!");
            }
        } else if (tasks.size() != taskList.size()) {
            for (Task value : tasks) {
                // 如果执行任务列表中缺少任务
                if (!taskList.contains(value.getId())) {
                    // 先从redis里面取
                    Task tarTask;
                    if ((tarTask = (Task) redisUtil.get(value.getId().toString())) != null) {
                        this.add(tarTask);
                    } else {
                        LambdaQueryWrapper<Task> taskWrapper = new LambdaQueryWrapper<>();
                        taskWrapper.eq(Task::getId, value.getId());
                        Task task = taskMapper.selectOne(taskWrapper);
                        this.add(task);
                        nums++;
                    }
                }
            }
        }
        log.info("遗漏任务数：{}", nums);
    }
}
