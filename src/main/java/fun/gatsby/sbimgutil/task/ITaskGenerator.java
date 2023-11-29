package fun.gatsby.sbimgutil.task;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ITask;

import java.io.IOException;
import java.util.List;

public interface ITaskGenerator {
    List<ITask> generate() throws IOException ;
    TaskTypeEnum getTaskType();
}
