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
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TaskExcutor {


    ForkJoinPool forkJoinPool;
    AppConfig.ProcessTask processTask;

    public static ConsoleProgressBar CPB;
    private TaskGroup<Runnable> taskGroup;

    public TaskExcutor(AppConfig.ProcessTask processTask, String taskName, int maxWorkerNum) throws IOException {
        forkJoinPool = new ForkJoinPool(maxWorkerNum);
        this.processTask = processTask;
        init();
    }

    public void init() throws IOException {
        if (!processTask.isEnable())
            return;
        String taskType = processTask.getTaskType();
        TaskTypeEnum taskTypeEnum = TaskTypeEnum.valueOf(taskType);

        String inDirPath = processTask.getInDirPath();
        File inDir = new File(inDirPath);
        List<File> imgFiles = new LinkedList<>();

        FileFetchUtils.fetchFileRecursively(imgFiles, inDir,
                ImageTransformTask.SUPPORTED_FILE_FILTER
        );

        imgFiles.sort(Comparator.comparing(File::getName));

        String fileNameRegex = processTask.getFileNameRegex();

        imgFiles = imgFiles.stream().filter(
                imgFile -> Strings.isBlank(fileNameRegex) || imgFile.getName().matches(fileNameRegex)
        ).toList();

        TaskGroup<Runnable> taskGroup = new ProcessTaskGroup(taskType);

        this.taskGroup = taskGroup;
        switch (taskTypeEnum) {
            case PDF_MERGE -> {
                LinkedHashMap<File, List<File>> dirToImgFilesMap = loadSortedDirToImgFilesMap(imgFiles);
                for (Map.Entry<File, List<File>> entry1 : dirToImgFilesMap.entrySet()) {
                    File dirThatFilesBelong = entry1.getKey();
                    File pdfOutFile =
                            genPdfOutFile(dirThatFilesBelong, processTask);
                    if (pdfOutFile.exists())
                        continue;
                    List<File> imgs = entry1.getValue();
                    String cataDirPath = processTask.getCataDirPath();
                    File cataFile = null;
                    if (Strings.isNotBlank(cataDirPath)) {
                        cataFile = new File(cataDirPath,
                                dirThatFilesBelong.getAbsolutePath().replace(new File(inDirPath).getAbsolutePath(), "") + ".txt"
                        );
                    }
                    PdfMergeTask task = new PdfMergeTask(imgs, pdfOutFile, cataFile);
                    taskGroup.add(task);
                }
            }
            case IMAGE_TRANSFORM, IMAGE_COMPRESS, DRAW_BLUR -> {
                //非pdf合并走这边
                for (File imgFile : imgFiles) {
                    File outFile = genOutFile(imgFile, processTask);
                    if (outFile.exists()) {
                        continue;
                    }
                    BaseTask task = switch (taskTypeEnum) {
                        case IMAGE_TRANSFORM -> new ImageTransformTask(imgFile, outFile, processTask.getFormat());
                        case IMAGE_COMPRESS -> new ImageCompressTask(imgFile, outFile, processTask.getCompressLimit());
                        case DRAW_BLUR -> new DrawBlurTask(imgFile, outFile, new File(processTask.getBlurImagePath()));
                        default -> null;
                    };
                    taskGroup.add(task);
                }
            }
        }

        // FIXME: 3/7/2023 进度条实现过于丑陋
        CPB = new ConsoleProgressBar(taskGroup.size());
    }

    public void start() throws ExecutionException, InterruptedException {
        taskGroup.forEach(e -> forkJoinPool.submit(e));
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

    private File genPdfOutFile(File dirFilesBelong, AppConfig.ProcessTask processTask) {
        String outFileName = dirFilesBelong.getName() + ".pdf";
        String outFilePath = dirFilesBelong.getParentFile().getAbsolutePath()
                .replace(new File(processTask.getInDirPath()).getAbsolutePath(), processTask.getOutDirPath());
        return new File(outFilePath, outFileName);
    }

    File genOutFile(File inFile, AppConfig.ProcessTask processTask) throws IOException {
        String inFileName = inFile.getName();
        String olDdirPath = inFile.getParentFile().getAbsolutePath();
        String newDirPath = olDdirPath.replace(
                new File(processTask.getInDirPath()).getAbsolutePath(),
                processTask.getOutDirPath()
        );
        String outFileName = inFileName;
        if (Strings.isNotBlank(processTask.getFormat())) {
            outFileName = inFileName.substring(0, inFileName.lastIndexOf(".")) + "." + processTask.getFormat();
        }
        return new File(newDirPath, outFileName);
    }
}
