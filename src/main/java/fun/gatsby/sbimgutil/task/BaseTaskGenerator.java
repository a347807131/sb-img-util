package fun.gatsby.sbimgutil.task;

import cn.hutool.core.io.FileUtil;
import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ITask;
import fun.gatsby.sbimgutil.utils.Const;
import lombok.AllArgsConstructor;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

@AllArgsConstructor
public class BaseTaskGenerator implements ITaskGenerator{

    AppConfig.GlobalTaskConfig gtc;
    AppConfig.ProcessTask processTask;
    private final TaskTypeEnum taskTypeEnum;


    public List<ITask> generate() throws IOException {
        var tasks = new LinkedList<ITask>();
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

    @Override
    public TaskTypeEnum getTaskType() {
        return taskTypeEnum;
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

    /**
     *
     * @param inFile
     * @param format 后缀 无分隔符
     * @return
     */
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
        return loadSortedDirToFilesMap().values().stream().collect(
                LinkedList::new,
                LinkedList::addAll,
                LinkedList::addAll
        );
    }

    public LinkedHashMap<File, List<File>> loadSortedDirToFilesMap() {
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

    LinkedHashMap<File, List<File>> loadSortedDirToFilesMap(FileFilter fileFilter) {
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