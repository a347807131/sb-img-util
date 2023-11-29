package fun.gatsby.sbimgutil.context;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ProcessTaskGroup;
import fun.gatsby.sbimgutil.schedule.TaskScheduleForkJoinPool;
import fun.gatsby.sbimgutil.utils.ConsoleProgressBar;
import fun.gatsby.sbimgutil.utils.Const;
import fun.gatsby.sbimgutil.utils.FileFetchUtils;
import fun.gatsby.sbimgutil.utils.Label;
import fun.gatsby.sbimgutil.task.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Slf4j
public class TaskExecutor {

    private final AppConfig.GlobalTaskConfig gtc;
    private final Map<String, AppConfig.ProcessTask> taskMap;
    TaskScheduleForkJoinPool forkJoinPool;
    public static ConsoleProgressBar CPB;

    public TaskExecutor(AppConfig.GlobalTaskConfig gtc, Map<String, AppConfig.ProcessTask> taskMap, TaskTypeEnum...taskTypes) throws IOException {
        this.forkJoinPool = new TaskScheduleForkJoinPool(gtc.getMaxWorkerNum());
        this.gtc=gtc;
        this.taskMap=taskMap;
        for (TaskTypeEnum taskType : taskTypes) {
            List<Runnable> tasks = loadTasks(taskType);
            this.forkJoinPool.scheduleBatch(tasks);
        }
        CPB = new ConsoleProgressBar(this.forkJoinPool.getTaskCount());
    }


    public List<Runnable> loadTasks(TaskTypeEnum taskType) throws IOException {
        final AppConfig.ProcessTask processTask=taskMap.get(taskType.name());
        var taskGroup = new ProcessTaskGroup(taskType.taskCnName);
        BaseTask.TaskGenerator taskGenerator = taskType.newTaskGenerator(gtc, processTask);
        if(taskGenerator!=null){
            taskGroup.addAll(taskGenerator.generate());
        }
        return taskGroup;
    }

    public void excute() throws ExecutionException, InterruptedException {
        forkJoinPool.start();
    }

    public static ConsoleProgressBar getGlobalConsoleProgressBar() {
        return CPB;
    }
}
