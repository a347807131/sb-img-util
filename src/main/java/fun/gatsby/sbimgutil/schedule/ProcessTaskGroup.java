package fun.gatsby.sbimgutil.schedule;

import cn.hutool.core.date.LocalDateTimeUtil;
import fun.gatsby.sbimgutil.utils.ConsoleProgressBar;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.stream.Collectors;

@Slf4j
public class ProcessTaskGroup extends TaskGroup<Runnable> {

    LocalDateTime startDateTime;

    public ProcessTaskGroup(String name) {
        super();
        this.name = name;

        preTask = () -> {
            log.debug("任务组 :{} 开始执行", name);
            startDateTime = LocalDateTime.now();
        };

        postTask = () -> {
            long between = LocalDateTimeUtil.between(startDateTime, LocalDateTime.now(), ChronoUnit.SECONDS);
            String hms = ConsoleProgressBar.genHMS(between);
            log.info("任务组: {} 执行完毕，总共耗时: {}, 平均单个任务耗时: {} s", name, hms, between / this.size());
            if(!errTasks.isEmpty())
                log.warn("任务组: {} 出错任务统计,共计:{}条,详情如下:\n{}",
                        name,
                        errTasks.size(),
                        String.join("\n", errTasks)
                );
        };
    }


    LinkedList<String> errTasks = new LinkedList<>();
    @Override
    protected void onTaskException(Runnable task, Exception e) {
        String line=task.toString() +" 失败原因:"+e.getMessage();
        errTasks.add(line);
    }

}
