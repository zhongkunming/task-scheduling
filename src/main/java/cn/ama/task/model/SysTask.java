package cn.ama.task.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

@Data
@TableName(value = "sys_task")
public class SysTask implements Serializable {
    @TableId(value = "task_name", type = IdType.INPUT)
    private String taskName;

    @TableField(value = "task_cron")
    private String taskCron;

    @TableField(value = "execute_bean")
    private String executeBean;

    @TableField(value = "task_enable")
    private Boolean taskEnable;

    @TableField(value = "retry_time")
    private Integer retryTime;

    @TableField(value = "is_concurrent")
    private Boolean isConcurrent;

    private static final long serialVersionUID = 1L;

    public static final String COL_TASK_NAME = "task_name";

    public static final String COL_TASK_CRON = "task_cron";

    public static final String COL_EXECUTE_BEAN = "execute_bean";

    public static final String COL_TASK_ENABLE = "task_enable";

    public static final String COL_RETRY_TIME = "retry_time";

    public static final String COL_IS_CONCURRENT = "is_concurrent";
}