package com.example.sbimgutil.context;

import com.example.sbimgutil.config.AppConfig;
import com.example.sbimgutil.schedule.MyTaskJoinPool;
import com.example.sbimgutil.schedule.TaskGroup;
import com.example.sbimgutil.task.BaseTask;
import com.example.sbimgutil.task.ImageTransformTask;
import com.example.sbimgutil.task.PdfMergeTask;
import com.example.sbimgutil.utils.FileFetchUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TaskExcutor {

    private final AppConfig appConfig;

    MyTaskJoinPool myTaskJoinPool = new MyTaskJoinPool(4);

    public TaskExcutor(AppConfig appConfig){
        this.appConfig = appConfig;
    }

    public void execute() throws ExecutionException, InterruptedException, IOException {
        Map<String, AppConfig.ProcessTask> taskMap = appConfig.getProcessTasks();
        for (Map.Entry<String, AppConfig.ProcessTask> entry : taskMap.entrySet()) {

            AppConfig.ProcessTask taskConfig = entry.getValue();
            String taskType = taskConfig.getTaskType();
            TaskTypeEnum taskTypeEnum = TaskTypeEnum.valueOf(taskType);
            List<Runnable> tasks =null;
            switch (taskTypeEnum){
                case IMAGE_TRANSFORM:
                    tasks=processIT(taskConfig);
                    break;
                case PDF_MERGE:
                    tasks=procesPM(taskConfig);
                    break;
                case IMAGE_COMPRESS:
                    tasks=procesIC(taskConfig);
                    break;
                case DRAWING_BLUR:
                    break;
            }
            myTaskJoinPool.scheduleBatch(tasks);
        }
        myTaskJoinPool.start();
    }

    private List<Runnable> procesIC(AppConfig.ProcessTask taskConfig) {
        return null;
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
                .sorted(Map.Entry.comparingByKey()).collect(Collectors.toList());
        return new LinkedHashMap<>(sortedEntries.size()) {{
            sortedEntries.forEach(e -> put(e.getKey(), e.getValue()));
        }};
    }

    private List<Runnable> procesPM(AppConfig.ProcessTask taskConfig) {
//        String outDirPath = taskConfig.getOutDirPath();
//        String format = taskConfig.getFormat();
//        LinkedList<Runnable> tasks = new LinkedList<>();
//        for (Map.Entry<String, List<File>> entry : sortedVolumeToImgFilesMap.entrySet()) {
//            String volumeKey = entry.getKey();
//            List<File> files = entry.getValue();
//            File outFile= genPdfout();
//            //检查点问题
//            PdfMergeTask pdfMergeTask = new PdfMergeTask(files, outFile, format);
//            // TODO: 2023/3/5
//        }
        return null;
    }

    // TODO: 2023/3/5  
    private File genPdfout() {
        return null;
    }

    private List<Runnable> processIT(AppConfig.ProcessTask taskConfig) throws IOException {
        String outDirPath = taskConfig.getOutDirPath();
        String inDirPath = taskConfig.getInDirPath();
        String format = taskConfig.getFormat();
        File inDir = new File(inDirPath);
        List<File> imgFiles = new LinkedList<>();
        FileFetchUtils.fetchFileRecursively(imgFiles,inDir,
                VolumeDirProcessTask.supported_file_filter
        );
        imgFiles.sort(Comparator.comparing(File::getName));

        TaskGroup<Runnable> taskGroup = new TaskGroup<>() {
        };
        //todo 失败的任务需要将信息保存下来，以便后续处理
        for (File imgFile : imgFiles) {
            File outFile = genOutFile(imgFile, outDirPath, format);
            ImageTransformTask imageTransformTask = new ImageTransformTask(imgFile, outFile, format);
            taskGroup.add(imageTransformTask);
        }
        return taskGroup;
//        for (Map.Entry<String, List<File>> entry : sortedVolumeToImgFilesMap.entrySet()) {
//            String volumeKey = entry.getKey();
//            List<File> files = entry.getValue();
//            TaskGroup<Runnable> taskGroup = new TaskGroup<>();
//            taskGroup.setTaskAfterAllDone(() -> {
//                //检查点 fixme 颗粒度问题
//                System.out.println("任务组"+volumeKey+"执行完毕");
//            });
//            for (File file : files) {
//                File outFile = genOutFile(file, outDirPath, format);
//                ImageTransformTask imageTransformTask = new ImageTransformTask(file, outFile, format);
//                taskGroup.add(imageTransformTask);
//            }
//        }

            //找到依赖他的任务，并将自己加入到依赖他的任务的依赖队列中
            // TODO: 2023/2/26 图片处理任务依赖问题，批处理任务依赖问题
//                        taskConfig.getTaskDepentOn().
//                        tasks.addLast(taskConfig.getTaskDepentOn().getTask());
    }

    File genOutFile(File inFile, String outDirPath, String format) throws IOException {
        // FIXME: 2023/2/26
        String inFileName = inFile.getName();
        String outFileName = inFileName.substring(0,inFileName.lastIndexOf(".")) + "." + format;
        File outFile = new File(outDirPath, outFileName);
        FileUtils.forceMkdirParent(outFile);
        return outFile;
    }
}
enum TaskTypeEnum{
    IMAGE_TRANSFORM,
    PDF_MERGE,
    IMAGE_COMPRESS,
    DRAWING_BLUR
}