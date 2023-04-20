package com.example.sbimgutil.context;

import com.example.sbimgutil.config.AppConfig;
import com.example.sbimgutil.schedule.TaskScheduleForkJoinPool;
import com.example.sbimgutil.schedule.TaskGroup;
import com.example.sbimgutil.task.*;
import com.example.sbimgutil.utils.ConsoleProgressBar;
import com.example.sbimgutil.utils.FileFetchUtils;
import org.apache.logging.log4j.util.Strings;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TaskExcutor {


    TaskScheduleForkJoinPool myTaskJoinPool;
    Map<String, AppConfig.ProcessTask> processTasks;

    public static ConsoleProgressBar CPB;

    public TaskExcutor(AppConfig appConfig) throws IOException {
        processTasks = appConfig.getProcessTasks();
        myTaskJoinPool = new TaskScheduleForkJoinPool(appConfig.getMaxWorkerNum());
        init();
    }

    public TaskExcutor(AppConfig.ProcessTask processTask, String taskName, int maxWorkerNum) throws IOException {
        myTaskJoinPool = new TaskScheduleForkJoinPool(maxWorkerNum);
        processTasks = new HashMap<>();
        processTasks.put(taskName, processTask);
        init();
    }

    public void init() throws IOException {
        Map<String, AppConfig.ProcessTask> taskMap = processTasks;
        List<Map.Entry<String, AppConfig.ProcessTask>> taskEntrys = taskMap.entrySet().stream().filter(e -> e.getValue().isEnable()).toList();

        for (Map.Entry<String, AppConfig.ProcessTask> entry : taskEntrys) {
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
                    imgFile -> Strings.isBlank(fileNameRegex) || imgFile.getName().matches(fileNameRegex)
            ).toList();

            TaskGroup<Runnable> taskGroup = new ProcessTaskGroup(entry.getKey());

            switch (taskTypeEnum) {
                case PDF_MERGE: {
                    LinkedHashMap<File, List<File>> dirToImgFilesMap = loadSortedDirToImgFilesMap(imgFiles);
                    for (Map.Entry<File, List<File>> entry1 : dirToImgFilesMap.entrySet()) {
                        File dirThatFilesBelong = entry1.getKey();
                        File pdfOutFile =
                                genPdfOutFile(dirThatFilesBelong, taskConfig);
                        if (pdfOutFile.exists())
                            continue;
                        String cataDirPath = taskConfig.getCataDirPath();
                        //中段
                        String interlude = dirThatFilesBelong.getAbsolutePath().replace(new File(inDirPath).getAbsolutePath(), "");
                        List<File> imgs = entry1.getValue();
                        String cataFilePath = cataDirPath + interlude + ".txt";
                        File cataFile = new File(cataFilePath);
                        if (Strings.isBlank(cataDirPath))
                            cataFile = null;

                        PdfMergeTask task = new PdfMergeTask(imgs, pdfOutFile, cataFile);
                        taskGroup.add(task);
                    }
                }
                case IMAGE_TRANSFORM, IMAGE_COMPRESS, DRAW_BLUR: {
                    //非pdf合并走这边
                    for (File imgFile : imgFiles) {
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
            // FIXME: 3/7/2023 进度条实现过于丑陋
            CPB = new ConsoleProgressBar(taskGroup.size());
        }
    }

    public void start() throws ExecutionException, InterruptedException {
        myTaskJoinPool.start();
    }

    public int getTotalTask() {
        return CPB.getTotal();
    }

    public static ConsoleProgressBar getGlobalConsoleProgressBar() {
        return CPB;
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
                .replace(new File(taskConfig.getInDirPath()).getAbsolutePath(), taskConfig.getOutDirPath())
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
        if (Strings.isNotBlank(taskConfig.getFormat())) {
            outFileName = inFileName.substring(0, inFileName.lastIndexOf(".")) + "." + taskConfig.getFormat();
        }
        return new File(newDirPath, outFileName);
    }
}
