package fun.gatsby.sbimgutil.context;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ITask;
import fun.gatsby.sbimgutil.schedule.ProcessTaskGroup;
import fun.gatsby.sbimgutil.schedule.TaskGroup;
import fun.gatsby.sbimgutil.schedule.TaskScheduleForkJoinPool;
import fun.gatsby.sbimgutil.utils.ConsoleProgressBar;
import fun.gatsby.sbimgutil.task.*;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Consumer;

@Slf4j
public class TaskExecutor {

    private final AppConfig.GlobalTaskConfig gtc;
    private final TaskGroup<Runnable> taskGroup;
    ForkJoinPool forkJoinPool;
    public static ConsoleProgressBar CPB;

    public TaskExecutor(AppConfig.GlobalTaskConfig gtc, Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> entry) throws IOException {
        this.forkJoinPool = new TaskScheduleForkJoinPool(gtc.getMaxWorkerNum());
        this.gtc=gtc;
        taskGroup=loadTasks(entry);
    }

    public TaskExecutor(
            AppConfig.GlobalTaskConfig gtc,
            Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> entry,
            Runnable funcPerTaskDone,
            Consumer<String> doneConsumer
    ) throws IOException {
        this.forkJoinPool = new TaskScheduleForkJoinPool(gtc.getMaxWorkerNum());
        this.gtc=gtc;
        TaskTypeEnum taskType = entry.getKey();
        AppConfig.ProcessTask processTask = entry.getValue();
        var taskGroup = new ProcessTaskGroup(taskType.taskCnName,funcPerTaskDone,doneConsumer);
        BaseTask.TaskGenerator taskGenerator = taskType.newTaskGenerator(gtc, processTask);
        if(taskGenerator!=null){
            taskGroup.addAll(taskGenerator.generate());
        }
        this.taskGroup=taskGroup;
    }

    public TaskGroup<Runnable> loadTasks(Map.Entry<TaskTypeEnum, AppConfig.ProcessTask> entry) throws IOException {
        TaskTypeEnum taskTypeEnum = entry.getKey();
        AppConfig.ProcessTask processTask = entry.getValue();
        var taskGroup = new ProcessTaskGroup(taskTypeEnum.taskCnName);
        BaseTask.TaskGenerator taskGenerator = taskTypeEnum.newTaskGenerator(gtc, processTask);
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
    public void excuteAsync() {
        forkJoinPool.submit(() -> taskGroup.parallelStream().forEach(Runnable::run));
    }

    public int getTaskCount(){
        return taskGroup.size();
    }
}
