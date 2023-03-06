package com.example.sbimgutil.context;

import com.example.sbimgutil.config.AppConfig;
import com.example.sbimgutil.schedule.MyTaskJoinPool;
import com.example.sbimgutil.schedule.TaskGroup;
import com.example.sbimgutil.task.*;
import com.example.sbimgutil.utils.FileFetchUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TaskExcutor {

    private final AppConfig appConfig;

    MyTaskJoinPool myTaskJoinPool = new MyTaskJoinPool(8);

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

            List<Runnable> tasks = switch (taskTypeEnum) {
                case IMAGE_TRANSFORM -> processIT(taskConfig);
                case PDF_MERGE -> procesPM(taskConfig);
                case IMAGE_COMPRESS -> procesIC(taskConfig);
                case DRAW_BLUR -> procesDB(taskConfig);
            };
            myTaskJoinPool.scheduleBatch(tasks);
            myTaskJoinPool.start();
        }
    }


    LinkedHashMap<File, List<File>> loadSortedDirToImgFilesMap(File inDir) {
        List<File> imgFiles = new LinkedList<>();

        // TODO: 2023/3/2 靠检查点过滤文件
        FileFetchUtils.fetchFileRecursively(imgFiles,inDir,
                VolumeDirProcessTask.supported_file_filter
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

    private List<Runnable> procesDB(AppConfig.ProcessTask taskConfig) throws IOException {
        String inDirPath = taskConfig.getInDirPath();
        String format = taskConfig.getFormat();
        File inDir = new File(inDirPath);
        List<File> imgFiles = new LinkedList<>();
        FileFetchUtils.fetchFileRecursively(imgFiles,inDir,
                ImageTransformTask.SUPPORTED_FILE_FILTER
        );
        imgFiles.sort(Comparator.comparing(File::getName));

        TaskGroup<Runnable> taskGroup = new TaskGroup<>() {};
        String fileNameRegex = taskConfig.getFileNameRegex();
        // FIXME: 3/6/2023 按文件分任务

        imgFiles = imgFiles.stream().filter(imgFile -> fileNameRegex == null || imgFile.getName().matches(fileNameRegex)).toList();
        for (File imgFile : imgFiles) {
            if(fileNameRegex!=null && !imgFile.getName().matches(fileNameRegex)){
                continue;
            }
            File outFile = genOutFile(imgFile, taskConfig);
            if(outFile.exists()){
                continue;
            }
            BaseTask imageTransformTask = new DrawBlurTask(imgFile, outFile, new File(taskConfig.getBlurImagePath()));
            taskGroup.add(imageTransformTask);
        }
        return taskGroup;
    }

    private List<Runnable> procesIC(AppConfig.ProcessTask taskConfig) throws IOException {
        String inDirPath = taskConfig.getInDirPath();
        String format = taskConfig.getFormat();
        File inDir = new File(inDirPath);
        List<File> imgFiles = new LinkedList<>();
        FileFetchUtils.fetchFileRecursively(imgFiles,inDir,
                ImageTransformTask.SUPPORTED_FILE_FILTER
        );
        imgFiles.sort(Comparator.comparing(File::getName));

        TaskGroup<Runnable> taskGroup = new TaskGroup<>() {};
        String fileNameRegex = taskConfig.getFileNameRegex();
        for (File imgFile : imgFiles) {
            if(fileNameRegex!=null && !imgFile.getName().matches(fileNameRegex)){
                continue;
            }
            File outFile = genOutFile(imgFile, taskConfig);
            if(outFile.exists()){
                continue;
            }
            BaseTask task = new ImageCompressTask(imgFile, outFile, 500);
            taskGroup.add(task);
        }
        return taskGroup;
    }


    private List<Runnable> procesPM(AppConfig.ProcessTask taskConfig) {
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
        TaskGroup<Runnable> taskGroup = new TaskGroup<>() {};
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

    private List<Runnable> processIT(AppConfig.ProcessTask taskConfig) throws IOException {
        String inDirPath = taskConfig.getInDirPath();
        String format = taskConfig.getFormat();
        File inDir = new File(inDirPath);
        List<File> imgFiles = new LinkedList<>();
        FileFetchUtils.fetchFileRecursively(imgFiles,inDir,
            ImageTransformTask.SUPPORTED_FILE_FILTER
        );
        imgFiles.sort(Comparator.comparing(File::getName));

        TaskGroup<Runnable> taskGroup = new TaskGroup<>() {};
        String fileNameRegex = taskConfig.getFileNameRegex();
        for (File imgFile : imgFiles) {
            if(fileNameRegex!=null && !imgFile.getName().matches(fileNameRegex)){
                continue;
            }
            File outFile = genOutFile(imgFile, taskConfig);
            if(outFile.exists() || outFile.length()!=0){
                continue;
            }
            ImageTransformTask imageTransformTask = new ImageTransformTask(imgFile, outFile, format);
            taskGroup.add(imageTransformTask);
        }
        return taskGroup;

    // TODO: 2023/2/26 图片处理任务依赖问题，批处理任务依赖问题
//                        taskConfig.getTaskDepentOn().
//                        tasks.addLast(taskConfig.getTaskDepentOn().getTask());
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