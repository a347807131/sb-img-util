package fun.gatsby.sbimgutil.schedule;

import cn.hutool.core.date.LocalDateTimeUtil;
import fun.gatsby.sbimgutil.utils.ConsoleProgressBar;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;

@Slf4j
public class ProcessTaskGroup extends TaskGroup<Runnable> {

    LocalDateTime startDateTime;

    public ProcessTaskGroup(String name) {
        super();
        this.name = name;

        preTask = () -> {
            log.debug("name:{} 开始执行", name);
            startDateTime = LocalDateTime.now();
        };
        postTask = () -> {
            long between = LocalDateTimeUtil.between(startDateTime, LocalDateTime.now(), ChronoUnit.SECONDS);
            String hms = ConsoleProgressBar.genHMS(between);
            log.info("任务组: {} 执行完毕，总共耗时: {}, 平均单个任务耗时: {} s", name, hms, between / this.size());
        };
    }


    LinkedList<String> errTasks = new LinkedList<>();
    @Override
    protected void onTaskException(Runnable task, Exception e) {
    }
}
