package cc.xun.schedule.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author xun
 * @create 2022/10/29 15:21
 */
@Data
@TableName("schedule")
public class Time {

    @TableId(type = IdType.AUTO)
    Integer id;

    LocalDateTime time;

    String text;
}
