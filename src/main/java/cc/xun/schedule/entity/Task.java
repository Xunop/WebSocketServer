package cc.xun.schedule.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author xun
 * @create 2022/10/29 21:43
 */
@Data
@TableName("task")
public class Task implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.INPUT)
    private Integer id;

    /**
     * 当前周数
     */
    private Integer weekYear;


    /**
     * 设课组
     */
    @TableField("`group`")
    private String group;

    /**
     * 授课主题
     */
    private String topic;

    /**
     * 主讲人
     */
    private String lecturer;

    /**
     * 预计人数
     */
    private Integer numOfPeople;

    /**
     * 会议号
     */
    private String meetingNumber;

    /**
     * 授课形式
     */
    // todo
    @TableField("`mode`")
    private String mode;

    /**
     * 授课日期
     * 只有周几的形式
     */
    private String lectureDayOfWeek;

    /**
     * 授课时间
     * 精确到点
     */
    private String lectureTime;

    /**
     * 是否执行这个任务
     */
    private boolean isExecute;

    /**
     * 任务执行次数
     */

    private Integer exeNums;

    /**
     * 任务需要执行的时间
     */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime startTime;
}
