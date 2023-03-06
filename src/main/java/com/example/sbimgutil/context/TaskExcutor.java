package com.example.sbimgutil.context;

import com.example.sbimgutil.config.AppConfig;
import com.example.sbimgutil.schedule.MyTaskJoinPool;
import com.example.sbimgutil.schedule.TaskGroup;
import com.example.sbimgutil.task.*;
import com.example.sbimgutil.utils.ConsoleProgressBar;
import com.example.sbimgutil.utils.FileFetchUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TaskExcutor {

    private final AppConfig appConfig;

    MyTaskJoinPool myTaskJoinPool = new MyTaskJoinPool(8);

    public final static ConsoleProgressBar CPB = new ConsoleProgressBar();

    public TaskExcutor(AppConfig appConfig){
        this.appConfig = appConfig;
    }

    public void execute() throws ExecutionException, InterruptedException, IOException {
        Map<String, AppConfig.ProcessTask> taskMap = appConfig.getProcessTasks();
        for (Map.Entry<String, AppConfig.ProcessTask> entry : taskMap.entrySet()) {
            if (!entry.getValue().isEnable())
                continue;
            AppConfig.ProcessTask taskConfig = entry.getValue();
            String taskType = taskConfig.getTaskType();
            TaskTypeEnum taskTypeEnum = TaskTypeEnum.valueOf(taskType);

            String inDirPath = taskConfig.getInDirPath();
            File inDir = new File(inDirPath);
            List<File> imgFiles = new LinkedList<>();

            FileFetchUtils.fetchFileRecursively(imgFiles,inDir,
                    ImageTransformTask.SUPPORTED_FILE_FILTER
            );

            imgFiles.sort(Comparator.comparing(File::getName));

            String fileNameRegex = taskConfig.getFileNameRegex();

            imgFiles = imgFiles.stream().filter(
                    imgFile -> fileNameRegex == null || imgFile.getName().matches(fileNameRegex)
            ).toList();

            TaskGroup<Runnable> taskGroup = new TaskGroup<>() {
            };

            switch (taskTypeEnum) {
                case PDF_MERGE: {
                    LinkedHashMap<File, List<File>> dirToImgFilesMap = loadSortedDirToImgFilesMap(imgFiles);
                    for (Map.Entry<File, List<File>> entry1 : dirToImgFilesMap.entrySet()) {
                        File dirThatFilesBelong = entry1.getKey();
                        List<File> imgs = entry1.getValue();
                        File pdfOutFile = genPdfOutFile(dirThatFilesBelong, taskConfig);
                        // TODO: 3/6/2023 目录文件
                        PdfMergeTask task = new PdfMergeTask(imgs, pdfOutFile);
                        taskGroup.add(task);
                    }
                }
                case IMAGE_TRANSFORM, IMAGE_COMPRESS, DRAW_BLUR: {
                    //非pdf合并走这边
                    for (File imgFile : imgFiles) {
                        if (fileNameRegex != null && !imgFile.getName().matches(fileNameRegex)) {
                            continue;
                        }
                        File outFile = genOutFile(imgFile, taskConfig);
                        if (outFile.exists()) {
                            continue;
                        }
                        BaseTask task = switch (taskTypeEnum) {
                            case IMAGE_TRANSFORM -> {
                                yield new ImageTransformTask(imgFile, outFile, taskConfig.getFormat());
                            }
                            case IMAGE_COMPRESS -> {
                                yield new ImageCompressTask(imgFile, outFile, taskConfig.getCompressLimit());
                            }
                            case DRAW_BLUR -> {
                                yield new DrawBlurTask(imgFile, outFile, new File(taskConfig.getBlurImagePath()));
                            }
                            default -> null;
                        };
                        if (task != null)
                            taskGroup.add(task);
                    }
                }
            }
            myTaskJoinPool.scheduleBatch(taskGroup);
            myTaskJoinPool.start();
        }
    }

    LinkedHashMap<File, List<File>> loadSortedDirToImgFilesMap(List<File> imgFiles) {

        Map<File, List<File>> volumeToImgFilesMap = imgFiles.parallelStream().collect(
                Collectors.groupingBy(File::getParentFile)
        );

        List<Map.Entry<File, List<File>>> sortedEntries = volumeToImgFilesMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).toList();
        return new LinkedHashMap<>(sortedEntries.size()) {{
            sortedEntries.forEach(e -> put(e.getKey(), e.getValue()));
        }};
    }

    private File genPdfOutFile(File dirFilesBelong, AppConfig.ProcessTask taskConfig) {
        String outFileName = dirFilesBelong.getName() + ".pdf";

        String outFilePath = dirFilesBelong.getAbsolutePath()
                .replace(taskConfig.getInDirPath(), taskConfig.getOutDirPath())
                .replace(dirFilesBelong.getName(), outFileName);
        return new File(outFilePath);
    }

    File genOutFile(File inFile, AppConfig.ProcessTask taskConfig) throws IOException {
        String inFileName = inFile.getName();
        String olDdirPath = inFile.getParentFile().getAbsolutePath();
        String newDirPath = olDdirPath.replace(
                new File(taskConfig.getInDirPath()).getAbsolutePath(),
                taskConfig.getOutDirPath()
        );
        String outFileName =inFileName;
        if(taskConfig.getFormat()!=null){
            String format= inFileName.substring(inFileName.lastIndexOf(".")+1);
            outFileName = inFileName.substring(0,inFileName.lastIndexOf(".")) + "." + taskConfig.getFormat();
        }

        return new File(newDirPath, outFileName);
    }
}
enum TaskTypeEnum{
    IMAGE_TRANSFORM,
    PDF_MERGE,
    IMAGE_COMPRESS,
    DRAW_BLUR
}