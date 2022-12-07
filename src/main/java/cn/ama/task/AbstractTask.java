package cn.ama.task;


import cn.ama.task.mapper.SysTaskLogMapper;
import cn.ama.task.mapper.SysTaskMapper;
import cn.ama.task.model.SysTask;
import cn.ama.task.model.SysTaskLog;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.system.SystemUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AbstractTask {

    @Resource
    RedissonClient redissonClient;

    @Resource
    SysTaskMapper sysTaskMapper;

    @Resource
    SysTaskLogMapper sysTaskLogMapper;

    private String taskName;

    private SysTask sysTask;

    private Exception exception;

    public final void run(String taskName) {
        this.taskName = taskName;

        if (!before()) {
            return;
        }

        String executeBean = sysTask.getExecuteBean();

        String[] args = null;
        int i = StrUtil.indexOf(executeBean, '(');
        if (i > 0) {
            String params = StrUtil.sub(executeBean, i + 1, executeBean.length() - 1);
            List<String> split = StrUtil.split(params, ",");
            args = new String[split.size()];
            args = split.toArray(args);
        }

        if (sysTask.getIsConcurrent()) {
            scheduleWithRetry(args);
        } else {
            RLock taskLock = redissonClient.getLock(lockName());
            boolean haveLock = taskLock.tryLock();
            try {
                if (!haveLock) {
                    return;
                }
                scheduleWithRetry(args);
            } finally {
                taskLock.forceUnlock();
            }
        }
        after();
    }

    private void scheduleWithRetry(String... args) {
        for (int i = -1; i < sysTask.getRetryTime(); i++) {
            try {
                this.runTask(args);
                break;
            } catch (Exception e) {
                this.exception = e;
                saveLog();
                try {
                    TimeUnit.MILLISECONDS.sleep(1);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    private boolean before() {
        SysTask sysTask = sysTaskMapper.selectById(taskName);
        this.sysTask = sysTask;
        return sysTask.getTaskEnable();
    }

    private void after() {
        if (this.exception == null) {
            saveLog();
        }
    }

    private void saveLog() {
        Exception innerException = this.exception;
        SysTaskLog sysTaskLog = new SysTaskLog();

        sysTaskLog.setServerName(SystemUtil.getHostInfo().getName());
        sysTaskLog.setTaskName(taskName);
        sysTaskLog.setScheduleId(Dispatch.TASK_SCHEDULE_MAPPING.get(taskName));
        sysTaskLog.setScheduleTime(new Date());
        if (innerException == null) {
            sysTaskLog.setSuccess(true);
        } else {
            sysTaskLog.setSuccess(false);
            sysTaskLog.setExceptionMessage(innerException.getMessage());
            sysTaskLog.setExceptionStack(JSONUtil.toJsonStr(Arrays.copyOf(innerException.getStackTrace(), 5)));
        }
        sysTaskLogMapper.insert(sysTaskLog);
    }

    private String lockName() {
        return MessageFormat.format("SYS:TASK:CONCURRENT:LOCK:{0}", taskName);
    }

    public abstract void runTask(String... args) throws Exception;
}
