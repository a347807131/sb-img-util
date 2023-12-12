package fun.gatsby.sbimgutil.schedule;

import cn.hutool.core.date.LocalDateTimeUtil;
import fun.gatsby.sbimgutil.utils.ConsoleProgressBar;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class ProcessTaskGroup extends TaskGroup<Runnable> {

    LocalDateTime startDateTime;

    public ProcessTaskGroup(String name) {
        super();
        this.name = name;

        preTask = () -> {
            log.debug("任务组:[{}] 开始执行，共计任务数[{}]", name,size());
            startDateTime = LocalDateTime.now();
        };

        postTask = () -> {
            long between = LocalDateTimeUtil.between(startDateTime, LocalDateTime.now(), ChronoUnit.SECONDS);
            String hms = ConsoleProgressBar.genHMS(between);
            log.info("任务组:[{}]  执行完毕，共计任务数：{}, 总共耗时: {}, 平均单个任务耗时: {} s, 出错任务数:{}",
                    name, size(),hms, between / this.size(),errTasks.size()
            );
            if(!errTasks.isEmpty())
                log.warn("任务组:[{}] 出错任务统计,共计:{}条,详情如下:\n{}",
                        name,
                        errTasks.size(),
                        String.join("\n", errTasks)
                );
        };
    }

    public ProcessTaskGroup(String name, Runnable funcPerTaskDone) {
        this(name);
        this.taskPerDone = funcPerTaskDone;
    }
    public ProcessTaskGroup(String name, Runnable funcPerTaskDone,Consumer<String> postTask) {
        this(name);
        this.taskPerDone = funcPerTaskDone;
        var oldPostTask=this.postTask;
        this.postTask=()->{
            oldPostTask.run();
            long between = LocalDateTimeUtil.between(startDateTime, LocalDateTime.now(), ChronoUnit.SECONDS);
            String hms = ConsoleProgressBar.genHMS(between);
            String msg = String.format("任务组:[%s]  执行完毕，共计任务数：%d, 总共耗时: %s, 平均单个任务耗时: %d s, 出错任务数:%d",
                    name, size(), hms, between / this.size(), errTasks.size()
            );
            postTask.accept(msg);
        };
    }

    List<String> errTasks = new LinkedList<>();

    @Override
    protected void onTaskException(Runnable task, Exception e) {
        String line=task.toString() +" 失败原因:"+e.getMessage();
        errTasks.add(line);
    }

}
