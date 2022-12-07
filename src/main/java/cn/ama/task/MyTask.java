package cn.ama.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("myTask")
public class MyTask extends AbstractTask {
    public void runTask(String... args) throws Exception {
        log.info(" Task run {}", args);
    }
}
