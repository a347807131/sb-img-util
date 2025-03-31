package fun.gatsby.sbimgutil.task;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ITask;

public class ProcessManager<T extends ITask> {
    AppConfig.GlobalTaskConfig gtc;
    AppConfig.ProcessTask processTask;

    public ProcessManager(AppConfig.GlobalTaskConfig gtc, AppConfig.ProcessTask processTask) {
        this.gtc = gtc;
        this.processTask = processTask;
    }
}

