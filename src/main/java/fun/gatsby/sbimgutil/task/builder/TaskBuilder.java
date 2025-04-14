package fun.gatsby.sbimgutil.task.builder;

import fun.gatsby.sbimgutil.schedule.ITask;
import fun.gatsby.sbimgutil.task.TaskTypeEnum;

import java.io.IOException;
import java.util.List;

public interface TaskBuilder<T extends ITask> {
    List<T> build() throws IOException;

    Class<T> getTaskClass();
}
