package com.example.sbimgutil.context;

import com.example.sbimgutil.config.AppConfig;
import com.example.sbimgutil.schedule.MyTaskJoinPool;
import com.example.sbimgutil.schedule.TaskGroup;
import com.example.sbimgutil.task.ImageTransformTask;
import com.example.sbimgutil.task.PdfMergeTask;
import com.example.sbimgutil.utils.FileFetchUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class TaskExxcutor {

    private final AppConfig appConfig;

    MyTaskJoinPool myTaskJoinPool = new MyTaskJoinPool(4);

    public TaskExxcutor(AppConfig appConfig){
        this.appConfig = appConfig;
    }

    public void execute(){
        List<AppConfig.ProcessTask> taskConfigs = appConfig.getEnabledProcessTaskConfigs();
        for (AppConfig.ProcessTask taskConfig : taskConfigs) {
            String taskType = taskConfig.getTaskType();
            TaskTypeEnum taskTypeEnum = TaskTypeEnum.valueOf(taskConfig.getTaskType());
            var sortedVolumeToImgFilesMap = loadSortedVolumeToImgFilesMap(taskConfig);
            switch (taskTypeEnum){
                case IMAGE_TRANSFORM:
                    processIT(sortedVolumeToImgFilesMap,taskConfig);
                    break;
                case PDF_MERGE:
                    procesPM(sortedVolumeToImgFilesMap,taskConfig);
                    break;
                case IMAGE_COMPRESS:
                    procesPM();
                    break;
                case DRAWING_BLUR:
                    break;
            }
        }
    }

    LinkedHashMap<String, List<File>> loadSortedVolumeToImgFilesMap(AppConfig.ProcessTask taskConfig) {
        File inDir = new File(taskConfig.getInDirPath());
        String outDirPath = taskConfig.getOutDirPath();
        List<File> imgFiles = new LinkedList<>();

        // TODO: 2023/3/2 靠检查点过滤文件
        FileFetchUtils.fetchFileRecursively(imgFiles,inDir,
                VolumeDirProcessTask.supported_file_filter
        );
        //todo 分组添加任务
        imgFiles = imgFiles.stream().sorted(Comparator.comparing(File::getName)).collect(Collectors.toList());

        Map<String, List<File>> volumeToImgFilesMap = imgFiles.parallelStream().collect(Collectors.groupingBy(e -> {
            File volumeDir = e.getParentFile();
            File bookDir = volumeDir.getParentFile();
            return bookDir.getName() + '/' + volumeDir.getName();
        }));

        List<Map.Entry<String, List<File>>> sortedEntries = volumeToImgFilesMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).toList();
        return new LinkedHashMap<>(sortedEntries.size()) {{
            sortedEntries.forEach(e -> put(e.getKey(), e.getValue()));
        }};
    }

    private void procesPM(LinkedHashMap<String, List<File>> sortedVolumeToImgFilesMap, AppConfig.ProcessTask taskConfig) {
        String outDirPath = taskConfig.getOutDirPath();
        String format = taskConfig.getFormat();
        LinkedList<Runnable> tasks = new LinkedList<>();
        for (Map.Entry<String, List<File>> entry : sortedVolumeToImgFilesMap.entrySet()) {
            String volumeKey = entry.getKey();
            List<File> files = entry.getValue();
            File outFile= genPdfout();
            //检查点问题
            Pdf imageTransformTask = new PdfMergeTask(files, outFile, format);
            taskGroup.add(imageTransformTask);
        }
        myTaskJoinPool.scheduleBatch(tasks);
    }

    private void processIT(LinkedHashMap<String, List<File>> sortedVolumeToImgFilesMap, AppConfig.ProcessTask taskConfig) {
        String outDirPath = taskConfig.getOutDirPath();
        String format = taskConfig.getFormat();
        for (Map.Entry<String, List<File>> entry : sortedVolumeToImgFilesMap.entrySet()) {
            String volumeKey = entry.getKey();
            List<File> files = entry.getValue();
            TaskGroup<Runnable> taskGroup = new TaskGroup<>();
            taskGroup.setTaskAfterAllDone(() -> {
                //检查点 fixme 颗粒度问题
                System.out.println("任务组"+volumeKey+"执行完毕");
            });
            for (File file : files) {
                File outFile = genOutFile(file, outDirPath, format);
                ImageTransformTask imageTransformTask = new ImageTransformTask(file, outFile, format);
                taskGroup.add(imageTransformTask);
            }
            myTaskJoinPool.scheduleBatch(taskGroup);
        }

            //找到依赖他的任务，并将自己加入到依赖他的任务的依赖队列中
            // TODO: 2023/2/26 图片处理任务依赖问题，批处理任务依赖问题
//                        taskConfig.getTaskDepentOn().
//                        tasks.addLast(taskConfig.getTaskDepentOn().getTask());
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