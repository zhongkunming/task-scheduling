package cn.ama.task;

import cn.ama.task.mapper.SysTaskMapper;
import cn.ama.task.model.SysTask;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.pattern.parser.PatternParser;
import cn.hutool.cron.task.Task;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class Dispatch implements ApplicationListener<ApplicationReadyEvent> {

    public static final Map<String, String> TASK_SCHEDULE_MAPPING = new ConcurrentHashMap<>();

    @Resource
    private SysTaskMapper sysTaskMapper;

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        loadCron();
        enableAutoRefreshCron();
        startCron();
    }


    private void loadCron() {
        List<SysTask> tasks = sysTaskMapper.selectList(new QueryWrapper<>());
        if (CollectionUtil.isEmpty(tasks)) {
            return;
        }

        CronUtil.setMatchSecond(true);
        for (SysTask task : tasks) {
            checkCron(task.getTaskCron());
            addTask(task);
        }

    }

    private void enableAutoRefreshCron() {
        CronUtil.schedule("0 55 23 * * *", (Task) () -> {
            new Thread(() -> {
                log.info("刷新任务管理器");
                CronUtil.stop();
                loadCron();
                enableAutoRefreshCron();
                startCron();
                log.info("刷新任务管理器成功");
            }).start();
        });
    }

    private void addTask(SysTask task) {
        String scheduleId = CronUtil.schedule(task.getTaskCron(), (Task) () -> {
            String executeBean = task.getExecuteBean();
            String beanName;
            int i = StrUtil.indexOf(executeBean, '(');
            if (i > 0) {
                beanName = StrUtil.sub(executeBean, 0, i);
            } else {
                beanName = executeBean;
            }
            AbstractTask actuallyTask = SpringUtil.getBean(beanName);
            actuallyTask.run(task.getTaskName());
        });
        TASK_SCHEDULE_MAPPING.put(task.getTaskName(), scheduleId);
        log.info("当前 JVM taskName: {} -> scheduleId: {}", task.getTaskName(), scheduleId);
    }

    private void startCron() {
        CronUtil.start(true);
    }


    private boolean checkCron(String cron) {
        try {
            PatternParser.parse(cron);
        } catch (Exception e) {
            log.info("Cron 表达式验证未通过 : {}", cron);
            throw new RuntimeException("Cron 表达式验证未通过, " + cron);
        }
        return true;
    }
}
