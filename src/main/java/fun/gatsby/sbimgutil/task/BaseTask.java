package fun.gatsby.sbimgutil.task;


import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.context.TaskExecutor;
import fun.gatsby.sbimgutil.schedule.ITask;
import fun.gatsby.sbimgutil.schedule.TaskStateEnum;
import fun.gatsby.sbimgutil.utils.ConsoleProgressBar;
import fun.gatsby.sbimgutil.utils.Const;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;


@Slf4j
public abstract class BaseTask implements ITask {

    //bugfixed
    static {
        ImageIO.getImageWritersByFormatName("jpeg2000").next();
    }

    private LocalDateTime startDate;
    protected String name;
    protected TaskStateEnum state = TaskStateEnum.NEW;
    protected File outFile;

    @Override
    public void before() throws IOException {
        if (outFile != null) {
            outFile = new File(outFile.getParentFile(), outFile.getName() + ".tmp");
            if (outFile.exists()) {
                Files.delete(outFile.toPath());
            }
            if (!outFile.getParentFile().exists()) {
                FileUtils.forceMkdirParent(outFile);
            }
        }

        state = TaskStateEnum.RUNNING;
        startDate = LocalDateTime.now();
    }


    @Override
    public void after() {
        if (outFile != null && outFile.exists()) {
            String fileName = outFile.getName().substring(0, outFile.getName().lastIndexOf("."));
            File finalFile = new File(outFile.getParentFile(), fileName);
            if(finalFile.exists()){
                finalFile.delete();
            }
            outFile.renameTo(finalFile);
        }

        long between = LocalDateTimeUtil.between(startDate, LocalDateTime.now(), ChronoUnit.SECONDS);
        log.debug("任务完成:[{}] ,执行耗时：{}s", name, between);
        state = TaskStateEnum.FINISHED;

        ConsoleProgressBar progressBar = TaskExecutor.getGlobalConsoleProgressBar();
        if (progressBar != null) {
            progressBar.iterate();
        }
    }

    @Override
    public void onError(Throwable e) {
        state = TaskStateEnum.ERROR;
        log.error("任务"+name+"执行异常",e);
    }

    @Override
    public String toString() {
        return name;
    }

    @AllArgsConstructor
    public static class TaskGenerator{

        AppConfig.GlobalTaskConfig gtc;
        AppConfig.ProcessTask processTask;
        private final TaskTypeEnum taskTypeEnum;


        public List<ITask> generate() throws IOException {
            List<ITask> tasks = new LinkedList<>();
            for (File imgFile : loadImageFiles()) {
                File outFile = genOutFile(imgFile,processTask.getFormat());
                if (outFile.exists() && !gtc.isEnforce()) {
                    continue;
                }
                ITask task =  switch (taskTypeEnum){
                    case IMAGE_TRANSFORM -> new ImageTransformTask(imgFile, outFile, processTask.getFormat());
                    case IMAGE_COMPRESS -> new ImageCompressTask(imgFile, outFile, processTask.getCompressLimit());
                    case DRAW_BLUR -> new DrawBlurTask(imgFile, outFile, new File(processTask.getBlurImagePath()));
                    case BOOK_IMAGE_FIX -> new BookImageFixTask(imgFile, outFile);
                    case FIVE_BACKSPACE_REPLACE -> new FiveBackspaceReplaceTask(imgFile, outFile);
                    default -> null;
                };
                tasks.add(task);
            }
            return tasks;
        }

        File genPdfOutFile(File dirFilesBelong) {
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

        List<File> loadImageFiles() {
            return loadSortedDirToImgFilesMap().values().stream().collect(
                    LinkedList::new,
                    LinkedList::addAll,
                    LinkedList::addAll
            );
        }

        LinkedHashMap<File, List<File>> loadSortedDirToImgFilesMap() {
            Path inPath = Path.of(gtc.getInDirPath());
            String fileNameRegex = gtc.getFileNameRegex();
            //@formatter:off-->
            return FileUtil.loopFiles(inPath.toFile()).stream()
                    .filter(file -> Strings.isBlank(fileNameRegex) || file.getName().matches(fileNameRegex))
                    .filter(file -> Const.SUPORTTED_FORMATS.contains(FileUtil.extName(file)))
                    .collect(
                            LinkedHashMap::new,
                            (m, k) -> {
                                File parent = k.getParentFile();
                                m.computeIfAbsent(parent, v -> new LinkedList<>()).add(k);
                            },
                            LinkedHashMap::putAll
                    );
            //@formatter:on-->
        }
    }
}
