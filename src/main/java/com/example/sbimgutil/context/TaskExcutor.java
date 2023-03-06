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

            TaskGroup<Runnable> taskGroup = new TaskGroup<>() {};

            for (File imgFile : imgFiles) {
                if(fileNameRegex!=null && !imgFile.getName().matches(fileNameRegex)){
                    continue;
                }
                File outFile = genOutFile(imgFile, taskConfig);
                if(outFile.exists()){
                    continue;
                }

                Runnable task = switch (taskTypeEnum) {
                    case IMAGE_TRANSFORM ->{
                        yield  new ImageTransformTask(imgFile,outFile,taskConfig.getFormat());
                    }
                    case PDF_MERGE -> null;
                    case IMAGE_COMPRESS -> {
                        yield new ImageCompressTask(imgFile,outFile,taskConfig.getCompressLimit());
                    }
                    case DRAW_BLUR ->{
                        yield new DrawBlurTask(imgFile,outFile,new File(taskConfig.getBlurImagePath()));
                    }
                };
                if(task==null){
                    continue;
                }
                taskGroup.add(task);
            }
            
            if (taskTypeEnum==TaskTypeEnum.PDF_MERGE){
                LinkedHashMap<File, List<File>> dirToImgFilesMap = loadSortedDirToImgFilesMap(inDir);
                for (Map.Entry<File, List<File>> entry1 : dirToImgFilesMap.entrySet()) {
                    File dirFilesBelong = entry1.getKey();
                    List<File> imgs = entry1.getValue();
                    // TODO: 3/6/2023  
                    PdfMergeTask task = new PdfMergeTask(imgs, dirFilesBelong);
                    taskGroup.add(task);
                }
            }

            myTaskJoinPool.scheduleBatch(taskGroup);
            myTaskJoinPool.start();
        }
    }


    LinkedHashMap<File, List<File>> loadSortedDirToImgFilesMap(File inDir) {
        List<File> imgFiles = new LinkedList<>();

        FileFetchUtils.fetchFileRecursively(imgFiles,inDir,
                ImageTransformTask.SUPPORTED_FILE_FILTER
        );
        imgFiles = imgFiles.stream().sorted(Comparator.comparing(File::getName)).collect(Collectors.toList());

        Map<File, List<File>> volumeToImgFilesMap = imgFiles.parallelStream().collect(
                Collectors.groupingBy(File::getParentFile)
        );

        List<Map.Entry<File, List<File>>> sortedEntries = volumeToImgFilesMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).toList();
        return new LinkedHashMap<>(sortedEntries.size()) {{
            sortedEntries.forEach(e -> put(e.getKey(), e.getValue()));
        }};
    }

    private Runnable procesPM(File imgFile, AppConfig.ProcessTask taskConfig) {
        String outDirPath = taskConfig.getOutDirPath();
        LinkedList<Runnable> tasks = new LinkedList<>();
        String inDirPath = taskConfig.getInDirPath();

        LinkedHashMap<File, List<File>> dirToImgFilesMap = loadSortedDirToImgFilesMap(new File(inDirPath));
        for (Map.Entry<File, List<File>> entry : dirToImgFilesMap.entrySet()) {
            File dirThatFilesBelong = entry.getKey();
            List<File> imgFiles = entry.getValue();
            File outFile=genPdfOutFile(dirThatFilesBelong,taskConfig);
            if(outFile.exists()){
                continue;
            }
            /// FIXME: 3/6/2023 目录文件路径
            File cataFile = new File(outFile.getAbsolutePath().replace(".pdf", ".txt"));
            PdfMergeTask pdfMergeTask = new PdfMergeTask(imgFiles,outFile,cataFile);
            tasks.add(pdfMergeTask);
        }
        File inDir = new File(inDirPath);
        return null;
    }

    private File genPdfOutFile(File dirFilesBelong, AppConfig.ProcessTask taskConfig) {
        String outFileName = dirFilesBelong.getName() + ".pdf";

        String outFilePath = dirFilesBelong.getAbsolutePath()
                .replace(taskConfig.getInDirPath(), taskConfig.getOutDirPath())
                .replace(dirFilesBelong.getName(), outFileName);
        return new File(outFilePath);
    }

    // TODO: 2023/3/5  
    private File genPdfout() {
        return null;
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