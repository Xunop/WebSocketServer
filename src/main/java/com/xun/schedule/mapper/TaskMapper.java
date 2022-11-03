package com.xun.schedule.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xun.schedule.entity.Task;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author xun
 * @create 2022/10/29 14:53
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {
}
