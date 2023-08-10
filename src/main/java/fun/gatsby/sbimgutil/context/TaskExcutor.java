package fun.gatsby.sbimgutil.context;

import cn.hutool.core.lang.func.VoidFunc0;
import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ProcessTaskGroup;
import fun.gatsby.sbimgutil.schedule.Scheduler;
import fun.gatsby.sbimgutil.schedule.TaskScheduleForkJoinPool;
import fun.gatsby.sbimgutil.schedule.TaskGroup;
import fun.gatsby.sbimgutil.utils.ConsoleProgressBar;
import fun.gatsby.sbimgutil.utils.Const;
import fun.gatsby.sbimgutil.utils.FileFetchUtils;
import fun.gatsby.sbimgutil.utils.Label;
import fun.gatsby.sbimgutil.task.*;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TaskExcutor {


    private final TaskTypeEnum taskType;
    private final AppConfig.GlobalTaskConfig gtc;
    TaskScheduleForkJoinPool myForkJoinPool;
    AppConfig.ProcessTask processTask;

    public static ConsoleProgressBar CPB;
    private TaskGroup<Runnable> taskGroup;

    public TaskExcutor(AppConfig.ProcessTask processTask, TaskTypeEnum taskType, AppConfig.GlobalTaskConfig gtc) throws IOException {
        myForkJoinPool = new TaskScheduleForkJoinPool(gtc.getMaxWorkerNum());
        this.gtc=gtc;
        this.processTask = processTask;
        this.taskType = taskType;
        init();
    }

    public void init() throws IOException {

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

        this.taskGroup = new ProcessTaskGroup(taskType.taskCnName);
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
                    taskGroup.add(task);
                }
            }
            case IMAGE_TRANSFORM, IMAGE_COMPRESS, DRAW_BLUR,BOOK_IMAGE_FIX -> {
                //非pdf合并走这边
                for (File imgFile : imgFiles) {
                    File outFile = genOutFile(imgFile, processTask.getFormat());
                    if (outFile.exists() && !gtc.isEnforce()) {
                        continue;
                    }
                    BaseTask task = switch (taskType) {
                        case IMAGE_TRANSFORM -> new ImageTransformTask(imgFile, outFile, processTask.getFormat());
                        case IMAGE_COMPRESS -> new ImageCompressTask(imgFile, outFile, processTask.getCompressLimit());
                        case DRAW_BLUR -> new DrawBlurTask(imgFile, outFile, new File(processTask.getBlurImagePath()));
                        case BOOK_IMAGE_FIX -> new BookImageFixTask(imgFile, outFile);
                        default -> null;
                    };
                    taskGroup.add(task);
                }
            }
            case IMAGE_CUT -> {
                File labelFile;
                if(processTask.getLabelFilePath()==null)
                    labelFile = new File(inDirPath, "Label.txt");
                else
                    labelFile = new File(processTask.getLabelFilePath());
                List<String> labelLines = Files.readAllLines(labelFile.toPath());
                for (String labelLine : labelLines) {
                    Label label = Label.parse(inDir.getParentFile().toPath(), labelLine);
                    File outDir = genOutFile(label.getMarkedImageFile()).getParentFile();
                    ImageCutTask imageCutTask = new ImageCutTask(label, outDir.toPath());
                    taskGroup.add(imageCutTask);
                }
            }
        }

        myForkJoinPool.scheduleBatch(taskGroup);
        CPB = new ConsoleProgressBar(taskGroup.size());
    }

    public void excute() throws ExecutionException, InterruptedException {
        myForkJoinPool.start();
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
            outFileName = inFileName.substring(0, inFileName.lastIndexOf(".")) + "." + processTask.getFormat();
        }
        String olDdirPath = inFile.getParentFile().getAbsolutePath();
        String midpiece = olDdirPath.replace(
                new File(gtc.getInDirPath()).getAbsolutePath(),
                ""
        );
        return Path.of(gtc.getOutDirPath(), midpiece, outFileName).toFile();
    }

}
