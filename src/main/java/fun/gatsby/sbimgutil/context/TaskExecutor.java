package fun.gatsby.sbimgutil.context;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ProcessTaskGroup;
import fun.gatsby.sbimgutil.schedule.TaskScheduleForkJoinPool;
import fun.gatsby.sbimgutil.utils.ConsoleProgressBar;
import fun.gatsby.sbimgutil.utils.Const;
import fun.gatsby.sbimgutil.utils.FileFetchUtils;
import fun.gatsby.sbimgutil.utils.Label;
import fun.gatsby.sbimgutil.task.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Slf4j
public class TaskExecutor {

    private final AppConfig.GlobalTaskConfig gtc;
    private final Map<String, AppConfig.ProcessTask> taskMap;
    TaskScheduleForkJoinPool forkJoinPool;
    public static ConsoleProgressBar CPB;

    public TaskExecutor(AppConfig.GlobalTaskConfig gtc, Map<String, AppConfig.ProcessTask> taskMap, TaskTypeEnum...taskTypes) throws IOException {
        this.forkJoinPool = new TaskScheduleForkJoinPool(gtc.getMaxWorkerNum());
        this.gtc=gtc;
        this.taskMap=taskMap;
        for (TaskTypeEnum taskType : taskTypes) {
            List<Runnable> tasks = loadTasks(taskType);
            this.forkJoinPool.scheduleBatch(tasks);
        }
        CPB = new ConsoleProgressBar(this.forkJoinPool.getTaskCount());
    }


    public List<Runnable> loadTasks(TaskTypeEnum taskType) throws IOException {
        final AppConfig.ProcessTask processTask=taskMap.get(taskType.name());

        String inDirPath = gtc.getInDirPath();
        File inDir = new File(inDirPath);
        Path outPath = Path.of(gtc.getOutDirPath());
        Path inPath = Path.of(gtc.getInDirPath());
        List<File> imgFiles = new LinkedList<>();

        if(gtc.isRecursive())
            FileFetchUtils.fetchFileRecursively(imgFiles, inDir,
                    Const.SUPPORTED_FILE_FILTER
            );
        else
            FileFetchUtils.fetchFile(imgFiles, inDir,
                    Const.SUPPORTED_FILE_FILTER
            );

        imgFiles.sort(Comparator.comparing(File::getName));
        String fileNameRegex = gtc.getFileNameRegex();
        imgFiles = imgFiles.stream().filter(
                imgFile -> Strings.isBlank(fileNameRegex) || imgFile.getName().matches(fileNameRegex)
        ).toList();

        log.info("本次任务将处理{}个文件", imgFiles.size());

        var tasks = new ProcessTaskGroup(taskType.taskCnName);
        switch (taskType) {
            case PDF_MERGE -> {
                LinkedHashMap<File, List<File>> dirToImgFilesMap = loadSortedDirToImgFilesMap(imgFiles);
                for (Map.Entry<File, List<File>> entry : dirToImgFilesMap.entrySet()) {
                    File dirThatFilesBelong = entry.getKey();
                    File outFile =
                            genPdfOutFile(dirThatFilesBelong);
                    if (outFile.exists() && !gtc.isEnforce())
                        continue;
                    List<File> imgs = entry.getValue();
                    String cataDirPath = processTask.getCataDirPath();
                    File cataFile = null;
                    if (Strings.isNotBlank(cataDirPath)) {
                        String cataFileName = dirThatFilesBelong.getAbsolutePath().replace(new File(inDirPath).getAbsolutePath(), "") + ".txt";
                        cataFile = new File(cataDirPath, cataFileName);
                    }
                    PdfMergeTask task = new PdfMergeTask(imgs, outFile, cataFile);
                    tasks.add(task);
                }
            }
            case SEARCH_ABLE_PDF_GENERATE -> {
                var labelFiles = new LinkedList<File>();
                FileFetchUtils.fetchFileRecursively(labelFiles, inDir, file -> {
                    if(file.isDirectory())
                        return true;
                    return file.getName().equals("Label.txt");
                });

                for (File labelFile : labelFiles) {
                    File dirThatFilesBelong = labelFile.getParentFile();
                    File outFile = genPdfOutFile(dirThatFilesBelong);
                    if (outFile.exists() && !gtc.isEnforce())
                        continue;
                    String cataDirPath = processTask.getCataDirPath();
                    File cataFile = null;
                    if (Strings.isNotBlank(cataDirPath)) {
                        String cataFileName = dirThatFilesBelong.getAbsolutePath().replace(new File(inDirPath).getAbsolutePath(), "") + ".txt";
                        cataFile = new File(cataDirPath, cataFileName);
                    }
                    var task = new OcrRetXmlGenerateTask(outFile,labelFile,cataFile);
                    tasks.add(task);
                }
            }
            case IMAGE_TRANSFORM, IMAGE_COMPRESS, DRAW_BLUR,BOOK_IMAGE_FIX ,FIVE_BACKSPACE_REPLACE-> {
                for (File imgFile : imgFiles) {
                    File outFile = genOutFile(imgFile,
                            taskType.equals(TaskTypeEnum.IMAGE_TRANSFORM)? processTask.getFormat():null
                    );
                    if (outFile.exists() && !gtc.isEnforce()) {
                        continue;
                    }
                    BaseTask task = switch (taskType) {
                        case IMAGE_TRANSFORM -> new ImageTransformTask(imgFile, outFile, processTask.getFormat());
                        case IMAGE_COMPRESS -> new ImageCompressTask(imgFile, outFile, processTask.getCompressLimit());
                        case DRAW_BLUR -> new DrawBlurTask(imgFile, outFile, new File(processTask.getBlurImagePath()));
                        case BOOK_IMAGE_FIX -> new BookImageFixTask(imgFile, outFile);
                        case FIVE_BACKSPACE_REPLACE -> new FiveBackspaceReplaceTask(imgFile, outFile);
                        default -> null;
                    };
                    tasks.add(task);
                }
            }
            case IMAGE_CUT -> {
                File labelFile;
                if(processTask.getLabelFilePath()==null)
                    labelFile = new File(inDirPath, "Label.txt");
                else
                    labelFile = new File(processTask.getLabelFilePath());
                List<String> labelLines = Files.readAllLines(labelFile.toPath());
                Path parentDirPath = inDir.getParentFile().toPath();
                for (String labelLine : labelLines) {
                    Label label = Label.parse(parentDirPath, labelLine);
                    File outDir = genOutFile(label.getMarkedImageFile()).getParentFile();
                    ImageCutTask imageCutTask = new ImageCutTask(label, outDir.toPath());
                    tasks.add(imageCutTask);
                }
            }
        }
        return tasks;
    }

    public void excute() throws ExecutionException, InterruptedException {
        forkJoinPool.start();
    }

    public static ConsoleProgressBar getGlobalConsoleProgressBar() {
        return CPB;
    }

    LinkedHashMap<File, List<File>> loadSortedDirToImgFilesMap(List<File> imgFiles) {
        return imgFiles.parallelStream().collect(
                LinkedHashMap::new,
                (m,k)-> {
                    File parent = k.getParentFile();
                    m.computeIfAbsent(parent, v->new LinkedList<>()).add(k);
                },
                LinkedHashMap::putAll
        );
    }

    private File genPdfOutFile(File dirFilesBelong) {
        String outFileName = dirFilesBelong.getName() + ".pdf";
        String midpiece = dirFilesBelong.getAbsolutePath().replace(
                new File(gtc.getInDirPath()).getAbsolutePath(), ""
        );
        Path fleOutDirPath = Path.of(gtc.getOutDirPath(), midpiece);
        if (!StringUtils.isEmpty(midpiece)) {
            fleOutDirPath = fleOutDirPath.getParent();
        }
        Path outFilePath = fleOutDirPath.resolve(outFileName);
        return outFilePath.toFile();
    }
    File genOutFile(File inFile) {
        return genOutFile(inFile,null);
    }

    File genOutFile(File inFile,String format) {
        String inFileName = inFile.getName();
        String outFileName = inFileName;
        if (Strings.isNotBlank(format)) {
            outFileName = inFileName.substring(0, inFileName.lastIndexOf(".")) + "." +format;
        }
        String olDdirPath = inFile.getParentFile().getAbsolutePath();
        String midpiece = olDdirPath.replace(
                new File(gtc.getInDirPath()).getAbsolutePath(),
                ""
        );
        return Path.of(gtc.getOutDirPath(), midpiece, outFileName).toFile();
    }
}
