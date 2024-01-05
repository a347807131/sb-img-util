package fun.gatsby.sbimgutil.context;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ProcessTaskGroup;
import fun.gatsby.sbimgutil.schedule.TaskGroup;
import fun.gatsby.sbimgutil.schedule.TaskScheduleForkJoinPool;
import fun.gatsby.sbimgutil.task.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.function.IntConsumer;

@Slf4j
public class TaskExecutor {

    private final AppConfig.GlobalTaskConfig gtc;
    private final TaskGroup<Runnable> taskGroup;
    ForkJoinPool forkJoinPool;

    public TaskExecutor(AppConfig.GlobalTaskConfig gtc, Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> entry) throws IOException {
        this.forkJoinPool = new TaskScheduleForkJoinPool(gtc.getMaxWorkerNum());
        this.gtc=gtc;
        taskGroup=loadTasks(entry);
    }

    public TaskExecutor(
            AppConfig.GlobalTaskConfig gtc,
            Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> entry,
            IntConsumer setTaskCountBeforeExcutionComsumer,
            Runnable funcPerTaskDone
    ) throws IOException {
        this.forkJoinPool = new TaskScheduleForkJoinPool(gtc.getMaxWorkerNum());
        this.gtc=gtc;
        TaskTypeEnum taskType = entry.getKey();
        AppConfig.ProcessTask processTask = entry.getValue();
        var taskGroup = new ProcessTaskGroup(taskType.taskCnName,funcPerTaskDone);
        BaseTaskGenerator taskGenerator = taskType.newTaskGenerator(gtc, processTask);
        if(taskGenerator!=null){
            taskGroup.addAll(taskGenerator.generate());
        }
        this.taskGroup=taskGroup;
        setTaskCountBeforeExcutionComsumer.accept(taskGroup.size());
    }

    public TaskGroup<Runnable> loadTasks(Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> entry) throws IOException {
        TaskTypeEnum taskTypeEnum = entry.getKey();
        AppConfig.ProcessTask processTask = entry.getValue();
        var taskGroup = new ProcessTaskGroup(taskTypeEnum.taskCnName);
        BaseTaskGenerator taskGenerator = taskTypeEnum.newTaskGenerator(gtc, processTask);
        if(taskGenerator!=null){
            taskGroup.addAll(taskGenerator.generate());
        }
        return taskGroup;
    }

    public void excute() throws ExecutionException, InterruptedException {
        ForkJoinTask<?> forkJoinTask =
                this.forkJoinPool.submit(() -> taskGroup.parallelStream().forEach(Runnable::run));
        forkJoinTask.get();
    }

    public int getTaskCount(){
        return taskGroup.size();
    }
}
