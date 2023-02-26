package com.example.sbimgutil.context;

import com.example.sbimgutil.config.AppConfig;
import com.example.sbimgutil.task.BaseTask;
import com.example.sbimgutil.task.ImageTransformTask;
import com.example.sbimgutil.utils.FileFetchUtils;

import java.io.File;
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
                    File inDir = new File(taskConfig.getInDirPath());
                    String outDirPath = taskConfig.getOutDirPath();
                    String format = taskConfig.getFormat();
                    LinkedList<File> imgFiles = new LinkedList<>();
                    FileFetchUtils.fetchFileRecursively(imgFiles,inDir,
                            VolumeDirProcessTask.supported_file_filter
                            );
                    for (File imgFile : imgFiles) {
                        File outFile = genOutFile(imgFile, outDirPath, format);
                        var task = new ImageTransformTask(imgFile,outFile,format);
                        tasks.add(task);
                    }
                    if(taskConfig.getTaskDepentOn() != null){
                        //找到依赖他的任务，并将自己加入到依赖他的任务的依赖队列中
                        // TODO: 2023/2/26 图片处理任务依赖问题，批处理任务依赖问题
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
    File genOutFile(File inFile, String outDirPath, String format){
        // FIXME: 2023/2/26
        String inFileName = inFile.getName();
        String outFileName = inFileName.substring(0,inFileName.lastIndexOf(".")) + "." + format;
        return new File(outDirPath + File.separator + outFileName);
    }
}
enum TaskTypeEnum{
    IMAGE_TRANSFORM,
    PDF_MERGE,
    IMAGE_COMPRESS,
    DRAWING_BLUR
}