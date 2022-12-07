package cn.ama.task.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
@TableName(value = "sys_task_log")
public class SysTaskLog implements Serializable {
    public static final String COL_SUCESS = "sucess";
    @TableField(value = "`server_name`")
    private String serverName;

    @TableField(value = "task_name")
    private String taskName;

    @TableField(value = "schedule_id")
    private String scheduleId;

    @TableField(value = "success")
    private Boolean success;

    @TableField(value = "schedule_time")
    private Date scheduleTime;

    @TableField(value = "exception_message")
    private String exceptionMessage;

    @TableField(value = "exception_stack")
    private String exceptionStack;

    private static final long serialVersionUID = 1L;

    public static final String COL_SERVER_NAME = "server_name";

    public static final String COL_TASK_NAME = "task_name";

    public static final String COL_SCHEDULE_ID = "schedule_id";

    public static final String COL_SUCCESS = "success";

    public static final String COL_SCHEDULE_TIME = "schedule_time";

    public static final String COL_EXCEPTION_MESSAGE = "exception_message";

    public static final String COL_EXCEPTION_STACK = "exception_stack";
}