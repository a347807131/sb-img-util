package com.example.sbimgutil.context;

import com.example.sbimgutil.config.AppConfig;
import com.example.sbimgutil.task.ImageTransformTask;

import java.util.LinkedList;
import java.util.List;

public class TaskExxcutor {

    private final AppConfig appConfig;

    public TaskExxcutor(AppConfig appConfig){
        this.appConfig = appConfig;
    }

    public void execute(){
        List<AppConfig.ProcessTask> taskConfigs = appConfig.getEnabledProcessTaskConfigs();
        for (AppConfig.ProcessTask taskConfig : taskConfigs) {
            String taskType = taskConfig.getTaskType();
            TaskTypeEnum taskTypeEnum = TaskTypeEnum.valueOf(taskConfig.getTaskType());
            LinkedList<Runnable> tasks = new LinkedList<>();
            switch (taskTypeEnum){
                case IMAGE_TRANSFORM:
                    ImageTransformTask task = new ImageTransformTask(taskConfig);
                    if(taskConfig.getTaskDepentOn() != null){
//                        taskConfig.getTaskDepentOn().
//                        tasks.addLast(taskConfig.getTaskDepentOn().getTask());
                    }
                    break;
                case PDF_MERGE:
                    break;
                case IMAGE_COMPRESS:
                    break;
                case DRAWING_BLUR:
                    break;
            }
        }
    }
}
enum TaskTypeEnum{
    IMAGE_TRANSFORM,
    PDF_MERGE,
    IMAGE_COMPRESS,
    DRAWING_BLUR
}