package fun.gatsby.sbimgutil.context;

import cn.hutool.core.util.ReflectUtil;
import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ITask;
import fun.gatsby.sbimgutil.schedule.ProcessTaskGroup;
import fun.gatsby.sbimgutil.schedule.TaskGroup;
import fun.gatsby.sbimgutil.schedule.TaskScheduleForkJoinPool;
import fun.gatsby.sbimgutil.task.*;
import fun.gatsby.sbimgutil.task.builder.TaskBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

@Slf4j
public class TaskExecutor {

    private final AppConfig.GlobalTaskConfig gtc;
    private final TaskGroup<Runnable> taskGroup;
    ForkJoinPool forkJoinPool;

    public TaskExecutor(AppConfig.GlobalTaskConfig gtc, Map.Entry<TaskEnum, Map<String,Object>> entry) throws IOException {
        this.forkJoinPool = new TaskScheduleForkJoinPool(gtc.getThreads());
        this.gtc=gtc;
        taskGroup=loadTasks(entry);
        if (Objects.equals(gtc.getInDir(),gtc.getOutDir()))
            throw new IOException("输入输出目录不能相同");
    }

    public TaskExecutor(
            AppConfig.GlobalTaskConfig gtc,
            Map.Entry<TaskEnum, Map<String,Object>> taskEnumToConfigMapEntry,
            Runnable funcPerTaskDone
    ) throws IOException {
        this.forkJoinPool = new TaskScheduleForkJoinPool(gtc.getThreads());
        this.gtc=gtc;
        TaskEnum taskType = taskEnumToConfigMapEntry.getKey();
        var configMap = taskEnumToConfigMapEntry.getValue();
        var taskGroup = new ProcessTaskGroup(taskType.getCnName(),funcPerTaskDone);

        var tasks = taskType.getBuilder(gtc, configMap).build();
        taskGroup.addAll(tasks);
        this.taskGroup=taskGroup;
    }

    public TaskGroup<Runnable> loadTasks(Map.Entry<TaskEnum, Map<String,Object>> taskEnumToConfigMapEntry) throws IOException {
        TaskEnum taskType = taskEnumToConfigMapEntry.getKey();
        var configMap = taskEnumToConfigMapEntry.getValue();
        var taskGroup = new ProcessTaskGroup(taskType.getCnName());
        var tasks = taskType.getBuilder(gtc, configMap).build();
        taskGroup.addAll(tasks);
        return taskGroup;
    }

    public void excute() throws ExecutionException, InterruptedException {
        log.info("任务总数：{}",taskGroup.size());
        log.info("启动参数{}",gtc);
        ForkJoinTask<?> forkJoinTask =
                this.forkJoinPool.submit(() -> taskGroup.parallelStream().forEach(Runnable::run));
        forkJoinTask.get();
    }

    public int getTaskCount(){
        return taskGroup.size();
    }
}
